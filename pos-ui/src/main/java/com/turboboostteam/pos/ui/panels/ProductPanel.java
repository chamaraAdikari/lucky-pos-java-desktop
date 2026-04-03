package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.dao.CategoryDao;
import com.turboboostteam.pos.model.product.Product;
import com.turboboostteam.pos.service.ProductService;
import com.turboboostteam.pos.ui.components.ProductTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductService productService;
    private final CategoryDao categoryDao;
    private ProductTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public ProductPanel() {
        this.productService = AppContext.getBean(ProductService.class);
        this.categoryDao    = AppContext.getBean(CategoryDao.class);
        buildUI();
        loadProducts();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill, insets 16, wrap 1", "[grow]"));

        // Header
        JLabel title = new JLabel("Product Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title);

        // Search + Buttons bar
        JPanel toolbar = new JPanel(new MigLayout("insets 0",
                "[grow][][]"));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText",
                "Search by name or barcode...");
        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                });

        JButton addBtn    = new JButton("➕ Add Product");
        JButton editBtn   = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");
        JButton barcodeBtn = new JButton("🔲 Barcode");

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            Product selected = getSelectedProduct();
            if (selected != null) openForm(selected);
            else JOptionPane.showMessageDialog(this,
                    "Please select a product to edit.");
        });
        deleteBtn.addActionListener(e -> handleDelete());
        barcodeBtn.addActionListener(e -> {
            Product selected = getSelectedProduct();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a product to view barcode.");
                return;
            }
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            new BarcodeViewerDialog(parent, selected).setVisible(true);
        });

        toolbar.add(searchField, "growx");
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(barcodeBtn);

        add(toolbar, "growx");

        // Table
        tableModel = new ProductTableModel(List.of());
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.getColumnModel().getColumn(6).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, "grow");

        // Status bar
        JLabel statusLabel = new JLabel("Ready");
        add(statusLabel, "growx");
    }

    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        tableModel.setProducts(products);
    }

    private void filter() {
        String text = searchField.getText().trim().toLowerCase();
        TableRowSorter<ProductTableModel> sorter =
                new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + text, 1, 2)); // search name + barcode
        }
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        int modelRow = table.convertRowIndexToModel(row);
        return tableModel.getProductAt(modelRow);
    }

    private void openForm(Product existing) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        ProductFormDialog dialog = new ProductFormDialog(
                parent, productService, categoryDao,
                existing, this::loadProducts);
        dialog.setVisible(true);
    }

    private void handleDelete() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate product: " + selected.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            productService.deactivateProduct(selected.getId());
            loadProducts();
        }
    }
}