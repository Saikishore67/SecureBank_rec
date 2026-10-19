
package com.securebank.ui;

import com.securebank.exception.AccountLockedException;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.service.Bank;
import com.securebank.util.PinHasher;

import java.sql.SQLException;
import java.util.Scanner;

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

            // Call the actual banking service.
            Bank bank = new Bank();
            Account account = bank.withdraw(accountNumber, pin, amount);

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("[SUCCESS] Withdrawal completed.");
            System.out.printf("Withdrawn amount : Rs. %.2f%n", amount);
            System.out.printf("Remaining balance: Rs. %.2f%n",
                    account.getBalance());
            System.out.println("----------------------------------------");

        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] Account not found.");
        } catch (AccountLockedException e) {
            System.out.println("[ERROR] Account is locked. Please contact the bank.");
        } catch (InvalidPinException e) {
            System.out.println("[ERROR] Invalid PIN.");
        } catch (InsufficientFundsException e) {
            System.out.println("[ERROR] Insufficient funds for this withdrawal.");
        } catch (InvalidAmountException e) {
            System.out.println("[ERROR] Invalid withdrawal amount.");
        } catch (SQLException e) {
            System.out.println("[ERROR] A database error occurred. Please try again.");
        } catch (RuntimeException e) {
            System.out.println("[ERROR] Unable to complete withdrawal: "
                    + e.getMessage());
        }
    }
}
