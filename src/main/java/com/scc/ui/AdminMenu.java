


package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.User;
import com.scc.model.ActivityLog;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;
    private final int currentAdminId;
    private final String adminUsername;

    public AdminMenu(
            UserDao userDao,
            Scanner scanner,
            int currentAdminId,
            String adminUsername) {

        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
        this.scanner = scanner;
        this.currentAdminId = currentAdminId;
        this.adminUsername = adminUsername;
    }

    // ADMIN DASHBOARD
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
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout");
            System.out.println("================================");

            int choice = readIntInput("Choose an option: ");

            switch (choice) {

                case 0 ->
                        System.out.println("No action selected.");

                case 1 ->
                        viewAllUsers();

                case 2 ->
                        viewPendingUsers();

                case 3 ->
                        approveUser();

                case 4 ->
                        updateUser();

                case 5 ->
                        auditLogMenu();

                case 6 -> {
                    activityLogDao.logAction(
                            currentAdminId,
                            "LOGOUT",
                            "User " + adminUsername
                                    + " (ID: " + currentAdminId
                                    + ") logged out."
                    );

                    System.out.println("Admin logged out.");
                    running = false;
                }

                default ->
                        System.out.println("Invalid option. Choose 1 to 6.");
            }
        }
    }

    // VIEW ALL USERS
    private void viewAllUsers() {

        System.out.println("\n========== ALL USERS ==========");

        displayUsers(userDao.getAllUsers());
    }

    // VIEW PENDING USERS
    private void viewPendingUsers() {

        System.out.println("\n======= PENDING ACCOUNTS =======");

        displayUsers(userDao.getPendingUsers());
    }

    // APPROVE USER ACCOUNT
    private void approveUser() {

        System.out.println("\n======= APPROVE ACCOUNT =======");

        displayUsers(userDao.getPendingUsers());

        int userId = readIntInput(
                "Enter User ID to approve (0 to cancel): "
        );

        if (userId <= 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        // DAO now records the audit event in the same transaction.
        boolean success = userDao.approveUser(
                userId,
                currentAdminId
        );

        if (success) {

            System.out.println("Account approved successfully!");
            System.out.println("Activity recorded in audit logs.");

        } else {

            System.out.println(
                    "Approval failed. Check the User ID and account status."
            );
        }
    }

    // UPDATE USER ROLE AND STATUS
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

        if (targetUserId <= 0) {
            System.out.println("Update cancelled.");
            return;
        }

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

        if (targetUserId == currentAdminId) {

            System.out.println(
                    "WARNING: Modifying the currently active admin "
                            + "account is restricted."
            );

            return;
        }

        String oldRole = targetUser.role();
        String oldStatus = targetUser.accountStatus();

        System.out.println("\nSelected user: " + targetUser.username());
        System.out.println("Current role: " + oldRole);
        System.out.println("Current status: " + oldStatus);

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

        // DAO records the role/status change and audit entry together.
        boolean success = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus,
                currentAdminId
        );

        if (success) {

            System.out.println(
                    "User role and account status updated successfully!"
            );

            System.out.println("Activity recorded in audit logs.");

        } else {

            System.out.println(
                    "Update failed. Please check the database and try again."
            );
        }
    }

    // SYSTEM AUDIT LOG MENU
    private void auditLogMenu() {

        boolean running = true;

        while (running) {

            System.out.println("\n======================================");
            System.out.println("       SYSTEM AUDIT LOGS");
            System.out.println("======================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Admin Dashboard");
            System.out.println("======================================");

            int choice = readIntInput("Choose an option: ");

            switch (choice) {

                case 1 ->
                        viewAllAuditLogs();

                case 2 ->
                        filterLogsByUserId();

                case 3 ->
                        clearHistoricalLogs();

                case 4 ->
                        running = false;

                default ->
                        System.out.println(
                                "Invalid option. Choose 1 to 4."
                        );
            }
        }
    }

    // VIEW ALL AUDIT LOGS
    private void viewAllAuditLogs() {

        System.out.println("\n========== ALL SYSTEM AUDIT LOGS ==========");

        List<ActivityLog> logs = activityLogDao.getAllLogs();

        displayAuditLogs(logs);
    }

    // FILTER AUDIT LOGS BY USER ID
    private void filterLogsByUserId() {

        System.out.println("\n======= FILTER AUDIT LOGS =======");

        int userId = readIntInput(
                "Enter User ID to search (0 to cancel): "
        );

        if (userId <= 0) {
            System.out.println("Search cancelled.");
            return;
        }

        List<ActivityLog> logs =
                activityLogDao.getLogsByUserId(userId);

        System.out.println("\nAudit logs for User ID: " + userId);

        displayAuditLogs(logs);
    }

    // CLEAR HISTORICAL AUDIT LOGS
    private void clearHistoricalLogs() {

        System.out.println("\n======= CLEAR HISTORICAL LOGS =======");

        List<ActivityLog> logs = activityLogDao.getAllLogs();

        if (logs.isEmpty()) {
            System.out.println("No historical logs to clear.");
            return;
        }

        System.out.println("Total logs found: " + logs.size());

        System.out.println(
                "WARNING: This operation will delete all existing logs."
        );

        System.out.print(
                "Type CLEAR to confirm, or anything else to cancel: "
        );

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equals("CLEAR")) {
            System.out.println("Clear operation cancelled.");
            return;
        }

        boolean success = activityLogDao.clearAllLogs();

        if (success) {

            activityLogDao.logAction(
                    currentAdminId,
                    "AUDIT_LOGS_CLEARED",
                    "Admin ID " + currentAdminId
                            + " cleared " + logs.size()
                            + " historical audit log entries."
            );

            System.out.println("Historical logs cleared successfully.");

            System.out.println(
                    "A new audit entry was created to record this action."
            );

        } else {

            System.out.println("Failed to clear historical logs.");
        }
    }

    // DISPLAY AUDIT LOGS
    private void displayAuditLogs(List<ActivityLog> logs) {

        if (logs.isEmpty()) {
            System.out.println("No audit logs found.");
            return;
        }

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(
                "---------------------------------------------------------------------------------------------------------------"
        );

        for (ActivityLog log : logs) {

            System.out.printf(
                    "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                    log.getId(),
                    log.getUserId() == 0
                            ? "SYSTEM"
                            : log.getUserId(),
                    truncate(log.getActionType(), 18),
                    truncate(log.getDescription(), 35),
                    truncate(log.getTimestamp(), 20)
            );
        }

        System.out.println(
                "---------------------------------------------------------------------------------------------------------------"
        );

        System.out.println("Total records: " + logs.size());
    }

    // TRUNCATE LONG TEXT
    private String truncate(String value, int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    // READ INTEGER INPUT
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

    // VALIDATE USER ROLE
    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable roles:");
            System.out.println("1. CUSTOMER");
            System.out.println("2. BAKER");
            System.out.println("3. ORDER_CLERK");
            System.out.println("4. ADMIN");
            System.out.println("Enter 0 to cancel.");

            System.out.print("Enter new role: ");

            String role = scanner.nextLine()
                    .trim()
                    .toUpperCase();

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

                default ->
                        System.out.println(
                                "Invalid role. Choose a listed role and try again."
                        );
            }
        }
    }

    // VALIDATE ACCOUNT STATUS
    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("Enter 0 to cancel.");

            System.out.print("Enter new status: ");

            String status = scanner.nextLine()
                    .trim()
                    .toUpperCase();

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

                default ->
                        System.out.println(
                                "Invalid status. Enter APPROVED, PENDING, or 0."
                        );
            }
        }
    }

    // DISPLAY USERS
    private void displayUsers(List<User> users) {

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.printf(
                "%-8s %-22s %-18s %-16s %-12s%n",
                "ID",
                "FULL NAME",
                "USERNAME",
                "ROLE",
                "STATUS"
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





