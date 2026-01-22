package com.oxalio.invoice.controller;

import com.oxalio.invoice.client.FneStickerClient;
import com.oxalio.invoice.client.FneStickerClient.*;
import com.oxalio.invoice.config.FneConfiguration;
import com.oxalio.invoice.entity.InvoiceEntity;
import com.oxalio.invoice.entity.InvoiceLineEntity;
import com.oxalio.invoice.repository.InvoiceRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour l'intégration FNE.
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
    public ResponseEntity<FneInvoiceResponse> signInvoice(
            @Valid @RequestBody InvoiceSignRequest request) {
        
        log.info("Creating {} invoice - Template: {}", 
            request.getInvoiceType(), request.getTemplate());
        
        // Construire la requête FNE
        FneInvoiceRequest fneRequest = new FneInvoiceRequest();
        fneRequest.setInvoiceType(request.getInvoiceType());
        fneRequest.setPaymentMethod(request.getPaymentMethod());
        fneRequest.setTemplate(request.getTemplate());
        fneRequest.setIsRne(request.getIsRne() != null ? request.getIsRne() : false);
        
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
        // ✨ NOUVEAU : Stocker les UUID FNE dans la base de données
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
        
        // ════════════════════════════════════════════════════════════════
        // ✨ NOUVEAU : Récupérer l'UUID FNE depuis la base de données
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
        private String invoiceType;      // "sale" ou "purchase"
        private String paymentMethod;    // "cash", "mobile-money", etc.
        private String template;         // "B2B", "B2C", "B2F"
        private Boolean isRne;
        
        // Client/Fournisseur
        private String clientNcc;
        private String clientCompanyName;
        private String clientPhone;
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
        private List<InvoiceItemDto> items;
        
        // ✨ NOUVEAU : ID interne de la facture pour stocker les UUID FNE
        private Long internalInvoiceId;
    }

    @Data
    public static class InvoiceItemDto {
        private String reference;
        private List<String> taxes;
        private String description;
        private Integer quantity;
        private BigDecimal amount;          // Prix unitaire HT
        private Integer discount;
        private String measurementUnit;
    }

    @Data
    public static class RefundRequest {
        private List<RefundItemDto> items;
        
        @Data
        public static class RefundItemDto {
            private String id;
            private Integer quantity;
        }
    }
}