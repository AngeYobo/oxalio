package com.oxalio.invoice.dto;

import com.oxalio.invoice.model.SellerDTO;
import com.oxalio.invoice.model.BuyerDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {

    private String invoiceNumber;

    @NotBlank(message = "Le type de facture est obligatoire")
    @Pattern(regexp = "STANDARD|PROFORMA|CREDIT_NOTE", message = "Type de facture invalide")
    private String invoiceType;

    @NotBlank(message = "La devise est obligatoire")
    @Pattern(regexp = "XOF|USD|EUR", message = "Devise non supportée")
    private String currency;

    @NotNull(message = "Les informations du vendeur sont obligatoires")
    @Valid
    private SellerDTO seller;

    @NotNull(message = "Les informations de l'acheteur sont obligatoires")
    @Valid
    private BuyerDTO buyer;

    @NotEmpty(message = "Au moins une ligne de facture est requise")
    @Valid
    private List<InvoiceLineDTO> lines;

    @NotNull(message = "Les totaux sont obligatoires")
    @Valid
    private TotalsDTO totals;

    @NotBlank(message = "Le mode de paiement est obligatoire")
    @Pattern(regexp = "CASH|TRANSFER|CARD|MOBILE", message = "Mode de paiement invalide")
    private String paymentMode;

    @Size(max = 500, message = "Les notes ne peuvent excéder 500 caractères")
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineDTO {

        /** Référence article (facultatif) */
        @Size(max = 64, message = "La référence (SKU) ne peut excéder 64 caractères")
        private String sku;

        /** Unité (facultatif), ex: PCS, KG, L, H… */
        @Size(max = 16, message = "L'unité ne peut excéder 16 caractères")
        private String unit;

        @NotBlank(message = "La description du produit est obligatoire")
        @Size(min = 3, max = 500, message = "La description doit contenir entre 3 et 500 caractères")
        private String description;

        @NotNull(message = "La quantité est obligatoire")
        @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
        @Digits(integer = 10, fraction = 3, message = "Format de quantité invalide") // <-- 3 décimales pour coller au scale=3
        private BigDecimal quantity;

        @NotNull(message = "Le prix unitaire est obligatoire")
        @DecimalMin(value = "0.00", message = "Le prix unitaire doit être positif ou nul")
        @Digits(integer = 15, fraction = 2, message = "Format de prix invalide")
        private BigDecimal unitPrice;

        @NotNull(message = "Le taux de TVA est obligatoire")
        @DecimalMin(value = "0.00", message = "Le taux de TVA doit être positif ou nul")
        @DecimalMax(value = "100.00", message = "Le taux de TVA ne peut excéder 100%")
        @Digits(integer = 3, fraction = 2, message = "Format de taux de TVA invalide")
        private BigDecimal vatRate;

        @NotNull(message = "Le montant de TVA est obligatoire")
        @DecimalMin(value = "0.00", message = "Le montant de TVA doit être positif ou nul")
        @Digits(integer = 15, fraction = 2, message = "Format de montant de TVA invalide")
        private BigDecimal vatAmount;

        @NotNull(message = "La remise est obligatoire (peut être 0)")
        @DecimalMin(value = "0.00", message = "La remise doit être positive ou nulle")
        @Digits(integer = 15, fraction = 2, message = "Format de remise invalide")
        private BigDecimal discount;

        @Size(max = 100, message = "Le code produit ne peut excéder 100 caractères")
        private String productCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalsDTO {

        @NotNull(message = "Le sous-total est obligatoire")
        @DecimalMin(value = "0.00")
        @Digits(integer = 15, fraction = 2)
        private BigDecimal subtotal;

        @NotNull(message = "Le total de TVA est obligatoire")
        @DecimalMin(value = "0.00")
        @Digits(integer = 15, fraction = 2)
        private BigDecimal totalVat;

        @NotNull(message = "Le montant total est obligatoire")
        @DecimalMin(value = "0.00")
        @Digits(integer = 15, fraction = 2)
        private BigDecimal totalAmount;

        @DecimalMin(value = "0.00")
        @Digits(integer = 15, fraction = 2)
        private BigDecimal totalDiscount;

        // 🔥 champs manquants (CAUSE DE L'ERREUR)
        @DecimalMin(value = "0.00")
        @Digits(integer = 15, fraction = 2)
        private BigDecimal otherTaxes;

        @DecimalMin(value = "0.00")
        @Digits(integer = 15, fraction = 2)
        private BigDecimal totalToPay;
    }

    @NotBlank(message = "Le template est obligatoire (B2B ou B2C)")
    @Pattern(regexp = "B2B|B2C", message = "Le template doit être B2B (Facture) ou B2C (Reçu)")
    private String template; //

    private Boolean isRne;   // Pour la traçabilité DGI
    
    private String rne;      // Référence du reçu d'origine

}
