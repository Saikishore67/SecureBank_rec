
package com.securebank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public final class DBConnection {

    private static final String CONFIG_FILE = "/db.properties";

    private DBConnection() {
        // Prevent object creation
    }

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();

        try (InputStream input = DBConnection.class
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new SQLException(
                        "db.properties not found in src/main/resources."
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new SQLException(
                    "Failed to load database configuration.", e
            );
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || username == null || password == null) {
            throw new SQLException(
                    "Database configuration is incomplete."
            );
        }

        return DriverManager.getConnection(url, username, password);
    }
}
