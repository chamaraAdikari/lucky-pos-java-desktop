package com.turboboostteam.pos.ui.components;

import com.turboboostteam.pos.service.SessionManager;
import com.turboboostteam.pos.ui.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HeaderPanel extends JPanel {

    private final JFrame parentFrame;
    private final Runnable onLogout;
    private JLabel timeLabel;

    public HeaderPanel(JFrame parentFrame, Runnable onLogout) {
        this.parentFrame = parentFrame;
        this.onLogout = onLogout;
        buildUI();
        startClock();
    }

    private void buildUI() {
        setLayout(new MigLayout("fillx, insets 8 16 8 16", "[left][grow][right]"));

        // App name
        JLabel appName = new JLabel("Lucky POS");
        appName.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Cashier info
        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        String role     = SessionManager.getInstance().getCurrentUser().getRole().name();
        JLabel userLabel = new JLabel("👤 " + username + "  |  " + role);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Clock
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Theme toggle
        JButton themeBtn = new JButton("🌙 Theme");
        themeBtn.addActionListener(e -> ThemeManager.toggle(parentFrame));

        // Logout
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> onLogout.run());

        add(appName);
        add(userLabel, "center");
        add(timeLabel, "gap 16");
        add(themeBtn,  "gap 8");
        add(logoutBtn, "gap 8");
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            timeLabel.setText("🕐 " + time);
        });
        timer.start();
    }
}