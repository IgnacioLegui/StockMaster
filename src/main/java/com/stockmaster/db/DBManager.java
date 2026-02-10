package com.stockmaster.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DBManager {
    private static String DB_URL = "jdbc:sqlite:stockmaster.db";
    private static Connection connection;

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

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Read schema.sql from resources
            InputStream is = DBManager.class.getResourceAsStream("/sql/schema.sql");
            if (is == null) {
                System.err.println("Could not find schema.sql");
                return;
            }
            
            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            
            // Allow multiple statements execution if supported or split by ;
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
            
            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}
