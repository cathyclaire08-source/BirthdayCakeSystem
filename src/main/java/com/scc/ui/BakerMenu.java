
package com.scc.ui;

import java.util.Scanner;

public class BakerMenu {

    private final Scanner scanner;

    public BakerMenu(Scanner scanner) {
        this.scanner = scanner;
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
                case "1" -> showFeature("View Cake Products");
                case "2" -> showFeature("Add Cake Product");
                case "3" -> showFeature("Update Cake Product");
                case "4" -> showFeature("Remove or Deactivate Cake");
                case "5" -> showFeature("View Orders for Preparation");
                case "6" -> showFeature("Update Cake Preparation Status");
                case "7" -> {
                    System.out.println("Baker logged out.");
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

