package com.turboboostteam.pos.ui.panels;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;

public class SalesPanel extends JPanel {
    public SalesPanel() {
        setLayout(new MigLayout("fill"));
        add(new JLabel("🛒 Sales Panel — Coming Day 5"), "center");
    }
}