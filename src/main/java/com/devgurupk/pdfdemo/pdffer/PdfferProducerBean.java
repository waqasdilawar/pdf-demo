package com.devgurupk.pdfdemo.pdffer;

import com.devgurupk.pdfdemo.PDFferTemplates.DefaultPdfTemplate;
import com.devgurupk.pdfdemo.PDFferTemplates.invoice.PdfInvoiceTemplate;
import com.devgurupk.pdfdemo.pdffer.template.PdfTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PdfferProducerBean {

    public byte[] generatePdfDocument(String templateName, Map<String, Object> data) {
        PdfTemplate template = findTemplate(templateName);
        template.setPdfData(data);
        if (!template.validate()) {
            throw new IllegalArgumentException("PDF Template payload is not valid");
        }
        template.generate();
        return template.getPdfContent();
    }

    PdfTemplate findTemplate(String templateName) {
        return switch (templateName) {
            case "invoice" -> new PdfInvoiceTemplate();
            default -> new DefaultPdfTemplate();
        };
    }

}
