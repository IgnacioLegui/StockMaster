package com.stockmaster.dao;

import com.stockmaster.db.DBManager;
import com.stockmaster.model.StockMovement;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StockMovementDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void insert(StockMovement movement) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, type, quantity, reason, user_id, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movement.getProductId());
            pstmt.setString(2, movement.getType());
            pstmt.setInt(3, movement.getQuantity());
            pstmt.setString(4, movement.getReason());
            pstmt.setInt(5, movement.getUserId());
            pstmt.setString(6, LocalDateTime.now().format(FMT));
            pstmt.executeUpdate();
        }
    }

    public List<StockMovement> findByProduct(int productId) {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.*, p.name as product_name, u.username FROM stock_movements sm " +
                     "LEFT JOIN products p ON sm.product_id = p.id " +
                     "LEFT JOIN users u ON sm.user_id = u.id " +
                     "WHERE sm.product_id = ? ORDER BY sm.id DESC";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding movements: " + e.getMessage());
        }
        return list;
    }

    public List<StockMovement> findAll(int limit) {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.*, p.name as product_name, u.username FROM stock_movements sm " +
                     "LEFT JOIN products p ON sm.product_id = p.id " +
                     "LEFT JOIN users u ON sm.user_id = u.id " +
                     "ORDER BY sm.id DESC LIMIT ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all movements: " + e.getMessage());
        }
        return list;
    }

    public List<StockMovement> findByDateRange(String from, String to) {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.*, p.name as product_name, u.username FROM stock_movements sm " +
                     "LEFT JOIN products p ON sm.product_id = p.id " +
                     "LEFT JOIN users u ON sm.user_id = u.id " +
                     "WHERE sm.timestamp >= ? AND sm.timestamp <= ? ORDER BY sm.id DESC";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, from);
            pstmt.setString(2, to + " 23:59:59");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding movements by date: " + e.getMessage());
        }
        return list;
    }

    private StockMovement map(ResultSet rs) throws SQLException {
        StockMovement m = new StockMovement();
        m.setId(rs.getInt("id"));
        m.setProductId(rs.getInt("product_id"));
        m.setType(rs.getString("type"));
        m.setQuantity(rs.getInt("quantity"));
        m.setReason(rs.getString("reason"));
        m.setUserId(rs.getInt("user_id"));
        try { m.setProductName(rs.getString("product_name")); } catch (SQLException ignored) {}
        try { m.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        String ts = rs.getString("timestamp");
        if (ts != null) m.setTimestamp(LocalDateTime.parse(ts, FMT));
        return m;
    }
}
