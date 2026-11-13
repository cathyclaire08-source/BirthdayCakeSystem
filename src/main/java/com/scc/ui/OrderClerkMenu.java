




package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.OrderClerkDao;
import com.scc.dao.OrderDao;
import com.scc.dao.PaymentDao;
import com.scc.model.Order;
import com.scc.model.Payment;

import java.util.List;
import java.util.Scanner;

public class OrderClerkMenu {

    private final Scanner scanner;
    private final int userId;
    private final String username;

    private final OrderDao orderDao;
    private final OrderClerkDao orderClerkDao;
    private final PaymentDao paymentDao;
    private final ActivityLogDao activityLogDao;

    public OrderClerkMenu(
            Scanner scanner,
            int userId,
            String username) {

        this.scanner = scanner;
        this.userId = userId;
        this.username = username;

        this.orderDao = new OrderDao();
        this.orderClerkDao = new OrderClerkDao();
        this.paymentDao = new PaymentDao();
        this.activityLogDao = new ActivityLogDao();
    }

    // ==========================================
    // MAIN MENU
    // ==========================================

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n================================");
            System.out.println("       ORDER CLERK MENU");
            System.out.println("================================");
            System.out.println("1. View All Customer Orders");
            System.out.println("2. Search Customer Orders");
            System.out.println("3. Update Order Status");
            System.out.println("4. Record Payment");
            System.out.println("5. Verify Payment");
            System.out.println("6. Update Pickup or Delivery Status");
            System.out.println("7. View Daily Cake Sales Report");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAllOrders();

                case "2" -> searchOrders();

                case "3" -> updateOrderStatus(false);

                case "4" -> recordPayment();

                case "5" -> verifyPayment();

                case "6" -> updateOrderStatus(true);

                case "7" -> orderClerkDao.printDailySalesReport();

                case "8" -> {
                    activityLogDao.logAction(
                            userId,
                            "LOGOUT",
                            "User " + username
                                    + " (ID: " + userId
                                    + ") logged out."
                    );

                    System.out.println("Order Clerk logged out.");
                    running = false;
                }

                default ->
                        System.out.println(
                                "Invalid option. Please try again."
                        );
            }
        }
    }

    // ==========================================
    // VIEW ALL ORDERS
    // ==========================================

    private void viewAllOrders() {

        System.out.println("\n========== ALL CUSTOMER ORDERS ==========");

        List<Order> orders = orderClerkDao.getAllOrders();

        displayOrders(orders);
    }

    // ==========================================
    // SEARCH ORDERS
    // ==========================================

    private void searchOrders() {

        System.out.println("\n========== SEARCH CUSTOMER ORDERS ==========");

        System.out.print(
                "Enter Order ID, Customer Name, Cake Name, or Status: "
        );

        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return;
        }

        List<Order> orders = orderClerkDao.searchOrders(keyword);

        displayOrders(orders);
    }

    // ==========================================
    // DISPLAY ORDERS INCLUDING PAYMENT STATUS
    // ==========================================

    private void displayOrders(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            System.out.println("No customer orders found.");
            return;
        }

        for (Order order : orders) {

            System.out.println("------------------------------------------------");

            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Customer: " + order.getCustomerName());
            System.out.println("Cake: " + order.getCakeName());
            System.out.println("Quantity: " + order.getQuantity());

            System.out.println(
                    "Celebration Date: " + order.getCelebrationDate()
            );

            System.out.println(
                    "Pickup / Delivery: " + order.getPickupOrDelivery()
            );

            System.out.println("Address: " + order.getAddress());

            System.out.printf(
                    "Total Amount: ₱%.2f%n",
                    order.getTotalAmount()
            );

            System.out.println(
                    "Order Status: " + order.getOrderStatus()
            );

            // NEW: Retrieve payment status for this order.
            String paymentStatus =
                    orderClerkDao.getPaymentStatusByOrderId(
                            order.getOrderId()
                    );

            System.out.println(
                    "Payment Status: " + paymentStatus
            );

            System.out.println(
                    "Order Date: " + order.getOrderDate()
            );
        }

        System.out.println("------------------------------------------------");
        System.out.println("Total Orders Found: " + orders.size());
    }

    // ==========================================
    // RECORD PAYMENT
    // ==========================================

    private void recordPayment() {

        System.out.println("\n========== RECORD PAYMENT ==========");

        int orderId = readPositiveInteger("Enter Order ID: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        System.out.print("Enter Payment Amount: ₱");

        double amount = readPositiveAmount();

        if (amount <= 0) {
            System.out.println("Invalid payment amount.");
            return;
        }

        System.out.println("\nPayment Methods:");
        System.out.println("1. CASH ON PICKUP");
        System.out.println("2. GCASH");
        System.out.println("3. BANK TRANSFER");

        System.out.print("Choose payment method: ");

        String methodChoice = scanner.nextLine().trim();

        String paymentMethod = switch (methodChoice) {
            case "1" -> "CASH ON PICKUP";
            case "2" -> "GCASH";
            case "3" -> "BANK TRANSFER";
            default -> null;
        };

        if (paymentMethod == null) {
            System.out.println("Invalid payment method.");
            return;
        }

        System.out.println("\nPayment Details");
        System.out.println("Order ID: " + orderId);

        System.out.printf(
                "Amount: ₱%.2f%n",
                amount
        );

        System.out.println("Method: " + paymentMethod);
        System.out.println("Initial Status: PENDING");

        System.out.print("\nConfirm payment record? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Payment recording cancelled.");
            return;
        }

        paymentDao.createPaymentForOrderClerk(
                orderId,
                amount,
                paymentMethod,
                userId
        );
    }

    // ==========================================
    // VERIFY PAYMENT
    // ==========================================

    private void verifyPayment() {

        System.out.println("\n========== VERIFY PAYMENT ==========");

        int orderId = readPositiveInteger("Enter Order ID: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        Payment payment = paymentDao.getPaymentByOrderId(orderId);

        if (payment == null) {
            System.out.println("No payment record found for this order.");
            return;
        }

        System.out.println("\nPayment Details");
        System.out.println("--------------------------------");
        System.out.println("Payment ID: " + payment.getPaymentId());
        System.out.println("Order ID: " + payment.getOrderId());

        System.out.printf(
                "Amount: ₱%.2f%n",
                payment.getAmount()
        );

        System.out.println(
                "Payment Method: " + payment.getPaymentMethod()
        );

        System.out.println(
                "Current Status: " + payment.getPaymentStatus()
        );

        System.out.println(
                "Payment Date: " + payment.getPaymentDate()
        );

        System.out.println("--------------------------------");

        if (!"PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
            System.out.println(
                    "This payment is not pending. No verification update "
                            + "is available."
            );
            return;
        }

        System.out.println("1. Mark as PAID");
        System.out.println("2. Mark as REJECTED");
        System.out.println("0. Cancel");

        System.out.print("Choose verification result: ");

        String choice = scanner.nextLine().trim();

        String newStatus = switch (choice) {
            case "1" -> "PAID";
            case "2" -> "REJECTED";
            default -> null;
        };

        if (newStatus == null) {
            System.out.println("Payment verification cancelled.");
            return;
        }

        System.out.println("New Payment Status: " + newStatus);

        System.out.print("Confirm this update? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Payment verification cancelled.");
            return;
        }

        paymentDao.updatePaymentStatus(
                orderId,
                newStatus,
                userId
        );
    }

    // ==========================================
    // UPDATE ORDER STATUS
    // ==========================================

    private void updateOrderStatus(boolean deliveryOnly) {

        System.out.println("\n================================");

        if (deliveryOnly) {
            System.out.println(" UPDATE PICKUP / DELIVERY STATUS");
        } else {
            System.out.println("       UPDATE ORDER STATUS");
        }

        System.out.println("================================");

        int orderId = readPositiveInteger("Enter Order ID: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        String newStatus = selectOrderStatus(deliveryOnly);

        if (newStatus == null) {
            System.out.println("Status update cancelled.");
            return;
        }

        System.out.println("\nOrder ID: " + orderId);
        System.out.println("New Status: " + newStatus);

        System.out.print("Confirm status update? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Status update cancelled.");
            return;
        }

        boolean updated = orderDao.updateOrderStatus(
                orderId,
                newStatus,
                userId
        );

        if (updated) {
            System.out.println("Order tracking updated successfully.");
        }
    }

    // ==========================================
    // SELECT ORDER STATUS
    // ==========================================

    private String selectOrderStatus(boolean deliveryOnly) {

        System.out.println("\nAvailable Order Statuses:");

        if (deliveryOnly) {

            System.out.println("1. READY FOR PICKUP");
            System.out.println("2. READY FOR DELIVERY");
            System.out.println("3. OUT FOR DELIVERY");
            System.out.println("4. DELIVERED");
            System.out.println("5. COMPLETED");
            System.out.println("0. Cancel");

        } else {

            System.out.println("1. CONFIRMED");
            System.out.println("2. PREPARING");
            System.out.println("3. READY FOR PICKUP");
            System.out.println("4. READY FOR DELIVERY");
            System.out.println("5. OUT FOR DELIVERY");
            System.out.println("6. DELIVERED");
            System.out.println("7. COMPLETED");
            System.out.println("8. CANCELLED");
            System.out.println("0. Cancel");
        }

        System.out.print("Select status: ");

        String choice = scanner.nextLine().trim();

        if (deliveryOnly) {

            return switch (choice) {
                case "1" -> "READY FOR PICKUP";
                case "2" -> "READY FOR DELIVERY";
                case "3" -> "OUT FOR DELIVERY";
                case "4" -> "DELIVERED";
                case "5" -> "COMPLETED";
                default -> null;
            };

        } else {

            return switch (choice) {
                case "1" -> "CONFIRMED";
                case "2" -> "PREPARING";
                case "3" -> "READY FOR PICKUP";
                case "4" -> "READY FOR DELIVERY";
                case "5" -> "OUT FOR DELIVERY";
                case "6" -> "DELIVERED";
                case "7" -> "COMPLETED";
                case "8" -> "CANCELLED";
                default -> null;
            };
        }
    }

    // ==========================================
    // INPUT VALIDATION
    // ==========================================

    private int readPositiveInteger(String prompt) {

        System.out.print(prompt);

        String input = scanner.nextLine().trim();

        try {

            int number = Integer.parseInt(input);

            return number > 0 ? number : -1;

        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double readPositiveAmount() {

        String input = scanner.nextLine().trim();

        try {

            double amount = Double.parseDouble(input);

            if (!Double.isFinite(amount) || amount <= 0) {
                return -1;
            }

            return amount;

        } catch (NumberFormatException e) {
            return -1;
        }
    }
}





