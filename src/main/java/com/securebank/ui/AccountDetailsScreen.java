
package com.securebank.ui;

import com.securebank.exception.AccountLockedException;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.service.Bank;
import com.securebank.util.PinHasher;

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
            System.out.print("Enter your 4-digit PIN: ");
            String pin = scanner.nextLine().trim();

            if (!PinHasher.isValidFormat(pin)) {
                System.out.println("[ERROR] PIN must contain exactly 4 digits.");
                return;
            }

            Bank bank = new Bank();
            Account account = bank.getAccount(accountNumber, pin);

            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println("             ACCOUNT INFORMATION              ");
            System.out.println("----------------------------------------------");

            // Display details returned by the existing service.
            System.out.println(account.getDetails());

            System.out.println("----------------------------------------------");

        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] Account not found.");
        } catch (AccountLockedException e) {
            System.out.println("[ERROR] Account is locked.");
        } catch (InvalidPinException e) {
            System.out.println("[ERROR] Invalid PIN.");
        } catch (SQLException e) {
            System.out.println("[ERROR] A database error occurred. Please try again.");
        }
    }
}
