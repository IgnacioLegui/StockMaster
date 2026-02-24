package com.stockmaster.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * Database manager with connection pooling and WAL mode for LAN concurrency.
 * 
 * Features:
 * - Connection pool (configurable size) for multi-threaded access
 * - SQLite WAL mode enables concurrent reads while writing
 * - Busy timeout prevents "database is locked" errors on LAN
 * - Thread-safe connection acquisition and release
 */
public class DBManager {
    private static String DB_URL = "jdbc:sqlite:stockmaster.db";
    private static final int POOL_SIZE = 5;
    private static final int BUSY_TIMEOUT_MS = 30000; // 30s for LAN latency
    private static BlockingQueue<Connection> connectionPool;
    private static boolean initialized = false;

    static {
        try {
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = System.getProperty("user.home");
            }
            java.nio.file.Path dbFolder = java.nio.file.Paths.get(appData, "StockMaster", "database");
            java.nio.file.Files.createDirectories(dbFolder);
            DB_URL = "jdbc:sqlite:" + dbFolder.resolve("stockmaster.db").toString();
        } catch (Exception e) {
            System.err.println("Error creating database directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a connection from the pool. Caller MUST return it via releaseConnection().
     * For backward compatibility, single-use callers can still use try-with-resources
     * (the connection won't be returned to the pool, but a new one will be created).
     */
    public static synchronized Connection getConnection() throws SQLException {
        initPool();
        
        // Try to get from pool first
        Connection conn = connectionPool.poll();
        if (conn != null && !conn.isClosed()) {
            return conn;
        }
        
        // Pool empty or connection was stale — create new
        return createConnection();
    }

    /**
     * Return a connection to the pool for reuse.
     */
    public static void releaseConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed() && !conn.getAutoCommit()) {
                conn.setAutoCommit(true); // reset state
            }
            if (!conn.isClosed()) {
                if (!connectionPool.offer(conn)) {
                    // Pool is full, close the extra connection
                    conn.close();
                }
            }
        } catch (SQLException e) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Initialize database and set up connection pool.
     */
    public static void initializeDatabase() {
        try {
            initPool();
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                // Read schema.sql from resources
                InputStream is = DBManager.class.getResourceAsStream("/sql/schema.sql");
                if (is == null) {
                    System.err.println("Could not find schema.sql");
                    releaseConnection(conn);
                    return;
                }
                
                String sql = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining("\n"));
                
                for (String statement : sql.split(";")) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement);
                    }
                }
                
                System.out.println("Database initialized successfully.");
            }
            
            // Run migrations for existing databases
            runMigrations(conn);
            
            releaseConnection(conn);
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Run migrations to upgrade existing databases.
     * Each migration is idempotent — safe to run multiple times.
     */
    private static void runMigrations(Connection conn) {
        String[] migrations = {
            "ALTER TABLE products ADD COLUMN barcode TEXT",
            "ALTER TABLE products ADD COLUMN tax_rate REAL DEFAULT 21.0",
            // Stock Movements
            "CREATE TABLE IF NOT EXISTS stock_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, type TEXT NOT NULL, quantity INTEGER NOT NULL, reason TEXT, user_id INTEGER, timestamp TEXT NOT NULL, FOREIGN KEY (product_id) REFERENCES products(id))",
            "CREATE INDEX IF NOT EXISTS idx_movements_product ON stock_movements(product_id)",
            "CREATE INDEX IF NOT EXISTS idx_movements_timestamp ON stock_movements(timestamp)",
            // Sales
            "CREATE TABLE IF NOT EXISTS sales (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_date TEXT NOT NULL, user_id INTEGER, subtotal REAL DEFAULT 0, tax_amount REAL DEFAULT 0, total REAL DEFAULT 0, payment_method TEXT DEFAULT 'CASH', status TEXT DEFAULT 'COMPLETED', FOREIGN KEY (user_id) REFERENCES users(id))",
            "CREATE TABLE IF NOT EXISTS sale_items (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id INTEGER NOT NULL, product_id INTEGER NOT NULL, product_name TEXT, quantity INTEGER NOT NULL, unit_price REAL NOT NULL, tax_rate REAL DEFAULT 21.0, line_total REAL NOT NULL, FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE, FOREIGN KEY (product_id) REFERENCES products(id))",
            "CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date)",
            "CREATE INDEX IF NOT EXISTS idx_sale_items_sale ON sale_items(sale_id)",
            // Purchase Orders
            "CREATE TABLE IF NOT EXISTS purchase_orders (id INTEGER PRIMARY KEY AUTOINCREMENT, supplier_id INTEGER NOT NULL, order_date TEXT NOT NULL, status TEXT DEFAULT 'PENDING', total REAL DEFAULT 0, notes TEXT, FOREIGN KEY (supplier_id) REFERENCES suppliers(id))",
            "CREATE TABLE IF NOT EXISTS purchase_order_items (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, product_id INTEGER NOT NULL, quantity INTEGER NOT NULL, unit_cost REAL NOT NULL, line_total REAL NOT NULL, FOREIGN KEY (order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE, FOREIGN KEY (product_id) REFERENCES products(id))",
            "CREATE INDEX IF NOT EXISTS idx_po_supplier ON purchase_orders(supplier_id)",
            "CREATE INDEX IF NOT EXISTS idx_po_items_order ON purchase_order_items(order_id)",
            // Price History
            "CREATE TABLE IF NOT EXISTS price_history (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, old_buy REAL, new_buy REAL, old_sell REAL, new_sell REAL, changed_by TEXT, timestamp TEXT NOT NULL, FOREIGN KEY (product_id) REFERENCES products(id))",
            "CREATE INDEX IF NOT EXISTS idx_price_history_product ON price_history(product_id)"
        };
        
        for (String sql : migrations) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                // "duplicate column name" or "table already exists" = already applied, ignore
                if (!e.getMessage().contains("duplicate column") && 
                    !e.getMessage().contains("already exists")) {
                    System.err.println("Migration warning: " + e.getMessage());
                }
            }
        }
    }

    // ─── Internal ───

    private static synchronized void initPool() throws SQLException {
        if (initialized) return;
        
        connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        
        // Pre-fill pool
        for (int i = 0; i < POOL_SIZE; i++) {
            connectionPool.offer(createConnection());
        }
        
        initialized = true;
        System.out.println("Connection pool initialized (size=" + POOL_SIZE + ", WAL mode)");
    }

    private static Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        
        try (Statement stmt = conn.createStatement()) {
            // Enable WAL mode — allows concurrent reads while writing
            stmt.execute("PRAGMA journal_mode=WAL");
            
            // Set busy timeout — prevents "database is locked" errors on LAN
            stmt.execute("PRAGMA busy_timeout=" + BUSY_TIMEOUT_MS);
            
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys=ON");
            
            // Synchronous NORMAL — good balance of safety and performance
            stmt.execute("PRAGMA synchronous=NORMAL");
        }
        
        return conn;
    }

    /**
     * Shutdown the connection pool gracefully.
     */
    public static synchronized void shutdown() {
        if (connectionPool == null) return;
        for (Connection conn : connectionPool) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
        connectionPool.clear();
        initialized = false;
        System.out.println("Connection pool shut down.");
    }
}
