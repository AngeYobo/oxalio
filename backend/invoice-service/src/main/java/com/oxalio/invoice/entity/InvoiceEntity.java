package com.oxalio.invoice.entity;

import com.oxalio.invoice.model.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data                        // ✅ Génère automatiquement tous les getters/setters, equals, hashCode, toString
@NoArgsConstructor           // ✅ Génère un constructeur vide
@AllArgsConstructor          // ✅ Génère un constructeur avec tous les champs
@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private String invoiceType;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Instant issueDate;

    // Seller
    private String sellerTaxId;
    private String sellerCompanyName;
    private String sellerAddress;

    // Buyer
    private String buyerTaxId;
    private String buyerName;
    private String buyerAddress;

    // Totals
    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalVat;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    // Payment
    private String paymentMode;

    // Sticker / QR
    private String stickerId;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String qrBase64;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    // Champs DGI
    private String dgiReference;
    private Instant dgiSubmittedAt;
}
