

package com.scc.dao;

import com.scc.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderClerkDao {

    // ==========================================
    // VIEW ALL CUSTOMER ORDERS
    // ==========================================

    public List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT o.order_id,
                       u.full_name AS customer_name,
                       c.cake_name,
                       o.quantity,
                       o.celebration_date,
                       o.pickup_or_delivery,
                       o.address,
                       o.total_amount,
                       o.order_status,
                       o.order_date
                FROM orders o
                JOIN users u ON o.user_id = u.id
                JOIN cakes c ON o.cake_id = c.cake_id
                ORDER BY o.order_date DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve all orders: " + e.getMessage()
            );
        }

        return orders;
    }

    // ==========================================
    // GET PAYMENT STATUS FOR AN ORDER
    // ==========================================

    public String getPaymentStatusByOrderId(int orderId) {

        if (orderId <= 0) {
            return "NO PAYMENT";
        }

        String sql = """
                SELECT payment_status
                FROM payments
                WHERE order_id = ?
                ORDER BY payment_id DESC
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    String status = rs.getString("payment_status");

                    return status == null || status.isBlank()
                            ? "UNKNOWN" : status;
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve payment status: " + e.getMessage()
            );

            return "ERROR";
        }

        return "NO PAYMENT";
    }

    // ==========================================
    // SEARCH CUSTOMER ORDERS
    // ==========================================

    public List<Order> searchOrders(String keyword) {

        List<Order> orders = new ArrayList<>();

        if (keyword == null || keyword.isBlank()) {
            return orders;
        }

        String sql = """
                SELECT o.order_id,
                       u.full_name AS customer_name,
                       c.cake_name,
                       o.quantity,
                       o.celebration_date,
                       o.pickup_or_delivery,
                       o.address,
                       o.total_amount,
                       o.order_status,
                       o.order_date
                FROM orders o
                JOIN users u ON o.user_id = u.id
                JOIN cakes c ON o.cake_id = c.cake_id
                WHERE CAST(o.order_id AS TEXT) LIKE ?
                   OR u.full_name LIKE ?
                   OR c.cake_name LIKE ?
                   OR o.order_status LIKE ?
                ORDER BY o.order_date DESC
                """;

        String searchValue = "%" + keyword.trim() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchValue);
            pstmt.setString(2, searchValue);
            pstmt.setString(3, searchValue);
            pstmt.setString(4, searchValue);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to search orders: " + e.getMessage()
            );
        }

        return orders;
    }

    // ==========================================
    // DAILY CAKE SALES REPORT
    // ==========================================

    public void printDailySalesReport() {

        String sql = """
                SELECT o.order_id,
                       u.full_name AS customer_name,
                       c.cake_name,
                       o.quantity,
                       o.total_amount,
                       o.order_status,
                       COALESCE(
                           (
                               SELECT p.payment_status
                               FROM payments p
                               WHERE p.order_id = o.order_id
                               ORDER BY p.payment_id DESC
                               LIMIT 1
                           ),
                           'NO PAYMENT'
                       ) AS payment_status,
                       o.order_date
                FROM orders o
                JOIN users u ON o.user_id = u.id
                JOIN cakes c ON o.cake_id = c.cake_id
                WHERE date(o.order_date) = date('now', 'localtime')
                ORDER BY o.order_date
                """;

        double totalSales = 0;
        int totalOrders = 0;
        int paidOrders = 0;

        System.out.println("\n==============================================================");
        System.out.println("                 DAILY CAKE SALES REPORT");
        System.out.println("==============================================================");

        System.out.printf(
                "%-8s %-18s %-15s %-5s %-12s %-12s%n",
                "ORDER", "CUSTOMER", "CAKE", "QTY",
                "TOTAL", "PAYMENT"
        );

        System.out.println("--------------------------------------------------------------");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                totalOrders++;

                double amount = rs.getDouble("total_amount");
                String paymentStatus = rs.getString("payment_status");

                totalSales += amount;

                if ("PAID".equalsIgnoreCase(paymentStatus)) {
                    paidOrders++;
                }

                System.out.printf(
                        "%-8d %-18.18s %-15.15s %-5d ₱%-11.2f %-12s%n",
                        rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getString("cake_name"),
                        rs.getInt("quantity"),
                        amount,
                        paymentStatus
                );
            }

            System.out.println("--------------------------------------------------------------");

            System.out.println("Total Orders: " + totalOrders);

            System.out.printf(
                    "Total Order Value: ₱%.2f%n",
                    totalSales
            );

            System.out.println("Paid Orders: " + paidOrders);

            System.out.println(
                    "Unpaid / Other Orders: " + (totalOrders - paidOrders)
            );

            System.out.println("==============================================================");

        } catch (SQLException e) {
            System.err.println(
                    "Unable to generate daily sales report: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // MAP DATABASE RESULT TO ORDER MODEL
    // ==========================================

    private Order mapOrder(ResultSet rs) throws SQLException {

        return new Order(
                rs.getInt("order_id"),
                rs.getString("customer_name"),
                rs.getString("cake_name"),
                rs.getInt("quantity"),
                rs.getString("celebration_date"),
                rs.getString("pickup_or_delivery"),
                rs.getString("address"),
                rs.getDouble("total_amount"),
                rs.getString("order_status"),
                rs.getString("order_date")
        );
    }
}

