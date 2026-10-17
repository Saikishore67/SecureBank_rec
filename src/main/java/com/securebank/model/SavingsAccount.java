package com.securebank.model;

import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;

public class SavingsAccount extends Account {

    public static final double MIN_BALANCE = 1000.0;
    public static final double INTEREST_RATE = 0.04;

    // Opening a brand-new Savings account
    public SavingsAccount(int accountNumber, String holderName,
                          double openingBalance, String pinHash) {
        super(accountNumber, holderName, AccountType.SAVINGS, openingBalance, pinHash);
        if (Double.isNaN(openingBalance) || openingBalance < MIN_BALANCE) {
            throw new IllegalArgumentException(
                    "Savings account needs an opening balance of at least Rs. " + MIN_BALANCE);
        }
    }

    // Rebuilding a Savings account from a saved database row
    public SavingsAccount(int accountNumber, String holderName, double balance,
                          String pinHash, int failedAttempts, boolean locked) {
        super(accountNumber, holderName, AccountType.SAVINGS, balance, pinHash,
                failedAttempts, locked);
    }

    @Override
    public void withdraw(double amount, Transaction.Type type)
            throws InsufficientFundsException, InvalidAmountException {
        validateAmount(amount);
        double available = getBalance() - MIN_BALANCE;
        if (amount > available) {
            String message = String.format(
                    "Minimum balance of Rs. %.2f must be maintained. Available to withdraw: Rs. %.2f, short by Rs. %.2f.",
                    MIN_BALANCE, available, amount - available);
            throw new InsufficientFundsException(message, amount, available);
        }
        applyDebit(amount, type);
    }

    // One year of interest. Returns the amount credited.
    public double applyInterest() {
        double interest = Math.round(getBalance() * INTEREST_RATE * 100.0) / 100.0;
        applyCredit(interest, Transaction.Type.INTEREST);
        return interest;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + String.format(
                "%nMin Balance: Rs. %.2f%nInterest   : %.0f%% per year",
                MIN_BALANCE, INTEREST_RATE * 100);
    }
}