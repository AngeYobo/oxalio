package com.oxalio.invoice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private String buyerName;
    private Double totalAmount;
    private String currency;
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.RECEIVED;

    @Column(length = 2048)
    private String dgiResponse;

    // Getters & setters
    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getCreatedAt() { return createdAt; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public String getDgiResponse() { return dgiResponse; }
    public void setDgiResponse(String dgiResponse) { this.dgiResponse = dgiResponse; }
}
