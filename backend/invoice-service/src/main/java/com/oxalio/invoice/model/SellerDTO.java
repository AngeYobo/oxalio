package com.oxalio.invoice.model;

public class SellerDTO {
    private String taxId;
    private String companyName;
    private String address;

    // Getters/Setters
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
