package com.oxalio.invoice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    
    /**
     * ID de la facture originale à rembourser
     */
    private Long originalInvoiceId;
    
    /**
     * Raison du remboursement (requis par FNE)
     */
    private String reason;
    
    /**
     * Lignes à rembourser (partiellement ou totalement)
     */
    private List<RefundLineRequest> lines;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundLineRequest {
        private String fneItemId;      // ID FNE de l'item à rembourser
        private BigDecimal quantity;    // Quantité à rembourser
        private BigDecimal amount;      // Montant à rembourser
    }
}