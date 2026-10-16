CREATE DATABASE IF NOT EXISTS securebank;
USE securebank;

-- Drop in child-first order so the script can be re-run during development
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    account_number  INT            NOT NULL AUTO_INCREMENT,
    holder_name     VARCHAR(100)   NOT NULL,
    account_type    ENUM('SAVINGS','CURRENT') NOT NULL,
    balance         DECIMAL(15,2)  NOT NULL,          -- signed: Current accounts can go negative
    pin_hash        VARCHAR(100)   NOT NULL,          -- BCrypt hash is 60 chars
    failed_attempts INT            NOT NULL DEFAULT 0,
    locked          BOOLEAN        NOT NULL DEFAULT FALSE,
    created_date    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_number),
    CONSTRAINT chk_failed_attempts CHECK (failed_attempts >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=1001;

CREATE TABLE transactions (
    transaction_id  INT            NOT NULL AUTO_INCREMENT,
    account_number  INT            NOT NULL,
    type            ENUM('DEPOSIT','WITHDRAW','TRANSFER_IN','TRANSFER_OUT','INTEREST') NOT NULL,
    amount          DECIMAL(15,2)  NOT NULL,
    balance_after   DECIMAL(15,2)  NOT NULL,          -- can be negative (overdraft)
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_txn_account FOREIGN KEY (account_number)
      REFERENCES accounts (account_number),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    INDEX idx_txn_account (account_number, transaction_id)
) ENGINE=InnoDB;


show tables;

desc accounts;
desc transactions;