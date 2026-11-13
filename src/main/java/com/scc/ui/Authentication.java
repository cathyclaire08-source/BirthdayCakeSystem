



package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;
    private final MainMenu mainMenu;
    private final ActivityLogDao activityLogDao;

    public Authentication(UserDao userDao, Scanner scanner) {

        this.userDao = userDao;
        this.scanner = scanner;
        this.mainMenu = new MainMenu(scanner);
        this.activityLogDao = new ActivityLogDao();
    }

    // ==========================================
    // MAIN AUTHENTICATION MENU
    // ==========================================

    public void start() {

        boolean running = true;

        while (running) {

            String choice = mainMenu.showMenu();

            switch (choice) {

                case "1" ->
                        registerUser();

                case "2" ->
                        loginUser();

                case "3" -> {
                    System.out.println(
                            "Thank you for using the Birthday Cake Ordering System!"
                    );
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
    // USER REGISTRATION
    // ==========================================

    private void registerUser() {

        System.out.println("\n================================");
        System.out.println("         USER REGISTRATION");
        System.out.println("================================");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter role: ");
        String role = scanner.nextLine().trim();

        System.out.print("Enter contact number: ");
        String contactNumber = scanner.nextLine().trim();

        userDao.registerUser(
                username,
                fullName,
                password,
                role,
                contactNumber
        );
    }

    // ==========================================
    // USER LOGIN
    // ==========================================

    private void loginUser() {

        System.out.println("\n================================");
        System.out.println("             USER LOGIN");
        System.out.println("================================");

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
                    "Login unsuccessful. Please check your credentials "
                            + "or account status."
            );

            return;
        }

        int userId = userDao.getApprovedUserId(
                username,
                password
        );

        if (userId <= 0) {

            System.out.println(
                    "Unable to retrieve user account information."
            );

            return;
        }

        // Validate role before recording successful login.
        String normalizedRole = role.trim().toUpperCase();

        if (!normalizedRole.equals("ADMIN")
                && !normalizedRole.equals("CUSTOMER")
                && !normalizedRole.equals("BAKER")
                && !normalizedRole.equals("ORDER_CLERK")) {

            System.out.println(
                    "No menu is available for this user role. "
                            + "Please contact the administrator."
            );

            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + username + "!");
        System.out.println("Role: " + normalizedRole);

        // Record successful authentication.
        activityLogDao.logAction(
                userId,
                "LOGIN",
                "User " + username
                        + " (ID: " + userId + ") logged in."
        );

        // Open the appropriate role menu.
        switch (normalizedRole) {

            case "ADMIN" ->
                    new AdminMenu(
                            userDao,
                            scanner,
                            userId,
                            username
                    ).start();

            case "CUSTOMER" ->
                    new CustomerMenu(
                            scanner,
                            userId,
                            username
                    ).start();

            case "BAKER" ->
                    new BakerMenu(
                            scanner,
                            userId,
                            username
                    ).start();

            case "ORDER_CLERK" ->
                    new OrderClerkMenu(
                            scanner,
                            userId,
                            username
                    ).start();

            default ->
                    System.out.println("Unable to open user menu.");
        }
    }
}









