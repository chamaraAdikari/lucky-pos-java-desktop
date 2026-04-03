package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.service.payment.*;
import net.miginfocom.swing.MigLayout;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class CashPaymentDialog extends JDialog {

    private final MonetaryAmount totalAmount;
    private PaymentResult result = null;

    private JTextField tenderedField;
    private JLabel changeLabel;
    private JLabel errorLabel;
    private JButton confirmBtn;

    public CashPaymentDialog(Frame parent,
                             MonetaryAmount totalAmount) {
        super(parent, "Cash Payment", true);
        this.totalAmount = totalAmount;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "wrap 2, fillx, insets 24, gapy 10",
                "[140][grow]"));

        // Title
        JLabel title = new JLabel("Cash Payment");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, "span 2, gapbottom 8");

        // Total
        JLabel totalLabel = new JLabel("Total Amount:");
        totalLabel.setFont(
                new Font("SansSerif", Font.BOLD, 14));
        JLabel totalValue = new JLabel(
                totalAmount.toString());
        totalValue.setFont(
                new Font("SansSerif", Font.BOLD, 18));
        totalValue.setForeground(new Color(34, 139, 34));
        add(totalLabel);
        add(totalValue);

        // Quick cash buttons
        add(new JLabel("Quick Cash:"));
        JPanel quickBtns = new JPanel(
                new MigLayout("insets 0", "[][][][]"));

        // Generate quick cash options
        double total = totalAmount.getNumber().doubleValue();
        double[] quickAmounts = getQuickAmounts(total);

        for (double amt : quickAmounts) {
            JButton btn = new JButton(
                    String.format("%.2f", amt));
            btn.addActionListener(e ->
                    tenderedField.setText(
                            String.format("%.2f", amt)));
            quickBtns.add(btn);
        }
        add(quickBtns, "growx");

        // Tendered amount
        add(new JLabel("Cash Tendered:"));
        tenderedField = new JTextField();
        tenderedField.setFont(
                new Font("Monospaced", Font.BOLD, 16));
        tenderedField.getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            public void insertUpdate(
                                    javax.swing.event.DocumentEvent e) {
                                calculateChange();
                            }
                            public void removeUpdate(
                                    javax.swing.event.DocumentEvent e) {
                                calculateChange();
                            }
                            public void changedUpdate(
                                    javax.swing.event.DocumentEvent e) {
                                calculateChange();
                            }
                        });
        add(tenderedField, "growx");

        // Change
        add(new JLabel("Change:"));
        changeLabel = new JLabel("—");
        changeLabel.setFont(
                new Font("SansSerif", Font.BOLD, 18));
        add(changeLabel);

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        add(errorLabel, "span 2");

        // Buttons
        JButton cancelBtn = new JButton("Cancel");
        confirmBtn = new JButton("✔ Confirm Payment");
        confirmBtn.setBackground(new Color(34, 139, 34));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(
                new Font("SansSerif", Font.BOLD, 13));

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> handleConfirm());

        add(cancelBtn,   "span 2, split 2, right");
        add(confirmBtn,  "gap 8");

        setPreferredSize(new Dimension(420, 380));
    }

    private double[] getQuickAmounts(double total) {
        // Smart quick amounts above total
        double[] options = {
                Math.ceil(total),
                Math.ceil(total / 5) * 5,
                Math.ceil(total / 10) * 10,
                Math.ceil(total / 50) * 50
        };
        // Remove duplicates
        return java.util.Arrays.stream(options)
                .distinct()
                .filter(a -> a >= total)
                .limit(4)
                .toArray();
    }

    private void calculateChange() {
        String text = tenderedField.getText().trim();
        if (text.isEmpty()) {
            changeLabel.setText("—");
            confirmBtn.setEnabled(false);
            return;
        }
        try {
            BigDecimal tendered = new BigDecimal(text);
            MonetaryAmount tenderedAmt =
                    Money.of(tendered, "USD");

            if (tenderedAmt.isLessThan(totalAmount)) {
                changeLabel.setText("Insufficient!");
                changeLabel.setForeground(Color.RED);
                confirmBtn.setEnabled(false);
            } else {
                MonetaryAmount change =
                        tenderedAmt.subtract(totalAmount);
                changeLabel.setText(change.toString());
                changeLabel.setForeground(
                        new Color(34, 139, 34));
                confirmBtn.setEnabled(true);
                errorLabel.setText(" ");
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("Invalid amount");
            changeLabel.setForeground(Color.RED);
            confirmBtn.setEnabled(false);
        }
    }

    private void handleConfirm() {
        try {
            BigDecimal tendered = new BigDecimal(
                    tenderedField.getText().trim());
            MonetaryAmount tenderedAmt =
                    Money.of(tendered, "USD");

            CashPayment cashPayment = new CashPayment();
            result = cashPayment.process(
                    totalAmount, tenderedAmt);

            if (result.isSuccess()) {
                dispose();
            } else {
                errorLabel.setText(result.getMessage());
            }
        } catch (Exception e) {
            errorLabel.setText(
                    "Invalid amount: " + e.getMessage());
        }
    }

    public PaymentResult getResult() { return result; }
}