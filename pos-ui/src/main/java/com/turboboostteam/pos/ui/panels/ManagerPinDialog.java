package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.service.UserService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

// Manager must confirm sensitive operations with PIN
public class ManagerPinDialog extends JDialog {

    private final UserService userService;
    private boolean confirmed = false;
    private JPasswordField pinField;
    private JLabel errorLabel;

    public ManagerPinDialog(Frame parent,
                            UserService userService,
                            String actionDescription) {
        super(parent, "Manager Authorisation", true);
        this.userService = userService;
        buildUI(actionDescription);
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI(String action) {
        setLayout(new MigLayout(
                "wrap 1, fillx, insets 20, gapy 10",
                "[center]"));

        // Warning icon
        JLabel icon = new JLabel("🔐");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 36));
        add(icon);

        // Title
        JLabel title = new JLabel(
                "Manager Authorisation Required");
        title.setFont(
                new Font("SansSerif", Font.BOLD, 14));
        add(title);

        // Action description
        JLabel actionLabel = new JLabel(
                "<html><center>" + action
                        + "</center></html>");
        actionLabel.setForeground(Color.GRAY);
        add(actionLabel);

        // Username
        add(new JLabel("Manager Username:"),
                "alignx left");
        JTextField usernameField = new JTextField();
        add(usernameField, "growx");

        // PIN
        add(new JLabel("Manager PIN:"), "alignx left");
        pinField = new JPasswordField();
        add(pinField, "growx");

        // Error
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        add(errorLabel);

        // Buttons
        JPanel btnRow = new JPanel(
                new MigLayout("insets 0", "[][]"));
        JButton cancelBtn  = new JButton("Cancel");
        JButton confirmBtn = new JButton("✔ Authorise");

        confirmBtn.setBackground(
                new Color(34, 139, 34));
        confirmBtn.setForeground(Color.WHITE);

        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(e -> {
            String username = usernameField
                    .getText().trim();
            String pin = new String(
                    pinField.getPassword());

            if (username.isEmpty() || pin.isEmpty()) {
                errorLabel.setText(
                        "Username and PIN required");
                return;
            }

            boolean valid = userService.login(
                    username, pin);

            if (valid) {
                // Re-login original user after check
                confirmed = true;
                dispose();
            } else {
                errorLabel.setText(
                        "Invalid credentials");
                pinField.setText("");
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn, "gap 8");
        add(btnRow);

        setPreferredSize(new Dimension(360, 380));
    }

    public boolean isConfirmed() { return confirmed; }
}