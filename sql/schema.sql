-- =====================================================================
--  SecureBank: database schema
--  Requires MySQL 8.0.16 or later (CHECK constraints are enforced from 8.0.16)
--  WARNING: running this script drops and recreates both tables,
--  so all existing data is erased. Use it for setup and development only.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS securebank
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE securebank;

-- Drop child table first because of the foreign key
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;

-- ---------------------------------------------------------------------
--  accounts
--  Maps to: Account (abstract), SavingsAccount, CurrentAccount
--  account_type decides which child class AccountDAO builds
-- ---------------------------------------------------------------------
CREATE TABLE accounts (
    account_number  INT            NOT NULL AUTO_INCREMENT,
    holder_name     VARCHAR(100)   NOT NULL,
    account_type    ENUM('SAVINGS','CURRENT') NOT NULL,   -- AccountType enum
    balance         DECIMAL(15,2)  NOT NULL,              -- signed: Current may go negative
    pin_hash        VARCHAR(100)   NOT NULL,              -- BCrypt hash (60 chars), never the raw PIN
    failed_attempts INT            NOT NULL DEFAULT 0,    -- wrong-PIN counter (AuthService)
    locked          BOOLEAN        NOT NULL DEFAULT FALSE,-- set after 3 wrong PINs
    created_date    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (account_number),

    CONSTRAINT chk_holder_name_not_blank
      CHECK (CHAR_LENGTH(TRIM(holder_name)) > 0),

    CONSTRAINT chk_failed_attempts
      CHECK (failed_attempts >= 0),

    -- SavingsAccount.MIN_BALANCE = 1000
    CONSTRAINT chk_savings_min_balance
      CHECK (account_type <> 'SAVINGS' OR balance >= 1000.00),

    -- CurrentAccount.OVERDRAFT_LIMIT = 5000
    CONSTRAINT chk_current_overdraft_limit
      CHECK (account_type <> 'CURRENT' OR balance >= -5000.00)
) ENGINE=InnoDB AUTO_INCREMENT=1001;   -- account numbers start at 1001

-- ---------------------------------------------------------------------
--  transactions
--  Maps to: Transaction (Type enum: DEPOSIT, WITHDRAW, TRANSFER_IN,
--           TRANSFER_OUT, INTEREST)
--  Every row is a money movement, so amount is always positive.
--  Direction comes from the type; the balance after is stored.
-- ---------------------------------------------------------------------
CREATE TABLE transactions (
    transaction_id  INT            NOT NULL AUTO_INCREMENT,
    account_number  INT            NOT NULL,
    type            ENUM('DEPOSIT','WITHDRAW','TRANSFER_IN','TRANSFER_OUT','INTEREST') NOT NULL,
    amount          DECIMAL(15,2)  NOT NULL,
    balance_after   DECIMAL(15,2)  NOT NULL,              -- may be negative (overdraft)
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (transaction_id),

    CONSTRAINT fk_txn_account
      FOREIGN KEY (account_number)
          REFERENCES accounts (account_number)
          ON UPDATE CASCADE
          ON DELETE RESTRICT,                               -- history must never be orphaned

    CONSTRAINT chk_amount_positive
      CHECK (amount > 0),

    -- Serves "last 5 transactions of an account, newest first":
    --   SELECT ... WHERE account_number = ? ORDER BY transaction_id DESC LIMIT 5
    INDEX idx_txn_account (account_number, transaction_id)
) ENGINE=InnoDB;