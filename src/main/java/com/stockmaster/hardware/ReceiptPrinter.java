package com.stockmaster.hardware;

import com.stockmaster.model.Product;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates and prints receipts for POS thermal printers.
 * Uses Java Print API — works with any system-installed printer.
 * For ESC/POS thermal printers, the text output works directly.
 */
public class ReceiptPrinter {

    private static final int RECEIPT_WIDTH = 40; // characters for 80mm thermal
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Format a receipt for a list of products (e.g., selected items).
     */
    public static String formatReceipt(List<Product> items, String cashierName, String storeName) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(center(storeName)).append("\n");
        sb.append(center("─".repeat(RECEIPT_WIDTH))).append("\n");
        sb.append("Date: ").append(LocalDateTime.now().format(FMT)).append("\n");
        sb.append("Cashier: ").append(cashierName).append("\n");
        sb.append("─".repeat(RECEIPT_WIDTH)).append("\n");

        // Column headers
        sb.append(String.format("%-20s %6s %6s %6s\n", "Item", "Price", "Qty", "Total"));
        sb.append("─".repeat(RECEIPT_WIDTH)).append("\n");

        // Items
        double grandTotal = 0;
        for (Product p : items) {
            double lineTotal = p.getPriceSell() * p.getStockCurrent();
            grandTotal += lineTotal;
            String name = p.getName().length() > 20 ? p.getName().substring(0, 17) + "..." : p.getName();
            sb.append(String.format("%-20s %6.2f %6d %6.2f\n", name, p.getPriceSell(), p.getStockCurrent(), lineTotal));
        }

        // Totals
        sb.append("─".repeat(RECEIPT_WIDTH)).append("\n");
        sb.append(String.format("%34s %6.2f\n", "TOTAL:", grandTotal));
        sb.append("─".repeat(RECEIPT_WIDTH)).append("\n");

        // Footer
        sb.append(center("Thank you for your purchase!")).append("\n");
        sb.append(center("StockMaster Enterprise")).append("\n");

        return sb.toString();
    }

    /**
     * Format a receipt for a single product (inventory ticket).
     */
    public static String formatProductTicket(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append(center("═".repeat(RECEIPT_WIDTH))).append("\n");
        sb.append(center("PRODUCT TICKET")).append("\n");
        sb.append(center("═".repeat(RECEIPT_WIDTH))).append("\n\n");
        sb.append("Product: ").append(product.getName()).append("\n");
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            sb.append("Barcode: ").append(product.getBarcode()).append("\n");
        }
        sb.append("Category: ").append(product.getCategoryName() != null ? product.getCategoryName() : "—").append("\n");
        sb.append("Sell Price: $").append(String.format("%.2f", product.getPriceSell())).append("\n");
        sb.append("Stock: ").append(product.getStockCurrent()).append("\n");
        sb.append("─".repeat(RECEIPT_WIDTH)).append("\n");
        sb.append("Printed: ").append(LocalDateTime.now().format(FMT)).append("\n");
        return sb.toString();
    }

    /**
     * Send text to the default system printer.
     * Returns true on success.
     */
    public static boolean printToDefault(String receiptText) {
        try {
            PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrinter == null) {
                System.err.println("No default printer found.");
                return false;
            }
            return printTo(defaultPrinter, receiptText);
        } catch (Exception e) {
            System.err.println("Error printing: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send text to a specific printer by name.
     */
    public static boolean printToNamed(String printerName, String receiptText) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().toLowerCase().contains(printerName.toLowerCase())) {
                return printTo(service, receiptText);
            }
        }
        System.err.println("Printer not found: " + printerName);
        return false;
    }

    /**
     * List available printer names.
     */
    public static String[] listPrinters() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        String[] names = new String[services.length];
        for (int i = 0; i < services.length; i++) {
            names[i] = services[i].getName();
        }
        return names;
    }

    /**
     * Send ESC/POS cash drawer open command (pulse pin 2).
     */
    public static boolean openCashDrawer() {
        // ESC/POS command: ESC p 0 25 250 (open pin 2, pulse 50ms on, 500ms off)
        byte[] cmd = new byte[]{0x1B, 0x70, 0x00, 0x19, (byte) 0xFA};
        try {
            PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrinter == null) return false;

            DocPrintJob job = defaultPrinter.createPrintJob();
            Doc doc = new SimpleDoc(new ByteArrayInputStream(cmd), DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            job.print(doc, new HashPrintRequestAttributeSet());
            return true;
        } catch (Exception e) {
            System.err.println("Error opening cash drawer: " + e.getMessage());
            return false;
        }
    }

    // ─── Helpers ───

    private static boolean printTo(PrintService service, String text) {
        try {
            DocPrintJob job = service.createPrintJob();
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            Doc doc = new SimpleDoc(new ByteArrayInputStream(bytes), DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            job.print(doc, new HashPrintRequestAttributeSet());
            System.out.println("Printed to: " + service.getName());
            return true;
        } catch (PrintException e) {
            System.err.println("Print error: " + e.getMessage());
            return false;
        }
    }

    private static String center(String text) {
        if (text.length() >= RECEIPT_WIDTH) return text;
        int pad = (RECEIPT_WIDTH - text.length()) / 2;
        return " ".repeat(pad) + text;
    }
}
