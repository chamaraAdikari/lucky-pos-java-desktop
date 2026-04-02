package com.turboboostteam.pos.ui.panels;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;

public class ProductPanel extends JPanel {
    public ProductPanel() {
        setLayout(new MigLayout("fill"));
        add(new JLabel("📦 Product Panel — Coming Day 6"), "center");
    }
}