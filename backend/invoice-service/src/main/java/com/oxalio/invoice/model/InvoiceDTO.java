package com.oxalio.invoice.model;

import com.oxalio.invoice.dto.SellerDTO;
import com.oxalio.invoice.dto.BuyerDTO;

import java.time.LocalDateTime;
import java.util.List;

public class InvoiceDTO {
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private String currency;
    private String invoiceType;
    private String paymentMode;

    private SellerDTO seller;
    private BuyerDTO buyer;
    private List<InvoiceLineDTO> lines;
    private TotalsDTO totals;

    private String status;
    private String hash;
    private String signature;
    private String qrCode;

    // Getters/Setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public SellerDTO getSeller() { return seller; }
    public void setSeller(SellerDTO seller) { this.seller = seller; }
    public BuyerDTO getBuyer() { return buyer; }
    public void setBuyer(BuyerDTO buyer) { this.buyer = buyer; }
    public List<InvoiceLineDTO> getLines() { return lines; }
    public void setLines(List<InvoiceLineDTO> lines) { this.lines = lines; }
    public TotalsDTO getTotals() { return totals; }
    public void setTotals(TotalsDTO totals) { this.totals = totals; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
