package com.securebank.ui;

import java.util.Scanner;

/** Shared formatting and console helpers for SecureBank screens. */
public final class ConsoleUtil {

    private static final int WIDTH = 50;

    private ConsoleUtil() {
        // Utility class.
    }

    public static void printHeader(String title) {
        String safeTitle = title == null ? "" : title.trim();
        printDivider();
        System.out.printf("| %-46s |%n", safeTitle);
        printDivider();
    }

    public static void printDivider() {
        System.out.println("+" + "-".repeat(WIDTH - 2) + "+");
    }

    public static void printSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printMoney(String label, double amount) {
        System.out.printf("%-20s Rs. %,10.2f%n", label + ":", amount);
    }

    public static void pause(Scanner scanner) {
        System.out.println();
        System.out.print("Press Enter to return to the main menu...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
