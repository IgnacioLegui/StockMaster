package com.stockmaster.dao;

import com.stockmaster.db.DBManager;
import com.stockmaster.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, description, category_id, supplier_id, price_buy, price_sell, stock_current, stock_min, barcode, tax_rate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setInt(4, product.getSupplierId());
            pstmt.setDouble(5, product.getPriceBuy());
            pstmt.setDouble(6, product.getPriceSell());
            pstmt.setInt(7, product.getStockCurrent());
            pstmt.setInt(8, product.getStockMin());
            pstmt.setString(9, product.getBarcode());
            pstmt.setDouble(10, product.getTaxRate());
            pstmt.executeUpdate();
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, category_id = ?, supplier_id = ?, price_buy = ?, price_sell = ?, stock_current = ?, stock_min = ?, barcode = ?, tax_rate = ? WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setInt(4, product.getSupplierId());
            pstmt.setDouble(5, product.getPriceBuy());
            pstmt.setDouble(6, product.getPriceSell());
            pstmt.setInt(7, product.getStockCurrent());
            pstmt.setInt(8, product.getStockMin());
            pstmt.setString(9, product.getBarcode());
            pstmt.setDouble(10, product.getTaxRate());
            pstmt.setInt(11, product.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Product findById(int id) {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding product: " + e.getMessage());
        }
        return null;
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id";
        
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all products: " + e.getMessage());
        }
        return products;
    }

    // Advanced Search (also searches by barcode)
    public List<Product> search(String query) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "WHERE p.name LIKE ? OR c.name LIKE ? OR p.barcode = ?";
        
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, query); // exact barcode match
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }
        return products;
    }

    /**
     * Find a product by its barcode (exact match).
     */
    public Product findByBarcode(String barcode) {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "WHERE p.barcode = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, barcode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding by barcode: " + e.getMessage());
        }
        return null;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("category_id"),
            rs.getInt("supplier_id"),
            rs.getDouble("price_buy"),
            rs.getDouble("price_sell"),
            rs.getInt("stock_current"),
            rs.getInt("stock_min")
        );
        p.setCategoryName(rs.getString("category_name"));
        p.setSupplierName(rs.getString("supplier_name"));
        try { p.setBarcode(rs.getString("barcode")); } catch (SQLException ignored) {}
        try { p.setTaxRate(rs.getDouble("tax_rate")); } catch (SQLException ignored) {}
        return p;
    }
}
