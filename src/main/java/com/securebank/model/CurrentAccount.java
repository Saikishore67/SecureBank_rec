package com.securebank.model;

import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;

public class CurrentAccount extends Account {

    public static final double OVERDRAFT_LIMIT = 5000.0;

    // Opening a brand-new Current account
    public CurrentAccount(int accountNumber, String holderName,
                          double openingBalance, String pinHash) {
        super(accountNumber, holderName, AccountType.CURRENT, openingBalance, pinHash);
        if (Double.isNaN(openingBalance) || openingBalance < 0) {
            throw new IllegalArgumentException(
                    "Opening balance cannot be negative.");
        }
    }

    // Rebuilding a Current account from a saved database row
    public CurrentAccount(int accountNumber, String holderName, double balance,
                          String pinHash, int failedAttempts, boolean locked) {
        super(accountNumber, holderName, AccountType.CURRENT, balance, pinHash,
                failedAttempts, locked);
    }

    @Override
    public void withdraw(double amount, Transaction.Type type)
            throws InsufficientFundsException, InvalidAmountException {
        validateAmount(amount);
        double available = getBalance() + OVERDRAFT_LIMIT;
        if (amount > available) {
            String message = String.format(
                    "Overdraft limit of Rs. %.2f exceeded. Available to withdraw: Rs. %.2f, short by Rs. %.2f.",
                    OVERDRAFT_LIMIT, available, amount - available);
            throw new InsufficientFundsException(message, amount, available);
        }
        applyDebit(amount, type);
    }

    @Override
    public String getDetails() {
        return super.getDetails() + String.format(
                "%nOverdraft  : Rs. %.2f", OVERDRAFT_LIMIT);
    }
}