package com.turboboostteam.pos.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(()->{
            JFrame frame = new JFrame("Lucky POS");
            JMenuBar manuBar = new JMenuBar();

            JMenu homeMenu = new JMenu("Home");
            JMenu theme = new JMenu("Theme");

            JMenuItem theme1 = new JMenuItem("cobolt");
            JMenuItem theme2 = new JMenuItem("dracula");

            theme.add(theme1);
            theme.add(theme2);

            manuBar.add(homeMenu);
            manuBar.add(theme);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1024,768);
            frame.setJMenuBar(manuBar);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
