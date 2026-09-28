package com.inventory.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {
    private static final Path DATABASE_DIRECTORY = Paths.get("data");
    private static final String DATABASE_URL = "jdbc:sqlite:data/inventory.db";
    private static final Path SCHEMA_FILE = Paths.get("schema.sql");

    private DatabaseConnection() {
        // Utility class: instances are not needed.
    }

    public static Connection getConnection() throws SQLException {
        createDatabaseDirectory();

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

    public static void initializeDatabase() throws SQLException {
        String schema = readSchemaFile();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String sqlStatement : schema.split(";")) {
                if (!sqlStatement.trim().isEmpty()) {
                    statement.execute(sqlStatement);
                }
            }
        }
    }

    private static void createDatabaseDirectory() throws SQLException {
        try {
            Files.createDirectories(DATABASE_DIRECTORY);
        } catch (IOException e) {
            throw new SQLException("Could not create the data directory.", e);
        }
    }

    private static String readSchemaFile() throws SQLException {
        try {
            return Files.readString(SCHEMA_FILE);
        } catch (IOException e) {
            throw new SQLException("Could not read schema.sql.", e);
        }
    }
}
