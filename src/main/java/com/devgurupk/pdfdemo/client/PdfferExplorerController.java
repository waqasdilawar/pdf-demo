package com.devgurupk.pdfdemo.client;

import com.devgurupk.pdfdemo.pdffer.PdfferProducerBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
@RequestMapping("explorer")
public class PdfferExplorerController {

    /*
     * Takes a Resource object that represents an HTML template and returns a String with the HTML
     * content and any placeholder properly resolved to the corresponding values.
     * No placeholders are available in this module
     */
    private static String htmlTemplateAsString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // The template resource for the /download page
    @Value("classpath:/explorer/download.html")
    private Resource downloadHtmlTemplate;

    private ObjectMapper mapper;
    private PdfferProducerBean pdffer;

    public PdfferExplorerController(ObjectMapper mapper, PdfferProducerBean pdffer) {
        this.mapper = mapper;
        this.pdffer = pdffer;
    }


    /**
     * Controller method mapped to GET /download by default
     * @return the HTML content for the download form
     */
    @GetMapping(value = "download", produces = TEXT_HTML_VALUE)
    @ResponseBody
    public String downloadForm() {
        return htmlTemplateAsString(downloadHtmlTemplate);
     }

    /**
     * Controller method mapped to POST /download by default
     * @param template the "pdfTemplate" HTML form field with the name of the template to apply
     *                 (only "default" available in this module)
     * @param payload the JSON text of the data for the PDF template
     * @return the bytes of the PDF file that was generated by the framework
     * @throws JsonProcessingException if the payload is not valid JSON text
     */
    @PostMapping(value = "download", produces = APPLICATION_PDF_VALUE)
    @ResponseBody
    public byte[] processDownloadForm(@RequestParam("pdfTemplate") String template, @RequestParam("pdfPayload") String payload) throws JsonProcessingException {
        Map<String,Object> pdfData = mapper.readValue(payload, ((Class<Map<String, Object>>)(Class<?>)Map.class));
        return pdffer.generatePdfDocument(template, pdfData);
     }

}
