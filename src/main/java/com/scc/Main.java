
package com.scc;

import com.scc.dao.UserDao;
import java.util.Scanner;
import com.scc.ui.MainMenu;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n================================");
            System.out.println("  BIRTHDAY CAKE ORDERING SYSTEM");
            System.out.println("================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    registerUser();
                    break;

                case "2":
                    System.out.println("Login feature is not implemented yet.");
                    break;

                case "3":
                    System.out.println("Thank you for using the system!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void registerUser() {
        System.out.println("\n--- USER REGISTRATION ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter contact number: ");
        String contactNumber = scanner.nextLine().trim();

        System.out.println("Select your role:");
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
                    "Registration successful! Your account is pending approval."
            );
        } else {
            System.out.println(
                    "Registration failed. Please check the error above."
            );
        }
    }
}

