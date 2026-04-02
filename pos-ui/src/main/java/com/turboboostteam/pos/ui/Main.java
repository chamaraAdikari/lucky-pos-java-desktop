package com.turboboostteam.pos.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.turboboostteam.pos.config.AppContext;
import com.turboboostteam.pos.ui.panels.LoginPanel;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // 1. Boot Spring Context + DB + Flyway
        AppContext.init();
        System.out.println("✅ DB connected & schema ready");

        // 2. Launch Swing UI
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lucky POS");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1024, 768);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new LoginPanel());  // ← Login screen
            frame.setVisible(true);
        });
    }
}