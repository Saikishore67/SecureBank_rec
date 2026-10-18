package com.securebank.service;

import com.securebank.dao.AccountDAO;
import com.securebank.dao.TransactionDAO;
import com.securebank.exception.AccountLockedException;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.model.AccountType;
import com.securebank.model.SavingsAccount;
import com.securebank.model.Transaction;
import com.securebank.util.DBConnection;
import com.securebank.util.PinHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Bank {

    private static final int STATEMENT_SIZE = 5;
    private static final int MAX_NAME_LENGTH = 100;

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final AuthService authService;

    public Bank() {
        this(new AccountDAO(), new TransactionDAO());
    }

    public Bank(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this(accountDAO, transactionDAO, new AuthService(accountDAO));
    }

    public Bank(AccountDAO accountDAO, TransactionDAO transactionDAO,
                AuthService authService) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.authService = authService;
    }

    // ---------- open account ----------

    public Account openAccount(AccountType type, String holderName,
                               double initialDeposit, String pin)
            throws InvalidAmountException, InvalidPinException, SQLException {

        if (type == null) {
            throw new IllegalArgumentException("Account type is required.");
        }
        if (holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException("Holder name is required.");
        }
        String name = holderName.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Holder name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        if (!PinHasher.isValidFormat(pin)) {
            throw new InvalidPinException("PIN must contain exactly 4 digits.");
        }
        if (!Double.isFinite(initialDeposit) || initialDeposit < 0) {
            throw new InvalidAmountException(initialDeposit);
        }

        double opening = round2(initialDeposit);

        if (type == AccountType.SAVINGS && opening < SavingsAccount.MIN_BALANCE) {
            throw new IllegalArgumentException(String.format(
                    "A Savings account needs an opening deposit of at least Rs. %.2f.",
                    SavingsAccount.MIN_BALANCE));
        }

        Account account = accountDAO.createAccount(
                name, type, opening, PinHasher.hash(pin));

        // The opening money shows up as the first line of the statement
        if (opening > 0) {
            Transaction first = new Transaction(
                    account.getAccountNumber(), Transaction.Type.DEPOSIT,
                    opening, opening);
            account.recordTransaction(first);
            try (Connection connection = DBConnection.getConnection()) {
                transactionDAO.save(connection, first);
            }
        }
        return account;
    }


    public Account getAccount(int accountNumber)
            throws AccountNotFoundException, SQLException {
        return requireAccount(accountNumber);
    }


    public Account deposit(int accountNumber, double amount)
            throws AccountNotFoundException, InvalidAmountException, SQLException {

        double value = normaliseAmount(amount);
        Account account = requireAccount(accountNumber);

        account.deposit(value);
        saveChanges(account);
        return account;
    }

    // ---------- withdraw ----------

    public Account withdraw(int accountNumber, String pin, double amount)
            throws AccountNotFoundException, AccountLockedException,
            InvalidPinException, InsufficientFundsException,
            InvalidAmountException, SQLException {

        double value = normaliseAmount(amount);
        Account account = requireAccount(accountNumber);

        authService.verifyPin(account, pin);

        // Savings or Current applies its own rule here (polymorphism)
        account.withdraw(value);
        saveChanges(account);
        return account;
    }

    // ---------- transfer ----------

    // Returns the sender's account so the UI can show the new balance
    public Account transfer(int fromAccount, int toAccount, String pin, double amount)
            throws AccountNotFoundException, AccountLockedException,
            InvalidPinException, InsufficientFundsException,
            InvalidAmountException, SQLException {

        if (fromAccount == toAccount) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must differ.");
        }

        double value = normaliseAmount(amount);

        Account sender = requireAccount(fromAccount);
        authService.verifyPin(sender, pin);
        Account receiver = requireAccount(toAccount);

        // Both changes happen in memory first. If the sender lacks funds,
        // the exception stops everything before the database is touched.
        sender.withdraw(value, Transaction.Type.TRANSFER_OUT);
        receiver.deposit(value, Transaction.Type.TRANSFER_IN);

        // One database transaction: both accounts and both history rows,
        // or nothing at all.
        saveChanges(sender, receiver);
        return sender;
    }

    // ---------- statement ----------

    // Newest first, at most 5 entries
    public List<Transaction> getStatement(int accountNumber, String pin)
            throws AccountNotFoundException, AccountLockedException,
            InvalidPinException, SQLException {

        Account account = requireAccount(accountNumber);
        authService.verifyPin(account, pin);
        return transactionDAO.findRecentByAccountNumber(accountNumber, STATEMENT_SIZE);
    }

    // ---------- saving ----------
    public void saveChanges(Account... accounts) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (Account account : accounts) {
                    if (!accountDAO.updateAccount(account)) {
                        throw new SQLException("Account "
                                + account.getAccountNumber()
                                + " could not be updated.");
                    }
                    Transaction latest = account.getLatestTransaction();
                    if (latest != null) {
                        transactionDAO.save(connection, latest);
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // ---------- helpers ----------
    private Account requireAccount(int accountNumber)
            throws AccountNotFoundException, SQLException {
        return accountDAO.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    // Reject bad amounts and round to 2 decimals to match DECIMAL(15,2)
    private double normaliseAmount(double amount) throws InvalidAmountException {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        double rounded = round2(amount);
        if (rounded <= 0) {
            throw new InvalidAmountException(amount);
        }
        return rounded;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}