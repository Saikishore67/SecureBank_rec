
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

public class TransferScreen {

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println("+--------------------------------------+");
        System.out.println("|             SECUREBANK               |");
        System.out.println("|             TRANSFER MONEY           |");
        System.out.println("+--------------------------------------+");

        try {
            System.out.print("Enter your account number: ");
            int fromAccount = readAccountNumber(scanner);

            if (fromAccount <= 0) {
                System.out.println("[ERROR] Enter a valid account number.");
                return;
            }

            System.out.print("Enter receiver account number: ");
            int toAccount = readAccountNumber(scanner);

            if (toAccount <= 0) {
                System.out.println("[ERROR] Enter a valid account number.");
                return;
            }

            if (fromAccount == toAccount) {
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

            Bank bank = new Bank();
            Account account = bank.transfer(
                    fromAccount, toAccount, pin, amount);

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("[SUCCESS] Transfer completed.");
            System.out.printf("Transferred amount: Rs. %.2f%n", amount);
            System.out.printf("Remaining balance : Rs. %.2f%n",
                    account.getBalance());
            System.out.println("----------------------------------------");

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Account number must be numeric.");
        } catch (AccountNotFoundException e) {
            System.out.println("[ERROR] Sender or receiver account not found.");
        } catch (AccountLockedException e) {
            System.out.println("[ERROR] Sender account is locked.");
        } catch (InvalidPinException e) {
            System.out.println("[ERROR] Invalid PIN.");
        } catch (InsufficientFundsException e) {
            System.out.println("[ERROR] Insufficient funds for this transfer.");
        } catch (InvalidAmountException e) {
            System.out.println("[ERROR] Invalid transfer amount.");
        } catch (SQLException e) {
            System.out.println("[ERROR] A database error occurred. Please try again.");
        } catch (RuntimeException e) {
            System.out.println("[ERROR] Unable to complete transfer: "
                    + e.getMessage());
        }
    }

    private static int readAccountNumber(Scanner scanner) {
        return Integer.parseInt(scanner.nextLine().trim());
    }
}
