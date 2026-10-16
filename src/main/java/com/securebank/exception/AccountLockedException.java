package com.securebank.exception;

public class AccountLockedException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int accountNumber;
    public AccountLockedException(int accountNumber) {
        super("Account " + accountNumber + " is locked after too many wrong PIN attempts.");
        this.accountNumber = accountNumber;
    }
    public int getAccountNumber() {
        return accountNumber;
    }
}