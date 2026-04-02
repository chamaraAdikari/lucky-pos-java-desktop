package com.turboboostteam.pos.ui.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class ThemeManager {

    private static boolean isDark = true;

    public static void toggle(JFrame frame) {
        try {
            if (isDark) {
                FlatLightLaf.setup();
            } else {
                FlatDarkLaf.setup();
            }
            isDark = !isDark;
            SwingUtilities.updateComponentTreeUI(frame);
            frame.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDark() { return isDark; }
}