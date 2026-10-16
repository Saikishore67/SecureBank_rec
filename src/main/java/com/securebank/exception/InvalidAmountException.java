package com.securebank.exception;

public class InvalidAmountException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidAmountException(double amount) {
        super("Invalid amount: " + amount + ". Amount must be a positive number.");
    }
}