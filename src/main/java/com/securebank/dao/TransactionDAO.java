package com.securebank.dao;

import com.securebank.model.Transaction;
import com.securebank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * JDBC data-access object for the transactions table.
 *
 * This DAO persists transaction records and reads them back. It deliberately
 * does not update account balances; that belongs to the service/account layer.
 */
public class TransactionDAO {

    private static final String INSERT_SQL = """
            INSERT INTO transactions
                (account_number, type, amount, balance_after, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT transaction_id, account_number, type, amount, balance_after, created_at
            FROM transactions
            WHERE transaction_id = ?
            """;

    private static final String FIND_BY_ACCOUNT_SQL = """
            SELECT transaction_id, account_number, type, amount, balance_after, created_at
            FROM transactions
            WHERE account_number = ?
            ORDER BY transaction_id DESC
            """;

    private static final String FIND_RECENT_SQL = """
            SELECT transaction_id, account_number, type, amount, balance_after, created_at
            FROM transactions
            WHERE account_number = ?
            ORDER BY transaction_id DESC
            LIMIT ?
            """;

    /**
     * Inserts one transaction and returns a Transaction containing the
     * generated database ID and persisted timestamp.
     *
     * The account row must already exist because transactions.account_number
     * has a foreign-key constraint to accounts.account_number.
     */
    public Transaction save(Connection connection, Transaction transaction) throws SQLException {
        Objects.requireNonNull(transaction, "transaction must not be null");

        if (transaction.getAccountNumber() <= 0) {
            throw new IllegalArgumentException("Account number must be positive.");
        }
        Objects.requireNonNull(transaction.getType(), "transaction type must not be null");

        if (!Double.isFinite(transaction.getAmount()) || transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be finite and positive.");
        }
        if (!Double.isFinite(transaction.getBalanceAfter())) {
            throw new IllegalArgumentException("Balance after transaction must be finite.");
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, transaction.getAccountNumber());
            statement.setString(2, transaction.getType().name());
            statement.setBigDecimal(3, java.math.BigDecimal.valueOf(transaction.getAmount()));
            statement.setBigDecimal(4, java.math.BigDecimal.valueOf(transaction.getBalanceAfter()));

            LocalDateTime timestamp = transaction.getTimestamp() == null
                    ? LocalDateTime.now()
                    : transaction.getTimestamp();
            statement.setTimestamp(5, Timestamp.valueOf(timestamp));

            int affectedRows = statement.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException("Inserting transaction failed: expected one row, got "
                        + affectedRows + ".");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new Transaction(
                            generatedId,
                            transaction.getAccountNumber(),
                            transaction.getType(),
                            transaction.getAmount(),
                            transaction.getBalanceAfter(),
                            timestamp
                    );
                }
            }

            throw new SQLException("Inserting transaction failed: no generated transaction ID returned.");
        }
    }

    /** Finds one transaction by its generated ID, or returns null if absent. */
    public Transaction findById(int transactionId) throws SQLException {
        if (transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive.");
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, transactionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    /**
     * Returns all transactions for an account, newest first.
     * Returns an empty list when the account has no transactions.
     */
    public List<Transaction> findByAccountNumber(int accountNumber) throws SQLException {
        validateAccountNumber(accountNumber);
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ACCOUNT_SQL)) {
            statement.setInt(1, accountNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapRow(resultSet));
                }
            }
        }
        return transactions;
    }

    /**
     * Returns at most {@code limit} newest transactions for an account,
     * newest first. Use limit=5 for the statement requirement in the README.
     */
    public List<Transaction> findRecentByAccountNumber(int accountNumber, int limit)
            throws SQLException {
        validateAccountNumber(accountNumber);
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive.");
        }

        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_RECENT_SQL)) {
            statement.setInt(1, accountNumber);
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapRow(resultSet));
                }
            }
        }
        return transactions;
    }

    private static Transaction mapRow(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp == null ? null : timestamp.toLocalDateTime();

        try {
            return new Transaction(
                    resultSet.getInt("transaction_id"),
                    resultSet.getInt("account_number"),
                    Transaction.Type.valueOf(resultSet.getString("type")),
                    resultSet.getBigDecimal("amount").doubleValue(),
                    resultSet.getBigDecimal("balance_after").doubleValue(),
                    createdAt
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new SQLException("Invalid transaction row in database.", exception);
        }
    }

    private static void validateAccountNumber(int accountNumber) {
        if (accountNumber <= 0) {
            throw new IllegalArgumentException("Account number must be positive.");
        }
    }
}
