package com.smartlibrary.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final DatabaseConfig CONFIG = DatabaseConfig.load();

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONFIG.url(), CONFIG.user(), CONFIG.password());
    }

    public static boolean testConnection() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    public static DatabaseConfig config() {
        return CONFIG;
    }
}
