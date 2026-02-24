package com.stockmaster.service;

import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.AuditDAO;
import com.stockmaster.dao.SupplierDAO;
import com.stockmaster.model.Supplier;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for Supplier operations.
 * Wraps SupplierDAO with audit logging and admin-only permission checks.
 */
public class SupplierService {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    public List<Supplier> findAll() {
        return supplierDAO.findAll();
    }

    public Supplier findById(int id) {
        return supplierDAO.findById(id);
    }

    public void insert(Supplier supplier) throws SQLException {
        requireAdmin();
        supplierDAO.insert(supplier);
        audit("INSERT", "SUPPLIER", 0, null, supplier.getName());
    }

    public void update(Supplier supplier) throws SQLException {
        requireAdmin();
        supplierDAO.update(supplier);
        audit("UPDATE", "SUPPLIER", supplier.getId(), null, supplier.getName());
    }

    public void delete(int id) throws SQLException {
        requireAdmin();
        supplierDAO.delete(id);
        audit("DELETE", "SUPPLIER", id, null, null);
    }

    private void requireAdmin() throws SecurityException {
        if (!SessionManager.getInstance().isAdmin()) {
            throw new SecurityException("Only administrators can manage suppliers.");
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
