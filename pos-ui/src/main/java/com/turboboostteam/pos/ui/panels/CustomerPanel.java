package com.turboboostteam.pos.ui.panels;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;

public class CustomerPanel extends JPanel {
    public CustomerPanel() {
        setLayout(new MigLayout("fill"));
        add(new JLabel("👤 Customer Panel — Coming Day 8"), "center");
    }
}