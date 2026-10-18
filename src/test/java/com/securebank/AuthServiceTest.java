package com.securebank;

import com.securebank.dao.AccountDAO;
import com.securebank.exception.AccountLockedException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.model.SavingsAccount;
import com.securebank.service.AuthService;
import com.securebank.util.PinHasher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests: no database needed. */
class AuthServiceTest {

    private static final String PIN = "1234";
    private static final String WRONG_PIN = "0000";

    private static String pinHash;   // BCrypt is slow, so hash once

    private FakeAccountDAO dao;
    private AuthService auth;
    private Account account;

    // Stands in for the real DAO: remembers how often it was asked to save
    private static class FakeAccountDAO extends AccountDAO {
        int saveCount = 0;
        boolean saveSucceeds = true;

        @Override
        public boolean updateAccount(Account account) {
            saveCount++;
            return saveSucceeds;
        }
    }

    @BeforeAll
    static void hashPinOnce() {
        pinHash = PinHasher.hash(PIN);
    }

    @BeforeEach
    void setUp() {
        dao = new FakeAccountDAO();
        auth = new AuthService(dao);
        account = new SavingsAccount(1001, "Auth Test User", 2000.00, pinHash, 0, false);
    }

    private InvalidPinException wrongPinExpectingInvalid() {
        return assertThrows(InvalidPinException.class,
                () -> auth.verifyPin(account, WRONG_PIN));
    }

    @Test
    void correctPinPassesWithoutSaving() throws Exception {
        auth.verifyPin(account, PIN);

        assertEquals(0, account.getFailedAttempts());
        assertFalse(account.isLocked());
        assertEquals(0, dao.saveCount);
    }

    @Test
    void correctPinResetsEarlierFailedAttempts() throws Exception {
        wrongPinExpectingInvalid();
        assertEquals(1, account.getFailedAttempts());
        int savesBefore = dao.saveCount;

        auth.verifyPin(account, PIN);

        assertEquals(0, account.getFailedAttempts());
        assertEquals(savesBefore + 1, dao.saveCount);
    }

    @Test
    void firstAndSecondWrongPinReportAttemptsLeft() {
        InvalidPinException first = wrongPinExpectingInvalid();
        assertEquals(2, first.getAttemptsRemaining());
        assertEquals(1, account.getFailedAttempts());
        assertEquals(1, dao.saveCount);

        InvalidPinException second = wrongPinExpectingInvalid();
        assertEquals(1, second.getAttemptsRemaining());
        assertEquals(2, account.getFailedAttempts());
        assertEquals(2, dao.saveCount);

        assertFalse(account.isLocked());
    }

    @Test
    void thirdWrongPinLocksAccountAndSavesIt() {
        wrongPinExpectingInvalid();
        wrongPinExpectingInvalid();

        AccountLockedException e = assertThrows(AccountLockedException.class,
                () -> auth.verifyPin(account, WRONG_PIN));

        assertEquals(1001, e.getAccountNumber());
        assertTrue(account.isLocked());
        assertEquals(3, account.getFailedAttempts());
        assertEquals(3, dao.saveCount);
    }

    @Test
    void lockedAccountIsRefusedEvenWithCorrectPin() {
        Account locked = new SavingsAccount(
                1002, "Locked User", 2000.00, pinHash, 3, true);

        assertThrows(AccountLockedException.class,
                () -> auth.verifyPin(locked, PIN));

        assertEquals(3, locked.getFailedAttempts());   // not changed
        assertEquals(0, dao.saveCount);                // nothing saved
    }

    @Test
    void accountStaysLockedAfterLockoutEvenWithCorrectPin() {
        wrongPinExpectingInvalid();
        wrongPinExpectingInvalid();
        assertThrows(AccountLockedException.class,
                () -> auth.verifyPin(account, WRONG_PIN));

        assertThrows(AccountLockedException.class,
                () -> auth.verifyPin(account, PIN));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12", "12345", "abcd", "12 4", "12.4"})
    void malformedPinIsRejectedWithoutCostingAnAttempt(String badPin) {
        InvalidPinException e = assertThrows(InvalidPinException.class,
                () -> auth.verifyPin(account, badPin));

        assertEquals(-1, e.getAttemptsRemaining());    // message-style exception
        assertEquals(0, account.getFailedAttempts());
        assertEquals(0, dao.saveCount);
    }

    @Test
    void twoWrongPinsThenCorrectPinDoesNotLock() throws Exception {
        wrongPinExpectingInvalid();
        wrongPinExpectingInvalid();

        auth.verifyPin(account, PIN);

        assertFalse(account.isLocked());
        assertEquals(0, account.getFailedAttempts());
    }

    @Test
    void remainingAttemptsCountsDownAndNeverGoesNegative() {
        assertEquals(3, auth.remainingAttempts(account));

        account.recordFailedAttempt();
        assertEquals(2, auth.remainingAttempts(account));

        account.recordFailedAttempt();
        account.recordFailedAttempt();
        assertEquals(0, auth.remainingAttempts(account));

        account.recordFailedAttempt();   // extra, should not go below zero
        assertEquals(0, auth.remainingAttempts(account));
    }

    @Test
    void failedSaveSurfacesAsSqlException() {
        dao.saveSucceeds = false;

        assertThrows(SQLException.class,
                () -> auth.verifyPin(account, WRONG_PIN));
    }
}