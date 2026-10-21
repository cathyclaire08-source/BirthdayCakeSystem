package com.scc;

import com.scc.dao.UserDao;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {

        System.out.println("\n==========================");
        System.out.println(" BIRTHDAY CAKE SYSTEM ");
        System.out.println("\n==========================");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Enter username: ");
                String loginUsername = sc.nextLine();

                System.out.print("Enter password: ");
                String loginPassword = sc.nextLine();

                boolean success = userDao.loginUser(
                        loginUsername,
                        loginPassword
                );

                if (success) {
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Invalid username or password.");
                }

                break;

            case "2":
                System.out.print("Enter new username: ");
                String user = sc.nextLine();

                System.out.print("Enter full name: ");
                String fullName = sc.nextLine();

                System.out.print("Enter password: ");
                String pass = sc.nextLine();

                System.out.print("Enter contact number: ");
                String contactNumber = sc.nextLine();

                System.out.print("Select Role [1] CUSTOMER | [2] BAKER: ");
                String roleChoice = sc.nextLine();

                String role;

                switch (roleChoice) {
                    case "1":
                        role = "CUSTOMER";
                        break;
                    case "2":
                        role = "BAKER";
                        break;
                    default:
                        role = null;

                }


                if (role != null) {

                    userDao.registerUser(user, fullName,  pass, role, contactNumber);
                } else {

                    System.out.println("Invalid role selection!");
                }
                break;
        }

    }

    }
