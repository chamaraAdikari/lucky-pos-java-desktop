package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.model.sale.SaleItem;
import com.turboboostteam.pos.model.sale.SaleTransaction;
import com.turboboostteam.pos.service.UserService;
import com.turboboostteam.pos.service.VoidRefundService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidRefundPanel extends JPanel {

    private final VoidRefundService voidRefundService;
    private final UserService userService;

    private SaleTableModel saleTableModel;
    private ItemTableModel itemTableModel;
    private JTable saleTable;
    private JTable itemTable;
    private JLabel statusLabel;

    public VoidRefundPanel() {
        this.voidRefundService =
                AppContext.getBean(VoidRefundService.class);
        this.userService =
                AppContext.getBean(UserService.class);
        buildUI();
        loadRecentSales();
    }
    public void refresh() {
        loadRecentSales();
    }
    private void buildUI() {
        setLayout(new MigLayout(
                "fill, insets 16, wrap 1", "[grow]"));

        JLabel title = new JLabel("Void & Refund");
        // Search bar
        JPanel searchRow = new JPanel(
                new MigLayout("insets 0", "[grow][]"));
        JTextField searchField = new JTextField();
        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search by sale number...");
        JButton searchBtn = new JButton("🔍 Search");

        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                loadRecentSales();
                return;
            }
            // Filter by sale number
            List<SaleTransaction> all =
                    voidRefundService.getRecentSales(100);
            List<SaleTransaction> filtered = all.stream()
                    .filter(s -> s.getSaleNumber()
                            .contains(query.toUpperCase()))
                    .toList();
            saleTableModel.setSales(filtered);
        });

        searchField.addActionListener(
                e -> searchBtn.doClick());

        searchRow.add(searchField, "growx");
        searchRow.add(searchBtn,   "gap 4");
        add(searchRow, "growx");
        title.setFont(
                new Font("SansSerif", Font.BOLD, 20));
        add(title);

        // Split pane
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(500);

        // ── Left — Recent sales ──
        JPanel leftPanel = new JPanel(
                new MigLayout("fill, wrap 1", "[grow]"));

        JLabel salesTitle = new JLabel(
                "Recent Completed Sales");
        salesTitle.setFont(
                new Font("SansSerif", Font.BOLD, 14));
        leftPanel.add(salesTitle);

        saleTableModel = new SaleTableModel(List.of());
        saleTable = new JTable(saleTableModel);
        saleTable.setRowHeight(28);
        saleTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        saleTable.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        loadSaleItems();
                    }
                });

        leftPanel.add(new JScrollPane(saleTable), "grow");

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(
                e -> loadRecentSales());
        leftPanel.add(refreshBtn, "right");
        split.setLeftComponent(leftPanel);

        // ── Right — Sale items ──
        JPanel rightPanel = new JPanel(
                new MigLayout("fill, wrap 1", "[grow]"));

        JLabel itemsTitle = new JLabel("Sale Items");
        itemsTitle.setFont(
                new Font("SansSerif", Font.BOLD, 14));
        rightPanel.add(itemsTitle);

        itemTableModel = new ItemTableModel(List.of());
        itemTable = new JTable(itemTableModel);
        itemTable.setRowHeight(28);
        rightPanel.add(
                new JScrollPane(itemTable), "grow");

        // Action buttons
        JPanel btnPanel = new JPanel(
                new MigLayout("insets 0", "[][]"));

        JButton voidBtn   = new JButton(
                "❌ Void Sale");
        JButton refundBtn = new JButton(
                "↩️ Refund Selected");

        voidBtn.setBackground(
                new Color(180, 40, 40));
        voidBtn.setForeground(Color.WHITE);
        refundBtn.setBackground(
                new Color(30, 100, 180));
        refundBtn.setForeground(Color.WHITE);

        voidBtn.addActionListener(
                e -> handleVoid());
        refundBtn.addActionListener(
                e -> handleRefund());

        btnPanel.add(voidBtn);
        btnPanel.add(refundBtn, "gap 8");
        rightPanel.add(btnPanel, "right");

        split.setRightComponent(rightPanel);
        add(split, "grow");

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(
                new Font("SansSerif", Font.ITALIC, 12));
        add(statusLabel, "growx");
    }

    private void loadRecentSales() {
        List<SaleTransaction> sales =
                voidRefundService.getRecentSales(20);
        saleTableModel.setSales(sales);
        itemTableModel.setItems(List.of());
    }

    private void loadSaleItems() {
        SaleTransaction selected = getSelectedSale();
        if (selected == null) return;
        List<SaleItem> items =
                voidRefundService.getSaleItems(
                        selected.getId());
        itemTableModel.setItems(items);
    }

    private SaleTransaction getSelectedSale() {
        int row = saleTable.getSelectedRow();
        if (row == -1) return null;
        return saleTableModel.getSaleAt(row);
    }

    private void handleVoid() {
        SaleTransaction sale = getSelectedSale();
        if (sale == null) {
            setStatus("Select a sale to void");
            return;
        }

        // Manager PIN confirmation
        Frame parent = (Frame) SwingUtilities
                .getWindowAncestor(this);
        ManagerPinDialog pinDialog =
                new ManagerPinDialog(parent,
                        userService,
                        "Void Sale #"
                                + sale.getSaleNumber());
        pinDialog.setVisible(true);

        if (!pinDialog.isConfirmed()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Void sale #"
                        + sale.getSaleNumber() + "?\n"
                        + "Stock will be restored.",
                "Confirm Void",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        voidRefundService.voidSale(
                sale.getId(), sale.getSaleNumber());
        setStatus("✅ Sale #"
                + sale.getSaleNumber() + " voided");
        loadRecentSales();
    }

    private void handleRefund() {
        SaleTransaction sale = getSelectedSale();
        if (sale == null) {
            setStatus("Select a sale to refund");
            return;
        }

        List<SaleItem> items =
                voidRefundService.getSaleItems(
                        sale.getId());
        if (items.isEmpty()) {
            setStatus("No items found for this sale");
            return;
        }

        // Manager PIN confirmation
        Frame parent = (Frame) SwingUtilities
                .getWindowAncestor(this);
        ManagerPinDialog pinDialog =
                new ManagerPinDialog(parent,
                        userService,
                        "Refund Sale #"
                                + sale.getSaleNumber());
        pinDialog.setVisible(true);

        if (!pinDialog.isConfirmed()) return;

        // Build refund map — all items full quantity
        Map<Long, Integer> refundItems = new HashMap<>();
        items.forEach(item ->
                refundItems.put(
                        item.getProductId(),
                        item.getQuantity()));

        voidRefundService.refundSale(
                sale.getId(),
                sale.getSaleNumber(),
                refundItems);

        setStatus("✅ Sale #"
                + sale.getSaleNumber() + " refunded");
        loadRecentSales();
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    // ── Table Models ──

    static class SaleTableModel
            extends AbstractTableModel {

        private final String[] cols = {
                "Sale #", "Date", "Total", "Status"
        };
        private List<SaleTransaction> sales;

        public SaleTableModel(
                List<SaleTransaction> sales) {
            this.sales = sales;
        }

        public void setSales(
                List<SaleTransaction> sales) {
            this.sales = sales;
            fireTableDataChanged();
        }

        public SaleTransaction getSaleAt(int row) {
            return sales.get(row);
        }

        @Override
        public int getRowCount() {
            return sales.size();
        }
        @Override
        public int getColumnCount() {
            return cols.length;
        }
        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int row, int col) {
            SaleTransaction s = sales.get(row);
            return switch (col) {
                case 0 -> s.getSaleNumber();
                case 1 -> s.getCreatedAt() != null
                        ? s.getCreatedAt().toLocalDate()
                        : "—";
                case 2 -> s.getTotalAmount();
                case 3 -> s.getStatus().name();
                default -> "";
            };
        }
    }

    static class ItemTableModel
            extends AbstractTableModel {

        private final String[] cols = {
                "Product", "Qty", "Unit Price", "Total"
        };
        private List<SaleItem> items;

        public ItemTableModel(List<SaleItem> items) {
            this.items = items;
        }

        public void setItems(List<SaleItem> items) {
            this.items = items;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return items.size();
        }
        @Override
        public int getColumnCount() {
            return cols.length;
        }
        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int row, int col) {
            SaleItem i = items.get(row);
            return switch (col) {
                case 0 -> i.getProductName();
                case 1 -> i.getQuantity();
                case 2 -> i.getUnitPrice();
                case 3 -> i.getLineTotal();
                default -> "";
            };
        }
    }
}