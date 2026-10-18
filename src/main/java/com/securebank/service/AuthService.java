package com.securebank.service;

import com.securebank.dao.AccountDAO;
import com.securebank.exception.AccountLockedException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.util.PinHasher;

import java.sql.SQLException;

/**
 * Decides whether a PIN attempt succeeds and what happens to the account.
 * The Account object only stores the state; the rules live here.
 */
public class AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 3;

    private final AccountDAO accountDAO;

    public AuthService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    /**
     * Checks one PIN attempt for the given account.
     * Returns normally only if the PIN is correct.
     *
     * @throws AccountLockedException if the account is already locked,
     *                                or becomes locked by this attempt
     * @throws InvalidPinException    if the PIN is malformed or wrong
     * @throws SQLException           if the attempt state cannot be saved
     */
    public void verifyPin(Account account, String pin)
            throws AccountLockedException, InvalidPinException, SQLException {

        // A locked account never reaches the PIN check or the counter
        if (account.isLocked()) {
            throw new AccountLockedException(account.getAccountNumber());
        }

        // Malformed input cannot be a real guess, so it costs no attempt
        if (!PinHasher.isValidFormat(pin)) {
            throw new InvalidPinException("PIN must contain exactly 4 digits.");
        }

        if (PinHasher.verify(pin, account.getPinHash())) {
            // Success: clear earlier mistakes, writing only if needed
            if (account.getFailedAttempts() > 0) {
                account.resetFailedAttempts();
                save(account);
            }
            return;
        }

        // Wrong PIN: count it and save immediately so it survives a restart
        account.recordFailedAttempt();

        if (account.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            account.lock();
            save(account);
            throw new AccountLockedException(account.getAccountNumber());
        }

        save(account);
        throw new InvalidPinException(remainingAttempts(account));
    }

    // Attempts left before the account locks (never below 0)
    public int remainingAttempts(Account account) {
        return Math.max(0, MAX_FAILED_ATTEMPTS - account.getFailedAttempts());
    }

    private void save(Account account) throws SQLException {
        if (!accountDAO.updateAccount(account)) {
            throw new SQLException("Could not save PIN-attempt state for account "
                    + account.getAccountNumber() + ".");
        }
    }
}