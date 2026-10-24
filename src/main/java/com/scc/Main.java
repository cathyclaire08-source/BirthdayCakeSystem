

package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Step 1: Initialize the database before starting the application.
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (RuntimeException e) {
            System.err.println("Cannot start the application.");
            System.err.println("Database initialization failed.");
            return;
        }

        // Step 2: Create shared instances.
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // Step 3: Delegate registration, login, and role routing.
        Authentication authentication =
                new Authentication(userDao, scanner);

        // Step 4: Start the authentication menu.
        authentication.start();
    }
}


