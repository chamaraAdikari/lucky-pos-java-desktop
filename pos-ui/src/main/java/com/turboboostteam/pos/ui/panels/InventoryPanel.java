package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.model.inventory.StockLevel;
import com.turboboostteam.pos.service.InventoryService;
import com.turboboostteam.pos.service.SessionManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService;
    private StockTableModel tableModel;
    private JLabel alertLabel;

    public InventoryPanel() {
        this.inventoryService = AppContext.getBean(InventoryService.class);
        buildUI();
        loadStock();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill, insets 16, wrap 1", "[grow]"));

        // Header
        JLabel title = new JLabel("Inventory Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title);

        // Alert bar
        alertLabel = new JLabel(" ");
        alertLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        alertLabel.setForeground(new Color(200, 80, 0));
        add(alertLabel);

        // Toolbar
        JPanel toolbar = new JPanel(new MigLayout("insets 0", "[grow][]"));
        JButton adjustBtn  = new JButton("📦 Adjust Stock");
        JButton refreshBtn = new JButton("🔄 Refresh");

        adjustBtn.addActionListener(e -> handleAdjust());
        refreshBtn.addActionListener(e -> loadStock());

        toolbar.add(new JLabel(""), "growx");
        toolbar.add(adjustBtn);
        toolbar.add(refreshBtn, "gap 4");
        add(toolbar, "growx");

        // Table
        tableModel = new StockTableModel(List.of());
        JTable table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Color low stock rows red
        table.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value, boolean isSelected,
                            boolean hasFocus, int row, int col) {
                        Component c = super.getTableCellRendererComponent(
                                t, value, isSelected, hasFocus, row, col);
                        StockLevel stock = tableModel.getStockAt(row);
                        if (!isSelected) {
                            if (stock.isOutOfStock()) {
                                c.setBackground(new Color(255, 200, 200));
                                c.setForeground(Color.RED);
                            } else if (stock.isLowStock()) {
                                c.setBackground(new Color(255, 235, 180));
                                c.setForeground(new Color(180, 100, 0));
                            } else {
                                c.setBackground(table.getBackground());
                                c.setForeground(table.getForeground());
                            }
                        }
                        return c;
                    }
                });

        // Store table reference for adjust button
        table.setName("stockTable");
        add(new JScrollPane(table), "grow");

        // Legend
        JPanel legend = new JPanel(new MigLayout("insets 4", "[][]20[][]"));
        legend.add(makeColorBox(new Color(255, 200, 200)));
        legend.add(new JLabel("Out of stock"));
        legend.add(makeColorBox(new Color(255, 235, 180)));
        legend.add(new JLabel("Low stock"));
        add(legend);

        // Store table for later use
        putClientProperty("table", table);
    }

    private JPanel makeColorBox(Color color) {
        JPanel box = new JPanel();
        box.setBackground(color);
        box.setPreferredSize(new Dimension(16, 16));
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return box;
    }

    private void loadStock() {
        List<StockLevel> stock = inventoryService.getAllStock();
        tableModel.setStock(stock);

        // Show alert for low stock items
        List<StockLevel> lowStock = inventoryService.getLowStockItems();
        if (!lowStock.isEmpty()) {
            alertLabel.setText("⚠️  " + lowStock.size()
                    + " item(s) are low on stock!");
        } else {
            alertLabel.setText(" ");
        }
    }

    private void handleAdjust() {
        JTable table = (JTable) getClientProperty("table");
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to adjust stock.");
            return;
        }

        StockLevel stock = tableModel.getStockAt(row);

        String input = JOptionPane.showInputDialog(this,
                "Enter new stock quantity for: "
                        + stock.getProductName()
                        + "\nCurrent: " + stock.getQuantity(),
                "Adjust Stock",
                JOptionPane.PLAIN_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int newQty = Integer.parseInt(input.trim());
            if (newQty < 0) {
                JOptionPane.showMessageDialog(this,
                        "Quantity cannot be negative.");
                return;
            }

            String notes = JOptionPane.showInputDialog(this,
                    "Reason for adjustment:",
                    "Stock Adjustment Notes",
                    JOptionPane.PLAIN_MESSAGE);

            String username = SessionManager.getInstance()
                    .getCurrentUser().getUsername();

            inventoryService.adjustStock(
                    stock.getProductId(), newQty,
                    notes != null ? notes : "Manual adjustment",
                    username);

            loadStock();
            JOptionPane.showMessageDialog(this,
                    "Stock updated successfully!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number.");
        }
    }

    // Inner table model
    static class StockTableModel extends AbstractTableModel {

        private final String[] cols = {
                "Product", "In Stock", "Low Stock Threshold", "Status"
        };
        private List<StockLevel> stock;

        public StockTableModel(List<StockLevel> stock) {
            this.stock = stock;
        }

        public void setStock(List<StockLevel> stock) {
            this.stock = stock;
            fireTableDataChanged();
        }

        public StockLevel getStockAt(int row) {
            return stock.get(row);
        }

        @Override public int getRowCount()    { return stock.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            StockLevel s = stock.get(row);
            return switch (col) {
                case 0 -> s.getProductName();
                case 1 -> s.getQuantity();
                case 2 -> s.getLowStockThreshold();
                case 3 -> s.isOutOfStock() ? "❌ Out of Stock"
                        : s.isLowStock()   ? "⚠️ Low Stock"
                          :                    "✅ OK";
                default -> "";
            };
        }
    }
}