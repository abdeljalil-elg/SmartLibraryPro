package com.smartlibrary.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String CONFIG_FILE = "/database.properties";

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Defaults and environment variables below keep the application startable.
        }

        String url = readValue("SMARTLIB_DB_URL", properties.getProperty("db.url"));
        String user = readValue("SMARTLIB_DB_USER", properties.getProperty("db.user", "root"));
        String password = readValue("SMARTLIB_DB_PASSWORD", properties.getProperty("db.password", ""));
        return new DatabaseConfig(url, user, password);
    }

    private static String readValue(String environmentKey, String fallback) {
        String value = System.getenv(environmentKey);
        return value == null || value.isBlank() ? fallback : value;
    }

    public String url() {
        return url;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }
}
