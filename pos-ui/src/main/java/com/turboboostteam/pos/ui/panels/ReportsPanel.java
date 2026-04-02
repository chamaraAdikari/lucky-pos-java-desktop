package com.turboboostteam.pos.ui.panels;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;

public class ReportsPanel extends JPanel {
    public ReportsPanel() {
        setLayout(new MigLayout("fill"));
        add(new JLabel("📊 Reports Panel — Coming Day 9"), "center");
    }
}