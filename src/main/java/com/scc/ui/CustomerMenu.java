
package com.scc.ui;

import java.util.Scanner;

public class CustomerMenu {

    private final Scanner scanner;

    public CustomerMenu(Scanner scanner) {
        this.scanner = scanner;
    }

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
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showFeature("View Available Cakes");
                case "2" -> showFeature("Create Cake Pre-Order");
                case "3" -> showFeature("View My Orders");
                case "4" -> showFeature("Update My Order");
                case "5" -> showFeature("Cancel My Order");
                case "6" -> showFeature("Track Order and Payment Status");
                case "7" -> {
                    System.out.println("Customer logged out.");
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

