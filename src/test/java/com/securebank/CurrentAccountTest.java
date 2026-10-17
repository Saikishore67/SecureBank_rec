package com.securebank;

import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.model.Account;
import com.securebank.model.CurrentAccount;
import com.securebank.model.SavingsAccount;
import com.securebank.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentAccountTest {

    private static final double DELTA = 0.001;

    private CurrentAccount account;

    @BeforeEach
    void setUp() {
        account = new CurrentAccount(2001, "Ravi", 5000.0, "hash");
    }

    // Acceptance test 2
    @Test
    void withdrawUsingOverdraftSucceedsAndBalanceGoesNegative() throws Exception {
        account.withdraw(9500);

        assertEquals(-4500.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawExactlyToOverdraftLimitIsAllowed() throws Exception {
        account.withdraw(10000);

        assertEquals(-5000.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawBeyondOverdraftLimitIsBlockedAndBalanceUnchanged() {
        InsufficientFundsException e = assertThrows(
                InsufficientFundsException.class, () -> account.withdraw(10000.01));

        assertEquals(10000.0, e.getAvailableAmount(), DELTA);
        assertEquals(0.01, e.getShortfall(), DELTA);
        assertTrue(e.getMessage().contains("5000"));
        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawWhileOverdrawnOnlyHasRemainingRoom() throws Exception {
        account.withdraw(9500);

        InsufficientFundsException e = assertThrows(
                InsufficientFundsException.class, () -> account.withdraw(501));

        assertEquals(500.0, e.getAvailableAmount(), DELTA);
        assertEquals(1.0, e.getShortfall(), DELTA);
        assertEquals(-4500.0, account.getBalance(), DELTA);
    }

    @Test
    void depositWhileOverdrawnReducesTheOverdraft() throws Exception {
        account.withdraw(9500);
        account.deposit(2000);

        assertEquals(-2500.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawInvalidAmountsAreRejected() {
        assertThrows(InvalidAmountException.class, () -> account.withdraw(0));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(-50));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(Double.NaN));

        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void depositInvalidAmountIsRejected() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(-100));
        assertThrows(InvalidAmountException.class, () -> account.deposit(0));

        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void openingWithNegativeBalanceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CurrentAccount(2002, "Sita", -1.0, "hash"));
    }

    @Test
    void openingWithZeroBalanceIsAllowed() {
        CurrentAccount empty = new CurrentAccount(2003, "Tarun", 0.0, "hash");

        assertEquals(0.0, empty.getBalance(), DELTA);
    }

    @Test
    void transactionRecordsTheOverdrawnBalance() throws Exception {
        account.withdraw(9500);

        Transaction latest = account.getLatestTransaction();

        assertEquals(Transaction.Type.WITHDRAW, latest.getType());
        assertEquals(9500.0, latest.getAmount(), DELTA);
        assertEquals(-4500.0, latest.getBalanceAfter(), DELTA);
    }

    @Test
    void loadedAccountKeepsNegativeBalanceAndPinState() {
        CurrentAccount loaded = new CurrentAccount(2004, "Uma", -3000.0, "hash", 2, true);

        assertEquals(-3000.0, loaded.getBalance(), DELTA);
        assertEquals(2, loaded.getFailedAttempts());
        assertTrue(loaded.isLocked());
    }

    @Test
    void sameWithdrawCallBehavesDifferentlyForEachAccountType() throws Exception {
        Account savings = new SavingsAccount(1001, "Asha", 5000.0, "hash");
        Account current = new CurrentAccount(2001, "Ravi", 5000.0, "hash");

        assertThrows(InsufficientFundsException.class, () -> savings.withdraw(9500));
        current.withdraw(9500);

        assertEquals(5000.0, savings.getBalance(), DELTA);
        assertEquals(-4500.0, current.getBalance(), DELTA);
    }
}