package com.turboboostteam.pos.ui.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BarcodeUtil {

    /**
     * Generates an EAN-13 barcode image for given barcode string
     */
    public static BufferedImage generateEAN13(String barcodeText,
                                              int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(
                    barcodeText,
                    BarcodeFormat.EAN_13,
                    width, height,
                    hints
            );
            return MatrixToImageWriter.toBufferedImage(matrix);

        } catch (Exception e) {
            // fallback — generate CODE_128 if EAN-13 fails
            try {
                MultiFormatWriter writer = new MultiFormatWriter();
                BitMatrix matrix = writer.encode(
                        barcodeText,
                        BarcodeFormat.CODE_128,
                        width, height
                );
                return MatrixToImageWriter.toBufferedImage(matrix);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Generates a random valid EAN-13 barcode string
     */
    public static String generateRandomEAN13() {
        StringBuilder sb = new StringBuilder();
        sb.append("490"); // prefix

        // 9 random digits
        for (int i = 0; i < 9; i++) {
            sb.append((int)(Math.random() * 10));
        }

        // Calculate check digit
        String digits = sb.toString();
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = Character.getNumericValue(digits.charAt(i));
            sum += (i % 2 == 0) ? d : d * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        sb.append(checkDigit);

        return sb.toString();
    }
}