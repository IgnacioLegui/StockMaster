package com.stockmaster.dao;

import com.stockmaster.db.DBManager;
import com.stockmaster.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Data Access Object for User authentication and management.
 * Uses SHA-256 with random salt for password storage.
 * Backward-compatible with legacy unsalted SHA-256 hashes.
 */
public class UserDAO {

    private static final SecureRandom RANDOM = new SecureRandom();

    // ==================== Authentication ====================
    
    public User authenticate(String username, String plainPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND active = 1";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (verifyPassword(plainPassword, storedHash)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null; // Auth failed
    }

    // ==================== CRUD ====================
    
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, display_name, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getDisplayName());
            pstmt.setInt(5, user.isActive() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, role = ?, display_name = ?, active = ? WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, user.getDisplayName());
            pstmt.setInt(4, user.isActive() ? 1 : 0);
            pstmt.setInt(5, user.getId());
            pstmt.executeUpdate();
        }
    }

    public void updatePassword(int userId, String newPlainPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashPassword(newPlainPassword));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
        return users;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public boolean hasAnyUser() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==================== Helpers ====================

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("display_name"),
                rs.getInt("active") == 1
        );
    }

    /**
     * SHA-256 hash with random salt. Format: "salt:hash" (Base64).
     * New passwords always use salted format.
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashHex = bytesToHex(hash);

            return saltBase64 + ":" + hashHex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verify a password against a stored hash.
     * Supports both salted (salt:hash) and legacy unsalted formats.
     */
    private static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            if (storedHash.contains(":")) {
                // New salted format: "salt:hash"
                String[] parts = storedHash.split(":", 2);
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                String expectedHash = parts[1];

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(salt);
                byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
                return bytesToHex(hash).equals(expectedHash);
            } else {
                // Legacy unsalted format
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
                return bytesToHex(hash).equals(storedHash);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}

