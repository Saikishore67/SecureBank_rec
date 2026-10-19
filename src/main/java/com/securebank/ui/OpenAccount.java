package com.securebank.ui;

import com.securebank.exception.InvalidAmountException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.model.AccountType;
import com.securebank.service.Bank;

import java.sql.SQLException;
import java.util.Scanner;

/** Screen for opening a Savings or Current account. */
public final class OpenAccount {

    private OpenAccount() {
        // Utility class.
    }

    public static void show(Scanner scanner, Bank bank) {
        ConsoleUtil.printHeader("OPEN NEW ACCOUNT");

        String holderName = InputHelper.readNonBlank(scanner, "Enter account holder name: ");
        AccountType type = InputHelper.readAccountType(scanner);

        double openingDeposit;
        if (type == AccountType.SAVINGS) {
            System.out.printf("Minimum opening balance for Savings: Rs. %.2f%n",
                    com.securebank.model.SavingsAccount.MIN_BALANCE);
            openingDeposit = InputHelper.readNonNegativeAmount(
                    scanner, "Enter opening deposit (Rs.): ");
            while (openingDeposit < com.securebank.model.SavingsAccount.MIN_BALANCE) {
                ConsoleUtil.printError(String.format(
                        "Savings account requires at least Rs. %.2f.",
                        com.securebank.model.SavingsAccount.MIN_BALANCE));
                openingDeposit = InputHelper.readNonNegativeAmount(
                        scanner, "Enter opening deposit (Rs.): ");
            }
        } else {
            openingDeposit = InputHelper.readNonNegativeAmount(
                    scanner, "Enter opening deposit (Rs.): ");
        }

        String pin = InputHelper.readPin(scanner, "Create a 4-digit PIN: ");
        String confirmPin = InputHelper.readPin(scanner, "Confirm your 4-digit PIN: ");

        while (!pin.equals(confirmPin)) {
            ConsoleUtil.printError("PINs do not match. Please try again.");
            pin = InputHelper.readPin(scanner, "Create a 4-digit PIN: ");
            confirmPin = InputHelper.readPin(scanner, "Confirm your 4-digit PIN: ");
        }

        try {
            Account account = bank.openAccount(type, holderName, openingDeposit, pin);

            ConsoleUtil.printSuccess("Your account has been created.");
            ConsoleUtil.printDivider();
            System.out.println(account.getDetails());
            ConsoleUtil.printDivider();
            ConsoleUtil.printInfo("Keep your account number safe. Never share your PIN.");
        } catch (InvalidAmountException | InvalidPinException exception) {
            ConsoleUtil.printError(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            ConsoleUtil.printError(exception.getMessage());
        } catch (SQLException exception) {
            ConsoleUtil.printError(
                    "Account creation failed because the database operation was unsuccessful.");
            System.err.println("Database detail: " + exception.getMessage());
        }
    }
}
