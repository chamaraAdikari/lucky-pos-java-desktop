package com.turboboostteam.pos.ui.panels;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;

public class InventoryPanel extends JPanel {
    public InventoryPanel() {
        setLayout(new MigLayout("fill"));
        add(new JLabel("🏭 Inventory Panel — Coming Day 7"), "center");
    }
}