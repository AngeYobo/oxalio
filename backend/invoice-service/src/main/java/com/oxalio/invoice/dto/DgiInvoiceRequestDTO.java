package com.oxalio.invoice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO pour la requête envoyée à la DGI.
 * Ce format doit coller au format d'API DGI (FNE).
 */
public class DgiInvoiceRequestDTO {

    private String invoiceNumber;       // Numéro facture unique
    private String invoiceType;         // STANDARD / AVOIR / etc.
    private String currency;            // XOF, EUR...
    private Instant issueDate;
    private Seller seller;
    private Buyer buyer;
    private List<LineItem> lines;
    private Totals totals;
    private String paymentMode;         // ex: CASH, TRANSFER, CARD

    public static class Seller {
        public String taxId;
        public String companyName;
        public String address;
    }

    public static class Buyer {
        public String taxId;
        public String name;
        public String address;
    }

    public static class LineItem {
        public String description;
        public BigDecimal quantity;
        public BigDecimal unitPrice;
        public BigDecimal vatRate;
        public BigDecimal vatAmount;
        public BigDecimal discount;
    }

    public static class Totals {
        public BigDecimal subtotal;
        public BigDecimal totalVat;
        public BigDecimal totalAmount;
    }

    // Getters & Setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getIssueDate() { return issueDate; }
    public void setIssueDate(Instant issueDate) { this.issueDate = issueDate; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public Buyer getBuyer() { return buyer; }
    public void setBuyer(Buyer buyer) { this.buyer = buyer; }

    public List<LineItem> getLines() { return lines; }
    public void setLines(List<LineItem> lines) { this.lines = lines; }

    public Totals getTotals() { return totals; }
    public void setTotals(Totals totals) { this.totals = totals; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}
