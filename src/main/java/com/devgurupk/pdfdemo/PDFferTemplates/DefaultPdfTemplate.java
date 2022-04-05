package com.devgurupk.pdfdemo.PDFferTemplates;

import com.devgurupk.pdfdemo.pdffer.template.PdfTemplate;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import java.util.Map;

public class DefaultPdfTemplate implements PdfTemplate {
    private Map<String, Object> data;
    private byte[] output;

	@Override
	public Map<String, Object> getPdfData() {
		return data;
	}

	@Override
	public void setPdfData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public void generate() {
	    // We will write our PDF to this output stream as a sequence of bytes
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// The PdfDocument object represents your PDF file and its backing storage (the output stream in our case)
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(os));
        // The default page size will be A4
        pdfDocument.setDefaultPageSize(PageSize.A4);

        // The Document object is an abstraction representing the structure of the PDF and its contents
        Document layoutDocument = new Document(pdfDocument);
        // We leave a small margin between the page edges and the contents
        layoutDocument.setMargins(25f, 25f, 25f, 25f);

        // Define a table that takes up all available width on the page
        // The table will have three columns of relative size 47.50 | 5.00 | 47.50
        Table table = new Table(UnitValue.createPercentArray(new float[] { 47.5f, 5f, 47.5f }))
                .useAllAvailableWidth()
                .setMargins(0f, 0f, 0f, 0f)
                ;

        // We add three header cells to the table for the first row (the header row)
        // Header cells are special because they are repeated on every page if the table crosses over to a new page

        Cell keyCell = new Cell().add(new Paragraph("KEY").setBold())
                .setBorder(Border.NO_BORDER)
                ;
        table.addHeaderCell(keyCell);

        table.addHeaderCell(new Cell().setBorder(Border.NO_BORDER));

        Cell valueCell = new Cell().add(new Paragraph("VALUE").setBold())
                .setBorder(Border.NO_BORDER)
                ;
        table.addHeaderCell(valueCell);

        //

        for (Map.Entry<String,Object> pair : data.entrySet()) {

            Cell kCell = new Cell().add(new Paragraph(pair.getKey()))
                    .setBorder(Border.NO_BORDER)
                    ;

            table.addCell(kCell);

            table.addCell(new Cell().setBorder(Border.NO_BORDER));
            Cell vCell = new Cell().add(new Paragraph(pair.getValue().toString()))
                    .setBorder(Border.NO_BORDER)
                    ;
            table.addCell(vCell);

        }

        // We add the full table to the document layout
        layoutDocument.add(table);

        // By closing the Document object we finalize the PDF, which gets generated into the output stream we created before
        layoutDocument.close();

        // We get the bytes of the PDF from the output stream and store them in the template instance
        // The PDF bytes can be accessed from the outside with the getPdfContent method
        output = os.toByteArray();
	}

	@Override
	public byte[] getPdfContent() {
		return output;
	}

}
