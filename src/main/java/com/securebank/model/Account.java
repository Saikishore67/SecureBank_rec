package com.securebank.model;

import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;

public abstract class Account {

    private static final int HISTORY_SIZE = 5;

    private final int accountNumber;
    private final String holderName;
    private final AccountType type;
    private final String pinHash;
    private double balance;
    private int failedAttempts;
    private boolean locked;

    private final Transaction[] history = new Transaction[HISTORY_SIZE];
    private int nextSlot = 0;
    private int count = 0;

    // Constructor 1: opening a brand-new account
    protected Account(int accountNumber, String holderName, AccountType type,
                      double openingBalance, String pinHash) {
        this(accountNumber, holderName, type, openingBalance, pinHash, 0, false);
    }

    // Constructor 2: rebuilding an account from a saved database row
    protected Account(int accountNumber, String holderName, AccountType type,
                      double balance, String pinHash, int failedAttempts, boolean locked) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.type = type;
        this.balance = balance;
        this.pinHash = pinHash;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    // ---------- money operations ----------

    public void deposit(double amount) throws InvalidAmountException {
        deposit(amount, Transaction.Type.DEPOSIT);
    }

    public void deposit(double amount, Transaction.Type type) throws InvalidAmountException {
        validateAmount(amount);
        balance += amount;
        recordTransaction(new Transaction(accountNumber, type, amount, balance));
    }

    public void withdraw(double amount)
            throws InsufficientFundsException, InvalidAmountException {
        withdraw(amount, Transaction.Type.WITHDRAW);
    }

    // Each account type decides its own withdrawal rule
    public abstract void withdraw(double amount, Transaction.Type type)
            throws InsufficientFundsException, InvalidAmountException;

    protected void validateAmount(double amount) throws InvalidAmountException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new InvalidAmountException(amount);
        }
    }

    // Children call this AFTER their own rule check has passed
    protected void applyDebit(double amount, Transaction.Type type) {
        balance -= amount;
        recordTransaction(new Transaction(accountNumber, type, amount, balance));
    }

    // ---------- last-5 transaction history (circular buffer) ----------

    public void recordTransaction(Transaction t) {
        history[nextSlot] = t;
        nextSlot = (nextSlot + 1) % HISTORY_SIZE;
        if (count < HISTORY_SIZE) {
            count++;
        }
    }

    // Newest first
    public Transaction[] getRecentTransactions() {
        Transaction[] result = new Transaction[count];
        for (int i = 0; i < count; i++) {
            int index = (nextSlot - 1 - i + HISTORY_SIZE) % HISTORY_SIZE;
            result[i] = history[index];
        }
        return result;
    }

    public Transaction getLatestTransaction() {
        if (count == 0) {
            return null;
        }
        return history[(nextSlot - 1 + HISTORY_SIZE) % HISTORY_SIZE];
    }

    // ---------- PIN-attempt state (the decisions live in AuthService) ----------

    public void recordFailedAttempt() { failedAttempts++; }
    public void resetFailedAttempts() { failedAttempts = 0; }
    public void lock() { locked = true; }

    // ---------- details ----------

    public String getDetails() {
        return String.format(
                "Account No : %d%nHolder     : %s%nType       : %s%nBalance    : Rs. %.2f%nStatus     : %s",
                accountNumber, holderName, type, balance, locked ? "LOCKED" : "Active");
    }

    // ---------- getters ----------

    public int getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public AccountType getType() { return type; }
    public String getPinHash() { return pinHash; }
    public double getBalance() { return balance; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return locked; }
}