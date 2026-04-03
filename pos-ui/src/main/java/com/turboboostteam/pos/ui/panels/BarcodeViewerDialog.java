package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.model.product.Product;
import com.turboboostteam.pos.ui.util.BarcodeUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;

public class BarcodeViewerDialog extends JDialog {

    public BarcodeViewerDialog(Frame parent, Product product) {
        super(parent, "Barcode — " + product.getName(), true);
        buildUI(product);
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI(Product product) {
        setLayout(new MigLayout("wrap 1, fillx, insets 20", "[center]"));

        // Product name
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(nameLabel);

        // Barcode number
        JLabel barcodeText = new JLabel(product.getBarcode());
        barcodeText.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(barcodeText);

        // Generate barcode image
        BufferedImage barcodeImage = BarcodeUtil.generateEAN13(
                product.getBarcode(), 300, 100);

        if (barcodeImage != null) {
            JLabel imageLabel = new JLabel(
                    new ImageIcon(barcodeImage));
            add(imageLabel, "gaptop 10");
        } else {
            add(new JLabel("Could not generate barcode image"));
        }

        // Price
        JLabel priceLabel = new JLabel(
                "Price: " + product.getPrice().toString());
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(priceLabel, "gaptop 8");

        // Close + Print buttons
        JPanel btnPanel = new JPanel(new MigLayout("insets 0"));
        JButton printBtn = new JButton("🖨️ Print Label");
        JButton closeBtn = new JButton("Close");

        printBtn.addActionListener(e -> printBarcode(barcodeImage, product));
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(printBtn);
        btnPanel.add(closeBtn, "gap 8");
        add(btnPanel, "gaptop 12");

        setPreferredSize(new Dimension(380, 300));
    }

    private void printBarcode(BufferedImage image, Product product) {
        if (image == null) return;
        // Java Print Service
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            graphics.drawImage(image, 50, 50, 300, 100, null);
            return java.awt.print.Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}