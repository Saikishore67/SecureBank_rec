
package com.securebank.dao;

import com.securebank.model.Account;
import com.securebank.model.AccountType;
import com.securebank.model.SavingsAccount;
import com.securebank.model.CurrentAccount;
import com.securebank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO {

    // Create a new account
    public Account createAccount(
            String holderName,
            AccountType type,
            double openingBalance,
            String pinHash
    ) throws SQLException {

        if (holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException("Holder name is required.");
        }

        if (type == null || pinHash == null || pinHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Account type and PIN hash are required."
            );
        }

        if (!Double.isFinite(openingBalance)) {
            throw new IllegalArgumentException(
                    "Opening balance must be a valid number."
            );
        }

        if (type == AccountType.SAVINGS
                && openingBalance < SavingsAccount.MIN_BALANCE) {
            throw new IllegalArgumentException(
                    "Savings account requires at least Rs. "
                            + SavingsAccount.MIN_BALANCE
            );
        }

        if (type == AccountType.CURRENT && openingBalance < 0) {
            throw new IllegalArgumentException(
                    "Current account opening balance cannot be negative."
            );
        }

        String sql = """
                INSERT INTO accounts
                    (holder_name, account_type, balance, pin_hash)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, holderName);
            statement.setString(2, type.name());
            statement.setDouble(3, openingBalance);
            statement.setString(4, pinHash);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int accountNumber = keys.getInt(1);

                    return buildAccount(
                            accountNumber,
                            holderName,
                            type,
                            openingBalance,
                            pinHash,
                            0,
                            false
                    );
                }
            }

            throw new SQLException(
                    "Failed to retrieve generated account number."
            );
        }
    }

    // Find an account by its account number
    public Optional<Account> findByAccountNumber(int accountNumber)
            throws SQLException {

        String sql = """
                SELECT account_number, holder_name, account_type,
                       balance, pin_hash, failed_attempts, locked
                FROM accounts
                WHERE account_number = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountNumber);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapAccount(result));
                }
            }
        }

        return Optional.empty();
    }

    // Retrieve all accounts
    public List<Account> findAll() throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String sql = """
                SELECT account_number, holder_name, account_type,
                       balance, pin_hash, failed_attempts, locked
                FROM accounts
                ORDER BY account_number
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                accounts.add(mapAccount(result));
            }
        }

        return accounts;
    }

    // Save updated balance and security status
    public boolean updateAccount(Account account) throws SQLException {

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account must not be null."
            );
        }

        String sql = """
                UPDATE accounts
                SET balance = ?, failed_attempts = ?, locked = ?
                WHERE account_number = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, account.getBalance());
            statement.setInt(2, account.getFailedAttempts());
            statement.setBoolean(3, account.isLocked());
            statement.setInt(4, account.getAccountNumber());

            return statement.executeUpdate() == 1;
        }
    }

    // Convert a database row into the correct account object
    private Account mapAccount(ResultSet result) throws SQLException {

        return buildAccount(
                result.getInt("account_number"),
                result.getString("holder_name"),
                AccountType.valueOf(result.getString("account_type")),
                result.getDouble("balance"),
                result.getString("pin_hash"),
                result.getInt("failed_attempts"),
                result.getBoolean("locked")
        );
    }

    // Create SavingsAccount or CurrentAccount based on account type
    private Account buildAccount(
            int accountNumber,
            String holderName,
            AccountType type,
            double balance,
            String pinHash,
            int failedAttempts,
            boolean locked
    ) {

        switch (type) {
            case SAVINGS:
                return new SavingsAccount(
                        accountNumber,
                        holderName,
                        balance,
                        pinHash,
                        failedAttempts,
                        locked
                );

            case CURRENT:
                return new CurrentAccount(
                        accountNumber,
                        holderName,
                        balance,
                        pinHash,
                        failedAttempts,
                        locked
                );

            default:
                throw new IllegalArgumentException(
                        "Unsupported account type: " + type
                );
        }
    }
}
