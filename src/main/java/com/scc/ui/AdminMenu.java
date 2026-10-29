
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n================================");
            System.out.println("       ADMIN DASHBOARD");
            System.out.println("================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Accounts");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> {
                    System.out.println("Admin logged out.");
                    running = false;
                }
                default -> System.out.println(
                        "Invalid option. Please try again."
                );
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n========== ALL USERS ==========");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        displayUsers(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n======= PENDING ACCOUNTS =======");

        List<User> users = userDao.getPendingUsers();

        if (users.isEmpty()) {
            System.out.println("No pending accounts found.");
            return;
        }

        displayUsers(users);
    }

    private void approveUser() {
        System.out.println("\n======= APPROVE ACCOUNT =======");

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("There are no pending accounts.");
            return;
        }

        displayUsers(pendingUsers);

        System.out.print("Enter the User ID to approve: ");
        String input = scanner.nextLine().trim();

        try {
            int userId = Integer.parseInt(input);

            boolean found = pendingUsers.stream()
                    .anyMatch(user -> user.id() == userId);

            if (!found) {
                System.out.println(
                        "Invalid User ID or account is not pending."
                );
                return;
            }

            System.out.print(
                    "Approve this account? (Y/N): "
            );

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Approval cancelled.");
                return;
            }

            if (userDao.approveUser(userId)) {
                System.out.println(
                        "Account approved successfully!"
                );
            } else {
                System.out.println(
                        "Approval failed. Please try again."
                );
            }

        } catch (NumberFormatException e) {
            System.out.println(
                    "Invalid input. Please enter a numeric User ID."
            );
        }
    }

    private void displayUsers(List<User> users) {
        System.out.printf(
                "%-8s %-22s %-18s %-16s %-12s%n",
                "ID", "FULL NAME", "USERNAME",
                "ROLE", "STATUS"
        );

        System.out.println(
                "--------------------------------------------------------------------------"
        );

        for (User user : users) {
            System.out.printf(
                    "%-8d %-22s %-18s %-16s %-12s%n",
                    user.id(),
                    user.fullName(),
                    user.username(),
                    user.role(),
                    user.accountStatus()
            );
        }
    }
}

