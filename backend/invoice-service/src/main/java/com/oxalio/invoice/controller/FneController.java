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
import com.oxalio.invoice.service.QrCodeGenerator;
import com.oxalio.invoice.service.RneTicketPdfService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
    private final QrCodeGenerator qrCodeGenerator; // ✅ injecte
    private final RneTicketPdfService rneTicketPdfService; // recommandé (voir 2.3)

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
        if (isRne && request.getRne() != null) {
            fneRequest.setRne(request.getRne());
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

        // Garde anti-NPE : si FNE ne renvoie pas ce qu’on attend
        if (response == null || response.getInvoice() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Réponse FNE invalide: invoice manquant (response/invoice null)"
            );
        }

        // ════════════════════════════════════════════════════════════════
        // ✨ Stocker les UUID FNE dans la base de données (si internalInvoiceId fourni)
        // ════════════════════════════════════════════════════════════════
        if (request.getInternalInvoiceId() != null) {
            try {
                InvoiceEntity invoice = invoiceRepository.findById(request.getInternalInvoiceId())
                        .orElseThrow(() -> new RuntimeException("Invoice not found: " + request.getInternalInvoiceId()));

                // Stocker l'ID UUID, la référence DGI, FneToken, PaymentMethod, Template et IsRne pour request Rne
                invoice.setFneInvoiceId(response.getInvoice().getId());
                invoice.setFneReference(response.getReference());
                invoice.setFneToken(response.getToken());     // ✅ AJOUT
                invoice.setPaymentMethod(request.getPaymentMethod()); // ✅ utile pour ticket
                invoice.setTemplate(request.getTemplate());
                invoice.setIsRne(isRne);
                invoice.setRne(request.getRne());

                // Stocker les IDs des items
                List<InvoiceLineEntity> lines = invoice.getLines();
                List<FneInvoiceResponse.InvoiceDetails.InvoiceItemDetails> fneItems =
                        response.getInvoice().getItems();

                if (lines != null && fneItems != null) {
                    for (int i = 0; i < Math.min(lines.size(), fneItems.size()); i++) {
                        lines.get(i).setFneItemId(fneItems.get(i).getId());
                    }
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
    // GET RNE Ticket PDF
    // ════════════════════════════════════════════════════════════════

    /**
     * Télécharger le ticket RNE au format PDF
     * 
     * Ce endpoint génère et retourne un PDF de ticket RNE conforme aux 
     * spécifications FNE/DGI. Le ticket contient le QR code de vérification,
     * la référence FNE, et toutes les informations de la transaction.
     * 
     * @param internalId ID interne de la facture
     * @return PDF bytes du ticket RNE
     */
    @GetMapping(
        value = "/invoices/{internalId}/rne-ticket.pdf",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> downloadRneTicket(@PathVariable Long internalId) {
        
        log.info("📄 Request to download RNE ticket for invoice ID: {}", internalId);
        
        // 1. Récupérer la facture
        InvoiceEntity invoice = invoiceRepository.findById(internalId)
                .orElseThrow(() -> {
                    log.error("❌ Invoice not found: {}", internalId);
                    return new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invoice not found: " + internalId
                    );
                });
        
        log.debug("Invoice found: {}, Status: {}", invoice.getId(), invoice.getStatus());
        
        // ========================================
        // Guards indispensables
        // ========================================
        
        // 2. Vérifier que le token FNE existe
        if (invoice.getFneToken() == null || invoice.getFneToken().isBlank()) {
            log.error("❌ FNE token missing for invoice {}", internalId);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "FNE token non stocké. Impossible de générer le ticket RNE."
            );
        }
        
        // 3. Vérifier que la référence FNE existe
        if (invoice.getFneReference() == null || invoice.getFneReference().isBlank()) {
            log.error("❌ FNE reference missing for invoice {}", internalId);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "FNE reference non stockée. Impossible de générer le ticket RNE."
            );
        }
        
        // 4. Vérifier que le mode de paiement existe
        if (invoice.getPaymentMethod() == null || invoice.getPaymentMethod().isBlank()) {
            log.error("❌ Payment mode missing for invoice {}", internalId);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "paymentMethod non stocké. Impossible de générer le ticket RNE."
            );
        }
        
        // 5. Vérifier que le template existe
        if (invoice.getTemplate() == null || invoice.getTemplate().isBlank()) {
            log.error("❌ Template missing for invoice {}", internalId);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "template non stocké. Impossible de générer le ticket RNE."
            );
        }
        
        log.info("✅ All guards passed for invoice {}", internalId);
        
        // ========================================
        // Génération du QR Code
        // ========================================
        
        try {
            // Générer le QR code PNG depuis le token FNE
            // Le token contient l'URL de vérification DGI
            byte[] qrPng = qrCodeGenerator.generateQrCode(invoice.getFneToken(), 240, 240);
            
            log.info("✅ QR code generated: {} bytes", qrPng.length);
            
            // ========================================
            // Génération du PDF
            // ========================================
            
            byte[] pdfBytes = rneTicketPdfService.render(invoice, qrPng);
            
            log.info("✅ PDF generated: {} bytes", pdfBytes.length);
            
            // ========================================
            // Retour du PDF
            // ========================================
            
            String filename = "rne-" + invoice.getFneReference() + ".pdf";
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=" + filename)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("❌ Error generating RNE ticket for invoice {}: {}", 
                internalId, e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur lors de la génération du ticket RNE: " + e.getMessage()
            );
        }
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
