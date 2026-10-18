
package com.securebank.ui;

import java.util.Scanner;

public class AccountDetailsScreen {

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println("+----------------------------------------------+");
        System.out.println("|                 SECUREBANK                   |");
        System.out.println("|               ACCOUNT DETAILS                |");
        System.out.println("+----------------------------------------------+");

        try {
            System.out.print("Enter account number: ");
            String accountInput = scanner.nextLine().trim();

            int accountNumber;
            try {
                accountNumber = Integer.parseInt(accountInput);

                if (accountNumber <= 0) {
                    System.out.println(
                        "[ERROR] Account number must be greater than zero."
                    );
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(
                    "[ERROR] Account number must be numeric."
                );
                return;
            }

            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println("             ACCOUNT INFORMATION              ");
            System.out.println("----------------------------------------------");
            System.out.println("Account number : " + accountNumber);

            // Actual account information will be retrieved
            // through the Bank service after integration.
            System.out.println();
            System.out.println(
                "[INFO] Account details service is not connected yet."
            );

            System.out.println("----------------------------------------------");

        } catch (Exception e) {
            System.out.println(
                "[ERROR] Unable to retrieve account details. Please try again."
            );
        }
    }
}
