
package com.securebank.ui;

import com.securebank.exception.AccountNotFoundException;
import com.securebank.model.Account;
import com.securebank.service.Bank;

import java.sql.SQLException;
import java.util.Scanner;

public class AccountDetailsScreen {

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println("+----------------------------------------------+");
        System.out.println("|                 SECUREBANK                   |");
        System.out.println("|               ACCOUNT DETAILS                |");
        System.out.println("+----------------------------------------------+");

        System.out.print("Enter account number: ");
        String accountInput = scanner.nextLine().trim();

        int accountNumber;
        try {
            accountNumber = Integer.parseInt(accountInput);

            if (accountNumber <= 0) {
                System.out.println(
                        "[ERROR] Account number must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Account number must be numeric.");
            return;
        }

        try {
            Bank bank = new Bank();
            Account account = bank.getAccount(accountNumber);

            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println("             ACCOUNT INFORMATION              ");
            System.out.println("----------------------------------------------");

            // Display details returned by the existing service.
            System.out.println(account.getDetails());

            System.out.println("----------------------------------------------");

        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] Account not found.");
        } catch (SQLException e) {
            System.out.println(
                    "[ERROR] A database error occurred. Please try again.");
        }
    }
}
