package com.securebank;

import com.securebank.service.Bank;
import com.securebank.ui.MainMenu;

import java.util.Scanner;

public class BankApp {

    public static void main(String[] args) {

        System.out.println("Starting SecureBank...");

        Bank bank = new Bank();

        try (Scanner scanner = new Scanner(System.in)) {
            MainMenu.show(scanner, bank);
        }
    }
}