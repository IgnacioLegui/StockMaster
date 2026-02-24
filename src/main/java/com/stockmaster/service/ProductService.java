package com.stockmaster.service;

import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.AuditDAO;
import com.stockmaster.dao.PriceHistoryDAO;
import com.stockmaster.dao.ProductDAO;
import com.stockmaster.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for Product operations.
 * Wraps ProductDAO with audit logging and permission checks.
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final AuditDAO auditDAO = new AuditDAO();
    private final PriceHistoryDAO priceHistoryDAO = new PriceHistoryDAO();

    public List<Product> findAll() {
        return productDAO.findAll();
    }

    public List<Product> search(String query) {
        return productDAO.search(query);
    }

    public void insert(Product product) throws SQLException {
        productDAO.insert(product);
        audit("INSERT", "PRODUCT", 0, null, product.getName());
    }

    public void update(Product product) throws SQLException {
        // Log price changes before updating
        Product old = productDAO.findById(product.getId());
        if (old != null) {
            SessionManager session = SessionManager.getInstance();
            String user = session.isLoggedIn() ? session.getCurrentUsername() : "system";
            priceHistoryDAO.log(product.getId(), old.getPriceBuy(), product.getPriceBuy(),
                                old.getPriceSell(), product.getPriceSell(), user);
        }
        productDAO.update(product);
        audit("UPDATE", "PRODUCT", product.getId(), null, product.getName());
    }

    public void delete(int id) throws SQLException {
        productDAO.delete(id);
        audit("DELETE", "PRODUCT", id, null, null);
    }

    /**
     * Batch insert for CSV import. Returns count of successfully inserted products.
     */
    public int batchInsert(List<Product> products) {
        int success = 0;
        for (Product p : products) {
            try {
                productDAO.insert(p);
                success++;
            } catch (SQLException e) {
                System.err.println("Batch insert failed for: " + p.getName() + " - " + e.getMessage());
            }
        }
        if (success > 0) {
            audit("CSV_IMPORT", "PRODUCT", 0, null, success + " products imported");
        }
        return success;
    }

    private void audit(String action, String entity, int entityId, String oldVal, String newVal) {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            auditDAO.log(
                session.getCurrentUser().getId(),
                session.getCurrentUsername(),
                action, entity, entityId, oldVal, newVal
            );
        }
    }
}
