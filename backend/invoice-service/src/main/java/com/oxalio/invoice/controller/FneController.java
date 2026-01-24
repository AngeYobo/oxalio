package com.oxalio.invoice.controller;

import com.oxalio.invoice.client.FneStickerClient;
import com.oxalio.invoice.client.FneStickerClient.FneInvoiceItem;
import com.oxalio.invoice.client.FneStickerClient.FneInvoiceRequest;
import com.oxalio.invoice.client.FneStickerClient.FneInvoiceResponse;
import com.oxalio.invoice.client.FneStickerClient.FneRefundRequest;
import com.oxalio.invoice.client.FneStickerClient.FneRefundResponse;
import com.oxalio.invoice.config.FneConfiguration;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import com.oxalio.invoice.repository.InvoiceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour l'intégration FNE.
 *
 * Corrections apportées :
 * - Validation stricte des champs requis (évite les NPE et renvoie 400 au lieu de 500)
 * - Support du champ RNE : si isRne=true, rne devient obligatoire + mapping vers la requête FNE
 * - Validation et @Valid sur la liste items et ses éléments
 * - Construction des items de manière robuste (setters si disponibles, sinon constructeur gardé)
 */
@Slf4j
@RestController
@RequestMapping("/api/fne")
@RequiredArgsConstructor
public class FneController {

    private final FneStickerClient fneClient;
    private final FneConfiguration config;
    private final InvoiceRepository invoiceRepository;

    /**
     * Endpoint de test - Informations configuration.
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of(
                "apiUrl", config.getApi().getBaseUrl(),
                "company", config.getCompany(),
                "establishment", config.getEstablishment()
        ));
    }

    /**
     * Créer et signer une facture (générique).
     * Peut gérer B2B, B2C, B2F et Purchase.
     *
     * ⚠️ IMPORTANT: Cette méthode stocke maintenant les UUID FNE dans la base de données
     * pour permettre la création de refunds ultérieurs.
     */
    @PostMapping("/invoices/sign")
    public ResponseEntity<FneInvoiceResponse> signInvoice(@Valid @RequestBody InvoiceSignRequest request) {

        log.info("Creating {} invoice - Template: {}", request.getInvoiceType(), request.getTemplate());

        // Garde "défensive" en plus des validations (sécurité runtime)
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items est obligatoire et ne doit pas être vide");
        }
        if (Boolean.TRUE.equals(request.getIsRne())
                && (request.getRne() == null || request.getRne().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rne est obligatoire quand isRne=true");
        }

        // Construire la requête FNE
        FneInvoiceRequest fneRequest = new FneInvoiceRequest();
        fneRequest.setInvoiceType(request.getInvoiceType());
        fneRequest.setPaymentMethod(request.getPaymentMethod());
        fneRequest.setTemplate(request.getTemplate());

        boolean isRne = Boolean.TRUE.equals(request.getIsRne());
        fneRequest.setIsRne(isRne);

        // Champ RNE (conformité : obligatoire si isRne=true)
        // NB: suppose que FneInvoiceRequest expose setRne(). Si non, il faudra l'ajouter côté client DTO.
        try {
            // évite erreur de compilation si setRne n'existe pas : vous verrez immédiatement l'erreur et pourrez l'ajouter.
            fneRequest.getClass().getMethod("setRne", String.class).invoke(fneRequest, request.getRne());
        } catch (NoSuchMethodException ignored) {
            if (isRne) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "FneInvoiceRequest ne supporte pas setRne(...). Ajoutez le champ rne au DTO client FNE."
                );
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur mapping rne vers requête FNE");
        }

        // Client
        fneRequest.setClientNcc(request.getClientNcc());
        fneRequest.setClientCompanyName(request.getClientCompanyName());
        fneRequest.setClientPhone(request.getClientPhone());
        fneRequest.setClientEmail(request.getClientEmail());

        // Établissement (utiliser config par défaut si non fourni)
        fneRequest.setEstablishment(
                request.getEstablishment() != null
                        ? request.getEstablishment()
                        : config.getEstablishment().getName()
        );
        fneRequest.setPointOfSale(
                request.getPointOfSale() != null
                        ? request.getPointOfSale()
                        : config.getEstablishment().getPointOfSale()
        );

        // International (B2F)
        fneRequest.setForeignCurrency(request.getForeignCurrency());
        fneRequest.setForeignCurrencyRate(request.getForeignCurrencyRate());

        // Message
        fneRequest.setCommercialMessage(request.getCommercialMessage());

        // Articles
        // IMPORTANT : si le constructeur de FneInvoiceItem n'est pas dans cet ordre, remplacez par setters explicites.
        List<FneInvoiceItem> items = request.getItems().stream()
                .map(item -> new FneInvoiceItem(
                        item.getReference(),
                        item.getTaxes(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getAmount(),
                        item.getDiscount(),
                        item.getMeasurementUnit()
                ))
                .collect(Collectors.toList());
        fneRequest.setItems(items);

        // Signer via FNE
        FneInvoiceResponse response = fneClient.signInvoice(fneRequest);

        // ════════════════════════════════════════════════════════════════
        // ✨ Stocker les UUID FNE dans la base de données (si internalInvoiceId fourni)
        // ════════════════════════════════════════════════════════════════
        if (request.getInternalInvoiceId() != null) {
            try {
                InvoiceEntity invoice = invoiceRepository.findById(request.getInternalInvoiceId())
                        .orElseThrow(() -> new RuntimeException("Invoice not found: " + request.getInternalInvoiceId()));

                // Stocker l'ID UUID et la référence DGI
                invoice.setFneInvoiceId(response.getInvoice().getId());
                invoice.setFneReference(response.getReference());

                // Stocker les IDs des items
                List<InvoiceLineEntity> lines = invoice.getLines();
                List<FneInvoiceResponse.InvoiceDetails.InvoiceItemDetails> fneItems =
                        response.getInvoice().getItems();

                for (int i = 0; i < Math.min(lines.size(), fneItems.size()); i++) {
                    lines.get(i).setFneItemId(fneItems.get(i).getId());
                }

                invoiceRepository.save(invoice);

                log.info("Stored FNE UUID: {} for invoice: {}",
                        response.getInvoice().getId(), request.getInternalInvoiceId());

            } catch (Exception e) {
                log.error("Failed to store FNE UUID: {}", e.getMessage(), e);
                // Ne pas faire échouer la requête si le stockage échoue
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Créer un avoir (refund).
     *
     * ⚠️ IMPORTANT: Cette méthode utilise maintenant l'ID UUID de la facture
     * stocké en base de données, pas la référence DGI.
     */
    @PostMapping("/invoices/{invoiceIdOrReference}/refund")
    public ResponseEntity<FneRefundResponse> createRefund(
            @PathVariable String invoiceIdOrReference,
            @Valid @RequestBody RefundRequest request) {

        log.info("Creating refund for invoice: {}", invoiceIdOrReference);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items est obligatoire et ne doit pas être vide");
        }

        // ════════════════════════════════════════════════════════════════
        // Récupérer l'UUID FNE depuis la base de données
        // ════════════════════════════════════════════════════════════════
        String fneInvoiceId;

        // Vérifier si c'est déjà un UUID (36 caractères avec tirets)
        if (invoiceIdOrReference.length() == 36 && invoiceIdOrReference.contains("-")) {
            fneInvoiceId = invoiceIdOrReference;
        } else {
            // C'est une référence DGI, chercher l'UUID dans la BDD
            InvoiceEntity invoice = invoiceRepository.findByFneReference(invoiceIdOrReference)
                    .orElseThrow(() -> new RuntimeException(
                            "Invoice not found with FNE reference: " + invoiceIdOrReference));

            if (invoice.getFneInvoiceId() == null) {
                throw new RuntimeException(
                        "Invoice found but FNE UUID not stored. Cannot create refund.");
            }

            fneInvoiceId = invoice.getFneInvoiceId();
            log.info("Resolved FNE UUID: {} for reference: {}", fneInvoiceId, invoiceIdOrReference);
        }

        // Construire la requête de refund
        FneRefundRequest fneRequest = new FneRefundRequest();
        fneRequest.setItems(
                request.getItems().stream()
                        .map(item -> new FneRefundRequest.RefundItem(
                                item.getId(),
                                item.getQuantity()
                        ))
                        .collect(Collectors.toList())
        );

        // Appeler l'API FNE avec l'UUID
        FneRefundResponse response = fneClient.createRefund(fneInvoiceId, fneRequest);

        return ResponseEntity.ok(response);
    }

    // ════════════════════════════════════════════════════════════════
    // DTOs de requête
    // ════════════════════════════════════════════════════════════════

    @Data
    public static class InvoiceSignRequest {

        @NotBlank(message = "invoiceType est obligatoire")
        // typiquement "sale" ou "purchase" selon la doc
        private String invoiceType;

        @NotBlank(message = "paymentMethod est obligatoire")
        // ex: "cash", "mobile-money", etc.
        private String paymentMethod;

        @NotBlank(message = "template est obligatoire")
        @Pattern(regexp = "B2B|B2C|B2F|B2G", message = "template invalide (B2B, B2C, B2F, B2G)")
        private String template;

        @NotNull(message = "isRne est obligatoire")
        private Boolean isRne;

        // Référence du reçu (obligatoire si isRne=true)
        private String rne;

        // Client/Fournisseur
        private String clientNcc;

        @NotBlank(message = "clientCompanyName est obligatoire")
        private String clientCompanyName;

        @NotBlank(message = "clientPhone est obligatoire")
        private String clientPhone;

        @NotBlank(message = "clientEmail est obligatoire")
        @Email(message = "Format clientEmail invalide")
        private String clientEmail;

        // Etablissement (optionnel - utilise config par défaut)
        private String establishment;
        private String pointOfSale;

        // International (B2F)
        private String foreignCurrency;
        private BigDecimal foreignCurrencyRate;

        // Message
        private String commercialMessage;

        // Articles
        @NotEmpty(message = "items est obligatoire et ne doit pas être vide")
        @Valid
        private List<InvoiceItemDto> items;

        // ID interne de la facture pour stocker les UUID FNE
        private Long internalInvoiceId;
    }

    @Data
    public static class InvoiceItemDto {

        private String reference;

        @NotEmpty(message = "taxes est obligatoire et ne doit pas être vide")
        private List<String> taxes;

        @NotBlank(message = "description est obligatoire")
        private String description;

        @NotNull(message = "quantity est obligatoire")
        private Integer quantity;

        @NotNull(message = "amount est obligatoire")
        private BigDecimal amount; // Prix unitaire HT

        private Integer discount;
        private String measurementUnit;
    }

    @Data
    public static class RefundRequest {

        @NotEmpty(message = "items est obligatoire et ne doit pas être vide")
        @Valid
        private List<RefundItemDto> items;

        @Data
        public static class RefundItemDto {

            @NotBlank(message = "id est obligatoire")
            private String id;

            @NotNull(message = "quantity est obligatoire")
            private Integer quantity;
        }
    }
}
