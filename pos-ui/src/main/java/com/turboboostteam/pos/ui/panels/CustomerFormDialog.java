package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.model.customer.Customer;
import com.turboboostteam.pos.service.CustomerService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class CustomerFormDialog extends JDialog {

    private final CustomerService customerService;
    private final Customer existing;
    private final Runnable onSave;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JLabel errorLabel;

    public CustomerFormDialog(Frame parent,
                              CustomerService service,
                              Customer existing,
                              Runnable onSave) {
        super(parent,
                existing == null
                        ? "Add Customer"
                        : "Edit Customer",
                true);
        this.customerService = service;
        this.existing        = existing;
        this.onSave          = onSave;
        buildUI();
        if (existing != null) populate();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new MigLayout(
                "wrap 2, fillx, insets 20, gapy 8",
                "[120][grow]"));

        JLabel title = new JLabel(
                existing == null
                        ? "New Customer"
                        : "Edit Customer");
        title.setFont(
                new Font("SansSerif", Font.BOLD, 16));
        add(title, "span 2, gapbottom 12");

        add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        add(firstNameField, "growx");

        add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        add(lastNameField, "growx");

        add(new JLabel("Phone:"));
        phoneField = new JTextField();
        add(phoneField, "growx");

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField, "growx");

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        add(errorLabel, "span 2");

        JButton saveBtn   = new JButton(
                existing == null
                        ? "Add Customer"
                        : "Save Changes");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        add(cancelBtn, "span 2, split 2, right");
        add(saveBtn);

        setPreferredSize(new Dimension(400, 320));
    }

    private void populate() {
        firstNameField.setText(existing.getFirstName());
        lastNameField.setText(existing.getLastName());
        phoneField.setText(existing.getPhone());
        emailField.setText(existing.getEmail());
    }

    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String phone     = phoneField.getText().trim();
        String email     = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            errorLabel.setText(
                    "First and last name are required");
            return;
        }
        if (phone.isEmpty()) {
            errorLabel.setText("Phone is required");
            return;
        }

        Customer customer = new Customer.Builder()
                .id(existing != null
                        ? existing.getId() : null)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .email(email)
                .loyaltyPoints(existing != null
                        ? existing.getLoyaltyPoints() : 0)
                .loyaltyTier(existing != null
                        ? existing.getLoyaltyTier()
                        : com.turboboostteam.pos.model
                          .customer.LoyaltyTier.BRONZE)
                .totalSpent(existing != null
                        ? existing.getTotalSpent()
                        : java.math.BigDecimal.ZERO)
                .build();

        if (existing == null) {
            customerService.addCustomer(customer);
        } else {
            customerService.updateCustomer(customer);
        }

        onSave.run();
        dispose();
    }
}