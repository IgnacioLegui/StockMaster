package com.stockmaster.service;

import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.AuditDAO;
import com.stockmaster.dao.CategoryDAO;
import com.stockmaster.model.Category;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for Category operations.
 * Wraps CategoryDAO with audit logging and admin-only permission checks.
 */
public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public List<Category> findAll() {
        return categoryDAO.findAll();
    }

    public Category findById(int id) {
        return categoryDAO.findById(id);
    }

    public void insert(Category category) throws SQLException {
        requireAdmin();
        categoryDAO.insert(category);
        audit("INSERT", "CATEGORY", 0, null, category.getName());
    }

    public void update(Category category) throws SQLException {
        requireAdmin();
        categoryDAO.update(category);
        audit("UPDATE", "CATEGORY", category.getId(), null, category.getName());
    }

    public void delete(int id) throws SQLException {
        requireAdmin();
        categoryDAO.delete(id);
        audit("DELETE", "CATEGORY", id, null, null);
    }

    private void requireAdmin() throws SecurityException {
        if (!SessionManager.getInstance().isAdmin()) {
            throw new SecurityException("Only administrators can manage categories.");
        }
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
