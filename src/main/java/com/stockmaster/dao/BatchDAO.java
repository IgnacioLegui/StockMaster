package com.stockmaster.dao;

import com.stockmaster.db.DBManager;
import com.stockmaster.model.Batch;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for product batch/lot management.
 * Supports FIFO ordering (oldest batches consumed first).
 */
public class BatchDAO {

    public void insert(Batch batch) throws SQLException {
        String sql = "INSERT INTO product_batches (product_id, lot_number, expiry_date, quantity, cost_price, received_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batch.getProductId());
            pstmt.setString(2, batch.getLotNumber());
            pstmt.setString(3, batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : null);
            pstmt.setInt(4, batch.getQuantity());
            pstmt.setDouble(5, batch.getCostPrice());
            pstmt.setString(6, batch.getReceivedDate() != null ? batch.getReceivedDate().toString() : LocalDate.now().toString());
            pstmt.executeUpdate();
        }
    }

    public void update(Batch batch) throws SQLException {
        String sql = "UPDATE product_batches SET lot_number = ?, expiry_date = ?, quantity = ?, cost_price = ? WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, batch.getLotNumber());
            pstmt.setString(2, batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : null);
            pstmt.setInt(3, batch.getQuantity());
            pstmt.setDouble(4, batch.getCostPrice());
            pstmt.setInt(5, batch.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM product_batches WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get all batches for a product, ordered by expiry date (FIFO: soonest expiry first).
     */
    public List<Batch> findByProductId(int productId) {
        List<Batch> batches = new ArrayList<>();
        String sql = "SELECT b.*, p.name as product_name FROM product_batches b " +
                     "LEFT JOIN products p ON b.product_id = p.id " +
                     "WHERE b.product_id = ? ORDER BY b.expiry_date ASC, b.received_date ASC";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(mapBatch(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding batches: " + e.getMessage());
        }
        return batches;
    }

    /**
     * Get ALL batches across all products (for dashboard overview).
     */
    public List<Batch> findAll() {
        List<Batch> batches = new ArrayList<>();
        String sql = "SELECT b.*, p.name as product_name FROM product_batches b " +
                     "LEFT JOIN products p ON b.product_id = p.id " +
                     "ORDER BY b.expiry_date ASC";
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                batches.add(mapBatch(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all batches: " + e.getMessage());
        }
        return batches;
    }

    /**
     * Count batches expiring within N days (for dashboard KPI).
     */
    public int countExpiringSoon(int days) {
        String sql = "SELECT COUNT(*) FROM product_batches " +
                     "WHERE expiry_date IS NOT NULL AND quantity > 0 " +
                     "AND expiry_date <= date('now', '+' || ? || ' days') " +
                     "AND expiry_date >= date('now')";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting expiring batches: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Count already expired batches with remaining quantity.
     */
    public int countExpired() {
        String sql = "SELECT COUNT(*) FROM product_batches " +
                     "WHERE expiry_date IS NOT NULL AND quantity > 0 AND expiry_date < date('now')";
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Get actual batches expiring within N days (for Reports).
     */
    public List<Batch> findExpiringSoon(int days) {
        List<Batch> batches = new ArrayList<>();
        String sql = "SELECT b.*, p.name as product_name FROM product_batches b " +
                     "LEFT JOIN products p ON b.product_id = p.id " +
                     "WHERE b.expiry_date IS NOT NULL AND b.quantity > 0 " +
                     "AND b.expiry_date <= date('now', '+' || ? || ' days') " +
                     "ORDER BY b.expiry_date ASC";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) batches.add(mapBatch(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding expiring batches: " + e.getMessage());
        }
        return batches;
    }

    /**
     * FIFO consume: reduces quantity from the oldest batches first.
     * Returns the actual quantity consumed.
     */
    public int consumeFIFO(int productId, int quantityToConsume) throws SQLException {
        List<Batch> batches = findByProductId(productId);
        int remaining = quantityToConsume;

        for (Batch batch : batches) {
            if (remaining <= 0) break;
            if (batch.getQuantity() <= 0) continue;

            int consume = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - consume);
            update(batch);
            remaining -= consume;
        }

        return quantityToConsume - remaining;
    }

    /**
     * Total stock across all batches for a product.
     */
    public int getTotalBatchStock(int productId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM product_batches WHERE product_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    private Batch mapBatch(ResultSet rs) throws SQLException {
        Batch b = new Batch();
        b.setId(rs.getInt("id"));
        b.setProductId(rs.getInt("product_id"));
        b.setLotNumber(rs.getString("lot_number"));
        String expiryStr = rs.getString("expiry_date");
        if (expiryStr != null && !expiryStr.isEmpty()) {
            b.setExpiryDate(LocalDate.parse(expiryStr));
        }
        b.setQuantity(rs.getInt("quantity"));
        b.setCostPrice(rs.getDouble("cost_price"));
        String receivedStr = rs.getString("received_date");
        if (receivedStr != null && !receivedStr.isEmpty()) {
            b.setReceivedDate(LocalDate.parse(receivedStr));
        }
        try {
            b.setProductName(rs.getString("product_name"));
        } catch (SQLException e) {
            // product_name column may not be in all queries
        }
        return b;
    }
}
