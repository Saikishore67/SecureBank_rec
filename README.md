# SecureBank: Team Overview

## 1. About the Project

SecureBank is a command-line banking application for a small bank that offers **Savings** and **Current** accounts. Each account type follows its own rules for withdrawals, interest and minimum balance. Staff use a console tool to open accounts, handle transactions and block invalid operations safely.

**Tech stack:** Java 17 or later, MySQL 8, Maven

**Approach:** Object-oriented design using inheritance, with exceptions used to stop illegal transactions, and a layered structure (UI, business logic, database access).

> **Note:** The original assignment says no database may be used, only `java.util` and `java.lang`. Using MySQL is our extension. Confirm approval with the professors before presenting.

---

## 2. Features

### Must have

| ID | Feature | Details |
|----|---------|---------|
| FR-1 | Open account | Savings (minimum balance Rs. 1000) or Current (overdraft up to Rs. 5000) |
| FR-2 | Deposit | Amount must be positive |
| FR-3 | Withdraw | Savings cannot drop below the minimum balance. Current may use the overdraft |
| FR-4 | Insufficient funds handling | A custom exception is thrown and caught when a withdrawal breaks the rules, and it reports the shortfall |
| FR-5 | Transfer | Move money between two accounts by account number |

### Should have

| ID | Feature | Details |
|----|---------|---------|
| FR-6 | PIN security | PIN required before any transaction. Account locks after 3 wrong attempts |
| FR-7 | Interest | 4% yearly interest on Savings accounts only |
| FR-8 | Statement | Shows the last 5 transactions of an account |

### Extra features from the database extension

- Accounts and transactions persist between runs
- PINs stored in hashed form, never as plain text
- Transfers are all-or-nothing: if any part fails, nothing changes
- Account details screen

---

## 3. Business Rules

- The parent `Account` type can never be used directly. Only Savings and Current accounts exist.
- Account balance can only be changed through the account's own methods.
- Savings minimum balance (Rs. 1000) and Current overdraft limit (Rs. 5000) are fixed values.
- Every transaction needs the correct PIN.
- Three wrong PIN attempts lock the account.
- Deposits, withdrawals and transfers must use positive amounts.

---

## 4. Object-Oriented Concepts Demonstrated

- **Inheritance:** Account is the parent. SavingsAccount and CurrentAccount are the children.
- **Method overriding:** withdraw and display details behave differently per account type.
- **Constructor chaining** using the `super` keyword
- **Polymorphism:** an Account reference holding either child type
- **Custom exception** and use of the `final` keyword
- **Arrays:** fixed-size storage for the last 5 transactions
- **Loops and exception handling:** PIN retry logic, try/catch/finally for input and business errors

---

## 5. Application Screens (CLI)

The main menu offers:

1. Open Account
2. Deposit
3. Withdraw
4. Transfer
5. Statement
6. Account Details
7. Exit

**Interface goals:**

- Clear banner and boxed menu layout
- Colored messages: green for success, red for errors, yellow for warnings
- Aligned table for statements
- Masked PIN entry
- Friendly retry on invalid input, with no crashes on bad data

---

## 6. Project Structure

```
SecureBank_rec/
├── pom.xml
├── .gitignore
├── README.md
├── sql/
│   ├── schema.sql
│   └── sample_data.sql
└── src/
    ├── main/
    │   ├── java/com/securebank/
    │   │   ├── BankApp.java
    │   │   ├── model/
    │   │   │   ├── Account.java
    │   │   │   ├── SavingsAccount.java
    │   │   │   ├── CurrentAccount.java
    │   │   │   ├── AccountType.java
    │   │   │   └── Transaction.java
    │   │   ├── exception/
    │   │   │   ├── InsufficientFundsException.java
    │   │   │   ├── AccountLockedException.java
    │   │   │   ├── AccountNotFoundException.java
    │   │   │   ├── InvalidAmountException.java
    │   │   │   └── InvalidPinException.java
    │   │   ├── dao/
    │   │   │   ├── AccountDAO.java
    │   │   │   └── TransactionDAO.java
    │   │   ├── util/
    │   │   │   ├── DBConnection.java
    │   │   │   └── PinHasher.java
    │   │   ├── service/
    │   │   │   ├── Bank.java
    │   │   │   ├── AuthService.java
    │   │   │   └── InterestService.java
    │   │   └── ui/
    │   │       ├── MainMenu.java
    │   │       ├── ConsoleUtil.java
    │   │       ├── InputHelper.java
    │   │       ├── OpenAccountScreen.java
    │   │       ├── DepositScreen.java
    │   │       ├── WithdrawScreen.java
    │   │       ├── TransferScreen.java
    │   │       ├── StatementScreen.java
    │   │       └── AccountDetailsScreen.java
    │   └── resources/
    │       ├── db.properties            (not committed)
    │       └── db.properties.example
    └── test/java/com/securebank/
        ├── SavingsAccountTest.java
        ├── CurrentAccountTest.java
        ├── BankTransferTest.java
        └── AuthServiceTest.java
```

### What each package is for

| Package | Purpose |
|---------|---------|
| `model` | Account types and the transaction record |
| `exception` | Custom exceptions for each kind of failure |
| `dao` | All database queries, kept in one place |
| `util` | Database connection and PIN hashing helpers |
| `service` | Business logic: transfers, PIN checks, interest |
| `ui` | Everything the user sees and types |
| `sql` | Database setup scripts |
| `test` | Automated tests |

---

## 7. Layered Design

```
UI (ui)  →  Business logic (service)  →  Database access (dao)  →  MySQL
                      ↓
               Model + Exceptions
```

**Rules:**

- The UI never talks to the database directly.
- No database queries appear in the UI or model classes.
- Business rules live in the model and service layers only.

---

## 8. Team Split

### Person 1: Model and Exceptions (core OOP) - (Varun S)

The part the professors will look at most closely.

- Account (parent), SavingsAccount, CurrentAccount
- Minimum balance and overdraft rules, interest method (Savings only)
- Last 5 transactions storage
- AccountType and Transaction classes
- All five exception classes
- Tests: SavingsAccountTest, CurrentAccountTest

### Person 2: Database and Project Setup

- Database schema and sample data scripts
- Database connection setup and config files
- PIN hashing utility
- AccountDAO and TransactionDAO

### Person 3: Business Logic (Sai Kishore S)

- Bank service: open account, find account, deposit, withdraw, transfer
- Transfer safety (all-or-nothing)
- AuthService: PIN verification, failed-attempt counting, lockout after 3 tries
- InterestService: 4% interest for Savings accounts
- Tests: BankTransferTest, AuthServiceTest
- Maven configuration and `.gitignore`

### Person 4: CLI Interface

- BankApp entry point and MainMenu
- ConsoleUtil: banner, colors, tables, messages
- InputHelper: safe input reading and masked PIN
- One screen per menu option (Open, Deposit, Withdraw, Transfer, Statement, Account Details)

[//]: # (- Project README)

---

## 9. What the UI Needs from the Business Layer

Agree on these operations in the first hour so Person 4 can start before Person 3 finishes:

| Operation | Input | Possible failures |
|-----------|-------|-------------------|
| Open account | Type, holder name, initial deposit, PIN | Invalid amount |
| Deposit | Account number, amount | Account not found, invalid amount |
| Withdraw | Account number, PIN, amount | Insufficient funds, locked, wrong PIN, not found |
| Transfer | From account, to account, PIN, amount | Insufficient funds, locked, wrong PIN, not found |
| Get statement | Account number, PIN | Wrong PIN, locked, not found |
| Get account | Account number | Not found |

---

## 10. Database Overview

**accounts table:** account number (starts at 1001), holder name, account type, balance, hashed PIN, failed attempts, locked flag, created date

**transactions table:** transaction id, account number, type (deposit, withdraw, transfer in, transfer out, interest), amount, balance after, timestamp

---

## 11. Working Together

### Setup (everyone)

- Install JDK 17 or later, Maven, MySQL 8 and an IDE (IntelliJ recommended)
- Clone the repository and create your own local `db.properties` from the example file
- Run the schema script once to create your local database

[//]: # (### Branches)

[//]: # ()
[//]: # (| Branch | Owner |)

[//]: # (|--------|-------|)

[//]: # (| `feature/model` | Person 1 |)

[//]: # (| `feature/db` | Person 2 |)

[//]: # (| `feature/service` | Person 3 |)

[//]: # (| `feature/ui` | Person 4 |)

### Merge order

Model → Database → Service → UI

### Ground rules

- Only edit files in your own area. Tell the owner if you need a change elsewhere.
- Never commit `db.properties` or passwords.
- Pull from main before starting work each day.
- Agree on the operations table in Section 9 before writing code.

### Timeline suggestion

| Phase | Goal |
|-------|------|
| Hour 1 | Everyone agrees on structure and operations. One person pushes the empty skeleton |
| Phase 1 | Each person builds their own area, with stubs where they depend on others |
| Phase 2 | Merge in the agreed order and fix integration issues |
| Phase 3 | Test the full flow, polish the UI, write the README |

---

## 12. Acceptance Tests

The project is done when these scenarios from the original assignment work end to end:

1. **Savings withdrawal blocked:** withdrawing Rs. 9500 from a Savings account that would drop below Rs. 1000 fails with a message stating the minimum and the shortfall.
2. **Current account overdraft:** withdrawing Rs. 9500 from a Current account succeeds using the overdraft and shows the new negative balance.
3. **PIN lockout:** three wrong PINs lock the account and further transactions are refused.
4. **Transfer success:** both balances update correctly.
5. **Transfer failure:** if the sender lacks funds, neither account changes.
6. **Interest:** applying interest increases only Savings balances by 4%.
7. **Statement:** shows the last 5 transactions, oldest dropping off after the fifth.
8. **Invalid input:** negative amounts and non-numeric input are rejected without crashing.

---

## 13. Why Maven (for the presentation)

Maven is not strictly required, but it is the better choice once we add the MySQL driver as an external dependency:

- Downloads and versions the driver automatically, including its own dependencies
- Everyone gets the same build from a single command
- Standard project layout that any developer recognizes
- Runs tests automatically
- Standard practice in industry and easy to extend later