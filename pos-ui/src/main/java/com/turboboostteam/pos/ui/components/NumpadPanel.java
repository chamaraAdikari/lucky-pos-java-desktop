package com.turboboostteam.pos.ui.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class NumpadPanel extends JPanel {

    private final JTextField display;
    private final Consumer<String> onEnter;

    public NumpadPanel(String label, Consumer<String> onEnter) {
        this.onEnter = onEnter;
        setLayout(new MigLayout("wrap 3, insets 8, gapy 4, gapx 4",
                "[grow][grow][grow]"));

        // Display
        display = new JTextField();
        display.setFont(new Font("Monospaced", Font.BOLD, 18));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        add(lbl,     "span 3, growx, wrap");
        add(display, "span 3, growx, wrap");

        // Number buttons
        String[] btns = {
                "7","8","9",
                "4","5","6",
                "1","2","3",
                "C","0","⌫"
        };

        for (String btn : btns) {
            JButton b = new JButton(btn);
            b.setFont(new Font("SansSerif", Font.BOLD, 16));
            b.setFocusPainted(false);
            b.addActionListener(e -> handleButton(btn));
            add(b, "growx, h 44!");
        }

        // Enter button
        JButton enterBtn = new JButton("✔ Enter");
        enterBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        enterBtn.setBackground(new Color(34, 139, 34));
        enterBtn.setForeground(Color.WHITE);
        enterBtn.setFocusPainted(false);
        enterBtn.addActionListener(e -> handleEnter());
        add(enterBtn, "span 3, growx, h 44!");
    }

    private void handleButton(String btn) {
        switch (btn) {
            case "C"  -> display.setText("");
            case "⌫" -> {
                String t = display.getText();
                if (!t.isEmpty())
                    display.setText(t.substring(0, t.length() - 1));
            }
            default -> display.setText(display.getText() + btn);
        }
    }

    private void handleEnter() {
        String value = display.getText().trim();
        if (!value.isEmpty()) {
            onEnter.accept(value);
            display.setText("");
        }
    }

    public void clear() { display.setText(""); }

    public String getValue() { return display.getText(); }
}