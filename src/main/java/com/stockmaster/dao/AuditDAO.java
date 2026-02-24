package com.stockmaster.dao;

import com.stockmaster.db.DBManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Insert-only DAO for audit trail. Records are never modified or deleted.
 * Each record captures: WHO did WHAT to WHICH entity, with old/new values.
 */
public class AuditDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(int userId, String username, String action, String entity, int entityId, 
                    String oldValue, String newValue) {
        String sql = "INSERT INTO audit_log (user_id, username, action, entity, entity_id, old_value, new_value, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.setString(4, entity);
            pstmt.setInt(5, entityId);
            pstmt.setString(6, oldValue);
            pstmt.setString(7, newValue);
            pstmt.setString(8, LocalDateTime.now().format(FMT));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error writing audit log: " + e.getMessage());
        }
    }

    /**
     * Convenience: log without old/new values (for simple actions like DELETE, LOGIN).
     */
    public void log(int userId, String username, String action, String entity, int entityId) {
        log(userId, username, action, entity, entityId, null, null);
    }

    /**
     * Returns the last N audit log entries (most recent first).
     */
    public List<String[]> getRecentLogs(int limit) {
        List<String[]> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY id DESC LIMIT ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new String[]{
                        rs.getString("timestamp"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("entity"),
                        String.valueOf(rs.getInt("entity_id")),
                        rs.getString("old_value"),
                        rs.getString("new_value")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading audit log: " + e.getMessage());
        }
        return logs;
    }
}
