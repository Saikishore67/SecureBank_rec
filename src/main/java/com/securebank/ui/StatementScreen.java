
package com.securebank.ui;

import com.securebank.exception.AccountLockedException;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Transaction;
import com.securebank.service.Bank;
import com.securebank.util.PinHasher;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

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

            Bank bank = new Bank();
            List<Transaction> transactions =
                    bank.getStatement(accountNumber, pin);

            System.out.println();
            System.out.println(" Account: " + accountNumber);
            System.out.println("------------------------------------------------------+");
            System.out.printf("%-12s %-20s %12s %14s%n",
                    "Type", "Date & Time", "Amount", "Balance");
            System.out.println("------------------------------------------------------+");

            if (transactions.isEmpty()) {
                System.out.println("[INFO] No transactions found.");
            } else {
                for (Transaction transaction : transactions) {
                    System.out.printf("%-12s %-20s %12.2f %14.2f%n",
                            transaction.getType(),
                            String.valueOf(transaction.getTimestamp()),
                            transaction.getAmount(),
                            transaction.getBalanceAfter());
                }
            }

            System.out.println("------------------------------------------------------+");

        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] Account not found.");
        } catch (AccountLockedException e) {
            System.out.println("[ERROR] Account is locked.");
        } catch (InvalidPinException e) {
            System.out.println("[ERROR] Invalid PIN.");
        } catch (SQLException e) {
            System.out.println(
                    "[ERROR] A database error occurred. Please try again.");
        } catch (RuntimeException e) {
            System.out.println(
                    "[ERROR] Unable to retrieve the statement: "
                            + e.getMessage());
        }
    }
}
