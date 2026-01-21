package com.oxalio.invoice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "invoice_lines",
    indexes = {
        @Index(name = "idx_invoice_lines_invoice_id", columnList = "invoice_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "invoice")
@EqualsAndHashCode(exclude = "invoice")
public class InvoiceLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceEntity invoice;

    // Référence article (facultatif) – utile pour le PDF "spécimen"
    @Column(name = "sku", length = 64)
    private String sku;

    // Unité de mesure (ex: PCS, KG, L, H) – facultative
    @Column(name = "unit", length = 16)
    private String unit;

    @Column(nullable = false, length = 512)
    private String description;

    // Quantité : mets scale=3 si besoin de décimales fines
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    // Taux de TVA en %, ex: 18.00
    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discount;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    // Alias de compat : si tu avais "productCode" côté legacy
    @Deprecated
    @Column(name = "product_code", length = 100)
    private String productCode;

    // ════════════════════════════════════════════════════════════════
    // ✨ NOUVEAU CHAMP FNE (V8) - Pour système de refund
    // ════════════════════════════════════════════════════════════════
    
    /**
     * ID UUID de l'item retourné par l'API FNE.
     * Utilisé pour créer des refunds (avoirs) sur cet item spécifique.
     * Exemple: "d0e59056-dbeb-43e8-8086-5ae173cc8e62"
     */
    @Column(name = "fne_item_id", length = 36)
    private String fneItemId;

    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (discount == null)   discount   = BigDecimal.ZERO;
        if (vatAmount == null)  vatAmount  = BigDecimal.ZERO;
        if (quantity == null)   quantity   = BigDecimal.ZERO;
        if (unitPrice == null)  unitPrice  = BigDecimal.ZERO;
        if (vatRate == null)    vatRate    = BigDecimal.ZERO;
        if (lineTotal == null)  lineTotal  = BigDecimal.ZERO;
    }
}
