package com.securebank.model;

import java.time.LocalDateTime;

public class Transaction {

    public enum Type {
        DEPOSIT,
        WITHDRAW,
        TRANSFER_IN,
        TRANSFER_OUT,
        INTEREST
    }

    private final int transactionId;
    private final int accountNumber;
    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;

    // Full constructor: used by the DAO when loading a saved row
    public Transaction(int transactionId, int accountNumber, Type type,
                       double amount, double balanceAfter, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
    }

    // Convenience constructor: used when a new transaction happens right now
    public Transaction(int accountNumber, Type type, double amount, double balanceAfter) {
        this(0, accountNumber, type, amount, balanceAfter, LocalDateTime.now());
    }

    public int getTransactionId() { return transactionId; }
    public int getAccountNumber() { return accountNumber; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getTimestamp() { return timestamp; }
}