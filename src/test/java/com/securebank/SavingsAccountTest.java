package com.securebank;

import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.model.SavingsAccount;
import com.securebank.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavingsAccountTest {

    private static final double DELTA = 0.001;

    private SavingsAccount account;

    @BeforeEach
    void setUp() {
        account = new SavingsAccount(1001, "Asha", 5000.0, "hash");
    }

    @Test
    void withdrawWithinLimitReducesBalance() throws Exception {
        account.withdraw(1500);

        assertEquals(3500.0, account.getBalance(), DELTA);
    }

    // Acceptance test 1
    @Test
    void withdrawBelowMinimumIsBlockedAndBalanceUnchanged() {
        InsufficientFundsException e = assertThrows(
                InsufficientFundsException.class, () -> account.withdraw(9500));

        assertEquals(4000.0, e.getAvailableAmount(), DELTA);
        assertEquals(5500.0, e.getShortfall(), DELTA);
        assertTrue(e.getMessage().contains("1000"));
        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawExactlyToMinimumIsAllowed() throws Exception {
        account.withdraw(4000);

        assertEquals(1000.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawOnePaisaBelowMinimumIsBlocked() {
        assertThrows(InsufficientFundsException.class, () -> account.withdraw(4000.01));

        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void withdrawInvalidAmountsAreRejected() {
        assertThrows(InvalidAmountException.class, () -> account.withdraw(0));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(-50));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(Double.NaN));

        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void depositIncreasesBalance() throws Exception {
        account.deposit(250.50);

        assertEquals(5250.50, account.getBalance(), DELTA);
    }

    @Test
    void depositInvalidAmountIsRejected() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(-100));
        assertThrows(InvalidAmountException.class, () -> account.deposit(0));

        assertEquals(5000.0, account.getBalance(), DELTA);
    }

    @Test
    void openingBelowMinimumIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SavingsAccount(1002, "Bala", 999.99, "hash"));
    }

    // Acceptance test 6
    @Test
    void interestAddsFourPercentAndIsRecorded() {
        double interest = account.applyInterest();

        assertEquals(200.0, interest, DELTA);
        assertEquals(5200.0, account.getBalance(), DELTA);
        assertEquals(Transaction.Type.INTEREST, account.getLatestTransaction().getType());
    }

    @Test
    void interestIsRoundedToTwoDecimals() {
        SavingsAccount odd = new SavingsAccount(1003, "Chitra", 1234.56, "hash");

        assertEquals(49.38, odd.applyInterest(), DELTA);
        assertEquals(1283.94, odd.getBalance(), DELTA);
    }

    // Acceptance test 7
    @Test
    void historyKeepsOnlyLastFiveNewestFirst() throws Exception {
        for (int i = 1; i <= 7; i++) {
            account.deposit(i * 100);
        }

        Transaction[] recent = account.getRecentTransactions();

        assertEquals(5, recent.length);
        assertEquals(700.0, recent[0].getAmount(), DELTA);
        assertEquals(300.0, recent[4].getAmount(), DELTA);
    }

    @Test
    void historyRecordsDepositAndWithdrawTypes() throws Exception {
        account.deposit(100);
        account.withdraw(50);

        Transaction[] recent = account.getRecentTransactions();

        assertEquals(2, recent.length);
        assertEquals(Transaction.Type.WITHDRAW, recent[0].getType());
        assertEquals(Transaction.Type.DEPOSIT, recent[1].getType());
    }
}