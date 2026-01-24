package com.oxalio.invoice.entity;

import com.oxalio.invoice.model.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "invoices",
    indexes = {
        @Index(name = "idx_invoices_seller_taxid", columnList = "sellerTaxId"),
        @Index(name = "idx_invoices_buyer_taxid",  columnList = "buyerTaxId"),
        @Index(name = "idx_invoices_issue_date",    columnList = "issueDate")
    }
)
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro unique (ex: INV-2025-000123) */
    @Column(nullable = false, unique = true, length = 64)
    private String invoiceNumber;

    /** Type fonctionnel (ex: SALE) – si tu veux, passe-le en Enum plus tard */
    @Column(nullable = false, length = 32)
    private String invoiceType;

    @Column(nullable = false, length = 8)
    private String currency;

    /** Date d'émission – définie par le service ou par @PrePersist */
    @Column(nullable = false)
    private Instant issueDate;

    // ---------------- Seller (résumé minimal persistant)
    @Column(length = 64)
    private String sellerTaxId;

    @Column(length = 255)
    private String sellerCompanyName;

    @Column(length = 512)
    private String sellerAddress;

    // ---------------- Buyer (résumé minimal persistant)
    @Column(length = 64)
    private String buyerTaxId;

    @Column(length = 255)
    private String buyerName;

    @Column(length = 512)
    private String buyerAddress;

    // ---------------- Totaux
    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalVat;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    // ---------------- Paiement (optionnel)
    @Column(name = "payment_method", length = 64)
    private String paymentMethod;
    

    // ---------------- Sticker / QR
    @Column(length = 64)
    private String stickerId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String qrBase64; // PNG/JPG en Base64

    // ---------------- Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvoiceStatus status;

    // ---------------- DGI
    @Column(length = 128)
    private String dgiReference;

    private Instant dgiSubmittedAt;

    // ---------------- Champs FNE (V5)
    @Column
    private String sellerDisplayName;

    @Column
    private String pointOfSaleName;

    @Column(precision = 15, scale = 2)
    private BigDecimal otherTaxes;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalToPay;

    // ════════════════════════════════════════════════════════════════
    // ✨ NOUVEAUX CHAMPS FNE (V8) - Pour système de refund
    // ════════════════════════════════════════════════════════════════
    
    /**
     * ID UUID de la facture retourné par l'API FNE.
     * Utilisé pour créer des refunds (avoirs).
     * Exemple: "e359054f-79a9-4f2a-84fe-4a44ff6c263b"
     */
    @Column(name = "fne_invoice_id", length = 36)
    private String fneInvoiceId;

    /**
     * Référence DGI de la facture (affichage).
     * Exemple: "2505842N26000000036"
     */
    @Column(name = "fne_reference", length = 50)
    private String fneReference;

    // ---------------- Lignes
    @OneToMany(
        mappedBy = "invoice",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<InvoiceLineEntity> lines = new ArrayList<>();

    // ---------------- Hooks
    @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = Instant.now();
        }
        if (status == null) {
            status = InvoiceStatus.RECEIVED;
        }
    }

    // ---------------- Traçabilité RNE/FNE
    @Column(length = 10)
    private String template;   // Stockera "B2B" ou "B2C"

    @Column
    private Boolean isRne;     // Indique si c'est une régularisation de reçu

    @Column(length = 64)
    private String rne;        // Référence du reçu d'origine le cas échéant
}