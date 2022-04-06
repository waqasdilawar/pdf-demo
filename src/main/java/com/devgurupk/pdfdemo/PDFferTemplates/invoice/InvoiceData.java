package com.devgurupk.pdfdemo.PDFferTemplates.invoice;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceData {
    private String invoiceNo;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String client;
    private String sender;
    private String clientAddress;
    private String senderAddress;
    private String clientEmail;
    private String senderEmail;
    private String clientTaxCode;
    private String senderTaxCode;
    private String currency;
    private List<LineItemData> lineItems;
    private String footer1;
    private String footer2;
    private String bankDetails;

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getClientTaxCode() {
        return clientTaxCode;
    }

    public void setClientTaxCode(String clientTaxCode) {
        this.clientTaxCode = clientTaxCode;
    }

    public String getSenderTaxCode() {
        return senderTaxCode;
    }

    public void setSenderTaxCode(String senderTaxCode) {
        this.senderTaxCode = senderTaxCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<LineItemData> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItemData> lineItems) {
        this.lineItems = lineItems;
    }

    public String getFooter1() {
        return footer1;
    }

    public void setFooter1(String footer1) {
        this.footer1 = footer1;
    }

    public String getFooter2() {
        return footer2;
    }

    public void setFooter2(String footer2) {
        this.footer2 = footer2;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public double getSubtotal() {
        return lineItems.stream().mapToDouble(LineItemData::getItemTotal).sum();
    }

    public Map<String, Double> getTaxSubtotals() {
        Map<String, Double> totals = new HashMap<>(lineItems.size());
        for (LineItemData item : lineItems) {
            totals.put(item.getTaxName(), totals.getOrDefault(item.getTaxName(), 0.0) + item.getTaxTotal());
        }
        return totals;
    }

    public double getTotal() {
        return getSubtotal() + lineItems.stream().mapToDouble(LineItemData::getTaxTotal).sum();
    }
}
