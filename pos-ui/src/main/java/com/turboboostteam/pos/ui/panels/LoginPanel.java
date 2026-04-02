package com.turboboostteam.pos.ui.panels;

import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.service.UserService;
import com.turboboostteam.pos.ui.MainFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final UserService userService;
    private JTextField usernameField;
    private JPasswordField pinField;
    private JLabel errorLabel;

    public LoginPanel() {
        this.userService = AppContext.getBean(UserService.class);
        buildUI();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill, insets 0"));

        // Center card panel
        JPanel card = new JPanel(new MigLayout(
                "wrap 1, fillx, insets 40 50 40 50, gapy 12",
                "[center]"
        ));
        card.setPreferredSize(new Dimension(380, 420));

        // Title
        JLabel title = new JLabel("Lucky POS");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Username
        JLabel userLabel = new JLabel("Username");
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(280, 38));

        // PIN
        JLabel pinLabel = new JLabel("PIN");
        pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(280, 38));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Login button
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(280, 42));
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.addActionListener(e -> handleLogin());

        // Enter key on PIN triggers login
        pinField.addActionListener(e -> handleLogin());

        // Add to card
        card.add(title);
        card.add(subtitle, "gaptop 4");
        card.add(userLabel, "gaptop 20, alignx left");
        card.add(usernameField, "growx");
        card.add(pinLabel, "alignx left");
        card.add(pinField, "growx");
        card.add(errorLabel);
        card.add(loginBtn, "growx, gaptop 8");

        add(card, "center");
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String pin = new String(pinField.getPassword());

        if (username.isEmpty() || pin.isEmpty()) {
            errorLabel.setText("Username and PIN are required");
            return;
        }

        boolean success = userService.login(username, pin);

        if (success) {
            errorLabel.setText(" ");
            // Switch to MainFrame
            SwingUtilities.getWindowAncestor(this).dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
        } else {
            errorLabel.setText("Invalid username or PIN");
            pinField.setText("");
        }
    }
}