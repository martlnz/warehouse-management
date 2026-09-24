package com.team.warehouse.warehousemanagementoop.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class
                .getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file application.properties trong resources");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc application.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, username, password);
    }
}

