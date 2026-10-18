package com.securebank;

import com.securebank.dao.AccountDAO;
import com.securebank.exception.InvalidAmountException;
import com.securebank.model.Account;
import com.securebank.model.AccountType;
import com.securebank.model.SavingsAccount;
import com.securebank.util.PinHasher;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountDAOTest {

    @Test
    void shouldCreateAndRetrieveAccount() throws SQLException {
        AccountDAO dao = new AccountDAO();

        String holderName = "DAO Test User";
        String pinHash = PinHasher.hash("1234");

        Account created = dao.createAccount(
                holderName,
                AccountType.SAVINGS,
                2000.00,
                pinHash
        );

        assertNotNull(created);
        assertTrue(created.getAccountNumber() > 0);
        assertEquals(holderName, created.getHolderName());

        Optional<Account> found =
                dao.findByAccountNumber(created.getAccountNumber());

        assertTrue(found.isPresent());
        assertEquals(
                created.getAccountNumber(),
                found.get().getAccountNumber()
        );
        assertEquals(holderName, found.get().getHolderName());
        assertEquals(AccountType.SAVINGS, found.get().getType());
        assertEquals(2000.00, found.get().getBalance(), 0.01);
    }

    @Test
    void shouldReturnEmptyForNonExistingAccount() throws SQLException {
        AccountDAO dao = new AccountDAO();

        Optional<Account> found = dao.findByAccountNumber(-1);

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindCreatedAccountInAllAccounts() throws SQLException {
        AccountDAO dao = new AccountDAO();

        Account created = dao.createAccount(
                "Find All Test User",
                AccountType.SAVINGS,
                2000.00,
                PinHasher.hash("1234")
        );

        assertTrue(
                dao.findAll().stream()
                        .anyMatch(account ->
                                account.getAccountNumber()
                                        == created.getAccountNumber())
        );
    }

    @Test
    void shouldUpdateAccountBalance()
            throws SQLException, InvalidAmountException {
        AccountDAO dao = new AccountDAO();

        Account created = dao.createAccount(
                "Update Test User",
                AccountType.SAVINGS,
                2000.00,
                PinHasher.hash("1234")
        );

        created.deposit(100.00);

        assertTrue(dao.updateAccount(created));

        Optional<Account> updated =
                dao.findByAccountNumber(created.getAccountNumber());

        assertTrue(updated.isPresent());
        assertEquals(2100.00, updated.get().getBalance(), 0.01);
    }

    @Test
    void shouldReturnFalseWhenUpdatingNonExistingAccount()
            throws SQLException {
        AccountDAO dao = new AccountDAO();

        Account account = new SavingsAccount(
                -1,
                "Missing Test User",
                2000.00,
                PinHasher.hash("1234"),
                0,
                false
        );

        assertFalse(dao.updateAccount(account));
    }
}