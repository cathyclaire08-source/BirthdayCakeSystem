
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        String[] createTableStatements = {
                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                contact_number TEXT NOT NULL,
                account_status TEXT NOT NULL DEFAULT 'PENDING'
            )
            """,

                """
            CREATE TABLE IF NOT EXISTS cakes (
                cake_id INTEGER PRIMARY KEY AUTOINCREMENT,
                cake_name TEXT NOT NULL,
                flavor TEXT NOT NULL,
                size TEXT NOT NULL,
                price REAL NOT NULL,
                availability_status TEXT NOT NULL DEFAULT 'AVAILABLE'
            )
            """,

                """
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
            """,

                """
            CREATE TABLE IF NOT EXISTS payments (
                payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL UNIQUE,
                amount REAL NOT NULL,
                payment_method TEXT NOT NULL,
                payment_status TEXT NOT NULL DEFAULT 'PENDING',
                payment_date TEXT,
                FOREIGN KEY (order_id) REFERENCES orders(order_id)
            )
            """,

                """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                description TEXT,
                log_date_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """
        };

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : createTableStatements) {
                statement.executeUpdate(sql);
            }

            System.out.println(
                    "Database initialized successfully. All required tables are ready."
            );

        } catch (SQLException e) {
            System.err.println(
                    "Database initialization failed: " + e.getMessage()
            );
            throw new RuntimeException(
                    "Unable to initialize the database.", e
            );
        }
    }
}
