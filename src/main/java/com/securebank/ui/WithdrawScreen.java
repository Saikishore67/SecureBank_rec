
package com.securebank.ui;

import java.util.Scanner;

import com.securebank.util.PinHasher;

public class WithdrawScreen {

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println("+--------------------------------------+");
        System.out.println("|            SECUREBANK                |");
        System.out.println("|            WITHDRAW MONEY            |");
        System.out.println("+--------------------------------------+");

        try {
            System.out.print("Enter account number: ");
            String accountInput = scanner.nextLine().trim();

            if (accountInput.isEmpty()) {
                System.out.println("[ERROR] Account number cannot be empty.");
                return;
            }

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

            System.out.print("Enter withdrawal amount: ");
            String amountInput = scanner.nextLine().trim();

            double amount;
            try {
                amount = Double.parseDouble(amountInput);

                if (!Double.isFinite(amount) || amount <= 0) {
                    System.out.println("[ERROR] Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Enter a valid numeric amount.");
                return;
            }

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("Withdrawal details entered successfully.");
            System.out.println("Account number : " + accountNumber);
            System.out.printf("Amount         : %.2f%n", amount);
            System.out.println("----------------------------------------");

            // Service integration will be added after the Bank
            // service method signatures are confirmed.
            System.out.println(
                "[INFO] Withdrawal service is not connected yet."
            );

        } catch (Exception e) {
            System.out.println(
                "[ERROR] Unable to process the input. Please try again."
            );
        }
    }
}
