package com.securebank;

import com.securebank.dao.AccountDAO;
import com.securebank.exception.AccountLockedException;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.exception.InsufficientFundsException;
import com.securebank.exception.InvalidAmountException;
import com.securebank.exception.InvalidPinException;
import com.securebank.model.Account;
import com.securebank.model.AccountType;
import com.securebank.model.Transaction;
import com.securebank.service.Bank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests: need MySQL running, the schema loaded and a local
 * db.properties. Every test opens its own fresh accounts, so tests do not
 * interfere with each other or with existing data.
 */
class BankTransferTest {

    private static final String PIN = "1234";
    private static final String WRONG_PIN = "0000";
    private static final double DELTA = 0.01;

    private final Bank bank = new Bank();
    private final AccountDAO accountDAO = new AccountDAO();

    // ---------- helpers ----------

    private Account open(AccountType type, double opening) throws Exception {
        return bank.openAccount(type, "Test " + type, opening, PIN);
    }

    private Account reload(int accountNumber) throws Exception {
        return accountDAO.findByAccountNumber(accountNumber).orElseThrow();
    }
    private double balanceOf(Account account) throws Exception {
        return reload(account.getAccountNumber()).getBalance();
    }

    private List<Transaction> statementOf(Account account) throws Exception {
        return bank.getStatement(account.getAccountNumber(), PIN);
    }

    // ---------- acceptance 1 and 2: withdrawals ----------

    @Test
    void savingsWithdrawalBelowMinimumIsBlockedWithShortfall() throws Exception {
        Account savings = open(AccountType.SAVINGS, 2000.00);

        InsufficientFundsException e = assertThrows(
                InsufficientFundsException.class,
                () -> bank.withdraw(savings.getAccountNumber(), PIN, 9500.00));

        assertTrue(e.getMessage().contains("1000.00"), "message states the minimum");
        assertTrue(e.getMessage().contains("8500.00"), "message states the shortfall");
        assertEquals(8500.00, e.getShortfall(), DELTA);
        assertEquals(2000.00, balanceOf(savings), DELTA);
    }

    @Test
    void currentWithdrawalUsesOverdraftAndShowsNegativeBalance() throws Exception {
        Account current = open(AccountType.CURRENT, 5000.00);

        Account after = bank.withdraw(current.getAccountNumber(), PIN, 9500.00);

        assertEquals(-4500.00, after.getBalance(), DELTA);
        assertEquals(-4500.00, balanceOf(current), DELTA);
    }

    // ---------- acceptance 3: PIN lockout ----------

    @Test
    void threeWrongPinsLockAccountAndRefuseFurtherTransactions() throws Exception {
        Account sender = open(AccountType.SAVINGS, 5000.00);
        Account receiver = open(AccountType.SAVINGS, 5000.00);
        int from = sender.getAccountNumber();
        int to = receiver.getAccountNumber();

        InvalidPinException first = assertThrows(InvalidPinException.class,
                () -> bank.withdraw(from, WRONG_PIN, 100.00));
        assertEquals(2, first.getAttemptsRemaining());

        assertThrows(InvalidPinException.class,
                () -> bank.withdraw(from, WRONG_PIN, 100.00));
        assertThrows(AccountLockedException.class,
                () -> bank.withdraw(from, WRONG_PIN, 100.00));

        // Locked in the database, and refused even with the correct PIN
//        assertTrue(reload(from).isLocked());
        assertTrue(reload(from).isLocked());
        assertThrows(AccountLockedException.class,
                () -> bank.withdraw(from, PIN, 100.00));
        assertThrows(AccountLockedException.class,
                () -> bank.transfer(from, to, PIN, 100.00));

        assertEquals(5000.00, balanceOf(sender), DELTA);
        assertEquals(5000.00, balanceOf(receiver), DELTA);
    }

    // ---------- acceptance 4: transfer success ----------

    @Test
    void successfulTransferUpdatesBothBalancesAndBothStatements() throws Exception {
        Account sender = open(AccountType.CURRENT, 3000.00);
        Account receiver = open(AccountType.SAVINGS, 2000.00);

        Account returned = bank.transfer(
                sender.getAccountNumber(), receiver.getAccountNumber(), PIN, 1200.00);

        assertEquals(1800.00, returned.getBalance(), DELTA);
        assertEquals(1800.00, balanceOf(sender), DELTA);
        assertEquals(3200.00, balanceOf(receiver), DELTA);

        Transaction out = statementOf(sender).get(0);
        assertEquals(Transaction.Type.TRANSFER_OUT, out.getType());
        assertEquals(1200.00, out.getAmount(), DELTA);
        assertEquals(1800.00, out.getBalanceAfter(), DELTA);

        Transaction in = statementOf(receiver).get(0);
        assertEquals(Transaction.Type.TRANSFER_IN, in.getType());
        assertEquals(1200.00, in.getAmount(), DELTA);
        assertEquals(3200.00, in.getBalanceAfter(), DELTA);
    }

    // ---------- acceptance 5: transfer failure ----------

    @Test
    void transferWithInsufficientFundsChangesNeitherAccount() throws Exception {
        Account sender = open(AccountType.SAVINGS, 2000.00);
        Account receiver = open(AccountType.SAVINGS, 1500.00);

        InsufficientFundsException e = assertThrows(
                InsufficientFundsException.class,
                () -> bank.transfer(sender.getAccountNumber(),
                        receiver.getAccountNumber(), PIN, 1500.00));

        assertEquals(500.00, e.getShortfall(), DELTA);   // 1500 asked, 1000 available

        assertEquals(2000.00, balanceOf(sender), DELTA);
        assertEquals(1500.00, balanceOf(receiver), DELTA);
        assertEquals(1, statementOf(sender).size(), "only the opening deposit");
        assertEquals(1, statementOf(receiver).size(), "only the opening deposit");
    }

    @Test
    void overdraftBoundaryIsExactlyMinusFiveThousand() throws Exception {
        Account sender = open(AccountType.CURRENT, 500.00);
        Account receiver = open(AccountType.SAVINGS, 2000.00);
        int from = sender.getAccountNumber();
        int to = receiver.getAccountNumber();

        // 500 balance + 5000 overdraft = 5500 available: allowed exactly
        bank.transfer(from, to, PIN, 5500.00);
        assertEquals(-5000.00, balanceOf(sender), DELTA);
        assertEquals(7500.00, balanceOf(receiver), DELTA);

        // One more rupee goes past the limit
        assertThrows(InsufficientFundsException.class,
                () -> bank.transfer(from, to, PIN, 1.00));
        assertEquals(-5000.00, balanceOf(sender), DELTA);
        assertEquals(7500.00, balanceOf(receiver), DELTA);
    }

    @Test
    void transferWithWrongPinChangesNothing() throws Exception {
        Account sender = open(AccountType.SAVINGS, 5000.00);
        Account receiver = open(AccountType.SAVINGS, 5000.00);

        InvalidPinException e = assertThrows(InvalidPinException.class,
                () -> bank.transfer(sender.getAccountNumber(),
                        receiver.getAccountNumber(), WRONG_PIN, 100.00));

        assertEquals(2, e.getAttemptsRemaining());
        assertEquals(5000.00, balanceOf(sender), DELTA);
        assertEquals(5000.00, balanceOf(receiver), DELTA);
    }

    @Test
    void transferToUnknownAccountChangesNothing() throws Exception {
        Account sender = open(AccountType.SAVINGS, 5000.00);

        assertThrows(AccountNotFoundException.class,
                () -> bank.transfer(sender.getAccountNumber(), -1, PIN, 100.00));

        assertEquals(5000.00, balanceOf(sender), DELTA);
        assertEquals(1, statementOf(sender).size());
    }

    @Test
    void transferToSameAccountIsRejected() throws Exception {
        Account account = open(AccountType.SAVINGS, 5000.00);

        assertThrows(IllegalArgumentException.class,
                () -> bank.transfer(account.getAccountNumber(),
                        account.getAccountNumber(), PIN, 100.00));

        assertEquals(5000.00, balanceOf(account), DELTA);
    }

    // ---------- acceptance 7: statement ----------

    @Test
    void statementKeepsOnlyLastFiveNewestFirst() throws Exception {
        Account account = open(AccountType.SAVINGS, 10000.00);   // transaction 1
        int number = account.getAccountNumber();

        for (double amount : new double[] {100, 200, 300, 400, 500}) {   // 2 to 6
            bank.deposit(number, amount);
        }

        List<Transaction> statement = bank.getStatement(number, PIN);

        assertEquals(5, statement.size());
        double[] expectedNewestFirst = {500, 400, 300, 200, 100};
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedNewestFirst[i], statement.get(i).getAmount(), DELTA);
            assertEquals(Transaction.Type.DEPOSIT, statement.get(i).getType());
        }
        // The opening deposit of 10000 has dropped off the list
        assertTrue(statement.stream().noneMatch(t -> t.getAmount() == 10000.00));
    }

    // ---------- acceptance 8: invalid input ----------

    @ParameterizedTest
    @ValueSource(doubles = {0, -50, 0.001, Double.NaN,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidAmountsAreRejectedEverywhere(double badAmount) throws Exception {
        Account sender = open(AccountType.SAVINGS, 5000.00);
        Account receiver = open(AccountType.SAVINGS, 5000.00);
        int from = sender.getAccountNumber();
        int to = receiver.getAccountNumber();

        assertThrows(InvalidAmountException.class,
                () -> bank.deposit(from, badAmount));
        assertThrows(InvalidAmountException.class,
                () -> bank.withdraw(from, PIN, badAmount));
        assertThrows(InvalidAmountException.class,
                () -> bank.transfer(from, to, PIN, badAmount));

        assertEquals(5000.00, balanceOf(sender), DELTA);
        assertEquals(5000.00, balanceOf(receiver), DELTA);
    }
}