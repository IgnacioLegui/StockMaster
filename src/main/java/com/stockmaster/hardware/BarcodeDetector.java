package com.stockmaster.hardware;

import javafx.scene.control.TextField;

/**
 * Detects barcode scanner input by monitoring keystroke speed.
 * USB barcode scanners send characters much faster than human typing.
 * If multiple characters arrive within THRESHOLD_MS, it's likely a scan.
 */
public class BarcodeDetector {

    private static final long THRESHOLD_MS = 80; // Max ms between keystrokes for scanner
    private static final int MIN_LENGTH = 4;      // Minimum barcode length

    private long lastKeyTime = 0;
    private StringBuilder buffer = new StringBuilder();
    private BarcodeListener listener;

    @FunctionalInterface
    public interface BarcodeListener {
        void onBarcodeScanned(String barcode);
    }

    public BarcodeDetector(BarcodeListener listener) {
        this.listener = listener;
    }

    /**
     * Attach to a TextField to auto-detect barcode scanner input.
     * Normal typing goes through normally; fast input triggers scan callback.
     */
    public void attachTo(TextField field) {
        field.setOnKeyTyped(event -> {
            long now = System.currentTimeMillis();
            char c = event.getCharacter().isEmpty() ? 0 : event.getCharacter().charAt(0);

            if (c == '\n' || c == '\r') {
                // Enter key — scanner sends this at end of barcode
                if (buffer.length() >= MIN_LENGTH) {
                    String barcode = buffer.toString().trim();
                    buffer.setLength(0);
                    lastKeyTime = 0;
                    
                    // Clear the field and fire callback
                    javafx.application.Platform.runLater(() -> {
                        field.clear();
                        listener.onBarcodeScanned(barcode);
                    });
                    event.consume();
                    return;
                }
                buffer.setLength(0);
                return;
            }

            if (now - lastKeyTime > THRESHOLD_MS && lastKeyTime > 0) {
                // Too slow — human typing, reset buffer
                buffer.setLength(0);
            }

            buffer.append(c);
            lastKeyTime = now;
        });
    }

    /**
     * Manual barcode lookup (user typed or pasted a barcode).
     */
    public static boolean looksLikeBarcode(String input) {
        if (input == null || input.length() < MIN_LENGTH) return false;
        // EAN/UPC barcodes are purely numeric, 8-13 digits
        return input.matches("\\d{8,13}");
    }
}
