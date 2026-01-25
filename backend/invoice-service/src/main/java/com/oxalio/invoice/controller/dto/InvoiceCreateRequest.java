// src/main/java/com/oxalio/invoice/controller/dto/InvoiceCreateRequest.java
package com.oxalio.invoice.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceCreateRequest {

    @NotBlank
    private String invoiceType; // "sale" ou "purchase"

    @NotBlank
    private String currency; // "XOF" par défaut

    @NotBlank
    private String paymentMethod; // "cash", "mobile-money", etc.

    @NotBlank
    @Pattern(regexp = "B2B|B2C|B2F|B2G")
    private String template; // B2C etc.

    @NotNull
    private Boolean isRne;

    private String rne; // obligatoire si isRne=true

    // Buyer / Client
    @NotBlank
    private String clientCompanyName;

    @NotBlank
    private String clientPhone;

    @NotBlank
    @Email
    private String clientEmail;

    private String clientNcc; // optionnel selon template

    // POS
    @NotBlank
    private String establishment;

    @NotBlank
    private String pointOfSale;

    private String commercialMessage;

    @NotEmpty
    @Valid
    private List<Line> lines;

    @Data
    public static class Line {
        private String sku;
        private String unit;

        @NotBlank
        private String description;

        @NotNull
        @DecimalMin("0.000")
        private BigDecimal quantity;

        @NotNull
        @DecimalMin("0.00")
        private BigDecimal unitPrice;

        // Vat rate en % (ex 18.00). Mets 0 si exonéré.
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal vatRate;

        // Montant remise (valeur) ou 0
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal discount;
    }
}
