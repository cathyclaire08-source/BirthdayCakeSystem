

package com.scc.ui;

import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner;

    public MainMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public String showMenu() {
        System.out.println("\n================================");
        System.out.println("  BIRTHDAY CAKE ORDERING SYSTEM");
        System.out.println("================================");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");

        return scanner.nextLine().trim();
    }
}

