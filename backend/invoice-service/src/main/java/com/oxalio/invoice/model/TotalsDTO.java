package com.oxalio.invoice.model;

public class TotalsDTO {
    private double subtotal;
    private double totalVat;
    private double totalAmount;

    // Getters/Setters
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getTotalVat() { return totalVat; }
    public void setTotalVat(double totalVat) { this.totalVat = totalVat; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
