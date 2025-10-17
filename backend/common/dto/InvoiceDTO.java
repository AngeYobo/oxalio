package com.oxalio.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO partagé pour les factures.
 * - Sans Lombok pour compat maximale.
 * - Utilise BigDecimal pour les montants (précision financière).
 * - Enum pour le statut.
 */
public class InvoiceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant fonctionnel (ex: UUID ou numéro métier). */
    private String id;

    /** Montant total de la facture (TTC idealement). */
    @NotNull
    private BigDecimal amount;

    /** Code monnaie ISO 4217 (ex: XOF, EUR). */
    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency doit être un code ISO 4217 (3 lettres)")
    private String currency;

    /** Statut logique de la facture côté plateforme. */
    @NotNull
    private Status status;

    /** Horodatage de l’émission / création. */
    @NotNull
    private Instant timestamp;

    public InvoiceDTO() {
    }

    public InvoiceDTO(String id, BigDecimal amount, String currency, Status status, Instant timestamp) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.timestamp = timestamp;
    }

    // -------- Getters / Setters

    public String getId() {
        return id;
    }

    public InvoiceDTO setId(String id) {
        this.id = id;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public InvoiceDTO setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public InvoiceDTO setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public InvoiceDTO setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public InvoiceDTO setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    // -------- Compatibilité descendante (évite de casser l'existant)

    /**
     * @deprecated Utiliser {@link #getAmount()} (BigDecimal) pour la précision.
     */
    @Deprecated
    public double getAmountDouble() {
        return amount == null ? 0d : amount.doubleValue();
    }

    /**
     * @deprecated Utiliser {@link #setAmount(BigDecimal)}.
     */
    @Deprecated
    public InvoiceDTO setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
        return this;
    }

    // -------- equals / hashCode / toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceDTO)) return false;
        InvoiceDTO that = (InvoiceDTO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(amount, that.amount)
                && Objects.equals(currency, that.currency)
                && status == that.status
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, currency, status, timestamp);
    }

    @Override
    public String toString() {
        return "InvoiceDTO{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Statuts conseillés pour la plateforme (côté métier).
     * Garde DEMO_OK pour compat avec ton flux actuel, et ajoute ceux utiles en pré-prod.
     */
    public enum Status {
        DEMO_OK,
        DRAFT,
        SENT,
        VALIDATED,
        REJECTED,
        PENDING,
        PAID,
        ERROR
    }
}
