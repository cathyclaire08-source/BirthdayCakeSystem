



package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.CakeDao;
import com.scc.dao.OrderDao;
import com.scc.model.Cake;
import com.scc.model.Order;

import java.util.List;
import java.util.Scanner;

public class BakerMenu {

    private final Scanner scanner;
    private final int userId;
    private final String username;

    private final CakeDao cakeDao;
    private final OrderDao orderDao;
    private final ActivityLogDao activityLogDao;

    public BakerMenu(Scanner scanner, int userId, String username) {
        this.scanner = scanner;
        this.userId = userId;
        this.username = username;

        this.cakeDao = new CakeDao();
        this.orderDao = new OrderDao();
        this.activityLogDao = new ActivityLogDao();
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n================================");
            System.out.println("          BAKER MENU");
            System.out.println("================================");
            System.out.println("1. View Cake Products");
            System.out.println("2. Add Cake Product");
            System.out.println("3. Update Cake Product");
            System.out.println("4. Remove or Deactivate Cake");
            System.out.println("5. View Orders for Preparation");
            System.out.println("6. Update Cake Preparation Status");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewCakeProducts();

                case "2" -> addCakeProduct();

                case "3" -> updateCakeProduct();

                case "4" -> deactivateCake();

                case "5" -> viewOrdersForPreparation();

                case "6" -> updatePreparationStatus();

                case "7" -> {
                    activityLogDao.logAction(
                            userId,
                            "LOGOUT",
                            "User " + username
                                    + " (ID: " + userId
                                    + ") logged out."
                    );

                    System.out.println("Baker logged out.");
                    running = false;
                }

                default ->
                        System.out.println("Invalid option.");
            }
        }
    }

    private void viewCakeProducts() {

        System.out.println("\n========== AVAILABLE CAKES ==========");

        List<Cake> cakes = cakeDao.getAvailableCakes();

        if (cakes.isEmpty()) {
            System.out.println("No available cake products found.");
            return;
        }

        for (Cake cake : cakes) {

            System.out.println("--------------------------------");
            System.out.println("Cake ID: " + cake.getCakeId());
            System.out.println("Cake Name: " + cake.getCakeName());
            System.out.println("Flavor: " + cake.getFlavor());
            System.out.println("Size: " + cake.getSize());

            System.out.printf(
                    "Price: ₱%.2f%n",
                    cake.getPrice()
            );

            System.out.println(
                    "Status: " + cake.getAvailabilityStatus()
            );
        }
    }

    private void addCakeProduct() {

        System.out.println("\n========== ADD CAKE PRODUCT ==========");

        System.out.print("Cake Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Flavor: ");
        String flavor = scanner.nextLine().trim();

        System.out.print("Size: ");
        String size = scanner.nextLine().trim();

        if (name.isEmpty() || flavor.isEmpty() || size.isEmpty()) {
            System.out.println("All cake details are required.");
            return;
        }

        double price = readPositiveDouble("Price: ₱");

        if (price <= 0) {
            System.out.println("Invalid price.");
            return;
        }

        if (cakeDao.addCake(name, flavor, size, price)) {

            activityLogDao.logAction(
                    userId,
                    "CAKE_PRODUCT_ADDED",
                    "Baker added cake product: " + name
            );

            System.out.println("Cake product added successfully.");

        } else {

            System.out.println("Unable to add cake product.");
        }
    }

    private void updateCakeProduct() {

        System.out.println("\n========== UPDATE CAKE PRODUCT ==========");

        int cakeId = readPositiveInt("Enter Cake ID: ");

        if (cakeId <= 0) {
            System.out.println("Invalid Cake ID.");
            return;
        }

        Cake cake = cakeDao.getCakeById(cakeId);

        if (cake == null) {
            System.out.println("Cake product not found.");
            return;
        }

        System.out.println("Current cake details:");
        System.out.println("Name: " + cake.getCakeName());
        System.out.println("Flavor: " + cake.getFlavor());
        System.out.println("Size: " + cake.getSize());

        System.out.printf(
                "Price: ₱%.2f%n",
                cake.getPrice()
        );

        System.out.println("\nEnter the new cake details:");

        System.out.print("New Cake Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("New Flavor: ");
        String flavor = scanner.nextLine().trim();

        System.out.print("New Size: ");
        String size = scanner.nextLine().trim();

        if (name.isEmpty() || flavor.isEmpty() || size.isEmpty()) {
            System.out.println("All cake details are required.");
            return;
        }

        double price = readPositiveDouble("New Price: ₱");

        if (price <= 0) {
            System.out.println("Invalid price.");
            return;
        }

        if (cakeDao.updateCakeDetails(
                cakeId, name, flavor, size, price)) {

            activityLogDao.logAction(
                    userId,
                    "CAKE_PRODUCT_UPDATED",
                    "Updated cake ID: " + cakeId
                            + ", Name: " + name
            );

            System.out.println("Cake product updated successfully.");

        } else {

            System.out.println("Unable to update cake product.");
        }
    }

    private void deactivateCake() {

        System.out.println("\n========== DEACTIVATE CAKE ==========");

        int cakeId = readPositiveInt("Enter Cake ID: ");

        if (cakeId <= 0) {
            System.out.println("Invalid Cake ID.");
            return;
        }

        Cake cake = cakeDao.getCakeById(cakeId);

        if (cake == null) {
            System.out.println("Cake product not found.");
            return;
        }

        if ("UNAVAILABLE".equalsIgnoreCase(
                cake.getAvailabilityStatus())) {

            System.out.println("This cake is already unavailable.");
            return;
        }

        System.out.println("Cake: " + cake.getCakeName());
        System.out.print("Deactivate this cake? (Y/N): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Deactivation cancelled.");
            return;
        }

        if (cakeDao.updateCakeAvailability(
                cakeId, "UNAVAILABLE")) {

            activityLogDao.logAction(
                    userId,
                    "CAKE_PRODUCT_DEACTIVATED",
                    "Deactivated cake ID: " + cakeId
                            + ", Cake: " + cake.getCakeName()
            );

            System.out.println("Cake deactivated successfully.");

        } else {

            System.out.println("Unable to deactivate cake.");
        }
    }

    private void viewOrdersForPreparation() {

        System.out.println("\n========== ORDERS FOR PREPARATION ==========");

        List<Order> orders = orderDao.getOrdersForPreparation();

        if (orders.isEmpty()) {
            System.out.println("No orders currently require preparation.");
            return;
        }

        for (Order order : orders) {

            System.out.println("--------------------------------");
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Customer: " + order.getCustomerName());
            System.out.println("Cake: " + order.getCakeName());
            System.out.println("Quantity: " + order.getQuantity());
            System.out.println(
                    "Celebration Date: " + order.getCelebrationDate()
            );
            System.out.println(
                    "Order Type: " + order.getPickupOrDelivery()
            );
            System.out.println("Address: " + order.getAddress());

            System.out.printf(
                    "Total: ₱%.2f%n",
                    order.getTotalAmount()
            );

            System.out.println("Status: " + order.getOrderStatus());
        }
    }

    private void updatePreparationStatus() {

        System.out.println(
                "\n========== UPDATE PREPARATION STATUS =========="
        );

        int orderId = readPositiveInt("Enter Order ID: ");

        if (orderId <= 0) {
            System.out.println("Invalid Order ID.");
            return;
        }

        System.out.println("Select new preparation status:");
        System.out.println("1. PREPARING");
        System.out.println("2. READY");
        System.out.print("Choose status: ");

        String choice = scanner.nextLine().trim();

        String newStatus;

        switch (choice) {

            case "1" -> newStatus = "PREPARING";

            case "2" -> newStatus = "READY";

            default -> {
                System.out.println("Invalid status selection.");
                return;
            }
        }

        boolean success = orderDao.updateOrderStatus(
                orderId,
                newStatus,
                userId
        );

        if (success) {
            System.out.println(
                    "Preparation status updated to " + newStatus + "."
            );
        }
    }

    private int readPositiveInt(String prompt) {

        System.out.print(prompt);

        String input = scanner.nextLine().trim();

        try {

            int value = Integer.parseInt(input);

            return value > 0 ? value : -1;

        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double readPositiveDouble(String prompt) {

        System.out.print(prompt);

        String input = scanner.nextLine().trim();

        try {

            double value = Double.parseDouble(input);

            return Double.isFinite(value) && value > 0
                    ? value : -1;

        } catch (NumberFormatException e) {
            return -1;
        }
    }
}




