package com.devgurupk.pdfdemo.PDFferTemplates.invoice;

import com.devgurupk.pdfdemo.pdffer.template.PdfTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PdfInvoiceTemplate implements PdfTemplate {
    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private Document layoutDocument;
    private Map<String, Object> invoiceMap;
    private InvoiceData invoiceData;
    private byte[] pdfContent;
    private PageSize pageSize = PageSize.A4;
    private NumberFormat priceFormat;

    private PdfFont baseFont;

    static {
        jsonMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Map<String, Object> getPdfData() {
        return invoiceMap;
    }

    @Override
    public void setPdfData(Map<String, Object> data) {
        invoiceMap = data;
    }

    @Override
    public boolean validate() {
        try {
            invoiceData = jsonMapper.convertValue(invoiceMap, InvoiceData.class);
            priceFormat = NumberFormat.getCurrencyInstance();
            priceFormat.setCurrency(Currency.getInstance(invoiceData.getCurrency()));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void generate() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(os));
        pdfDocument.setDefaultPageSize(pageSize);
        try {
            baseFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        } catch (IOException e) {
            // This will not happen as font is built-in
            throw new RuntimeException(e);
        }
        layoutDocument = new Document(pdfDocument);
        InvoicePageEventHandler pageHandler = new InvoicePageEventHandler();
        pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, pageHandler);
        layoutDocument.setMargins(pageHandler.headerHeight + 25f,25f,/* // TODO bottom margin of layout should take footer into account */150f,25f);
        if (invoiceData != null || validate()) {
            addLineItems();
            addLineItemSummary();
            pageHandler.writeTotalPages(pdfDocument);
            layoutDocument.close();
        }
        pdfContent = os.toByteArray();
    }

    @Override
    public byte[] getPdfContent() {
        return pdfContent;
    }

    private void addLineItems() {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 50f, 10f, 15f, 10f, 15f }))
                .useAllAvailableWidth()
                .setMargins(0f, 0f, 13f, 0f)
                ;

        // headers
        Cell descHeader = new Cell().add(new Paragraph("Description").setBold().setFontColor(ColorConstants.WHITE))
                .setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(ColorConstants.WHITE, 1))
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addHeaderCell(descHeader);

        Cell qtyHeader = new Cell().add(new Paragraph("Quantity").setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(ColorConstants.WHITE, 1))
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addHeaderCell(qtyHeader);

        Cell priceHeader = new Cell().add(new Paragraph("Unit price").setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(ColorConstants.WHITE, 1))
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addHeaderCell(priceHeader);

        Cell vatHeader = new Cell().add(new Paragraph("VAT rate").setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(ColorConstants.WHITE, 1))
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addHeaderCell(vatHeader);

        Cell totalHeader = new Cell().add(new Paragraph("Net amount").setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addHeaderCell(totalHeader);

        float itemFontSize = 9f;
        float itemLeading = 0.9f;

        for (LineItemData item : invoiceData.getLineItems()) {
            table.addCell(new Cell().add(new Paragraph(item.getDescription()).setFontSize(itemFontSize).setMultipliedLeading(itemLeading).setKeepTogether(true)).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.GRAY, 1)));
            table.addCell(new Cell().add(new Paragraph(String.format(item.getUnitFormat(), item.getQuantity())).setFontSize(itemFontSize).setMultipliedLeading(itemLeading).setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.GRAY, 1)));
            table.addCell(new Cell().add(new Paragraph(priceFormat.format(item.getUnitPrice())).setFontSize(itemFontSize).setMultipliedLeading(itemLeading).setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.GRAY, 1)));
            table.addCell(new Cell().add(new Paragraph(percentFormat.format(item.getTaxRate())).setFontSize(itemFontSize).setMultipliedLeading(itemLeading).setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.GRAY, 1)));
            table.addCell(new Cell().add(new Paragraph(priceFormat.format(item.getItemTotal())).setFontSize(itemFontSize).setMultipliedLeading(itemLeading).setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(ColorConstants.LIGHT_GRAY).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.GRAY, 1)));
        }

        Cell cell = new Cell(1, 5).add(new Paragraph("continues...").setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(ColorConstants.WHITE, 1))
                .setBackgroundColor(ColorConstants.BLUE)
                ;

        table.addFooterCell(cell).setSkipLastFooter(true);

        layoutDocument.add(table);
    }

    public void addLineItemSummary() {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 63f, 19f, 18f }))
                .useAllAvailableWidth()
                .setMargins(0f, 0f, 13f, 0f)
                .setKeepTogether(true)
                ;

        // headers
        Cell msgCell = new Cell().add(new Paragraph("Please add the invoice number to your payment description.").setBold())
                .setBorder(Border.NO_BORDER)
                ;
        table.addCell(msgCell);

        Cell subtotLabelCell = new Cell().add(new Paragraph("Subtotal:"))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                ;
        table.addCell(subtotLabelCell);

        Cell subtotCell = new Cell().add(new Paragraph(priceFormat.format(invoiceData.getSubtotal())).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                ;
        table.addCell(subtotCell);

        for (Map.Entry<String, Double> tax : invoiceData.getTaxSubtotals().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toList())) {

            table.addCell(new Cell().setBorder(Border.NO_BORDER));

            Cell taxLabelCell = new Cell().add(new Paragraph(tax.getKey() + ":"))
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    ;
            table.addCell(taxLabelCell);

            Cell taxCell = new Cell().add(new Paragraph(priceFormat.format(tax.getValue())).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    ;
            table.addCell(taxCell);

        }

        table.addCell(new Cell().setBorder(Border.NO_BORDER));

        Cell totalLabelCell = new Cell().add(new Paragraph("Invoice Total:").setBold().setFontColor(ColorConstants.WHITE))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addCell(totalLabelCell);

        Cell totalCell = new Cell().add(new Paragraph(priceFormat.format(invoiceData.getTotal())).setTextAlignment(TextAlignment.RIGHT).setBold().setFontColor(ColorConstants.WHITE))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.BLUE)
                ;
        table.addCell(totalCell);

        layoutDocument.add(table);

    }

    private Div createMultipleLineDiv(List<String> lines) {
        Div outerDiv = new Div();
        for (String line : lines) {
            outerDiv.add(new Paragraph(line)
                    .setFontSize(11f)
                    .setMultipliedLeading(0.5f)
            );
        }
        return outerDiv;
    }

    private class InvoicePageEventHandler implements IEventHandler {
        private float phSide = 20;
        private float phX = 300;
        private float phY = 15;
        private float phSpace = 3;
        private float phDescent = 3;
        private float phFontSize = 9;
        private PdfFormXObject placeholderTotPages = new PdfFormXObject(new Rectangle(0, 0, phSide, phSide));

        private float headerHeight;
        private float footerHeight;

        InvoicePageEventHandler() {
            LayoutResult layoutResult = createFooterTable().createRendererSubTree().setParent(layoutDocument.getRenderer()).layout(new LayoutContext(new LayoutArea(1, pageSize)));
            footerHeight = layoutResult.getOccupiedArea().getBBox().getHeight();
            layoutResult = createHeaderDiv().createRendererSubTree().setParent(layoutDocument.getRenderer()).layout(new LayoutContext(new LayoutArea(1, pageSize)));
            headerHeight = layoutResult.getOccupiedArea().getBBox().getHeight();
        }

        private Paragraph createTitleTable() {
            Style titleStyle = new Style()
                    .setFont(baseFont)
                    .setFontSize(39f)
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBold()
                    .setMargins(0f, 0f, 13f, 0f)
                    ;
            return new Paragraph("INVOICE").addStyle(titleStyle);
        }

        private Table createBasicInfoTable() {
            Table table = new Table(UnitValue.createPercentArray(new float[] { 50f, 50f }))
                    .setMargins(0f, 0f, 13f, 0f)
                    ;

            // headers
            table.addCell(new Paragraph("Invoice #").setBold().setMultipliedLeading(0.9f));
            table.addCell(new Paragraph(invoiceData.getInvoiceNo()).setBold().setMultipliedLeading(0.9f));

            table.addCell(new Paragraph("Date:").setMultipliedLeading(0.9f));
            table.addCell(new Paragraph(invoiceData.getInvoiceDate().format(dateFormat)).setMultipliedLeading(0.9f));

            table.addCell(new Paragraph("Due Date").setMultipliedLeading(0.9f));
            table.addCell(new Paragraph(invoiceData.getDueDate().format(dateFormat)).setMultipliedLeading(0.9f));

            for (IElement element : table.getChildren()) {
                if (element instanceof Cell) ((Cell)element).setBorder(Border.NO_BORDER);
            }

            return table;
        }

        public Table createPartiesTable() {
            Table table = new Table(UnitValue.createPercentArray(new float[] { 48f, 4f, 48f }))
                    .useAllAvailableWidth()
                    .setMargins(0f, 0f, 13f, 0f)
                    ;

            // headers
            Cell sentToHeader = new Cell().add(new Paragraph("Sent to:").setBold())
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(ColorConstants.BLUE, 1))
                    ;
            table.addCell(sentToHeader);

            table.addCell(new Cell().setBorder(Border.NO_BORDER));

            Cell sentByHeader = new Cell().add(new Paragraph("Sent by:").setBold())
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(ColorConstants.BLUE, 1))
                    ;
            table.addCell(sentByHeader);

            // party names
            String sentTo = invoiceData.getClient() +
                    (invoiceData.getClientTaxCode() != null && !invoiceData.getClientTaxCode().isEmpty() ? "\nVAT ID: " + invoiceData.getClientTaxCode() : "") + "\n" +
                    invoiceData.getClientAddress() + "\n(" +
                    invoiceData.getClientEmail() +")";
            Div sentToDiv = createMultipleLineDiv(sentTo.lines().collect(Collectors.toList()));

            String sentBy = invoiceData.getSender() +
                    (invoiceData.getSenderTaxCode() != null && !invoiceData.getSenderTaxCode().isEmpty() ? "\nVAT ID: " + invoiceData.getSenderTaxCode() : "") + "\n" +
                    invoiceData.getSenderAddress() + "\n(" +
                    invoiceData.getSenderEmail() +")";
            Div sentByDiv = createMultipleLineDiv(sentBy.lines().collect(Collectors.toList()));

            ((Paragraph)sentToDiv.getChildren().get(0)).setBold();
            ((Paragraph)sentByDiv.getChildren().get(0)).setBold();

            table.addCell(new Cell().add(sentToDiv).setBorder(Border.NO_BORDER));
            table.addCell(new Cell().setBorder(Border.NO_BORDER));
            table.addCell(new Cell().add(sentByDiv).setBorder(Border.NO_BORDER));

            return table;
        }

        private Div createHeaderDiv() {
            Div div = new Div();
            div
                    .add(createTitleTable())
                    .add(createBasicInfoTable())
                    .add(createPartiesTable())
            ;

            return div;
        }

        private Table createFooterTable() {
            Table table = new Table(UnitValue.createPercentArray(new float[]{30f, 30f, 40f})).useAllAvailableWidth();
            Cell cell = new Cell(1,3).add(new Paragraph("Thank you for your business!").setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLUE, 1));
            table.addCell(cell);

            cell = new Cell().add(new Paragraph("Company contacts:").setBold()).setBorder(Border.NO_BORDER);
            table.addCell(cell);
            cell = new Cell().setBorder(Border.NO_BORDER);
            table.addCell(cell);
            cell = new Cell().add(new Paragraph("Bank account:").setBold()).setBorder(Border.NO_BORDER);
            table.addCell(cell);

            cell = new Cell().add(createMultipleLineDiv(invoiceData.getFooter1().lines().collect(Collectors.toList()))).setBorder(Border.NO_BORDER);
            table.addCell(cell);
            cell = new Cell().add(createMultipleLineDiv(invoiceData.getFooter2().lines().collect(Collectors.toList()))).setBorder(Border.NO_BORDER);
            table.addCell(cell);
            cell = new Cell().add(createMultipleLineDiv(invoiceData.getBankDetails().lines().collect(Collectors.toList()))).setBorder(Border.NO_BORDER);
            table.addCell(cell);

            return table;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent)event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            float rightMargin = layoutDocument.getRightMargin();
            float leftMargin = layoutDocument.getLeftMargin();
            float bottomMargin = 25f; // TODO this should not be hard coded - bottom margin of layout should take footer into account
            float topMargin = 25f; // TODO this should not be hard coded - bottom margin of layout should take footer into account

            int pageNumber = pdfDoc.getPageNumber(page);
            Paragraph p = new Paragraph()
                    .setFontSize(phFontSize)
                    .add("Page ")
                    .add(String.valueOf(pageNumber))
                    .add(" of");

            new Canvas(pdfCanvas, new Rectangle(rightMargin, page.getPageSize().getHeight() - topMargin - headerHeight, page.getPageSize().getWidth() - rightMargin - leftMargin, headerHeight))
                    .add(createHeaderDiv())
                    .close();

            new Canvas(pdfCanvas, new Rectangle(rightMargin, 0f, page.getPageSize().getWidth() - rightMargin - leftMargin, footerHeight + bottomMargin))
                    .add(createFooterTable())
                    .showTextAligned(p, phX, phY, TextAlignment.RIGHT)
                    .close();

            pdfCanvas.addXObjectAt(placeholderTotPages, phX + phSpace, phY - phDescent);

            pdfCanvas.release();
        }

        public void writeTotalPages(PdfDocument pdfDoc) {
            Canvas canvas = new Canvas(placeholderTotPages, pdfDoc);
            Paragraph p = new Paragraph()
                    .setFontSize(phFontSize)
                    .add(String.valueOf(pdfDoc.getNumberOfPages()));
            canvas.showTextAligned(p,0, phDescent, TextAlignment.LEFT);
            canvas.close();
        }
    }

}
