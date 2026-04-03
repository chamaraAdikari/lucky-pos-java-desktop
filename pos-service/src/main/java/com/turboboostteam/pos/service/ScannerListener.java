package com.turboboostteam.pos.service;

import java.util.function.Consumer;

public class ScannerListener {

    private final StringBuilder buffer = new StringBuilder();
    private final Consumer<String> onScan;
    private long lastKeyTime = 0;
    private static final int SCAN_TIMEOUT_MS = 100;

    public ScannerListener(Consumer<String> onScan) {
        this.onScan = onScan;
    }

    /**
     * Call this from KeyListener.keyTyped() on your panel
     * Scanner types fast — human typing is slow
     * We detect scanner by speed of input
     */
    public void onKeyTyped(char c) {
        long now = System.currentTimeMillis();

        // If too much time passed — reset buffer (human typing)
        if (now - lastKeyTime > SCAN_TIMEOUT_MS
                && buffer.length() > 0) {
            buffer.setLength(0);
        }
        lastKeyTime = now;

        if (c == '\n' || c == '\r') {
            // Enter key — scanner finished
            String scanned = buffer.toString().trim();
            if (scanned.length() >= 8) { // valid barcode length
                onScan.accept(scanned);
            }
            buffer.setLength(0);
        } else {
            buffer.append(c);
        }
    }

    public void reset() {
        buffer.setLength(0);
    }
}