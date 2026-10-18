
package com.securebank.ui;

import java.util.Scanner;

import com.securebank.util.PinHasher;

public class TransferScreen {

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println("+--------------------------------------+");
        System.out.println("|             SECUREBANK               |");
        System.out.println("|             TRANSFER MONEY           |");
        System.out.println("+--------------------------------------+");

        try {
            System.out.print("Enter sender account number: ");
            String senderInput = scanner.nextLine().trim();

            int senderAccount;
            try {
                senderAccount = Integer.parseInt(senderInput);

                if (senderAccount <= 0) {
                    System.out.println("[ERROR] Enter a valid sender account number.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Sender account number must be numeric.");
                return;
            }

            System.out.print("Enter receiver account number: ");
            String receiverInput = scanner.nextLine().trim();

            int receiverAccount;
            try {
                receiverAccount = Integer.parseInt(receiverInput);

                if (receiverAccount <= 0) {
                    System.out.println("[ERROR] Enter a valid receiver account number.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Receiver account number must be numeric.");
                return;
            }

            if (senderAccount == receiverAccount) {
                System.out.println("[ERROR] Sender and receiver accounts must differ.");
                return;
            }

            System.out.print("Enter your 4-digit PIN: ");
            String pin = scanner.nextLine().trim();

            if (!PinHasher.isValidFormat(pin)) {
                System.out.println("[ERROR] PIN must contain exactly 4 digits.");
                return;
            }

            System.out.print("Enter transfer amount: ");
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
            System.out.println("Transfer details entered successfully.");
            System.out.println("From account : " + senderAccount);
            System.out.println("To account   : " + receiverAccount);
            System.out.printf("Amount       : %.2f%n", amount);
            System.out.println("----------------------------------------");

            // Connect this screen to the Bank service after
            // its method signatures are available.
            System.out.println(
                "[INFO] Transfer service is not connected yet."
            );

        } catch (Exception e) {
            System.out.println(
                "[ERROR] Unable to process the input. Please try again."
            );
        }
    }
}
