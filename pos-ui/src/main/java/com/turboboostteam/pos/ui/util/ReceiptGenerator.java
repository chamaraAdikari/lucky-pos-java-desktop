package com.turboboostteam.pos.ui.util;

import com.turboboostteam.pos.model.sale.ReceiptData;
import com.turboboostteam.pos.model.sale.SaleItem;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.swing.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReceiptGenerator {

    private static final float PAGE_WIDTH  = 226f; // 80mm paper
    private static final float PAGE_HEIGHT = 600f;
    private static final float MARGIN      = 10f;
    private static final float LINE_HEIGHT = 14f;

    /**
     * Generate PDF receipt and return file path
     */
    public static File generatePdf(ReceiptData data)
            throws IOException {

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(
                new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
        doc.addPage(page);

        PDPageContentStream cs =
                new PDPageContentStream(doc, page);

        PDType1Font fontBold = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontNormal = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA);
        PDType1Font fontMono = new PDType1Font(
                Standard14Fonts.FontName.COURIER);

        float y = PAGE_HEIGHT - 20;

        // ── Header ──
        y = drawCentered(cs, fontBold, 12,
                "LUCKY POS", y);
        y = drawCentered(cs, fontNormal, 8,
                "Thank you for shopping!", y - 2);
        y = drawLine(cs, y - 4);

        // ── Sale info ──
        y = drawText(cs, fontNormal, 8,
                "Sale #: " + data.getSaleNumber(), y - 4);
        y = drawText(cs, fontNormal, 8,
                "Date: " + data.getDateTime()
                        .format(DateTimeFormatter
                                .ofPattern("dd/MM/yyyy HH:mm")),
                y);
        y = drawText(cs, fontNormal, 8,
                "Cashier: " + data.getCashierName(), y);
        y = drawLine(cs, y - 4);

        // ── Items ──
        y = drawText(cs, fontBold, 8,
                padRight("ITEM", 20)
                        + padLeft("QTY", 4)
                        + padLeft("PRICE", 8)
                        + padLeft("TOTAL", 8), y - 4);
        y = drawLine(cs, y - 2);

        for (SaleItem item : data.getItems()) {
            String name = item.getProductName();
            if (name.length() > 18)
                name = name.substring(0, 18);

            double lineTotal = item.getLineTotal()
                    .getNumber().doubleValue();
            double unitPrice = item.getUnitPrice()
                    .getNumber().doubleValue();

            y = drawText(cs, fontMono, 7,
                    padRight(name, 18)
                            + padLeft(
                            String.valueOf(item.getQuantity()),
                            4)
                            + padLeft(
                            String.format("%.2f", unitPrice),
                            8)
                            + padLeft(
                            String.format("%.2f", lineTotal),
                            8),
                    y);
        }

        y = drawLine(cs, y - 4);

        // ── Totals ──
        y = drawTwoCol(cs, fontNormal, 8,
                "Subtotal:",
                data.getSubtotal().toString(), y - 4);
        y = drawTwoCol(cs, fontNormal, 8,
                "Tax:",
                data.getTaxAmount().toString(), y);

        if (data.getDiscountAmount().isPositive()) {
            y = drawTwoCol(cs, fontNormal, 8,
                    "Discount:",
                    "- " + data.getDiscountAmount()
                            .toString(), y);
        }

        y = drawLine(cs, y - 2);
        y = drawTwoCol(cs, fontBold, 11,
                "TOTAL:",
                data.getTotalAmount().toString(), y - 4);
        y = drawLine(cs, y - 4);

        // ── Payment ──
        y = drawTwoCol(cs, fontNormal, 8,
                "Payment:",
                data.getPaymentType(), y - 4);

        if (data.getAmountTendered() != null) {
            y = drawTwoCol(cs, fontNormal, 8,
                    "Tendered:",
                    data.getAmountTendered().toString(), y);
        }
        if (data.getChangeAmount() != null
                && data.getChangeAmount().isPositive()) {
            y = drawTwoCol(cs, fontNormal, 8,
                    "Change:",
                    data.getChangeAmount().toString(), y);
        }

        y = drawText(cs, fontNormal, 7,
                "Ref: " + data.getPaymentReference(), y);
        y = drawLine(cs, y - 4);

        // ── Footer ──
        y = drawCentered(cs, fontNormal, 8,
                "*** Thank You! ***", y - 8);
        drawCentered(cs, fontNormal, 7,
                "Please come again!", y);

        cs.close();

        // Save to temp file
        File file = File.createTempFile(
                "receipt_" + data.getSaleNumber(), ".pdf");
        doc.save(file);
        doc.close();

        return file;
    }

    // ── Drawing helpers ──

    private static float drawText(PDPageContentStream cs,
                                  PDType1Font font,
                                  float size,
                                  String text,
                                  float y)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private static float drawCentered(PDPageContentStream cs,
                                      PDType1Font font,
                                      float size,
                                      String text,
                                      float y)
            throws IOException {
        float textWidth = font.getStringWidth(text)
                / 1000 * size;
        float x = (PAGE_WIDTH - textWidth) / 2;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private static float drawTwoCol(PDPageContentStream cs,
                                    PDType1Font font,
                                    float size,
                                    String left,
                                    String right,
                                    float y)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(left);
        cs.endText();

        float rightWidth = font.getStringWidth(right)
                / 1000 * size;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(
                PAGE_WIDTH - MARGIN - rightWidth, y);
        cs.showText(right);
        cs.endText();

        return y - LINE_HEIGHT;
    }

    private static float drawLine(PDPageContentStream cs,
                                  float y)
            throws IOException {
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_WIDTH - MARGIN, y);
        cs.stroke();
        return y - 4;
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    /**
     * Print the PDF file
     */
    public static void print(File pdfFile) {
        try {
            PDDocument doc = org.apache.pdfbox.Loader
                    .loadPDF(pdfFile);

            PrinterJob job = PrinterJob.getPrinterJob();

            // PDFBox 3.x printing API
            org.apache.pdfbox.printing.PDFPrintable printable =
                    new org.apache.pdfbox.printing.PDFPrintable(
                            doc,
                            org.apache.pdfbox.printing
                                    .Scaling.SHRINK_TO_FIT);

            job.setPrintable(printable);

            if (job.printDialog()) {
                job.print();
            }
            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Print error: " + e.getMessage());
        }
    }
}