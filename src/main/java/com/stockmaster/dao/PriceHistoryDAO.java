package com.stockmaster.dao;

import com.stockmaster.db.DBManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs price changes for products. Insert-only — records are never modified or deleted.
 */
public class PriceHistoryDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(int productId, double oldBuy, double newBuy, double oldSell, double newSell, String changedBy) {
        // Only log if something actually changed
        if (oldBuy == newBuy && oldSell == newSell) return;

        String sql = "INSERT INTO price_history (product_id, old_buy, new_buy, old_sell, new_sell, changed_by, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setDouble(2, oldBuy);
            pstmt.setDouble(3, newBuy);
            pstmt.setDouble(4, oldSell);
            pstmt.setDouble(5, newSell);
            pstmt.setString(6, changedBy);
            pstmt.setString(7, LocalDateTime.now().format(FMT));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging price history: " + e.getMessage());
        }
    }

    public List<String[]> findByProduct(int productId) {
        List<String[]> records = new ArrayList<>();
        String sql = "SELECT * FROM price_history WHERE product_id = ? ORDER BY id DESC";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new String[]{
                        rs.getString("timestamp"),
                        String.format("%.2f", rs.getDouble("old_buy")),
                        String.format("%.2f", rs.getDouble("new_buy")),
                        String.format("%.2f", rs.getDouble("old_sell")),
                        String.format("%.2f", rs.getDouble("new_sell")),
                        rs.getString("changed_by")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading price history: " + e.getMessage());
        }
        return records;
    }
}
