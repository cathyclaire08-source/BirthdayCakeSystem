

package com.scc.ui;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;
    private final MainMenu mainMenu;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.mainMenu = new MainMenu(scanner);
    }

    public void start() {
        boolean running = true;

        while (running) {
            String choice = mainMenu.showMenu();

            switch (choice) {
                case "1" -> registerUser();
                case "2" -> loginUser();
                case "3" -> {
                    System.out.println(
                            "Thank you for using the system!"
                    );
                    running = false;
                }
                default -> System.out.println(
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

        String role;

        switch (scanner.nextLine().trim()) {
            case "1" -> role = "CUSTOMER";
            case "2" -> role = "BAKER";
            case "3" -> role = "ORDER_CLERK";
            default -> {
                System.out.println("Invalid role selection.");
                return;
            }
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

        String role = userDao.getApprovedUserRole(
                username,
                password
        );

        if (role == null) {
            System.out.println(
                    "Login unsuccessful. Check your credentials "
                            + "or account approval status."
            );
            return;
        }

        System.out.println(
                "Login successful! Role: " + role
        );

        switch (role.trim().toUpperCase()) {
            case "ADMIN" ->
                    new AdminMenu(userDao, scanner).start();

            case "CUSTOMER" ->
                    new CustomerMenu(scanner).start();

            case "BAKER" ->
                    new BakerMenu(scanner).start();

            case "ORDER_CLERK" ->
                    new OrderClerkMenu(scanner).start();

            default ->
                    System.out.println(
                            "Unrecognized role. Contact the administrator."
                    );
        }
    }
}


