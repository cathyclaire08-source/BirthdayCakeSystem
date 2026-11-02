

package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;
    private final int currentAdminId;

    public AdminMenu(
            UserDao userDao,
            Scanner scanner,
            int currentAdminId) {

        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdminId = currentAdminId;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n================================");
            System.out.println("        ADMIN DASHBOARD");
            System.out.println("================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Accounts");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Logout");

            int choice = readIntInput("Choose an option (0 to cancel): ");

            switch (choice) {
                case 0 -> System.out.println("No action selected.");
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> updateUser();
                case 5 -> {
                    System.out.println("Admin logged out.");
                    running = false;
                }
                default -> System.out.println(
                        "Invalid option. Choose 1 to 5."
                );
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n========== ALL USERS ==========");
        displayUsers(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n======= PENDING ACCOUNTS =======");
        displayUsers(userDao.getPendingUsers());
    }

    private void approveUser() {
        System.out.println("\n======= APPROVE ACCOUNT =======");
        displayUsers(userDao.getPendingUsers());

        int userId = readIntInput(
                "Enter User ID to approve (0 to cancel): "
        );

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean success = userDao.approveUser(userId);

        System.out.println(success
                ? "Account approved successfully!"
                : "Approval failed. Check the User ID and account status.");
    }

    private void updateUser() {
        System.out.println("\n======= UPDATE USER =======");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        displayUsers(users);

        int targetUserId = readIntInput(
                "Enter target User ID (0 to cancel): "
        );

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        // Confirm that the target user exists.
        User targetUser = null;

        for (User user : users) {
            if (user.id() == targetUserId) {
                targetUser = user;
                break;
            }
        }

        if (targetUser == null) {
            System.out.println("User ID not found.");
            return;
        }

        // Prevent the admin from modifying their own account.
        if (targetUserId == currentAdminId) {
            System.out.println(
                    "WARNING: Modifying the currently active admin "
                            + "account is restricted."
            );
            return;
        }

        System.out.println("\nSelected user: " + targetUser.username());
        System.out.println("Current role: " + targetUser.role());
        System.out.println(
                "Current status: " + targetUser.accountStatus()
        );

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.println("\nNew role: " + newRole);
        System.out.println("New status: " + newStatus);

        String confirmation;

        while (true) {
            System.out.print("Save changes? (Y/N): ");
            confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("Y")
                    || confirmation.equalsIgnoreCase("N")) {
                break;
            }

            if (confirmation.equals("0")) {
                System.out.println("Update cancelled.");
                return;
            }

            System.out.println(
                    "Invalid input. Enter Y, N, or 0 to cancel."
            );
        }

        if (confirmation.equalsIgnoreCase("N")) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus
        );

        if (success) {
            System.out.println(
                    "User role and account status updated successfully!"
            );
        } else {
            System.out.println(
                    "Update failed. Please check the database and try again."
            );
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a whole number."
                );
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("\nAvailable roles:");
            System.out.println("1. CUSTOMER");
            System.out.println("2. BAKER");
            System.out.println("3. ORDER_CLERK");
            System.out.println("4. ADMIN");
            System.out.println("Enter 0 to cancel.");

            System.out.print("Enter new role: ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return null;
            }

            switch (role) {
                case "1", "CUSTOMER" -> {
                    return "CUSTOMER";
                }
                case "2", "BAKER" -> {
                    return "BAKER";
                }
                case "3", "ORDER_CLERK" -> {
                    return "ORDER_CLERK";
                }
                case "4", "ADMIN" -> {
                    return "ADMIN";
                }
                default -> System.out.println(
                        "Invalid role. Choose a listed role and try again."
                );
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("\nAvailable statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("Enter 0 to cancel.");

            System.out.print("Enter new status: ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return null;
            }

            switch (status) {
                case "1", "APPROVED" -> {
                    return "APPROVED";
                }
                case "2", "PENDING" -> {
                    return "PENDING";
                }
                default -> System.out.println(
                        "Invalid status. Enter APPROVED, PENDING, or 0."
                );
            }
        }
    }

    private void displayUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.printf(
                "%-8s %-22s %-18s %-16s %-12s%n",
                "ID", "FULL NAME", "USERNAME", "ROLE", "STATUS"
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


