package com.turboboostteam.pos.ui;

import com.turboboostteam.pos.service.SessionManager;
import com.turboboostteam.pos.ui.components.HeaderPanel;
import com.turboboostteam.pos.ui.components.SidebarPanel;
import com.turboboostteam.pos.ui.panels.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;

    public MainFrame() {
        buildUI();
    }

    private void buildUI() {
        setTitle("Lucky POS — " +
                SessionManager.getInstance().getCurrentUser().getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // Root layout
        JPanel root = new JPanel(new MigLayout(
                "fill, insets 0, gap 0",
                "[200!][grow]",
                "[48!][grow]"
        ));

        // Header
        HeaderPanel header = new HeaderPanel(this, this::handleLogout);

        // Sidebar
        SidebarPanel sidebar = new SidebarPanel(this::navigateTo);

        // Card layout content area
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.add(new SalesPanel(),     "SALES");
        contentArea.add(new ProductPanel(),   "PRODUCTS");
        contentArea.add(new InventoryPanel(), "INVENTORY");
        contentArea.add(new CustomerPanel(),  "CUSTOMERS");
        contentArea.add(new ReportsPanel(),   "REPORTS");
        contentArea.add(new VoidRefundPanel(), "VOIDREFUND");
        contentArea.add(new DashboardPanel(), "DASHBOARD");

        // Assemble
        root.add(header,      "span 2, growx, wrap");
        root.add(sidebar,     "growy");
        root.add(contentArea, "grow");

        setContentPane(root);

        // Show Sales first
        cardLayout.show(contentArea, "SALES");
    }

    private void navigateTo(String panelName) {
        cardLayout.show(contentArea, panelName);
        // Refresh panel when navigated to
        Component[] components = contentArea.getComponents();

        for (Component c : components) {
            if (c.isVisible()) {
                if (c instanceof VoidRefundPanel)
                    ((VoidRefundPanel) c).refresh();
                if (c instanceof ReportsPanel)
                    ((ReportsPanel) c).refresh();
                if (c instanceof DashboardPanel)
                    ((DashboardPanel) c).refresh();
            }
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            // Go back to login
            SwingUtilities.invokeLater(() -> {
                JFrame login = new JFrame("Lucky POS");
                login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                login.setSize(1024, 768);
                login.setLocationRelativeTo(null);
                login.setContentPane(new com.turboboostteam.pos.ui.panels.LoginPanel());
                login.setVisible(true);
            });
        }
    }
}