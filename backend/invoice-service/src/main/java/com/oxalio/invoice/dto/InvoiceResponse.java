package com.oxalio.invoice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO de réponse pour une facture créée ou consultée.
 * 
 * <p>Exemple de payload JSON :</p>
 * <pre>
 * {
 *   "id": 1,
 *   "invoiceNumber": "INV-2025-000002",
 *   "invoiceType": "STANDARD",
 *   "currency": "XOF",
 *   "issueDate": "2025-10-22T11:09:34.737941089Z",
 *   "seller": {
 *     "taxId": "CI1234567",
 *     "companyName": "Oxalio SARL",
 *     "address": "Abidjan, Plateau",
 *     "email": "contact@oxalio.com",
 *     "phone": "+2250701020304"
 *   },
 *   "buyer": {
 *     "taxId": "CI7654321",
 *     "name": "Client Démo",
 *     "address": "Cocody, Riviera",
 *     "email": "client@example.com",
 *     "phone": "+2250705060708"
 *   },
 *   "lines": [
 *     {
 *       "description": "Produit A",
 *       "quantity": 2,
 *       "unitPrice": 10000,
 *       "vatRate": 18,
 *       "vatAmount": 3600,
 *       "discount": 0,
 *       "productCode": "PROD-A001",
 *       "lineTotal": 23600
 *     }
 *   ],
 *   "totals": {
 *     "subtotal": 20000,
 *     "totalVat": 3600,
 *     "totalAmount": 23600,
 *     "totalDiscount": 0
 *   },
 *   "paymentMode": "TRANSFER",
 *   "stickerId": "STKR-BB460D09",
 *   "qrBase64": "iVBORw0KGgoAAAANSUhEUg...",
 *   "status": "RECEIVED",
 *   "dgiReference": null,
 *   "dgiSubmittedAt": null,
 *   "notes": "Facture de test",
 *   "createdAt": "2025-10-22T11:09:34.737941089Z",
 *   "updatedAt": "2025-10-22T11:09:34.737941089Z"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;

    private String invoiceNumber;

    private String invoiceType;

    private String currency;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant issueDate;

    private SellerDTO seller;

    private BuyerDTO buyer;

    private List<InvoiceLineDTO> lines;

    private TotalsDTO totals;

    private String paymentMode;

    private String stickerId;

    private String qrBase64;

    private String status;

    private String dgiReference;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant dgiSubmittedAt;

    private String notes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    // ✅ Champs ajoutés pour Mock & DGI
    private String reference;
    private String message;
    private String signature;
    private String qrCode;
    private Instant processedAt;

    /**
     * DTO pour les informations du vendeur dans la réponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerDTO {
        private String taxId;
        private String companyName;
        private String address;
        private String email;
        private String phone;
    }

    /**
     * DTO pour les informations de l'acheteur dans la réponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyerDTO {
        private String taxId;
        private String name;
        private String address;
        private String email;
        private String phone;
    }

    /**
     * DTO pour une ligne de facture dans la réponse.
     * Inclut le calcul automatique du montant total de la ligne.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineDTO {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal vatRate;
        private BigDecimal vatAmount;
        private BigDecimal discount;
        private String productCode;
        
        /**
         * Montant total de la ligne calculé automatiquement.
         * Formule : (quantity × unitPrice) - discount + vatAmount
         */
        private BigDecimal lineTotal;
        
        /**
         * Calcule et définit le montant total de cette ligne.
         * À appeler après l'initialisation des autres champs.
         */
        public void calculateLineTotal() {
            if (quantity != null && unitPrice != null) {
                BigDecimal subtotal = quantity.multiply(unitPrice);
                BigDecimal afterDiscount = discount != null ? subtotal.subtract(discount) : subtotal;
                this.lineTotal = vatAmount != null ? afterDiscount.add(vatAmount) : afterDiscount;
            }
        }
    }

    /**
     * DTO pour les totaux dans la réponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalsDTO {
        private BigDecimal subtotal;
        private BigDecimal totalVat;
        private BigDecimal totalAmount;
        private BigDecimal totalDiscount;
    }
}