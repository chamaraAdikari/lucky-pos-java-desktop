package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.model.product.Product;
import com.turboboostteam.pos.model.sale.ReceiptData;
import com.turboboostteam.pos.model.sale.SaleItem;
import com.turboboostteam.pos.model.sale.SaleTransaction;
import com.turboboostteam.pos.service.ProductService;
import com.turboboostteam.pos.service.SaleService;
import com.turboboostteam.pos.service.SessionManager;
import com.turboboostteam.pos.service.payment.PaymentResult;
import com.turboboostteam.pos.ui.components.CartTableModel;
import com.turboboostteam.pos.ui.components.NumpadPanel;
import net.miginfocom.swing.MigLayout;

import javax.money.MonetaryAmount;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SalesPanel extends JPanel {

    private final SaleService    saleService;
    private final ProductService productService;

    private SaleTransaction currentSale;
    private CartTableModel  cartModel;
    private JTable          cartTable;
    private JLabel          subtotalLabel;
    private JLabel          taxLabel;
    private JLabel          totalLabel;
    private JLabel          saleNumberLabel;
    private JLabel          statusLabel;
    private JTextField      barcodeField;

    public SalesPanel() {
        this.saleService    = AppContext.getBean(SaleService.class);
        this.productService = AppContext.getBean(ProductService.class);
        buildUI();
        startNewSale();
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "fill, insets 0, gap 0",
                "[grow][320!]",
                "[fill]"
        ));

        // ── Left side — product search + cart ──
        JPanel leftPanel = new JPanel(
                new MigLayout("fill, insets 12, wrap 1",
                        "[grow]"));

        // Header
        JLabel title = new JLabel("Sales");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        saleNumberLabel = new JLabel("");
        saleNumberLabel.setFont(
                new Font("Monospaced", Font.PLAIN, 12));

        JPanel headerRow = new JPanel(
                new MigLayout("insets 0", "[grow][]"));
        headerRow.add(title);
        headerRow.add(saleNumberLabel);
        leftPanel.add(headerRow, "growx");

        // Barcode search
        JPanel searchRow = new JPanel(
                new MigLayout("insets 0", "[grow][]"));
        barcodeField = new JTextField();
        barcodeField.putClientProperty(
                "JTextField.placeholderText",
                "Scan or type barcode...");
        barcodeField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        barcodeField.addActionListener(
                e -> handleBarcodeInput());

        JButton searchBtn = new JButton("🔍 Search");
        searchBtn.addActionListener(e -> handleBarcodeInput());

        searchRow.add(barcodeField, "growx");
        searchRow.add(searchBtn,    "gap 4");
        leftPanel.add(searchRow, "growx");

        // Product quick-select grid
        JPanel productGrid = buildProductGrid();
        JScrollPane gridScroll = new JScrollPane(productGrid);
        gridScroll.setPreferredSize(new Dimension(0, 200));
        gridScroll.setBorder(BorderFactory.createTitledBorder(
                "Quick Select"));
        leftPanel.add(gridScroll, "growx");

        // Cart table
        cartModel = new CartTableModel(List.of());
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(32);
        cartTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        cartTable.getColumnModel().getColumn(1).setMaxWidth(50);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder(
                "Cart"));
        leftPanel.add(cartScroll, "grow");

        // Cart action buttons
        JPanel cartBtns = new JPanel(
                new MigLayout("insets 0", "[][][grow][]"));
        JButton removeBtn = new JButton("🗑 Remove Item");
        JButton clearBtn  = new JButton("✖ Clear Cart");

        removeBtn.addActionListener(e -> handleRemoveItem());
        clearBtn.addActionListener(e -> handleClearCart());

        cartBtns.add(removeBtn);
        cartBtns.add(clearBtn, "gap 4");
        leftPanel.add(cartBtns, "growx");

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        leftPanel.add(statusLabel, "growx");

        // ── Right side — totals + numpad + payment ──
        JPanel rightPanel = new JPanel(
                new MigLayout("fill, insets 12, wrap 1",
                        "[grow]"));
        rightPanel.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, Color.GRAY));

        // Totals
        JPanel totalsPanel = new JPanel(
                new MigLayout("fillx, wrap 2",
                        "[grow][]"));
        totalsPanel.setBorder(BorderFactory.createTitledBorder(
                "Totals"));

        subtotalLabel = new JLabel("USD 0.00");
        taxLabel      = new JLabel("USD 0.00");
        totalLabel    = new JLabel("USD 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(subtotalLabel, "right");
        totalsPanel.add(new JLabel("Tax:"));
        totalsPanel.add(taxLabel,      "right");
        totalsPanel.add(new JSeparator(),
                "span 2, growx");
        totalsPanel.add(new JLabel("TOTAL:"));
        totalsPanel.add(totalLabel,    "right");
        rightPanel.add(totalsPanel, "growx");

        // Numpad for quantity
        NumpadPanel numpad = new NumpadPanel(
                "Set Quantity for Selected Item",
                this::handleQuantityChange);
        rightPanel.add(numpad, "growx");

        // Payment buttons
        JPanel paymentPanel = new JPanel(
                new MigLayout("fillx, wrap 1, gapy 6",
                        "[grow]"));
        paymentPanel.setBorder(BorderFactory.createTitledBorder(
                "Payment"));

        JButton cashBtn = new JButton("💵 Cash Payment");
        JButton cardBtn = new JButton("💳 Card Payment");
        JButton voidBtn = new JButton("❌ Void Sale");

        cashBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        voidBtn.setFont(new Font("SansSerif", Font.BOLD, 14));

        cashBtn.setBackground(new Color(34, 139, 34));
        cashBtn.setForeground(Color.WHITE);
        cardBtn.setBackground(new Color(30, 100, 180));
        cardBtn.setForeground(Color.WHITE);
        voidBtn.setBackground(new Color(180, 40, 40));
        voidBtn.setForeground(Color.WHITE);

        cashBtn.addActionListener(e -> handlePayment("CASH"));
        cardBtn.addActionListener(e -> handlePayment("CARD"));
        voidBtn.addActionListener(e -> handleVoidSale());

        paymentPanel.add(cashBtn, "growx, h 50!");
        paymentPanel.add(cardBtn, "growx, h 50!");
        paymentPanel.add(voidBtn, "growx, h 40!");
        rightPanel.add(paymentPanel, "growx");

        // Assemble
        add(leftPanel,  "grow");
        add(rightPanel, "growy");
    }

    // ── Product quick-select grid ──
    private JPanel buildProductGrid() {
        JPanel grid = new JPanel(new MigLayout(
                "wrap 4, insets 8, gapy 4, gapx 4",
                "[grow][grow][grow][grow]"));

        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            JButton btn = new JButton("<html><center>"
                    + p.getName()
                    + "<br><small>"
                    + p.getPrice()
                    + "</small></center></html>");
            btn.setFocusPainted(false);
            btn.addActionListener(e -> addProductToCart(p));
            grid.add(btn, "growx, h 60!");
        }
        return grid;
    }

    // ── Sale Management ──
    private void startNewSale() {
        Long cashierId = SessionManager.getInstance()
                .getCurrentUser().getId();
        currentSale = saleService.openSale(cashierId);
        saleNumberLabel.setText(currentSale.getSaleNumber());
        refreshCart();
        setStatus("New sale opened — scan or select a product");
    }

    private void addProductToCart(Product product) {
        SaleItem item = new SaleItem(
                product.getId(),
                product.getName(),
                1,
                product.getFinalPrice()
        );
        saleService.addItem(item);
        refreshCart();
        setStatus("Added: " + product.getName());
        barcodeField.requestFocus();
    }

    private void handleBarcodeInput() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) return;

        productService.findByBarcode(barcode).ifPresentOrElse(
                p -> {
                    addProductToCart(p);
                    barcodeField.setText("");
                },
                () -> {
                    setStatus("⚠️ Product not found: " + barcode);
                    barcodeField.selectAll();
                }
        );
    }

    private void handleRemoveItem() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            setStatus("Select an item to remove");
            return;
        }
        SaleItem item = cartModel.getItemAt(row);
        saleService.removeItem(item.getProductId());
        refreshCart();
        setStatus("Removed: " + item.getProductName());
    }

    private void handleClearCart() {
        if (currentSale.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all items from cart?",
                "Clear Cart",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentSale.getItems().forEach(item ->
                    saleService.removeItem(item.getProductId()));
            refreshCart();
            setStatus("Cart cleared");
        }
    }

    private void handleQuantityChange(String value) {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            setStatus("Select an item first, then enter quantity");
            return;
        }
        try {
            int qty = Integer.parseInt(value);
            SaleItem item = cartModel.getItemAt(row);
            saleService.updateQuantity(item.getProductId(), qty);
            refreshCart();
            setStatus("Quantity updated to " + qty);
        } catch (NumberFormatException e) {
            setStatus("Invalid quantity");
        }
    }

    private void handlePayment(String type) {
        if (currentSale.isEmpty()) {
            setStatus("⚠️ Cart is empty — add items first");
            return;
        }

        Frame parent = (Frame) SwingUtilities
                .getWindowAncestor(this);
        MonetaryAmount total = currentSale.getTotalAmount();
        PaymentResult paymentResult;

        if (type.equals("CASH")) {
            // Show cash payment dialog
            CashPaymentDialog dialog =
                    new CashPaymentDialog(parent, total);
            dialog.setVisible(true);
            paymentResult = dialog.getResult();

        } else {
            // Show card payment dialog
            CardPaymentDialog dialog =
                    new CardPaymentDialog(parent, total);
            dialog.setVisible(true);
            paymentResult = dialog.getResult();
        }

        // User cancelled
        if (paymentResult == null) return;

        if (paymentResult.isSuccess()) {
            try {
                com.turboboostteam.pos.model.enums.PaymentType
                        paymentType = com.turboboostteam.pos
                        .model.enums.PaymentType.valueOf(type);

                SaleTransaction completed =
                        saleService.commitSale(
                                paymentType, total);

                // Build receipt data
                ReceiptData receiptData = new ReceiptData(
                        completed.getSaleNumber(),
                        SessionManager.getInstance()
                                .getCurrentUser().getUsername(),
                        completed.getCompletedAt() != null
                                ? completed.getCompletedAt()
                                : java.time.LocalDateTime.now(),
                        completed.getItems(),
                        completed.getSubtotal(),
                        completed.getTaxAmount(),
                        completed.getDiscountAmount(),
                        completed.getTotalAmount(),
                        paymentResult.getChangeAmount() != null
                                ? total.add(paymentResult
                                            .getChangeAmount())
                                : total,
                        paymentResult.getChangeAmount(),
                        type,
                        paymentResult.getReference()
                );

                // Show receipt dialog
                ReceiptDialog receiptDialog =
                        new ReceiptDialog(parent, receiptData);
                receiptDialog.setVisible(true);

                startNewSale();

            } catch (Exception e) {
                setStatus("❌ Error: " + e.getMessage());
            }
        }
         else {
            setStatus("❌ Payment failed: "
                    + paymentResult.getMessage());
        }
    }

    private void handleVoidSale() {
        if (currentSale.isEmpty()) {
            setStatus("Nothing to void");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Void current sale?",
                "Void Sale",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            saleService.cancelSale();
            startNewSale();
            setStatus("Sale voided — new sale opened");
        }
    }

    // ── UI Helpers ──
    private void refreshCart() {
        List<SaleItem> items = currentSale != null
                ? currentSale.getItems()
                : List.of();
        cartModel.setItems(items);

        if (currentSale != null) {
            subtotalLabel.setText(
                    currentSale.getSubtotal().toString());
            taxLabel.setText(
                    currentSale.getTaxAmount().toString());
            totalLabel.setText(
                    currentSale.getTotalAmount().toString());
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}