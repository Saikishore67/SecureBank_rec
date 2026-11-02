package com.securebank.ui;

import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.exception.InvalidPinException;
import com.securebank.exception.AccountLockedException;
import com.securebank.model.Account;
import com.securebank.service.Bank;

import java.sql.SQLException;
import java.util.Scanner;

/** Screen for depositing money into an existing account. */
public final class DepositScreen {

    private DepositScreen() {
        // Utility class.
    }

    public static void show(Scanner scanner, Bank bank) {
        ConsoleUtil.printHeader("DEPOSIT MONEY");

        int accountNumber =
                InputHelper.readPositiveInt(scanner, "Enter account number: ");
        double amount =
                InputHelper.readPositiveAmount(scanner, "Enter deposit amount (Rs.): ");
        String pin =
                InputHelper.readPin(scanner, "Enter your 4-digit PIN: ");

        try {
            Account account = bank.deposit(accountNumber, amount, pin);

            ConsoleUtil.printSuccess("Deposit completed successfully.");
            System.out.println("Account number      : " + account.getAccountNumber());
            System.out.println("Account holder      : " + account.getHolderName());
            ConsoleUtil.printMoney("Deposited amount", amount);
            ConsoleUtil.printMoney("Available balance", account.getBalance());
        } catch (AccountNotFoundException | InvalidAmountException exception) {
            ConsoleUtil.printError(exception.getMessage());
        } catch (SQLException exception) {
            ConsoleUtil.printError(
                    "The deposit could not be saved. Please check the database connection.");
            System.err.println("Database detail: " + exception.getMessage());
        } catch (AccountLockedException e) {
            System.out.println("[ERROR] You're account is locker.");
        } catch (InvalidPinException e) {
            System.out.println("[ERROR] Invalid Pin");
        }
    }
}
