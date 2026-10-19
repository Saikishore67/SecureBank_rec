package com.securebank.ui;

import com.securebank.model.AccountType;
import com.securebank.util.PinHasher;

import java.util.Scanner;

/** Validates console input without creating additional Scanner instances. */
public final class InputHelper {

    private InputHelper() {
        // Utility class.
    }

    public static String readNonBlank(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            ConsoleUtil.printError("This field cannot be empty.");
        }
    }

    public static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                int number = Integer.parseInt(value);
                if (number > 0) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // Print the same validation message below.
            }

            ConsoleUtil.printError("Enter a whole number greater than zero.");
        }
    }

    public static int readIntInRange(
            Scanner scanner, String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                int number = Integer.parseInt(value);
                if (number >= minimum && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // Print the same validation message below.
            }

            ConsoleUtil.printError(
                    "Enter a number between " + minimum + " and " + maximum + ".");
        }
    }

    public static double readPositiveAmount(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                double amount = Double.parseDouble(value);
                if (Double.isFinite(amount) && amount > 0) {
                    return Math.round(amount * 100.0) / 100.0;
                }
            } catch (NumberFormatException ignored) {
                // Print the same validation message below.
            }

            ConsoleUtil.printError("Enter a valid amount greater than zero (e.g. 250.00).");
        }
    }

    public static double readNonNegativeAmount(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            try {
                double amount = Double.parseDouble(value);
                if (Double.isFinite(amount) && amount >= 0) {
                    return Math.round(amount * 100.0) / 100.0;
                }
            } catch (NumberFormatException ignored) {
                // Print the same validation message below.
            }

            ConsoleUtil.printError("Enter a valid amount that is zero or greater.");
        }
    }

    public static String readPin(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String pin = scanner.nextLine().trim();

            if (PinHasher.isValidFormat(pin)) {
                return pin;
            }

            ConsoleUtil.printError("PIN must contain exactly 4 digits.");
        }
    }

    public static AccountType readAccountType(Scanner scanner) {
        while (true) {
            System.out.println("Account type:");
            System.out.println("  1. Savings");
            System.out.println("  2. Current");
            int choice = readIntInRange(scanner, "Choose account type: ", 1, 2);

            return choice == 1 ? AccountType.SAVINGS : AccountType.CURRENT;
        }
    }
}
