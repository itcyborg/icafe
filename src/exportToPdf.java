/**
 * Created by itcyb on 1/20/2017.
 */

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.test.annotations.WrapToTest;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Set;

@WrapToTest
public class exportToPdf {
    String filename;
    String query;
    String date;
    String user;

    public exportToPdf(String file, String query, String user, int i) throws IOException {
        String filen = file.replace("-", "/");
        String[] temp = filen.split("_");
        String[] temp1 = temp[0].split(" ");
        if (i == 0) {
            this.filename = "reports/detailed_reports/" + temp1[0] + ".pdf";
        } else {
            this.filename = "reports/summarised_reports/" + temp1[0] + ".pdf";
        }
        this.date = file;
        this.query = query;
        this.user = user;
        File files = new File(filename);
        files.getParentFile().mkdirs();
    }

    public boolean createPdf() {
        boolean status = false;
        //Initialize PDF writer
        PdfWriter writer = null;
        try {
            writer = new PdfWriter(this.filename);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error exporting PDF.Close any open PDF documents and try again.");
            new logger(e.toString()).writelog();
        }
        //Initialize PDF document
        PdfDocument pdf = new PdfDocument(writer);
        // Initialize document
        Document document = new Document(pdf, new PageSize(PageSize.A4), true);
        document.setBottomMargin(100);
        document.setTopMargin(100);

        Paragraph title = new Paragraph("MAVELL CYBER CAFE").setBold().setUnderline().setFontSize(20);
        title.setRelativePosition(30, 10, 30, 10);
        document.add(title);
        document.add(new Paragraph("DATE:" + date).setBold().setItalic().setUnderline().setFontSize(12));
        //Add paragraph to the document
        document.add(new Paragraph("Report"));
        document.add(new Paragraph("------------------------------------------------------------------------------------------------------------"));
        Table table = new Table(6);
        ResultSet rs = new dbGetter(new dbConnector().getConnection(), query).getter();
        try {
            Cell c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("ID");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("User");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("StartTime");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("StopTime");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("Duration");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("Cost");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            double sum = 0;
            while (rs.next()) {
                Cell cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(new String(rs.getString("ID")));
                table.addCell(cell).setFontSize(8);
                cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(rs.getString("User"));
                table.addCell(cell).setFontSize(8);
                cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(rs.getString("StartTime"));
                table.addCell(cell).setFontSize(8);
                cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(rs.getString("StopTime"));
                table.addCell(cell).setFontSize(8);
                cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(rs.getString("Duration"));
                table.addCell(cell).setFontSize(8);
                cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(rs.getString("Cost"));
                table.addCell(cell).setFontSize(8);
                sum += Double.valueOf(rs.getString("Cost"));
            }
            document.add(table);
            document.add(new Paragraph("Total:" + sum).setFontSize(13).setBold().setUnderline());
            Paragraph footer = new Paragraph("Generated by:" + user);
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new TableFooterEventHandler(footer, document));
            //Close document
            document.close();
            status = true;
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }

    public boolean createPdf1() {
        boolean status = false;
        //Initialize PDF writer
        PdfWriter writer = null;
        try {
            writer = new PdfWriter(this.filename);
        } catch (FileNotFoundException e) {
            //JOptionPane.showMessageDialog(null,"Error exporting PDF.Close any open PDF documents and try again.");
            new logger(e.toString()).writelog();
        }
        //Initialize PDF document
        PdfDocument pdf = new PdfDocument(writer);
        // Initialize document
        Document document = new Document(pdf, new PageSize(PageSize.A4), true);
        document.setBottomMargin(100);

        Paragraph title = new Paragraph("MAVELL CYBER CAFE").setBold().setUnderline().setFontSize(20);
        title.setRelativePosition(30, 10, 30, 10);
        document.add(title);
        document.add(new Paragraph("DATE:" + date).setBold().setItalic().setUnderline().setFontSize(12));
        //Add paragraph to the document
        document.add(new Paragraph("Report"));
        document.add(new Paragraph("------------------------------------------------------------------------------------------------------------"));
        Table table = new Table(2);
        ResultSet rs = new dbGetter(new dbConnector().getConnection(), query).getter();
        try {
            Cell c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("User");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            c = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add("Amount");
            c.setBold();
            c.setFontSize(13);
            table.addCell(c);
            double sum = 0;
            Hashtable<String, Double> users = new Hashtable<>();
            while (rs.next()) {
                String dsuser = rs.getString("User");
                Double details = rs.getDouble("Cost");
                if (users.containsKey(dsuser)) {
                    Double examount = users.get(dsuser);
                    examount += details;
                    users.put(dsuser, examount);
                } else {
                    users.put(dsuser, details);
                }
                sum += details;
            }
            Set<String> keys = users.keySet();
            for (String user : keys) {
                Cell cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(user);
                table.addCell(cell);
                cell = new Cell(1, 1).setTextAlignment(TextAlignment.CENTER).add(String.valueOf(users.get(user)));
                table.addCell(cell);
            }
            document.add(table);
            document.add(new Paragraph("Total:" + sum).setFontSize(13).setBold().setUnderline());
            Paragraph footer = new Paragraph("Generated by:" + user);
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new TableFooterEventHandler(footer, document));
            //Close document
            document.close();
            status = true;
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }

    protected class TableFooterEventHandler implements IEventHandler {
        Document doc;
        private Paragraph table;

        public TableFooterEventHandler(Paragraph table, Document doc) {
            this.table = table;
            this.doc = doc;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNum = docEvent.getDocument().getPageNumber(page);
            int pageNums = docEvent.getDocument().getNumberOfPages();
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            new Canvas(canvas, pdfDoc, new Rectangle(36, 20, page.getPageSize().getWidth() - 72, 50))
                    .add(table)
                    .add(new Paragraph("Sign:_____________________________"))
                    .add(new Paragraph("Page " + pageNum + " of " + pageNums));

            Rectangle rect = new Rectangle(pdfDoc.getDefaultPageSize().getX() + doc.getLeftMargin(),
                    pdfDoc.getDefaultPageSize().getTop() - 15, 100, 0);
            Rectangle rect_right = new Rectangle(pdfDoc.getDefaultPageSize().getWidth() - doc.getLeftMargin() - 200,
                    pdfDoc.getDefaultPageSize().getTop() - 15, 250, 0);
            new Canvas(canvas, pdfDoc, rect)
                    .add(new Paragraph("Date:" + date));
            new Canvas(canvas, pdfDoc, rect_right)
                    .add(new Paragraph("Report Generated by:" + user));
        }
    }

}