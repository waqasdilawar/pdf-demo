package com.devgurupk.pdfdemo.pdffer.template;

import java.util.Map;

public interface PdfTemplate {
    Map<String, Object> getPdfData();
    void setPdfData(Map<String, Object> data);
    boolean validate();
    void generate();
    byte[] getPdfContent();
}
