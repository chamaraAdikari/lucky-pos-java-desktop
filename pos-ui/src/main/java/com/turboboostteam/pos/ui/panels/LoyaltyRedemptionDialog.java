package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.model.customer.Customer;
import com.turboboostteam.pos.service.LoyaltyService;
import net.miginfocom.swing.MigLayout;

import javax.money.MonetaryAmount;
import javax.swing.*;
import java.awt.*;

public class LoyaltyRedemptionDialog extends JDialog {

    private final Customer customer;
    private final LoyaltyService loyaltyService;
    private final MonetaryAmount cartTotal;
    private LoyaltyService.RedemptionResult result = null;

    private JSpinner pointsSpinner;
    private JLabel valueLabel;
    private JLabel maxLabel;
    private JLabel errorLabel;

    public LoyaltyRedemptionDialog(Frame parent,
                                   Customer customer,
                                   LoyaltyService service,
                                   MonetaryAmount cartTotal) {
        super(parent, "Redeem Loyalty Points", true);
        this.customer       = customer;
        this.loyaltyService = service;
        this.cartTotal      = cartTotal;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "wrap 2, fillx, insets 20, gapy 10",
                "[140][grow]"));

        // Title
        JLabel title = new JLabel("Redeem Points");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, "span 2");

        // Customer info
        add(new JLabel("Customer:"));
        add(new JLabel(customer.getFullName()));

        add(new JLabel("Tier:"));
        add(new JLabel(customer.getLoyaltyTier().name()
                + " (" + customer.getLoyaltyTier()
                .getMultiplier()
                + "x multiplier)"));

        add(new JLabel("Available Points:"));
        JLabel availableLabel = new JLabel(
                customer.getLoyaltyPoints() + " pts");
        availableLabel.setFont(
                new Font("SansSerif", Font.BOLD, 14));
        availableLabel.setForeground(
                new Color(34, 139, 34));
        add(availableLabel);

        // Max redeemable
        int maxRedeemable =
                loyaltyService.calculateMaxRedeemablePoints(
                        customer, cartTotal);
        add(new JLabel("Max Redeemable:"));
        maxLabel = new JLabel(maxRedeemable
                + " pts (20% of total)");
        maxLabel.setForeground(Color.GRAY);
        add(maxLabel);

        // Point value info
        add(new JLabel("Point Value:"));
        add(new JLabel("1 pt = USD 0.01"));

        // Points input
        add(new JLabel("Points to Redeem:"));
        pointsSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0, 0, maxRedeemable, 10));
        pointsSpinner.addChangeListener(e ->
                updateValue());
        add(pointsSpinner, "growx");

        // Discount value
        add(new JLabel("Discount Value:"));
        valueLabel = new JLabel("USD 0.00");
        valueLabel.setFont(
                new Font("SansSerif", Font.BOLD, 14));
        valueLabel.setForeground(
                new Color(34, 139, 34));
        add(valueLabel);

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        add(errorLabel, "span 2");

        // Buttons
        JButton redeemBtn = new JButton(
                "✔ Redeem Points");
        JButton cancelBtn = new JButton("Cancel");

        redeemBtn.setBackground(
                new Color(34, 139, 34));
        redeemBtn.setForeground(Color.WHITE);
        redeemBtn.setFont(
                new Font("SansSerif", Font.BOLD, 13));

        redeemBtn.addActionListener(e -> handleRedeem());
        cancelBtn.addActionListener(e -> dispose());

        add(cancelBtn,  "span 2, split 2, right");
        add(redeemBtn,  "gap 8");

        setPreferredSize(new Dimension(400, 380));
    }

    private void updateValue() {
        int points = (int) pointsSpinner.getValue();
        MonetaryAmount value =
                loyaltyService.calculateRedemptionValue(
                        points);
        valueLabel.setText(value.toString());
    }

    private void handleRedeem() {
        int points = (int) pointsSpinner.getValue();
        if (points == 0) {
            errorLabel.setText(
                    "Enter points to redeem");
            return;
        }

        var redemption = loyaltyService.redeemPoints(
                customer.getId(), points, cartTotal);

        if (redemption.isPresent()
                && redemption.get().isSuccess()) {
            result = redemption.get();
            dispose();
        } else {
            errorLabel.setText(
                    redemption.map(r -> r.getMessage())
                            .orElse("Redemption failed"));
        }
    }

    public LoyaltyService.RedemptionResult getResult() {
        return result;
    }
}