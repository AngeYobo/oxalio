// src/main/java/com/oxalio/invoice/dto/InvoiceResponse.java
package com.oxalio.invoice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private String invoiceType;
    
    private String template; 
    private Boolean isRne;   
    private String rne;      
    private Instant issueDate;
    private String currency;

    private SellerDTO seller;
    private BuyerDTO buyer;

    private List<InvoiceLineDTO> lines;
    private TotalsDTO totals;

    private String status;      // Enum -> String pour affichage
    private String paymentMode; // ✅ ajouté pour corriger le mapper
    private String stickerId;
    private String qrBase64;
    private String dgiReference;
    private Instant dgiSubmittedAt;

    private String notes;
    private String message;     // ✅ ajouté pour DgiClient.setMessage(...)

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SellerDTO {
        private String companyName;
        private String taxId;
        private String address;
        private String phone;
        private String email;
        private String regime;
        private String taxCenter;
        private String rccm;
        private String bankRef;
        private String establishment;
        // 🔥 Nouveaux champs validés
        private String sellerDisplayName;
        private String pointOfSaleName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BuyerDTO {
        private String name;
        private String taxId;
        private String address;
        private String phone;
        private String email;
        private String regime;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InvoiceLineDTO {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal vatRate;
        private BigDecimal vatAmount; // on peut l’afficher
        private BigDecimal discount;
        private BigDecimal lineTotal;
        private String sku;
        private String unit;
        private String productCode;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TotalsDTO {
        private BigDecimal subtotal;
        private BigDecimal totalVat;
        private BigDecimal totalAmount;
        private BigDecimal totalDiscount;
        // 🔥 Nouveaux champs validés
        private BigDecimal otherTaxes;
        private BigDecimal totalToPay;
    }
}
