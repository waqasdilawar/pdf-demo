package com.devgurupk.pdfdemo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class PdfferErrorController implements ErrorController {

    // The template resource for the /error page
    @Value("classpath:/explorer/error.html")
    private Resource errorHtmlTemplate;

    /*
     * Takes a Resource object that represents an HTML template and returns a String with the HTML
     * content and any placeholder properly resolved to the corresponding values.
     * No placeholders are available in this module
     */
    private static String htmlTemplateAsString(Resource resource, HttpServletRequest request) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
            Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
            return FileCopyUtils.copyToString(reader)
                    .replace("{{status}}", statusCode.toString())
                    .replace("{{exception}}", exception != null ? exception.getMessage() : "-");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @RequestMapping("/error")
    @ResponseBody
    public String handleError(HttpServletRequest request) {
        return htmlTemplateAsString(errorHtmlTemplate, request);
    }

}
