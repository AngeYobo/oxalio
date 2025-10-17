package com.oxalio.invoice.model;

public class BuyerDTO {
    private String taxId;
    private String name;
    private String address;

    // Getters/Setters
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
