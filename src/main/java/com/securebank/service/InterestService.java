package com.securebank.service;

import com.securebank.dao.AccountDAO;
import com.securebank.exception.AccountNotFoundException;
import com.securebank.model.Account;
import com.securebank.model.AccountType;
import com.securebank.model.SavingsAccount;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InterestService {

    public record InterestResult(int accountsCredited,
                                 double totalInterest,
                                 List<Integer> failedAccounts) {
    }

    private final AccountDAO accountDAO;
    private final Bank bank;

    public InterestService(AccountDAO accountDAO, Bank bank) {
        this.accountDAO = accountDAO;
        this.bank = bank;
    }

    /**
     * Credits one year of interest to a single Savings account.
     *
     * @return the interest amount that was credited
     * @throws AccountNotFoundException if the account does not exist
     * @throws IllegalArgumentException if the account is not a Savings account
     */
    public double applyInterest(int accountNumber)
            throws AccountNotFoundException, SQLException {

        Account account = accountDAO.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (!(account instanceof SavingsAccount savings)) {
            throw new IllegalArgumentException(
                    "Interest applies to Savings accounts only. Account "
                            + accountNumber + " is a Current account.");
        }

        return creditAndSave(savings);
    }


    public InterestResult applyInterestToAll() throws SQLException {

        int credited = 0;
        double total = 0.0;
        List<Integer> failed = new ArrayList<>();

        for (Account account : accountDAO.findAll()) {
            if (account.getType() != AccountType.SAVINGS
                    || !(account instanceof SavingsAccount savings)) {
                continue;
            }

            try {
                total += creditAndSave(savings);
                credited++;
            } catch (SQLException e) {
                // That account was rolled back and is unchanged
                failed.add(account.getAccountNumber());
            }
        }

        return new InterestResult(credited, round2(total), failed);
    }

    // Credit in memory, then save balance + INTEREST transaction together
    private double creditAndSave(SavingsAccount savings) throws SQLException {
        double interest = savings.applyInterest();
        bank.saveChanges(savings);
        return interest;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}