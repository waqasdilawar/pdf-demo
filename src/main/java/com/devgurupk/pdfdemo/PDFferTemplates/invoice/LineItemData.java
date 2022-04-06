package com.devgurupk.pdfdemo.PDFferTemplates.invoice;

public class LineItemData {
    private String description;
    private int quantity;
    private String unitFormat;
    private double unitPrice;
    private String taxName;
    private double taxRate;

    public String getUnitFormat() {
        return unitFormat;
    }

    public void setUnitFormat(String unitFormat) {
        this.unitFormat = unitFormat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxRName(String taxName) {
        this.taxName = taxName;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getItemTotal() {
        return unitPrice * quantity;
    }

    public double getTaxTotal() {
        return unitPrice * quantity * taxRate;
    }
}
