package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.model.report.*;
import com.turboboostteam.pos.service.ReportService;
import com.turboboostteam.pos.ui.util.ReportGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final ReportService reportService;
    private JLabel dateLabel;
    private JPanel summaryPanel;
    private JTextArea topProductsArea;
    private JTextArea hourlyArea;
    private LocalDate selectedDate = LocalDate.now();

    public ReportsPanel() {
        this.reportService =
                AppContext.getBean(ReportService.class);
        buildUI();
        loadReport();
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "fill, insets 16, wrap 1", "[grow]"));

        // Title + Date selector
        JPanel headerRow = new JPanel(
                new MigLayout("insets 0",
                        "[grow][][][]"));

        JLabel title = new JLabel("Sales Reports");
        title.setFont(
                new Font("SansSerif", Font.BOLD, 20));

        dateLabel = new JLabel(
                selectedDate.format(
                        DateTimeFormatter
                                .ofPattern("dd MMM yyyy")));
        dateLabel.setFont(
                new Font("SansSerif", Font.BOLD, 14));

        JButton prevBtn = new JButton("◀");
        JButton nextBtn = new JButton("▶");
        JButton todayBtn = new JButton("Today");
        JButton pdfBtn  = new JButton("📄 Export PDF");
        pdfBtn.setBackground(new Color(30, 100, 180));
        pdfBtn.setForeground(Color.WHITE);

        prevBtn.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            updateDateLabel();
            loadReport();
        });
        nextBtn.addActionListener(e -> {
            if (!selectedDate.equals(LocalDate.now())) {
                selectedDate = selectedDate.plusDays(1);
                updateDateLabel();
                loadReport();
            }
        });
        todayBtn.addActionListener(e -> {
            selectedDate = LocalDate.now();
            updateDateLabel();
            loadReport();
        });
        pdfBtn.addActionListener(e -> exportPdf());

        headerRow.add(title);
        headerRow.add(prevBtn);
        headerRow.add(dateLabel, "gap 8");
        headerRow.add(nextBtn,   "gap 4");
        headerRow.add(todayBtn,  "gap 8");
        headerRow.add(pdfBtn,    "gap 16");
        add(headerRow, "growx");

        // Summary cards
        summaryPanel = new JPanel(
                new MigLayout("insets 0",
                        "[grow][grow][grow][grow]"));
        add(summaryPanel, "growx");

        // Split — top products + hourly
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(400);

        // Top products
        JPanel productsPanel = new JPanel(
                new MigLayout("fill, wrap 1",
                        "[grow]"));
        productsPanel.add(new JLabel(
                "Top Products Today") {{
            setFont(new Font("SansSerif",
                    Font.BOLD, 14));
        }});
        topProductsArea = new JTextArea();
        topProductsArea.setEditable(false);
        topProductsArea.setFont(
                new Font("Monospaced", Font.PLAIN, 12));
        productsPanel.add(
                new JScrollPane(topProductsArea), "grow");
        split.setLeftComponent(productsPanel);

        // Hourly breakdown
        JPanel hourlyPanel = new JPanel(
                new MigLayout("fill, wrap 1",
                        "[grow]"));
        hourlyPanel.add(new JLabel(
                "Hourly Breakdown") {{
            setFont(new Font("SansSerif",
                    Font.BOLD, 14));
        }});
        hourlyArea = new JTextArea();
        hourlyArea.setEditable(false);
        hourlyArea.setFont(
                new Font("Monospaced", Font.PLAIN, 12));
        hourlyPanel.add(
                new JScrollPane(hourlyArea), "grow");
        split.setRightComponent(hourlyPanel);

        add(split, "grow");
    }

    private void loadReport() {
        SwingWorker<DailySalesReport, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected DailySalesReport doInBackground() {
                        return reportService
                                .getDailyReport(selectedDate);
                    }

                    @Override
                    protected void done() {
                        try {
                            DailySalesReport report = get();
                            updateUI(report);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
        worker.execute();
    }

    private void updateUI(DailySalesReport report) {
        // Update summary cards
        summaryPanel.removeAll();
        summaryPanel.add(makeSummaryCard(
                "Transactions",
                String.valueOf(
                        report.getTotalTransactions()),
                new Color(30, 100, 180)));
        summaryPanel.add(makeSummaryCard(
                "Revenue",
                "USD " + report.getTotalRevenue()
                        .setScale(2,
                                java.math.RoundingMode
                                        .HALF_UP),
                new Color(34, 139, 34)));
        summaryPanel.add(makeSummaryCard(
                "Tax Collected",
                "USD " + report.getTotalTax()
                        .setScale(2,
                                java.math.RoundingMode
                                        .HALF_UP),
                new Color(180, 100, 0)));
        summaryPanel.add(makeSummaryCard(
                "Discounts",
                "USD " + report.getTotalDiscount()
                        .setScale(2,
                                java.math.RoundingMode
                                        .HALF_UP),
                new Color(140, 40, 140)));
        summaryPanel.revalidate();
        summaryPanel.repaint();

        // Top products text
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "%-28s %6s %12s%n",
                "Product", "Qty", "Revenue"));
        sb.append("─".repeat(48)).append("\n");
        for (SaleReportItem item
                : report.getTopProducts()) {
            sb.append(String.format(
                    "%-28s %6d %12s%n",
                    item.getProductName(),
                    item.getQuantitySold(),
                    "USD " + item.getRevenue()
                            .setScale(2,
                                    java.math.RoundingMode
                                            .HALF_UP)));
        }
        if (report.getTopProducts().isEmpty()) {
            sb.append("No sales recorded today");
        }
        topProductsArea.setText(sb.toString());

        // Hourly text
        StringBuilder hb = new StringBuilder();
        hb.append(String.format(
                "%-8s %12s %12s%n",
                "Hour", "Transactions", "Revenue"));
        hb.append("─".repeat(34)).append("\n");
        for (HourlySale hour : report.getHourlySales()) {
            hb.append(String.format(
                    "%-8s %12d %12s%n",
                    hour.getHourLabel(),
                    hour.getTransactions(),
                    "USD " + hour.getRevenue()
                            .setScale(2,
                                    java.math.RoundingMode
                                            .HALF_UP)));
        }
        if (report.getHourlySales().isEmpty()) {
            hb.append("No hourly data today");
        }
        hourlyArea.setText(hb.toString());
    }

    private JPanel makeSummaryCard(String label,
                                   String value,
                                   Color accent) {
        JPanel card = new JPanel(
                new MigLayout("wrap 1, insets 12",
                        "[center]"));
        card.setBorder(BorderFactory
                .createMatteBorder(
                        3, 0, 0, 0, accent));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(
                new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(accent);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 11));
        nameLabel.setForeground(Color.GRAY);

        card.add(valueLabel);
        card.add(nameLabel);
        return card;
    }

    private void updateDateLabel() {
        dateLabel.setText(selectedDate.format(
                DateTimeFormatter
                        .ofPattern("dd MMM yyyy")));
    }

    private void exportPdf() {
        SwingWorker<File, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected File doInBackground()
                            throws Exception {
                        DailySalesReport report =
                                reportService.getDailyReport(
                                        selectedDate);
                        return ReportGenerator
                                .generateDailyReport(report);
                    }

                    @Override
                    protected void done() {
                        try {
                            File pdf = get();
                            JFileChooser chooser =
                                    new JFileChooser();
                            chooser.setSelectedFile(new File(
                                    "daily_report_"
                                            + selectedDate + ".pdf"));
                            if (chooser.showSaveDialog(
                                    ReportsPanel.this)
                                    == JFileChooser
                                    .APPROVE_OPTION) {
                                java.nio.file.Files.copy(
                                        pdf.toPath(),
                                        chooser.getSelectedFile()
                                                .toPath(),
                                        java.nio.file.StandardCopyOption
                                                .REPLACE_EXISTING);
                                JOptionPane.showMessageDialog(
                                        ReportsPanel.this,
                                        "Report exported!");
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(
                                    ReportsPanel.this,
                                    "Export failed: "
                                            + e.getMessage());
                        }
                    }
                };
        worker.execute();
    }

    public void refresh() { loadReport(); }
}