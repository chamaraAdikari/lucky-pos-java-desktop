package com.turboboostteam.pos.ui.components;

import com.turboboostteam.pos.model.user.Role;
import com.turboboostteam.pos.service.SessionManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class SidebarPanel extends JPanel {

    public interface NavListener {
        void onNavigate(String panelName);
    }

    private final NavListener navListener;
    private JButton activeButton;

    public SidebarPanel(NavListener navListener) {
        this.navListener = navListener;
        buildUI();
    }

    private void buildUI() {
        setLayout(new MigLayout("wrap 1, fillx, insets 12 8 12 8, gapy 4",
                "[grow]"));
        setPreferredSize(new Dimension(200, 0));

        // Menu title
        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        add(menuLabel, "gapbottom 8");

        // Nav buttons — always visible
        addNavButton("🛒  Sales",     "SALES");
        addNavButton("📦  Products",  "PRODUCTS");
        addNavButton("🏭  Inventory", "INVENTORY");
        addNavButton("👤  Customers", "CUSTOMERS");

        // Role-based — only ADMIN and SUPERVISOR
        Role role = SessionManager.getInstance().getCurrentUser().getRole();
        if (role == Role.ADMIN || role == Role.SUPERVISOR) {
            addNavButton("📊  Reports", "REPORTS");
            addNavButton("↩️  Void/Refund", "VOIDREFUND");
            addNavButton("📊  Reports",     "REPORTS");
        }

        // Settings — only ADMIN
        if (role == Role.ADMIN) {
            addNavButton("⚙️  Settings", "SETTINGS");
        }
    }

    private void addNavButton(String label, String panelName) {
        JButton btn = new JButton(label);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            setActive(btn);
            navListener.onNavigate(panelName);
        });

        add(btn, "growx");

        // Default first button active
        if (activeButton == null) {
            setActive(btn);
        }
    }

    private void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
        activeButton = btn;
        activeButton.setFont(new Font("SansSerif", Font.BOLD, 13));
    }
}