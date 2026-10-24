
package com.scc.ui;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;
    private final MainMenu mainMenu;

    // Reuse the same UserDao and Scanner created in Main.java.
    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.mainMenu = new MainMenu(scanner);
    }

    // Main entry for registration, login, and role routing.
    public void start() {
        boolean running = true;

        while (running) {
            String choice = mainMenu.showMenu();

            switch (choice) {
                case "1":
                    registerUser();
                    break;

                case "2":
                    loginUser();
                    break;

                case "3":
                    System.out.println("Thank you for using the system!");
                    running = false;
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Please choose 1, 2, or 3."
                    );
            }
        }
    }

    private void registerUser() {
        System.out.println("\n--- USER REGISTRATION ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter contact number: ");
        String contactNumber = scanner.nextLine().trim();

        if (username.isEmpty()
                || fullName.isEmpty()
                || password.isEmpty()
                || contactNumber.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        System.out.println("\nSelect your role:");
        System.out.println("1. Customer");
        System.out.println("2. Baker");
        System.out.println("3. Order Clerk");
        System.out.print("Choose role: ");

        String roleChoice = scanner.nextLine().trim();
        String role;

        switch (roleChoice) {
            case "1":
                role = "CUSTOMER";
                break;

            case "2":
                role = "BAKER";
                break;

            case "3":
                role = "ORDER_CLERK";
                break;

            default:
                System.out.println("Invalid role selection.");
                return;
        }

        boolean success = userDao.registerUser(
                username,
                fullName,
                password,
                role,
                contactNumber
        );

        if (success) {
            System.out.println(
                    "Registration successful! Your account is PENDING approval."
            );
            System.out.println(
                    "You can log in after an administrator approves your account."
            );
        } else {
            System.out.println(
                    "Registration failed. Check the error message above."
            );
        }
    }

    private void loginUser() {
        System.out.println("\n--- USER LOGIN ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Only APPROVED users can retrieve a role and enter the system.
        String role = userDao.getApprovedUserRole(username, password);

        if (role == null) {
            // Distinguish pending accounts from invalid credentials.
            if (userDao.loginUser(username, password)) {
                // This fallback should not normally be reached when the
                // DAO methods use the same account-status rules.
                System.out.println(
                        "Unable to determine your approved role."
                );
            } else {
                System.out.println(
                        "Login unsuccessful. Check your credentials or "
                                + "ask an administrator to approve your account."
                );
            }
            return;
        }

        System.out.println("Login successful! Role: " + role);

        switch (role.trim().toUpperCase()) {
            case "CUSTOMER":
                customerMenu();
                break;

            case "BAKER":
                bakerMenu();
                break;

            case "ORDER_CLERK":
                orderClerkMenu();
                break;

            default:
                System.out.println(
                        "Unrecognized role. Please contact the administrator."
                );
        }
    }

    private void customerMenu() {
        showRoleMenu(
                "CUSTOMER MENU",
                "View available cakes and prices",
                "Create and track cake pre-orders"
        );
    }

    private void bakerMenu() {
        showRoleMenu(
                "BAKER MENU",
                "Manage cake products",
                "Update cake preparation status"
        );
    }

    private void orderClerkMenu() {
        showRoleMenu(
                "ORDER CLERK MENU",
                "Manage customer orders",
                "Record payments and update order status"
        );
    }

    // Basic role-specific menu shells until the actual features are added.
    private void showRoleMenu(
            String title,
            String optionOne,
            String optionTwo) {

        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n================================");
            System.out.println("       " + title);
            System.out.println("================================");
            System.out.println("1. " + optionOne);
            System.out.println("2. " + optionTwo);
            System.out.println("3. Log out");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println(
                            "This feature will be implemented in the next phase."
                    );
                    break;

                case "2":
                    System.out.println(
                            "This feature will be implemented in the next phase."
                    );
                    break;

                case "3":
                    System.out.println("You have logged out.");
                    loggedIn = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}

