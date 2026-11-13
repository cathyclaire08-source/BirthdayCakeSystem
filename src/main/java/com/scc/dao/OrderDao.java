



package com.scc.dao;

import com.scc.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    private final ActivityLogDao activityLogDao;

    public OrderDao() {
        this.activityLogDao = new ActivityLogDao();
    }

    // ==========================================
    // CREATE CUSTOMER ORDER
    // ==========================================

    public int createOrder(
            int userId,
            int cakeId,
            int quantity,
            String celebrationDate,
            String pickupOrDelivery,
            String address,
            double totalAmount) {

        if (userId <= 0 || cakeId <= 0 || quantity <= 0
                || totalAmount <= 0
                || celebrationDate == null || celebrationDate.isBlank()
                || pickupOrDelivery == null || pickupOrDelivery.isBlank()) {

            System.out.println("Invalid order information.");
            return -1;
        }

        String sql = """
                INSERT INTO orders
                    (user_id, cake_id, quantity, celebration_date,
                     pickup_or_delivery, address, total_amount, order_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, cakeId);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, celebrationDate);
            pstmt.setString(5, pickupOrDelivery);
            pstmt.setString(6, address);
            pstmt.setDouble(7, totalAmount);

            if (pstmt.executeUpdate() != 1) {
                return -1;
            }

            int orderId;

            try (PreparedStatement idStatement =
                         conn.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet rs = idStatement.executeQuery()) {

                if (!rs.next()) {
                    return -1;
                }

                orderId = rs.getInt(1);
            }

            activityLogDao.logAction(
                    userId,
                    "CAKE_ORDER_CREATED",
                    "Customer created cake order. Order ID: " + orderId
                            + ", Cake ID: " + cakeId
                            + ", Quantity: " + quantity
            );

            System.out.println("Cake pre-order created successfully!");
            System.out.println("Order ID: " + orderId);

            return orderId;

        } catch (SQLException e) {
            System.err.println("Unable to create cake order: " + e.getMessage());
            return -1;
        }
    }

    // ==========================================
    // VIEW CUSTOMER'S OWN ORDERS
    // ==========================================

    public List<Order> getOrdersByCustomerId(int userId) {

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
                WHERE o.user_id = ?
                ORDER BY o.order_date DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    orders.add(new Order(
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
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Unable to retrieve customer orders: "
                    + e.getMessage());
        }

        return orders;
    }

    // ==========================================
    // UPDATE CUSTOMER'S OWN PENDING ORDER
    // ==========================================

    public boolean updateCustomerOrder(
            int orderId,
            int userId,
            int cakeId,
            int quantity,
            String celebrationDate,
            String pickupOrDelivery,
            String address) {

        if (orderId <= 0 || userId <= 0 || cakeId <= 0 || quantity <= 0
                || celebrationDate == null || celebrationDate.isBlank()
                || pickupOrDelivery == null || pickupOrDelivery.isBlank()) {

            System.out.println("Invalid order information.");
            return false;
        }

        String priceSql = """
                SELECT price
                FROM cakes
                WHERE cake_id = ?
                  AND availability_status = 'AVAILABLE'
                """;

        String updateSql = """
                UPDATE orders
                SET cake_id = ?,
                    quantity = ?,
                    celebration_date = ?,
                    pickup_or_delivery = ?,
                    address = ?,
                    total_amount = ?
                WHERE order_id = ?
                  AND user_id = ?
                  AND order_status = 'PENDING'
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            double cakePrice;

            try (PreparedStatement priceStmt =
                         conn.prepareStatement(priceSql)) {

                priceStmt.setInt(1, cakeId);

                try (ResultSet rs = priceStmt.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println(
                                "Cake not found or currently unavailable."
                        );
                        return false;
                    }

                    cakePrice = rs.getDouble("price");
                }
            }

            double totalAmount = cakePrice * quantity;

            try (PreparedStatement updateStmt =
                         conn.prepareStatement(updateSql)) {

                updateStmt.setInt(1, cakeId);
                updateStmt.setInt(2, quantity);
                updateStmt.setString(3, celebrationDate);
                updateStmt.setString(4, pickupOrDelivery);
                updateStmt.setString(5, address);
                updateStmt.setDouble(6, totalAmount);
                updateStmt.setInt(7, orderId);
                updateStmt.setInt(8, userId);

                if (updateStmt.executeUpdate() != 1) {
                    System.out.println(
                            "Order cannot be updated. Check ownership "
                                    + "or order status."
                    );
                    return false;
                }
            }

            activityLogDao.logAction(
                    userId,
                    "CUSTOMER_ORDER_UPDATED",
                    "Customer updated Order ID: " + orderId
            );

            System.out.println("Order updated successfully!");
            System.out.println("New total amount: " + totalAmount);

            return true;

        } catch (SQLException e) {
            System.err.println("Unable to update customer order: "
                    + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // CANCEL CUSTOMER'S OWN PENDING ORDER
    // ==========================================

    public boolean cancelCustomerOrder(int orderId, int userId) {

        if (orderId <= 0 || userId <= 0) {
            System.out.println("Invalid order information.");
            return false;
        }

        String sql = """
                UPDATE orders
                SET order_status = 'CANCELLED'
                WHERE order_id = ?
                  AND user_id = ?
                  AND order_status = 'PENDING'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            pstmt.setInt(2, userId);

            if (pstmt.executeUpdate() != 1) {
                System.out.println(
                        "Order cannot be cancelled. It may not belong "
                                + "to you or is no longer PENDING."
                );
                return false;
            }

            activityLogDao.logAction(
                    userId,
                    "CUSTOMER_ORDER_CANCELLED",
                    "Customer cancelled Order ID: " + orderId
            );

            System.out.println("Order cancelled successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println("Unable to cancel customer order: "
                    + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // GET CUSTOMER-FRIENDLY ORDER STATUS
    // ==========================================

    private String getOrderStatusDescription(
            String status,
            String pickupOrDelivery) {

        if (status == null) {
            return "Status unavailable.";
        }

        return switch (status.trim().toUpperCase()) {

            case "PENDING" ->
                    "Your order has been submitted and is awaiting confirmation.";

            case "CONFIRMED" ->
                    "Your order has been confirmed.";

            case "PREPARING" ->
                    "Your cake is currently being prepared.";

            case "READY" -> {
                if (pickupOrDelivery != null
                        && pickupOrDelivery.toUpperCase().contains("PICKUP")) {
                    yield "Your cake is ready for pickup.";
                }
                yield "Your cake is ready for delivery.";
            }

            case "READY FOR PICKUP" ->
                    "Your cake is ready. You may now pick it up.";

            case "READY FOR DELIVERY" ->
                    "Your cake is ready and waiting for delivery.";

            case "OUT FOR DELIVERY" ->
                    "Your order is now out for delivery. The Order Clerk is delivering your cake.";

            case "DELIVERED" ->
                    "Your cake has been delivered.";

            case "COMPLETED" ->
                    "Your order has been completed.";

            case "CANCELLED" ->
                    "This order has been cancelled.";

            default -> "Current order status: " + status;
        };
    }

    // ==========================================
    // VIEW CUSTOMER ORDER AND PAYMENT STATUS
    // ==========================================

    public boolean viewOrderAndPaymentStatus(int orderId, int userId) {

        String sql = """
                SELECT o.order_id,
                       c.cake_name,
                       o.quantity,
                       o.celebration_date,
                       o.pickup_or_delivery,
                       o.total_amount,
                       o.order_status,
                       COALESCE(p.payment_status, 'NO PAYMENT RECORD')
                           AS payment_status,
                       p.payment_method,
                       p.amount AS payment_amount
                FROM orders o
                JOIN cakes c ON o.cake_id = c.cake_id
                LEFT JOIN payments p ON o.order_id = p.order_id
                WHERE o.order_id = ?
                  AND o.user_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) {
                    System.out.println(
                            "Order not found or does not belong to you."
                    );
                    return false;
                }

                String status = rs.getString("order_status");
                String deliveryType = rs.getString("pickup_or_delivery");

                System.out.println("\n========== ORDER TRACKING ==========");
                System.out.println("Order ID: " + rs.getInt("order_id"));
                System.out.println("Cake: " + rs.getString("cake_name"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println(
                        "Celebration Date: "
                                + rs.getString("celebration_date")
                );
                System.out.println("Order Type: " + deliveryType);
                System.out.println(
                        "Total Amount: "
                                + rs.getDouble("total_amount")
                );

                System.out.println("\n---------- DELIVERY PROGRESS ----------");
                System.out.println("Current Status: " + status);
                System.out.println(
                        "Status Description: "
                                + getOrderStatusDescription(status, deliveryType)
                );

                System.out.println("\n---------- PAYMENT INFORMATION ----------");
                System.out.println(
                        "Payment Status: " + rs.getString("payment_status")
                );
                System.out.println(
                        "Payment Method: " + rs.getString("payment_method")
                );

                System.out.println("========================================");

                return true;
            }

        } catch (SQLException e) {
            System.err.println("Unable to track order: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // VIEW BAKER PREPARATION ORDERS
    // ==========================================

    public List<Order> getOrdersForPreparation() {

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
                WHERE UPPER(o.order_status)
                    IN ('PENDING', 'CONFIRMED', 'PREPARING')
                ORDER BY o.celebration_date, o.order_id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                orders.add(new Order(
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
                ));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve preparation orders: "
                            + e.getMessage()
            );
        }

        return orders;
    }

    // ==========================================
    // UPDATE ORDER STATUS
    // ==========================================

    public boolean updateOrderStatus(
            int orderId,
            String newStatus,
            int updatedByUserId) {

        if (orderId <= 0 || updatedByUserId <= 0
                || newStatus == null || newStatus.isBlank()) {

            System.out.println("Invalid order status information.");
            return false;
        }

        String updatedStatus = newStatus.trim().toUpperCase();

        List<String> validStatuses = List.of(
                "PENDING",
                "CONFIRMED",
                "PREPARING",
                "READY",
                "READY FOR PICKUP",
                "READY FOR DELIVERY",
                "OUT FOR DELIVERY",
                "DELIVERED",
                "COMPLETED",
                "CANCELLED"
        );

        if (!validStatuses.contains(updatedStatus)) {
            System.out.println("Invalid order status.");
            return false;
        }

        String oldStatus = null;

        String selectSql = """
                SELECT order_status
                FROM orders
                WHERE order_id = ?
                """;

        String updateSql = """
                UPDATE orders
                SET order_status = ?
                WHERE order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement selectStmt =
                         conn.prepareStatement(selectSql)) {

                selectStmt.setInt(1, orderId);

                try (ResultSet rs = selectStmt.executeQuery()) {

                    if (rs.next()) {
                        oldStatus = rs.getString("order_status");
                    } else {
                        System.out.println("Order not found.");
                        return false;
                    }
                }
            }

            if (oldStatus.equalsIgnoreCase(updatedStatus)) {
                System.out.println("Order already has this status.");
                return false;
            }

            try (PreparedStatement updateStmt =
                         conn.prepareStatement(updateSql)) {

                updateStmt.setString(1, updatedStatus);
                updateStmt.setInt(2, orderId);

                if (updateStmt.executeUpdate() != 1) {
                    return false;
                }
            }

            activityLogDao.logAction(
                    updatedByUserId,
                    "ORDER_STATUS_UPDATED",
                    "Order ID: " + orderId
                            + " | Status changed from "
                            + oldStatus + " to " + updatedStatus
            );

            System.out.println("Order status updated successfully!");
            System.out.println("Previous Status: " + oldStatus);
            System.out.println("New Status: " + updatedStatus);

            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Unable to update order status: " + e.getMessage()
            );
            return false;
        }
    }
}




