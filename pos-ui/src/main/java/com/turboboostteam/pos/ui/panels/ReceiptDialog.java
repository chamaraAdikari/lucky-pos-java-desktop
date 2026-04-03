package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.model.sale.ReceiptData;
import com.turboboostteam.pos.model.sale.SaleItem;
import com.turboboostteam.pos.ui.util.ReceiptGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class ReceiptDialog extends JDialog {

    private final ReceiptData receiptData;

    public ReceiptDialog(Frame parent,
                         ReceiptData receiptData) {
        super(parent, "Receipt", true);
        this.receiptData = receiptData;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "wrap 1, fill, insets 16",
                "[grow]"));

        // Title
        JLabel title = new JLabel("Sale Complete! ✅");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title);

        // Receipt preview panel
        JPanel preview = new JPanel(
                new MigLayout("wrap 1, fillx, insets 12",
                        "[grow]"));
        preview.setBorder(BorderFactory
                .createTitledBorder("Receipt Preview"));
        preview.setBackground(Color.WHITE);

        // Header
        addReceiptLine(preview,
                "LUCKY POS", true, 14, Color.BLACK);
        addReceiptLine(preview,
                "Sale #: " + receiptData.getSaleNumber(),
                false, 11, Color.GRAY);
        addReceiptLine(preview,
                "Date: " + receiptData.getDateTime()
                        .format(DateTimeFormatter
                                .ofPattern("dd/MM/yyyy HH:mm")),
                false, 11, Color.GRAY);
        addReceiptLine(preview,
                "Cashier: " + receiptData.getCashierName(),
                false, 11, Color.GRAY);

        // Separator
        preview.add(new JSeparator(), "growx");

        // Items
        for (SaleItem item : receiptData.getItems()) {
            JPanel itemRow = new JPanel(
                    new MigLayout("insets 0",
                            "[grow][]"));
            JLabel nameLabel = new JLabel(
                    item.getProductName()
                            + " x" + item.getQuantity());
            nameLabel.setFont(
                    new Font("SansSerif", Font.PLAIN, 11));
            JLabel priceLabel = new JLabel(
                    item.getLineTotal().toString());
            priceLabel.setFont(
                    new Font("SansSerif", Font.PLAIN, 11));
            itemRow.setBackground(Color.WHITE);
            itemRow.add(nameLabel);
            itemRow.add(priceLabel, "right");
            preview.add(itemRow, "growx");
        }

        preview.add(new JSeparator(), "growx");

        // Totals
        addTwoColLine(preview, "Subtotal:",
                receiptData.getSubtotal().toString());
        addTwoColLine(preview, "Tax:",
                receiptData.getTaxAmount().toString());

        preview.add(new JSeparator(), "growx");

        addTwoColLine(preview, "TOTAL:",
                receiptData.getTotalAmount().toString(),
                true, 14);

        preview.add(new JSeparator(), "growx");

        addTwoColLine(preview, "Payment:",
                receiptData.getPaymentType());

        if (receiptData.getChangeAmount() != null
                && receiptData.getChangeAmount()
                .isPositiveOrZero()) {
            addTwoColLine(preview, "Change:",
                    receiptData.getChangeAmount().toString());
        }

        addReceiptLine(preview,
                "*** Thank You! ***",
                true, 12, new Color(34, 139, 34));

        JScrollPane scroll = new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(320, 380));
        add(scroll, "grow");

        // Action buttons
        JPanel btnPanel = new JPanel(
                new MigLayout("insets 0",
                        "[][][grow][]"));

        JButton printBtn  = new JButton("🖨️ Print");
        JButton pdfBtn    = new JButton("📄 Save PDF");
        JButton skipBtn   = new JButton("Skip");

        printBtn.addActionListener(e -> handlePrint());
        pdfBtn.addActionListener(e -> handleSavePdf());
        skipBtn.addActionListener(e -> dispose());

        btnPanel.add(printBtn);
        btnPanel.add(pdfBtn, "gap 4");
        btnPanel.add(new JLabel(), "growx");
        btnPanel.add(skipBtn);
        add(btnPanel, "growx");

        setPreferredSize(new Dimension(360, 560));
    }

    private void addReceiptLine(JPanel panel, String text,
                                boolean bold, int size,
                                Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif",
                bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        panel.add(label, "growx");
    }

    private void addTwoColLine(JPanel panel,
                               String left, String right) {
        addTwoColLine(panel, left, right, false, 11);
    }

    private void addTwoColLine(JPanel panel, String left,
                               String right, boolean bold,
                               int size) {
        JPanel row = new JPanel(
                new MigLayout("insets 0", "[grow][]"));
        row.setBackground(Color.WHITE);
        JLabel l = new JLabel(left);
        JLabel r = new JLabel(right);
        int style = bold ? Font.BOLD : Font.PLAIN;
        l.setFont(new Font("SansSerif", style, size));
        r.setFont(new Font("SansSerif", style, size));
        row.add(l);
        row.add(r, "right");
        panel.add(row, "growx");
    }

    private void handlePrint() {
        SwingWorker<File, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected File doInBackground()
                            throws Exception {
                        return ReceiptGenerator
                                .generatePdf(receiptData);
                    }

                    @Override
                    protected void done() {
                        try {
                            File pdf = get();
                            ReceiptGenerator.print(pdf);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(
                                    ReceiptDialog.this,
                                    "Print error: "
                                            + e.getMessage());
                        }
                    }
                };
        worker.execute();
    }

    private void handleSavePdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(
                "receipt_"
                        + receiptData.getSaleNumber()
                        + ".pdf"));

        if (chooser.showSaveDialog(this)
                == JFileChooser.APPROVE_OPTION) {
            SwingWorker<Void, Void> worker =
                    new SwingWorker<>() {
                        @Override
                        protected Void doInBackground()
                                throws Exception {
                            File pdf = ReceiptGenerator
                                    .generatePdf(receiptData);
                            // Copy to chosen location
                            java.nio.file.Files.copy(
                                    pdf.toPath(),
                                    chooser.getSelectedFile()
                                            .toPath(),
                                    java.nio.file.StandardCopyOption
                                            .REPLACE_EXISTING);
                            return null;
                        }

                        @Override
                        protected void done() {
                            JOptionPane.showMessageDialog(
                                    ReceiptDialog.this,
                                    "PDF saved successfully!");
                        }
                    };
            worker.execute();
        }
    }
}