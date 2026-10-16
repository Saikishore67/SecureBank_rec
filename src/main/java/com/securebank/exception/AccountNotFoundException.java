package com.securebank.exception;

public class AccountNotFoundException extends Exception{
    private final long accountNumber;

//    passed account number does not exist in database
    public AccountNotFoundException(int accountNumber) {
        super("Account " + accountNumber + " not found.");
        this.accountNumber = accountNumber;
    }

//    Getting account number
    public long getAccountNumber() {
        return accountNumber;
    }
}
