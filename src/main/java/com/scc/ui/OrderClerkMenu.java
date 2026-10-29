
package com.scc.ui;

import java.util.Scanner;

public class OrderClerkMenu {

    private final Scanner scanner;

    public OrderClerkMenu(Scanner scanner) {
        this.scanner = scanner;
    }

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
                case "1" -> showFeature("View All Customer Orders");
                case "2" -> showFeature("Search Customer Orders");
                case "3" -> showFeature("Update Order Status");
                case "4" -> showFeature("Record Payment");
                case "5" -> showFeature("Verify Payment");
                case "6" -> showFeature("Update Pickup or Delivery Status");
                case "7" -> showFeature("Daily Cake Sales Report");
                case "8" -> {
                    System.out.println("Order Clerk logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void showFeature(String feature) {
        System.out.println("\nSelected: " + feature);
        System.out.println("This feature will be connected to the database.");
    }
}

