package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.model.customer.Customer;
import com.turboboostteam.pos.model.customer.LoyaltyTier;
import com.turboboostteam.pos.service.CustomerService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private CustomerTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public CustomerPanel() {
        this.customerService =
                AppContext.getBean(CustomerService.class);
        buildUI();
        loadCustomers();
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "fill, insets 16, wrap 1", "[grow]"));

        // Title
        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title);

        // Toolbar
        JPanel toolbar = new JPanel(
                new MigLayout("insets 0",
                        "[grow][][][]"));

        searchField = new JTextField();
        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search by name, phone or email...");
        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        search();
                    }
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        search();
                    }
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        search();
                    }
                });

        JButton addBtn    = new JButton("➕ Add Customer");
        JButton editBtn   = new JButton("✏️ Edit");
        JButton refreshBtn = new JButton("🔄 Refresh");

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            Customer selected = getSelected();
            if (selected != null) openForm(selected);
            else JOptionPane.showMessageDialog(this,
                    "Please select a customer.");
        });
        refreshBtn.addActionListener(e -> loadCustomers());

        toolbar.add(searchField, "growx");
        toolbar.add(addBtn);
        toolbar.add(editBtn,    "gap 4");
        toolbar.add(refreshBtn, "gap 4");
        add(toolbar, "growx");

        // Table
        tableModel = new CustomerTableModel(List.of());
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Color rows by tier
        table.setDefaultRenderer(Object.class,
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int col) {
                        Component c = super
                                .getTableCellRendererComponent(
                                        t, value, isSelected,
                                        hasFocus, row, col);
                        if (!isSelected) {
                            Customer customer =
                                    tableModel.getCustomerAt(row);
                            switch (customer.getLoyaltyTier()) {
                                case GOLD ->
                                        c.setBackground(
                                                new Color(255, 248, 200));
                                case SILVER ->
                                        c.setBackground(
                                                new Color(235, 235, 235));
                                default ->
                                        c.setBackground(
                                                table.getBackground());
                            }
                            c.setForeground(table.getForeground());
                        }
                        return c;
                    }
                });

        add(new JScrollPane(table), "grow");

        // Tier legend
        JPanel legend = new JPanel(
                new MigLayout("insets 4",
                        "[][]20[][]20[][]"));
        legend.add(makeColorBox(
                new Color(255, 248, 200)));
        legend.add(new JLabel("Gold"));
        legend.add(makeColorBox(
                new Color(235, 235, 235)));
        legend.add(new JLabel("Silver"));
        add(legend);
    }

    private JPanel makeColorBox(Color color) {
        JPanel box = new JPanel();
        box.setBackground(color);
        box.setPreferredSize(new Dimension(16, 16));
        box.setBorder(
                BorderFactory.createLineBorder(Color.GRAY));
        return box;
    }

    private void loadCustomers() {
        tableModel.setCustomers(
                customerService.getAllCustomers());
    }

    private void search() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCustomers();
        } else {
            tableModel.setCustomers(
                    customerService.search(query));
        }
    }

    private Customer getSelected() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        return tableModel.getCustomerAt(row);
    }

    private void openForm(Customer existing) {
        Frame parent = (Frame) SwingUtilities
                .getWindowAncestor(this);
        new CustomerFormDialog(parent,
                customerService, existing,
                this::loadCustomers)
                .setVisible(true);
    }

    // ── Inner Table Model ──
    static class CustomerTableModel
            extends AbstractTableModel {

        private final String[] cols = {
                "Name", "Phone", "Email",
                "Tier", "Points", "Total Spent"
        };
        private List<Customer> customers;

        public CustomerTableModel(
                List<Customer> customers) {
            this.customers = customers;
        }

        public void setCustomers(
                List<Customer> customers) {
            this.customers = customers;
            fireTableDataChanged();
        }

        public Customer getCustomerAt(int row) {
            return customers.get(row);
        }

        @Override
        public int getRowCount() {
            return customers.size();
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
            Customer c = customers.get(row);
            return switch (col) {
                case 0 -> c.getFullName();
                case 1 -> c.getPhone();
                case 2 -> c.getEmail();
                case 3 -> tierBadge(c.getLoyaltyTier());
                case 4 -> c.getLoyaltyPoints() + " pts";
                case 5 -> "USD " + c.getTotalSpent();
                default -> "";
            };
        }

        private String tierBadge(LoyaltyTier tier) {
            return switch (tier) {
                case GOLD   -> "🥇 GOLD";
                case SILVER -> "🥈 SILVER";
                case BRONZE -> "🥉 BRONZE";
            };
        }
    }
}