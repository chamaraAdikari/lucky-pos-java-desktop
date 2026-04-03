package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.service.payment.*;
import net.miginfocom.swing.MigLayout;

import javax.money.MonetaryAmount;
import javax.swing.*;
import java.awt.*;

public class CardPaymentDialog extends JDialog {

    private final MonetaryAmount totalAmount;
    private PaymentResult result = null;

    public CardPaymentDialog(Frame parent,
                             MonetaryAmount totalAmount) {
        super(parent, "Card Payment", true);
        this.totalAmount = totalAmount;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "wrap 1, fillx, insets 24, gapy 12",
                "[center]"));

        // Title
        JLabel title = new JLabel("Card Payment");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title);

        // Card icon
        JLabel cardIcon = new JLabel("💳");
        cardIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        add(cardIcon);

        // Total
        JLabel totalLabel = new JLabel(
                "Total: " + totalAmount);
        totalLabel.setFont(
                new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setForeground(new Color(34, 139, 34));
        add(totalLabel);

        // Instructions
        JLabel instructions = new JLabel(
                "<html><center>Please ask customer to<br>"
                        + "tap, insert or swipe card</center></html>");
        instructions.setFont(
                new Font("SansSerif", Font.PLAIN, 14));
        add(instructions, "gaptop 8");

        // Processing indicator
        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(false);
        progress.setPreferredSize(
                new Dimension(280, 12));
        add(progress, "growx, gaptop 8");

        JLabel statusLabel = new JLabel(
                "Waiting for card...");
        statusLabel.setFont(
                new Font("SansSerif", Font.ITALIC, 12));
        add(statusLabel);

        // Buttons
        JButton processBtn = new JButton(
                "✔ Process Card Payment");
        JButton cancelBtn  = new JButton("Cancel");

        processBtn.setBackground(new Color(30, 100, 180));
        processBtn.setForeground(Color.WHITE);
        processBtn.setFont(
                new Font("SansSerif", Font.BOLD, 13));
        processBtn.setPreferredSize(
                new Dimension(240, 44));

        processBtn.addActionListener(e -> {
            processBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            progress.setIndeterminate(true);
            statusLabel.setText("Processing...");

            // SwingWorker for async card processing
            SwingWorker<PaymentResult, Void> worker =
                    new SwingWorker<>() {
                        @Override
                        protected PaymentResult doInBackground() {
                            return new CardPayment()
                                    .process(totalAmount,
                                            totalAmount);
                        }

                        @Override
                        protected void done() {
                            try {
                                result = get();
                                progress.setIndeterminate(false);
                                if (result.isSuccess()) {
                                    progress.setValue(100);
                                    statusLabel.setText(
                                            "✅ Approved!");
                                    Timer timer = new Timer(
                                            800, ev -> dispose());
                                    timer.setRepeats(false);
                                    timer.start();
                                } else {
                                    progress.setValue(0);
                                    statusLabel.setText(
                                            "❌ " + result
                                                    .getMessage());
                                    processBtn.setEnabled(true);
                                    cancelBtn.setEnabled(true);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
            worker.execute();
        });

        cancelBtn.addActionListener(e -> dispose());

        add(processBtn, "gaptop 12, growx");
        add(cancelBtn,  "growx");

        setPreferredSize(new Dimension(360, 420));
    }

    public PaymentResult getResult() { return result; }
}