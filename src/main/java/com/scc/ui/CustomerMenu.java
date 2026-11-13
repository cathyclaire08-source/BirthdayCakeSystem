



package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.CakeDao;
import com.scc.dao.OrderDao;
import com.scc.dao.PaymentDao;
import com.scc.model.Cake;
import com.scc.model.Order;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {

    private final Scanner scanner;
    private final int userId;
    private final String username;

    private final ActivityLogDao activityLogDao;
    private final CakeDao cakeDao;
    private final OrderDao orderDao;
    private final PaymentDao paymentDao;

    public CustomerMenu(
            Scanner scanner,
            int userId,
            String username) {

        this.scanner = scanner;
        this.userId = userId;
        this.username = username;

        this.activityLogDao = new ActivityLogDao();
        this.cakeDao = new CakeDao();
        this.orderDao = new OrderDao();
        this.paymentDao = new PaymentDao();
    }

    // ==========================================
    // CUSTOMER MENU
    // ==========================================

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n================================");
            System.out.println("        CUSTOMER MENU");
            System.out.println("================================");
            System.out.println("1. View Available Cakes");
            System.out.println("2. Create Cake Pre-Order");
            System.out.println("3. View My Orders");
            System.out.println("4. Update My Order");
            System.out.println("5. Cancel My Order");
            System.out.println("6. Track Order and Payment Status");
            System.out.println("7. Add Payment Method to Existing Order");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAvailableCakes();

                case "2" -> createCakePreOrder();

                case "3" -> viewMyOrders();

                case "4" -> updateMyOrder();

                case "5" -> cancelMyOrder();

                case "6" -> trackOrderAndPaymentStatus();

                case "7" -> addPaymentMethodToExistingOrder();

                case "8" -> {
                    activityLogDao.logAction(
                            userId,
                            "LOGOUT",
                            "User " + username
                                    + " (ID: " + userId + ") logged out."
                    );

                    System.out.println("Customer logged out.");
                    running = false;
                }

                default -> System.out.println(
                        "Invalid option. Please choose 1-8."
                );
            }
        }
    }

    // ==========================================
    // 1. VIEW AVAILABLE CAKES
    // ==========================================

    private void viewAvailableCakes() {

        System.out.println("\n========== AVAILABLE CAKES ==========");

        List<Cake> cakes = cakeDao.getAvailableCakes();

        if (cakes.isEmpty()) {
            System.out.println("No available cakes at the moment.");
            return;
        }

        for (Cake cake : cakes) {

            System.out.println("--------------------------------");
            System.out.println("Cake ID: " + cake.getCakeId());
            System.out.println("Cake Name: " + cake.getCakeName());
            System.out.println("Flavor: " + cake.getFlavor());
            System.out.println("Size: " + cake.getSize());
            System.out.printf("Price: PHP %.2f%n", cake.getPrice());
            System.out.println(
                    "Availability: " + cake.getAvailabilityStatus()
            );
        }

        System.out.println("--------------------------------");
    }

    // ==========================================
    // 2. CREATE CAKE PRE-ORDER
    // ==========================================

    private void createCakePreOrder() {

        System.out.println("\n========== CREATE CAKE PRE-ORDER ==========");

        viewAvailableCakes();

        int cakeId = readPositiveInt("Enter Cake ID: ");

        if (cakeId <= 0) {
            System.out.println("Invalid Cake ID.");
            return;
        }

        Cake cake = cakeDao.getCakeById(cakeId);

        if (cake == null
                || !"AVAILABLE".equalsIgnoreCase(
                cake.getAvailabilityStatus())) {

            System.out.println("Cake is unavailable or does not exist.");
            return;
        }

        System.out.println("Selected Cake: " + cake.getCakeName());
        System.out.printf("Price: PHP %.2f%n", cake.getPrice());

        int quantity = readPositiveInt("Enter Quantity: ");

        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        String celebrationDate = readCelebrationDate();

        if (celebrationDate == null) {
            return;
        }

        String pickupOrDelivery = readPickupOrDelivery();

        if (pickupOrDelivery == null) {
            return;
        }

        String address = "";

        if (pickupOrDelivery.equals("DELIVERY")) {

            System.out.print("Enter Complete Delivery Address: ");
            address = scanner.nextLine().trim();

            if (address.isBlank()) {
                System.out.println("Delivery address is required.");
                return;
            }
        }

        double totalAmount = cake.getPrice() * quantity;

        System.out.println("\n========== ORDER SUMMARY ==========");
        System.out.println("Cake: " + cake.getCakeName());
        System.out.println("Quantity: " + quantity);
        System.out.println("Celebration Date: " + celebrationDate);
        System.out.println("Method: " + pickupOrDelivery);
        System.out.printf("Total Amount: PHP %.2f%n", totalAmount);

        System.out.print("Confirm this order? (Y/N): ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Order creation cancelled.");
            return;
        }

        String paymentMethod = readPaymentMethod();

        if (paymentMethod == null) {
            System.out.println("Order creation cancelled.");
            return;
        }

        int orderId = orderDao.createOrder(
                userId,
                cakeId,
                quantity,
                celebrationDate,
                pickupOrDelivery,
                address,
                totalAmount
        );

        if (orderId > 0) {

            System.out.println("Your order has been saved.");
            System.out.println("Your Order ID is: " + orderId);
            System.out.println("Order Status: PENDING");

            boolean paymentCreated = paymentDao.createPayment(
                    orderId,
                    totalAmount,
                    paymentMethod,
                    userId
            );

            if (paymentCreated) {

                System.out.println("Payment Method: " + paymentMethod);
                System.out.println("Payment Status: PENDING");

                System.out.println(
                        "Please wait for payment verification by the Order Clerk."
                );

            } else {

                System.out.println(
                        "WARNING: Your order was saved, but payment record creation failed."
                );

                System.out.println(
                        "Please contact the Order Clerk for assistance."
                );
            }
        }
    }

    // ==========================================
    // 3. VIEW MY ORDERS
    // ==========================================

    private void viewMyOrders() {

        System.out.println("\n========== MY CAKE ORDERS ==========");

        List<Order> orders = orderDao.getOrdersByCustomerId(userId);

        if (orders.isEmpty()) {
            System.out.println("You have no cake orders yet.");
            return;
        }

        for (Order order : orders) {

            System.out.println("--------------------------------");
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Cake: " + order.getCakeName());
            System.out.println("Quantity: " + order.getQuantity());

            System.out.println(
                    "Celebration Date: " + order.getCelebrationDate()
            );

            System.out.println(
                    "Pickup/Delivery: " + order.getPickupOrDelivery()
            );

            System.out.printf(
                    "Total: PHP %.2f%n",
                    order.getTotalAmount()
            );

            System.out.println(
                    "Order Status: " + order.getOrderStatus()
            );

            System.out.println(
                    "Order Date: " + order.getOrderDate()
            );
        }

        System.out.println("--------------------------------");
    }

    // ==========================================
    // 4. UPDATE MY ORDER
    // ==========================================

    private void updateMyOrder() {

        System.out.println("\n========== UPDATE MY ORDER ==========");

        viewMyOrders();

        int orderId = readPositiveInt("Enter Order ID to update: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        // Verify ownership and pending order status.
        List<Order> orders = orderDao.getOrdersByCustomerId(userId);

        Order selectedOrder = null;

        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                selectedOrder = order;
                break;
            }
        }

        if (selectedOrder == null) {
            System.out.println("Order not found in your account.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selectedOrder.getOrderStatus())) {
            System.out.println("Only pending orders can be updated.");
            return;
        }

        // Prevent payment and order totals from becoming inconsistent.
        if (paymentDao.getPaymentByOrderId(orderId) != null) {
            System.out.println(
                    "This order already has a payment record."
            );

            System.out.println(
                    "Please contact the Order Clerk before changing this order."
            );

            return;
        }

        viewAvailableCakes();

        int cakeId = readPositiveInt("Enter New Cake ID: ");

        if (cakeId <= 0) {
            System.out.println("Invalid Cake ID.");
            return;
        }

        Cake cake = cakeDao.getCakeById(cakeId);

        if (cake == null
                || !"AVAILABLE".equalsIgnoreCase(
                cake.getAvailabilityStatus())) {

            System.out.println("Cake is unavailable or does not exist.");
            return;
        }

        int quantity = readPositiveInt("Enter New Quantity: ");

        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        String celebrationDate = readCelebrationDate();

        if (celebrationDate == null) {
            return;
        }

        String pickupOrDelivery = readPickupOrDelivery();

        if (pickupOrDelivery == null) {
            return;
        }

        String address = "";

        if (pickupOrDelivery.equals("DELIVERY")) {

            System.out.print("Enter New Delivery Address: ");
            address = scanner.nextLine().trim();

            if (address.isBlank()) {
                System.out.println("Delivery address is required.");
                return;
            }
        }

        System.out.print("Confirm order update? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Order update cancelled.");
            return;
        }

        orderDao.updateCustomerOrder(
                orderId,
                userId,
                cakeId,
                quantity,
                celebrationDate,
                pickupOrDelivery,
                address
        );
    }

    // ==========================================
    // 5. CANCEL MY ORDER
    // ==========================================

    private void cancelMyOrder() {

        System.out.println("\n========== CANCEL MY ORDER ==========");

        viewMyOrders();

        int orderId = readPositiveInt("Enter Order ID to cancel: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        System.out.print(
                "Are you sure you want to cancel this order? (Y/N): "
        );

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        orderDao.cancelCustomerOrder(orderId, userId);
    }

    // ==========================================
    // 6. TRACK ORDER AND PAYMENT STATUS
    // ==========================================

    private void trackOrderAndPaymentStatus() {

        System.out.println("\n========== TRACK ORDER ==========");

        int orderId = readPositiveInt("Enter Order ID: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        orderDao.viewOrderAndPaymentStatus(orderId, userId);
    }

    // ==========================================
    // 7. ADD PAYMENT METHOD TO EXISTING ORDER
    // ==========================================

    private void addPaymentMethodToExistingOrder() {

        System.out.println("\n========== ADD PAYMENT METHOD ==========");

        viewMyOrders();

        int orderId = readPositiveInt("Enter Order ID: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        List<Order> orders = orderDao.getOrdersByCustomerId(userId);

        Order selectedOrder = null;

        for (Order order : orders) {

            if (order.getOrderId() == orderId) {
                selectedOrder = order;
                break;
            }
        }

        if (selectedOrder == null) {
            System.out.println("Order not found in your account.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selectedOrder.getOrderStatus())) {
            System.out.println(
                    "Payment can only be added to a pending order."
            );
            return;
        }

        if (paymentDao.getPaymentByOrderId(orderId) != null) {
            System.out.println(
                    "This order already has a payment record."
            );
            return;
        }

        System.out.printf(
                "Order Total: PHP %.2f%n",
                selectedOrder.getTotalAmount()
        );

        String paymentMethod = readPaymentMethod();

        if (paymentMethod == null) {
            return;
        }

        boolean created = paymentDao.createPayment(
                orderId,
                selectedOrder.getTotalAmount(),
                paymentMethod,
                userId
        );

        if (created) {

            System.out.println("Payment record created successfully.");
            System.out.println("Payment Method: " + paymentMethod);
            System.out.println("Payment Status: PENDING");

        } else {

            System.out.println("Unable to create payment record.");
        }
    }

    // ==========================================
    // PAYMENT METHOD SELECTION
    // ==========================================

    private String readPaymentMethod() {

        System.out.println("\n========== PAYMENT METHOD ==========");
        System.out.println("1. Cash on Pickup");
        System.out.println("2. GCash");
        System.out.println("3. Bank Transfer");
        System.out.print("Choose payment method: ");

        String choice = scanner.nextLine().trim();

        return switch (choice) {

            case "1" -> "CASH ON PICKUP";

            case "2" -> "GCASH";

            case "3" -> "BANK TRANSFER";

            default -> {
                System.out.println("Invalid payment selection.");
                yield null;
            }
        };
    }

    // ==========================================
    // INPUT HELPERS
    // ==========================================

    private int readPositiveInt(String message) {

        System.out.print(message);

        String input = scanner.nextLine().trim();

        try {

            int value = Integer.parseInt(input);

            if (value > 0) {
                return value;
            }

        } catch (NumberFormatException e) {

            System.out.println("Please enter a valid number.");
        }

        return -1;
    }

    private String readCelebrationDate() {

        System.out.print("Enter Celebration Date (YYYY-MM-DD): ");

        String date = scanner.nextLine().trim();

        try {

            LocalDate parsedDate = LocalDate.parse(date);

            if (parsedDate.isBefore(LocalDate.now())) {

                System.out.println(
                        "Celebration date cannot be in the past."
                );

                return null;
            }

            return date;

        } catch (DateTimeParseException e) {

            System.out.println(
                    "Invalid date. Please use YYYY-MM-DD."
            );

            return null;
        }
    }

    private String readPickupOrDelivery() {

        System.out.println("\nSelect Order Method:");
        System.out.println("1. PICKUP");
        System.out.println("2. DELIVERY");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine().trim();

        return switch (choice) {

            case "1" -> "PICKUP";

            case "2" -> "DELIVERY";

            default -> {
                System.out.println("Invalid selection.");
                yield null;
            }
        };
    }
}





