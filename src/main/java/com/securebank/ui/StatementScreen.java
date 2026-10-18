
package com.securebank.ui;

import java.util.Scanner;

import com.securebank.util.PinHasher;

public class StatementScreen {

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println("+------------------------------------------------------+");
        System.out.println("|                    SECUREBANK                        |");
        System.out.println("|                 ACCOUNT STATEMENT                    |");
        System.out.println("+------------------------------------------------------+");

        try {
            System.out.print("Enter account number: ");
            String accountInput = scanner.nextLine().trim();

            int accountNumber;
            try {
                accountNumber = Integer.parseInt(accountInput);

                if (accountNumber <= 0) {
                    System.out.println("[ERROR] Enter a valid account number.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Account number must be numeric.");
                return;
            }

            System.out.print("Enter your 4-digit PIN: ");
            String pin = scanner.nextLine().trim();

            if (!PinHasher.isValidFormat(pin)) {
                System.out.println("[ERROR] PIN must contain exactly 4 digits.");
                return;
            }

            System.out.println();
            System.out.println("------------------------------------------------------+");
            System.out.println(" Account: " + accountNumber);
            System.out.println("------------------------------------------------------+");
            System.out.printf("%-8s %-16s %12s %14s%n",
                    "Type", "Date & Time", "Amount", "Balance");
            System.out.println("------------------------------------------------------+");

            // Transaction history will be retrieved through
            // the Bank service once its method signature is available.
            System.out.println("[INFO] Statement service is not connected yet.");

            System.out.println("------------------------------------------------------+");

        } catch (Exception e) {
            System.out.println(
                "[ERROR] Unable to retrieve the statement. Please try again."
            );
        }
    }
}
