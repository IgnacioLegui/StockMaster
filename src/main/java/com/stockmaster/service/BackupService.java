package com.stockmaster.service;

import com.stockmaster.db.DBManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles backup and restore of the SQLite database file.
 */
public class BackupService {

    /**
     * Copy the current database file to the target location.
     */
    public static void backup(File targetFile) throws IOException {
        Path dbPath = getDbPath();
        if (!Files.exists(dbPath)) {
            throw new IOException("Database file not found: " + dbPath);
        }
        Files.copy(dbPath, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Also copy WAL and SHM files if they exist
        Path walPath = Paths.get(dbPath + "-wal");
        Path shmPath = Paths.get(dbPath + "-shm");
        if (Files.exists(walPath)) {
            Files.copy(walPath, Paths.get(targetFile.getPath() + "-wal"), StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(shmPath)) {
            Files.copy(shmPath, Paths.get(targetFile.getPath() + "-shm"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Restore a database from a backup file.
     * The application should be restarted after calling this.
     */
    public static void restore(File sourceFile) throws IOException {
        Path dbPath = getDbPath();

        // Shutdown connection pool first
        DBManager.shutdown();

        // Replace database file
        Files.copy(sourceFile.toPath(), dbPath, StandardCopyOption.REPLACE_EXISTING);

        // Also copy WAL and SHM if they exist in backup
        Path walSource = Paths.get(sourceFile.getPath() + "-wal");
        Path shmSource = Paths.get(sourceFile.getPath() + "-shm");
        if (Files.exists(walSource)) {
            Files.copy(walSource, Paths.get(dbPath + "-wal"), StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(shmSource)) {
            Files.copy(shmSource, Paths.get(dbPath + "-shm"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Generate a default backup filename with timestamp.
     */
    public static String getDefaultBackupName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "stockmaster_backup_" + timestamp + ".db";
    }

    private static Path getDbPath() {
        String appData = System.getenv("APPDATA");
        if (appData == null) {
            appData = System.getProperty("user.home");
        }
        return Paths.get(appData, "StockMaster", "database", "stockmaster.db");
    }
}
