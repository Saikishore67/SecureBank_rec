package com.securebank.ui;

import com.securebank.service.Bank;

import java.util.Scanner;

/**
 * Console entry point for SecureBank's interactive menu.
 */
public final class MainMenu {

    private MainMenu() {
        // Utility class.
    }

    public static void show(Scanner scanner, Bank bank) {
        boolean running = true;

        while (running) {
            ConsoleUtil.printHeader("MAIN MENU");
            System.out.println("  1. Open account");
            System.out.println("  2. Deposit money");
            System.out.println("  3. Withdraw money");
            System.out.println("  4. Transfer money");
            System.out.println("  5. Account details");
            System.out.println("  6. Account statement");
            System.out.println("  0. Exit");
            ConsoleUtil.printDivider();

            int choice = InputHelper.readIntInRange(
                    scanner, "Choose an option: ", 0, 6);

            try {
                switch (choice) {
                    case 1 -> OpenAccount.show(scanner, bank);
                    case 2 -> DepositScreen.show(scanner, bank);
                    case 3 -> WithdrawScreen.show(scanner);
                    case 4 -> TransferScreen.show(scanner);
                    case 5 -> AccountDetailsScreen.show(scanner);
                    case 6 -> StatementScreen.show(scanner);
                    case 0 -> {
                        running = false;
                        ConsoleUtil.printSuccess("Thank you for using SecureBank. Goodbye!");
                    }
                    default -> ConsoleUtil.printError("Invalid menu option.");
                }
            } catch (RuntimeException exception) {
                // Keep an unexpected UI failure from terminating the menu loop.
                ConsoleUtil.printError("Something went wrong: " + exception.getMessage());
            }

            if (running) {
                ConsoleUtil.pause(scanner);
            }
        }
    }
}
