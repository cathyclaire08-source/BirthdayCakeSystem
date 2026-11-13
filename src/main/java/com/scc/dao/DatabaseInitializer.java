

package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {

    public static void initializeDatabase() {

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            // USERS TABLE
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL,
                    contact_number TEXT NOT NULL,
                    account_status TEXT NOT NULL DEFAULT 'PENDING'
                )
            """);

            // CAKES TABLE
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cakes (
                    cake_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cake_name TEXT NOT NULL,
                    flavor TEXT NOT NULL,
                    size TEXT NOT NULL,
                    price REAL NOT NULL,
                    availability_status TEXT NOT NULL DEFAULT 'AVAILABLE'
                )
            """);

            // ORDERS TABLE
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS orders (
                    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    cake_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    celebration_date TEXT NOT NULL,
                    pickup_or_delivery TEXT NOT NULL,
                    address TEXT,
                    total_amount REAL NOT NULL,
                    order_status TEXT NOT NULL DEFAULT 'PENDING',
                    order_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (cake_id) REFERENCES cakes(cake_id)
                )
            """);

            // PAYMENTS TABLE
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL UNIQUE,
                    amount REAL NOT NULL,
                    payment_method TEXT NOT NULL,
                    payment_status TEXT NOT NULL DEFAULT 'PENDING',
                    payment_date TEXT,
                    FOREIGN KEY (order_id) REFERENCES orders(order_id)
                )
            """);

            // ACTIVITY LOGS TABLE
            createOrMigrateActivityLogs(connection, statement);

            // INDEXES
            statement.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_activity_logs_user_id
                ON activity_logs(user_id)
            """);

            statement.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_activity_logs_timestamp
                ON activity_logs(timestamp)
            """);

            System.out.println("Database initialized successfully.");
            System.out.println("All required tables and indexes are ready.");
            System.out.println("Activity Logs system is ready.");

        } catch (SQLException e) {

            System.err.println(
                    "Database initialization failed: " + e.getMessage()
            );

            throw new RuntimeException(
                    "Unable to initialize the database.", e
            );
        }
    }

    private static void createOrMigrateActivityLogs(
            Connection connection,
            Statement statement
    ) throws SQLException {

        boolean tableExists = false;

        try (ResultSet rs = statement.executeQuery("""
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                AND name = 'activity_logs'
                """)) {

            tableExists = rs.next();
        }

        if (!tableExists) {
            createActivityLogsTable(statement);
            return;
        }

        boolean oldSchema = hasColumn(
                connection, "activity_logs", "log_id"
        );

        boolean newSchema = hasColumn(
                connection, "activity_logs", "id"
        );

        if (newSchema
                && hasColumn(connection, "activity_logs", "action_type")
                && hasColumn(connection, "activity_logs", "timestamp")) {
            return;
        }

        if (!oldSchema) {
            throw new SQLException(
                    "Unrecognized activity_logs schema. "
                            + "Existing data was not modified."
            );
        }

        connection.setAutoCommit(false);

        try {
            statement.executeUpdate(
                    "DROP TABLE IF EXISTS activity_logs_old"
            );

            statement.executeUpdate("""
                    ALTER TABLE activity_logs
                    RENAME TO activity_logs_old
                    """);

            createActivityLogsTable(statement);

            statement.executeUpdate("""
                    INSERT INTO activity_logs
                    (id, user_id, action_type, description, timestamp)
                    SELECT
                        log_id,
                        user_id,
                        action,
                        description,
                        log_date_time
                    FROM activity_logs_old
                    """);

            statement.executeUpdate(
                    "DROP TABLE activity_logs_old"
            );

            connection.commit();

        } catch (SQLException e) {

            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void createActivityLogsTable(
            Statement statement
    ) throws SQLException {

        statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action_type TEXT NOT NULL,
                description TEXT,
                timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE CASCADE
            )
        """);
    }

    private static boolean hasColumn(
            Connection connection,
            String tableName,
            String columnName
    ) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {

                if (columnName.equalsIgnoreCase(
                        rs.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }
}

