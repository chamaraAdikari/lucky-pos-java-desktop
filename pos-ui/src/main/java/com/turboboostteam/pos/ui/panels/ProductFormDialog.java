package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.model.product.*;
import com.turboboostteam.pos.service.ProductService;
import com.turboboostteam.pos.dao.CategoryDao;
import com.turboboostteam.pos.ui.util.BarcodeUtil;
import net.miginfocom.swing.MigLayout;
import org.javamoney.moneta.Money;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductFormDialog extends JDialog {

    private final ProductService productService;
    private final CategoryDao categoryDao;
    private final Product existingProduct; // null = add mode
    private final Runnable onSave;

    private JTextField nameField;
    private JTextField barcodeField;
    private JTextField priceField;
    private JComboBox<Category> categoryCombo;
    private JComboBox<String> typeCombo;
    private JLabel errorLabel;

    public ProductFormDialog(Frame parent, ProductService productService,
                             CategoryDao categoryDao, Product existing,
                             Runnable onSave) {
        super(parent, existing == null ? "Add Product" : "Edit Product", true);
        this.productService   = productService;
        this.categoryDao      = categoryDao;
        this.existingProduct  = existing;
        this.onSave           = onSave;
        buildUI();
        if (existing != null) populateFields(existing);
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new MigLayout("wrap 2, fillx, insets 20, gapy 8",
                "[120][grow]"));

        // Title
        JLabel title = new JLabel(existingProduct == null
                ? "Add New Product" : "Edit Product");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, "span 2, gapbottom 12");

        // Name
        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField, "growx");

        // Barcode
        add(new JLabel("Barcode:"));
        barcodeField = new JTextField();
        add(barcodeField, "growx");

        // Barcode row — field + generate button
        add(new JLabel("Barcode:"));
        JPanel barcodeRow = new JPanel(new MigLayout("insets 0", "[grow][]"));
        barcodeField = new JTextField();
        JButton generateBtn = new JButton("⚡ Generate");
        generateBtn.addActionListener(e ->
                barcodeField.setText(BarcodeUtil.generateRandomEAN13())
        );
        barcodeRow.add(barcodeField, "growx");
        barcodeRow.add(generateBtn);
        add(barcodeRow, "growx");

        // Price
        add(new JLabel("Price (USD):"));
        priceField = new JTextField();
        add(priceField, "growx");

        // Type
        add(new JLabel("Type:"));
        typeCombo = new JComboBox<>(new String[]{"SIMPLE", "BUNDLE"});
        add(typeCombo, "growx");

        // Category
        add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>();
        List<Category> categories = categoryDao.findAll();
        categories.forEach(categoryCombo::addItem);
        add(categoryCombo, "growx");

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        add(errorLabel, "span 2");

        // Buttons
        JButton saveBtn   = new JButton(existingProduct == null
                ? "Add Product" : "Save Changes");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        add(cancelBtn, "span 2, split 2, right");
        add(saveBtn);

        setPreferredSize(new Dimension(420, 380));
    }

    private void populateFields(Product p) {
        nameField.setText(p.getName());
        barcodeField.setText(p.getBarcode());
        priceField.setText(p.getPrice()
                .getNumber()
                .numberValue(BigDecimal.class)
                .toString());
        typeCombo.setSelectedItem(p.getProductType());

        // Select matching category
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            Category c = categoryCombo.getItemAt(i);
            if (p.getCategory() != null
                    && c.getId().equals(p.getCategory().getId())) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void handleSave() {
        // Validation
        String name    = nameField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("Name is required");
            return;
        }
        if (priceStr.isEmpty()) {
            errorLabel.setText("Price is required");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                errorLabel.setText("Price cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid price format");
            return;
        }

        Category selectedCategory =
                (Category) categoryCombo.getSelectedItem();
        String type = (String) typeCombo.getSelectedItem();

        // Build product
        Product product;
        if ("BUNDLE".equals(type)) {
            product = new BundleProduct(
                    existingProduct != null ? existingProduct.getId() : null,
                    name, barcode,
                    Money.of(price, "USD"),
                    selectedCategory, true,
                    List.of(), 0.0
            );
        } else {
            product = new SimpleProduct(
                    existingProduct != null ? existingProduct.getId() : null,
                    name, barcode,
                    Money.of(price, "USD"),
                    selectedCategory, true
            );
        }

        // Save or update
        if (existingProduct == null) {
            productService.addProduct(product);
        } else {
            productService.updateProduct(product);
        }

        onSave.run(); // refresh table
        dispose();
    }
}