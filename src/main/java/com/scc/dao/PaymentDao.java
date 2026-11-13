


package com.scc.dao;

import com.scc.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    private final ActivityLogDao activityLogDao;

    public PaymentDao() {
        this.activityLogDao = new ActivityLogDao();
    }

    // ==========================================
    // VALIDATE PAYMENT METHOD
    // ==========================================

    private boolean isValidPaymentMethod(String paymentMethod) {

        if (paymentMethod == null || paymentMethod.isBlank()) {
            return false;
        }

        String method = paymentMethod.trim().toUpperCase();

        return method.equals("CASH ON PICKUP")
                || method.equals("GCASH")
                || method.equals("BANK TRANSFER");
    }

    // ==========================================
    // CUSTOMER: CREATE PAYMENT RECORD
    // ==========================================

    public boolean createPayment(
            int orderId,
            double amount,
            String paymentMethod,
            int userId) {

        if (orderId <= 0 || amount <= 0 || userId <= 0
                || !isValidPaymentMethod(paymentMethod)) {

            System.out.println("Invalid payment information.");
            return false;
        }

        String method = paymentMethod.trim().toUpperCase();

        String sql = """
                INSERT INTO payments
                    (order_id, amount, payment_method, payment_status)
                SELECT o.order_id, ?, ?, 'PENDING'
                FROM orders o
                WHERE o.order_id = ?
                  AND o.user_id = ?
                  AND o.order_status = 'PENDING'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setString(2, method);
            pstmt.setInt(3, orderId);
            pstmt.setInt(4, userId);

            if (pstmt.executeUpdate() != 1) {
                System.out.println(
                        "Unable to create payment. Check order ownership "
                                + "or order status."
                );
                return false;
            }

            activityLogDao.logAction(
                    userId,
                    "PAYMENT_CREATED",
                    "Payment record created for Order ID: " + orderId
                            + ", Amount: " + amount
                            + ", Method: " + method
            );

            System.out.println("Payment record created successfully.");
            System.out.println("Payment Status: PENDING");

            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Unable to create payment: " + e.getMessage()
            );
            return false;
        }
    }

    // ==========================================
    // ORDER CLERK: RECORD PAYMENT
    // ==========================================

    public boolean createPaymentForOrderClerk(
            int orderId,
            double amount,
            String paymentMethod,
            int clerkUserId) {

        if (orderId <= 0 || amount <= 0 || clerkUserId <= 0
                || !Double.isFinite(amount)
                || !isValidPaymentMethod(paymentMethod)) {

            System.out.println("Invalid payment information.");
            return false;
        }

        String method = paymentMethod.trim().toUpperCase();

        String sql = """
                INSERT INTO payments
                    (order_id, amount, payment_method, payment_status)
                SELECT o.order_id, ?, ?, 'PENDING'
                FROM orders o
                WHERE o.order_id = ?
                  AND UPPER(o.order_status) <> 'CANCELLED'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM payments p
                      WHERE p.order_id = o.order_id
                  )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setString(2, method);
            pstmt.setInt(3, orderId);

            if (pstmt.executeUpdate() != 1) {
                System.out.println(
                        "Payment was not recorded. Check whether the order "
                                + "exists, is cancelled, or already has a "
                                + "payment record."
                );
                return false;
            }

            activityLogDao.logAction(
                    clerkUserId,
                    "PAYMENT_CREATED",
                    "Order Clerk ID: " + clerkUserId
                            + " recorded payment for Order ID: " + orderId
                            + ", Amount: " + amount
                            + ", Method: " + method
            );

            System.out.println("Payment recorded successfully.");
            System.out.println("Payment Status: PENDING");

            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Unable to record payment: " + e.getMessage()
            );
            return false;
        }
    }

    // ==========================================
    // GET PAYMENT BY ORDER ID
    // ==========================================

    public Payment getPaymentByOrderId(int orderId) {

        if (orderId <= 0) {
            return null;
        }

        String sql = """
                SELECT payment_id, order_id, amount, payment_method,
                       payment_status, payment_date
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
                    return new Payment(
                            rs.getInt("payment_id"),
                            rs.getInt("order_id"),
                            rs.getDouble("amount"),
                            rs.getString("payment_method"),
                            rs.getString("payment_status"),
                            rs.getString("payment_date")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve payment: " + e.getMessage()
            );
        }

        return null;
    }

    // ==========================================
    // UPDATE PAYMENT STATUS
    // ==========================================

    public boolean updatePaymentStatus(
            int orderId,
            String newStatus,
            int updatedByUserId) {

        if (orderId <= 0 || updatedByUserId <= 0
                || newStatus == null || newStatus.isBlank()) {

            System.out.println("Invalid payment status information.");
            return false;
        }

        String updatedStatus = newStatus.trim().toUpperCase();

        if (!updatedStatus.equals("PAID")
                && !updatedStatus.equals("REJECTED")
                && !updatedStatus.equals("PENDING")) {

            System.out.println("Invalid payment status.");
            return false;
        }

        String oldStatus;

        String selectSql = """
                SELECT payment_status
                FROM payments
                WHERE order_id = ?
                ORDER BY payment_id DESC
                LIMIT 1
                """;

        String updateSql = """
                UPDATE payments
                SET payment_status = ?,
                    payment_date = CURRENT_TIMESTAMP
                WHERE payment_id = (
                    SELECT payment_id
                    FROM payments
                    WHERE order_id = ?
                    ORDER BY payment_id DESC
                    LIMIT 1
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement selectStmt =
                         conn.prepareStatement(selectSql)) {

                selectStmt.setInt(1, orderId);

                try (ResultSet rs = selectStmt.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("Payment record not found.");
                        return false;
                    }

                    oldStatus = rs.getString("payment_status");
                }
            }

            if (oldStatus.equalsIgnoreCase(updatedStatus)) {
                System.out.println("Payment already has this status.");
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
                    "PAYMENT_STATUS_UPDATED",
                    "Payment status for Order ID: " + orderId
                            + " changed from " + oldStatus
                            + " to " + updatedStatus
            );

            System.out.println("Payment status updated successfully.");

            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Unable to update payment: " + e.getMessage()
            );
            return false;
        }
    }

    // ==========================================
    // GET ALL PAYMENTS
    // ==========================================

    public List<Payment> getAllPayments() {

        List<Payment> payments = new ArrayList<>();

        String sql = """
                SELECT payment_id, order_id, amount, payment_method,
                       payment_status, payment_date
                FROM payments
                ORDER BY payment_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                payments.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("order_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status"),
                        rs.getString("payment_date")
                ));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Unable to retrieve payments: " + e.getMessage()
            );
        }

        return payments;
    }
}



