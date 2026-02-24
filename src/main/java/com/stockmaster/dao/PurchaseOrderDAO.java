package com.stockmaster.dao;

import com.stockmaster.db.DBManager;
import com.stockmaster.model.PurchaseOrder;
import com.stockmaster.model.PurchaseOrderItem;
import com.stockmaster.model.StockMovement;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final StockMovementDAO movementDAO = new StockMovementDAO();

    public int insert(PurchaseOrder order) throws SQLException {
        Connection conn = DBManager.getConnection();
        try {
            conn.setAutoCommit(false);

            String orderSql = "INSERT INTO purchase_orders (supplier_id, order_date, status, total, notes) VALUES (?, ?, ?, ?, ?)";
            int orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getSupplierId());
                pstmt.setString(2, order.getOrderDate().format(DATE_FMT));
                pstmt.setString(3, order.getStatus());
                pstmt.setDouble(4, order.getTotal());
                pstmt.setString(5, order.getNotes());
                pstmt.executeUpdate();
                ResultSet keys = pstmt.getGeneratedKeys();
                keys.next();
                orderId = keys.getInt(1);
            }

            String itemSql = "INSERT INTO purchase_order_items (order_id, product_id, quantity, unit_cost, line_total) VALUES (?, ?, ?, ?, ?)";
            for (PurchaseOrderItem item : order.getItems()) {
                try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.setDouble(4, item.getUnitCost());
                    pstmt.setDouble(5, item.getLineTotal());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            DBManager.releaseConnection(conn);
        }
    }

    /**
     * Mark order as RECEIVED — adds stock and creates stock movements.
     */
    public void receive(int orderId, int userId) throws SQLException {
        Connection conn = DBManager.getConnection();
        try {
            conn.setAutoCommit(false);

            // Update status
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE purchase_orders SET status = 'RECEIVED' WHERE id = ?")) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }

            // Get items and add stock
            List<PurchaseOrderItem> items = findItemsByOrder(orderId);
            for (PurchaseOrderItem item : items) {
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET stock_current = stock_current + ? WHERE id = ?")) {
                    pstmt.setInt(1, item.getQuantity());
                    pstmt.setInt(2, item.getProductId());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();

            // Log stock movements
            for (PurchaseOrderItem item : items) {
                try {
                    StockMovement mov = new StockMovement(item.getProductId(), "IN", item.getQuantity(), "PO #" + orderId, userId);
                    movementDAO.insert(mov);
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            DBManager.releaseConnection(conn);
        }
    }

    public void cancel(int orderId) throws SQLException {
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE purchase_orders SET status = 'CANCELLED' WHERE id = ?")) {
            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();
        }
    }

    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT po.*, s.name as supplier_name FROM purchase_orders po " +
                     "LEFT JOIN suppliers s ON po.supplier_id = s.id ORDER BY po.id DESC";
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            System.err.println("Error finding purchase orders: " + e.getMessage());
        }
        return list;
    }

    public List<PurchaseOrderItem> findItemsByOrder(int orderId) {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String sql = "SELECT poi.*, p.name as product_name FROM purchase_order_items poi " +
                     "LEFT JOIN products p ON poi.product_id = p.id WHERE poi.order_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderItem item = new PurchaseOrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    try { item.setProductName(rs.getString("product_name")); } catch (SQLException ignored) {}
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitCost(rs.getDouble("unit_cost"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding PO items: " + e.getMessage());
        }
        return items;
    }

    private PurchaseOrder mapOrder(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(rs.getInt("id"));
        po.setSupplierId(rs.getInt("supplier_id"));
        try { po.setSupplierName(rs.getString("supplier_name")); } catch (SQLException ignored) {}
        String dateStr = rs.getString("order_date");
        if (dateStr != null) po.setOrderDate(LocalDate.parse(dateStr, DATE_FMT));
        po.setStatus(rs.getString("status"));
        po.setTotal(rs.getDouble("total"));
        po.setNotes(rs.getString("notes"));
        return po;
    }
}
