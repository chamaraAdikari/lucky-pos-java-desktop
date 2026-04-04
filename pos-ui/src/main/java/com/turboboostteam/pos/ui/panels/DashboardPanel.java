package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.model.report.*;
import com.turboboostteam.pos.service.AnalyticsService;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final AnalyticsService analyticsService;

    private JPanel chartsPanel;
    private JComboBox<String> rangeCombo;
    private JLabel loadingLabel;

    public DashboardPanel() {
        this.analyticsService =
                AppContext.getBean(AnalyticsService.class);
        buildUI();
        loadCharts();
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "fill, insets 16, wrap 1", "[grow]"));

        // Header
        JPanel headerRow = new JPanel(
                new MigLayout("insets 0",
                        "[grow][][]"));

        JLabel title = new JLabel(
                "Sales Analytics Dashboard");
        title.setFont(
                new Font("SansSerif", Font.BOLD, 20));

        rangeCombo = new JComboBox<>(new String[]{
                "Last 7 Days",
                "Last 30 Days",
                "Today"
        });
        rangeCombo.addActionListener(
                e -> loadCharts());

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadCharts());

        headerRow.add(title);
        headerRow.add(rangeCombo, "gap 16");
        headerRow.add(refreshBtn, "gap 4");
        add(headerRow, "growx");

        // Loading indicator
        loadingLabel = new JLabel(
                "Loading charts...");
        loadingLabel.setVisible(false);
        add(loadingLabel);

        // Charts container
        chartsPanel = new JPanel(
                new MigLayout("fill, wrap 2",
                        "[grow][grow]",
                        "[grow][grow]"));
        add(chartsPanel, "grow");
    }

    private void loadCharts() {
        loadingLabel.setVisible(true);
        chartsPanel.removeAll();

        String range = (String) rangeCombo
                .getSelectedItem();

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        LocalDate to   = LocalDate.now();
                        LocalDate from = switch (range) {
                            case "Last 30 Days" ->
                                    to.minusDays(29);
                            case "Today" -> to;
                            default -> to.minusDays(6);
                        };

                        // 1. Revenue bar chart
                        Map<LocalDate, BigDecimal> revenue =
                                analyticsService
                                        .getDateRangeRevenue(
                                                from, to);
                        SwingUtilities.invokeLater(() ->
                                chartsPanel.add(
                                        buildRevenueChart(revenue),
                                        "grow"));

                        // 2. Top products pie chart
                        List<SaleReportItem> topProducts =
                                analyticsService.getTopProducts(
                                        from, to, 5);
                        SwingUtilities.invokeLater(() ->
                                chartsPanel.add(
                                        buildPieChart(topProducts),
                                        "grow"));

                        // 3. Hourly time series
                        List<HourlySale> hourly =
                                analyticsService.getTodayHourly();
                        SwingUtilities.invokeLater(() ->
                                chartsPanel.add(
                                        buildHourlyChart(hourly),
                                        "grow"));

                        // 4. Category revenue pie
                        Map<String, BigDecimal> catRevenue =
                                analyticsService
                                        .getCategoryRevenue(
                                                from, to);
                        SwingUtilities.invokeLater(() -> {
                            chartsPanel.add(
                                    buildCategoryChart(catRevenue),
                                    "grow");
                            chartsPanel.revalidate();
                            chartsPanel.repaint();
                            loadingLabel.setVisible(false);
                        });

                        return null;
                    }
                };
        worker.execute();
    }

    // ── Chart Builders ──

    private ChartPanel buildRevenueChart(
            Map<LocalDate, BigDecimal> data) {

        DefaultCategoryDataset dataset =
                new DefaultCategoryDataset();

        data.forEach((date, revenue) ->
                dataset.addValue(
                        revenue.doubleValue(),
                        "Revenue",
                        date.toString()));

        JFreeChart chart = ChartFactory
                .createBarChart(
                        "Daily Revenue",
                        "Date",
                        "USD",
                        dataset,
                        PlotOrientation.VERTICAL,
                        false, true, false);

        styleChart(chart);
        chart.getCategoryPlot()
                .getRenderer()
                .setSeriesPaint(0,
                        new Color(34, 139, 34));

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(400, 250));
        return panel;
    }

    private ChartPanel buildPieChart(
            List<SaleReportItem> items) {

        DefaultPieDataset<String> dataset =
                new DefaultPieDataset<>();

        if (items.isEmpty()) {
            dataset.setValue("No Data", 1);
        } else {
            items.forEach(item ->
                    dataset.setValue(
                            item.getProductName(),
                            item.getRevenue()
                                    .doubleValue()));
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Top Products by Revenue",
                dataset, true, true, false);

        styleChart(chart);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(400, 250));
        return panel;
    }

    private ChartPanel buildHourlyChart(
            List<HourlySale> hourly) {

        TimeSeries series = new TimeSeries(
                "Hourly Sales");

        if (hourly.isEmpty()) {
            // Show empty chart
            series.add(new Hour(0,
                    new Day()), 0);
        } else {
            hourly.forEach(h -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY,
                        h.getHour());
                series.addOrUpdate(
                        new Hour(h.getHour(),
                                new Day()),
                        h.getRevenue().doubleValue());
            });
        }

        TimeSeriesCollection dataset =
                new TimeSeriesCollection(series);

        JFreeChart chart = ChartFactory
                .createTimeSeriesChart(
                        "Today's Hourly Sales",
                        "Time",
                        "USD",
                        dataset,
                        false, true, false);

        styleChart(chart);
        chart.getXYPlot()
                .getRenderer()
                .setSeriesPaint(0,
                        new Color(30, 100, 180));

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(400, 250));
        return panel;
    }

    private ChartPanel buildCategoryChart(
            Map<String, BigDecimal> data) {

        DefaultPieDataset<String> dataset =
                new DefaultPieDataset<>();

        if (data.isEmpty()) {
            dataset.setValue("No Data", 1);
        } else {
            data.forEach((cat, rev) ->
                    dataset.setValue(cat,
                            rev.doubleValue()));
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Revenue by Category",
                dataset, true, true, false);

        styleChart(chart);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(400, 250));
        return panel;
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(
                UIManager.getColor("Panel.background"));
        chart.getPlot().setBackgroundPaint(
                UIManager.getColor("Panel.background"));
    }

    public void refresh() { loadCharts(); }
}