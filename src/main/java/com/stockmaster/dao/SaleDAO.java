package com.stockmaster.dao;

import com.stockmaster.db.DBManager;
import com.stockmaster.model.Sale;
import com.stockmaster.model.SaleItem;
import com.stockmaster.model.StockMovement;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StockMovementDAO movementDAO = new StockMovementDAO();

    /**
     * Insert a sale with all its items in a single transaction.
     * Deducts stock and creates stock movement records.
     */
    public int insertSale(Sale sale) throws SQLException {
        Connection conn = DBManager.getConnection();
        try {
            conn.setAutoCommit(false);

            // Insert sale header
            String saleSql = "INSERT INTO sales (sale_date, user_id, subtotal, tax_amount, total, payment_method, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int saleId;
            try (PreparedStatement pstmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, sale.getSaleDate().format(FMT));
                pstmt.setInt(2, sale.getUserId());
                pstmt.setDouble(3, sale.getSubtotal());
                pstmt.setDouble(4, sale.getTaxAmount());
                pstmt.setDouble(5, sale.getTotal());
                pstmt.setString(6, sale.getPaymentMethod());
                pstmt.setString(7, sale.getStatus());
                pstmt.executeUpdate();
                ResultSet keys = pstmt.getGeneratedKeys();
                keys.next();
                saleId = keys.getInt(1);
            }

            // Insert items + deduct stock
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, tax_rate, line_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String stockSql = "UPDATE products SET stock_current = stock_current - ? WHERE id = ? AND stock_current >= ?";

            for (SaleItem item : sale.getItems()) {
                // Insert sale item
                try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                    pstmt.setInt(1, saleId);
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setString(3, item.getProductName());
                    pstmt.setInt(4, item.getQuantity());
                    pstmt.setDouble(5, item.getUnitPrice());
                    pstmt.setDouble(6, item.getTaxRate());
                    pstmt.setDouble(7, item.getLineTotal());
                    pstmt.executeUpdate();
                }

                // Deduct stock
                try (PreparedStatement pstmt = conn.prepareStatement(stockSql)) {
                    pstmt.setInt(1, item.getQuantity());
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setInt(3, item.getQuantity());
                    int affected = pstmt.executeUpdate();
                    if (affected == 0) {
                        conn.rollback();
                        throw new SQLException("Insufficient stock for product: " + item.getProductName());
                    }
                }
            }

            conn.commit();

            // Log stock movements (after commit, non-critical)
            for (SaleItem item : sale.getItems()) {
                try {
                    StockMovement mov = new StockMovement(item.getProductId(), "OUT", item.getQuantity(), "Sale #" + saleId, sale.getUserId());
                    movementDAO.insert(mov);
                } catch (Exception ignored) {}
            }

            return saleId;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            DBManager.releaseConnection(conn);
        }
    }

    public List<Sale> findAll(int limit) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM sales s LEFT JOIN users u ON s.user_id = u.id ORDER BY s.id DESC LIMIT ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) sales.add(mapSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding sales: " + e.getMessage());
        }
        return sales;
    }

    public List<Sale> findByDateRange(String from, String to) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM sales s LEFT JOIN users u ON s.user_id = u.id " +
                     "WHERE s.sale_date >= ? AND s.sale_date <= ? ORDER BY s.id DESC";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, from);
            pstmt.setString(2, to + " 23:59:59");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) sales.add(mapSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding sales by date: " + e.getMessage());
        }
        return sales;
    }

    public List<SaleItem> findItemsBySale(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setId(rs.getInt("id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setTaxRate(rs.getDouble("tax_rate"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding sale items: " + e.getMessage());
        }
        return items;
    }

    /**
     * Get top selling products with total quantity sold.
     */
    public List<String[]> getTopProducts(int limit) {
        List<String[]> results = new ArrayList<>();
        String sql = "SELECT product_name, SUM(quantity) as total_qty, SUM(line_total) as total_revenue " +
                     "FROM sale_items GROUP BY product_id ORDER BY total_qty DESC LIMIT ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new String[]{
                        rs.getString("product_name"),
                        String.valueOf(rs.getInt("total_qty")),
                        String.format("%.2f", rs.getDouble("total_revenue"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting top products: " + e.getMessage());
        }
        return results;
    }

    /**
     * Get sales summary (total sales, total revenue, average sale).
     */
    public double[] getSalesSummary(String from, String to) {
        String sql = "SELECT COUNT(*) as count, COALESCE(SUM(total),0) as revenue, COALESCE(AVG(total),0) as avg_sale " +
                     "FROM sales WHERE status = 'COMPLETED'";
        if (from != null && to != null) {
            sql += " AND sale_date >= '" + from + "' AND sale_date <= '" + to + " 23:59:59'";
        }
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new double[]{rs.getInt("count"), rs.getDouble("revenue"), rs.getDouble("avg_sale")};
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales summary: " + e.getMessage());
        }
        return new double[]{0, 0, 0};
    }

    private Sale mapSale(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        String dateStr = rs.getString("sale_date");
        if (dateStr != null) s.setSaleDate(LocalDateTime.parse(dateStr, FMT));
        s.setUserId(rs.getInt("user_id"));
        try { s.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        s.setSubtotal(rs.getDouble("subtotal"));
        s.setTaxAmount(rs.getDouble("tax_amount"));
        s.setTotal(rs.getDouble("total"));
        s.setPaymentMethod(rs.getString("payment_method"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
