# Bank Management System – Desktop Application

A modern, feature-rich banking administration desktop client built with Java Swing.  
It provides a clean, intuitive interface for managing employees, customers, accounts, and transactions – all with real-time search and pagination.

![Java](https://img.shields.io/badge/Java-17%2B-blue) ![Swing](https://img.shields.io/badge/UI-Swing-orange) ![License](https://img.shields.io/badge/License-MIT-green)

---

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running the Application](#running-the-application)
- [Module Overview](#module-overview)
  - [Dashboard](#dashboard)
  - [Customers](#customers)
  - [Accounts](#accounts)
  - [Employees](#employees)
  - [Transactions](#transactions)
- [UI/UX Highlights](#uiux-highlights)
- [Future Improvements](#future-improvements)
- [License](#license)

---

## Features

- Dashboard – Key metrics at a glance (total customers, accounts, employees, transactions).
- Full CRUD – Create, Read, Update, Delete for Customers, Accounts, Employees, and Transactions.
- Smart Search – Instant filtering across all modules.
- Pagination – 10 entries per page with intuitive navigation (< > and page numbers).
- Role-based UI – Admin role indicator (ready for extension).
- Modern Look – Custom rounded buttons, soft color palette, alternating table rows, and hover effects.
- Status Indicators – Visual badges for account status (Active/Inactive) and transaction status (Success/Pending/Failed).
- Transaction History – Recent transactions list on the dashboard.

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| UI | Java Swing (javax.swing) |
| Look & Feel | Custom rendering with Graphics2D, rounded panels |
| Data Access | DAO pattern (EmployeeDAO, CustomerDAO, AccountDAO, TransactionDAO) |
| Data Model | POJOs (Employee, Customer, Account, Transaction) |
| Date/Time | java.time (LocalDateTime) |
| Number Formatting | NumberFormat (currency) |
| Build Tool | Any (no external dependencies; plain Java) |

---

## Project Structure
bank/
├── dao/
│   ├── AccountDAO.java
│   ├── CustomerDAO.java
│   ├── EmployeeDAO.java
│   ├── TransactionDAO.java
│   └── impl/
│       ├── AccountDAOImpl.java
│       ├── CustomerDAOImpl.java
│       ├── EmployeeDAOImpl.java
│       └── TransactionDAOImpl.java
│
├── model/
│   ├── Account.java
│   ├── Customer.java
│   ├── Employee.java
│   └── Transaction.java
│
└── ui/
    ├── BankApplication.java
    ├── LoginForm.java
    ├── DashboardForm.java
    ├── DashboardPanel.java
    ├── SidebarPanel.java
    ├── NavigationListener.java
    ├── SummaryCardPanel.java
    ├── RoundedPanel.java
    ├── AccountPanel.java
    ├── AddAccountDialog.java
    ├── CustomersPanel.java
    ├── AddCustomerDialog.java
    ├── EmployeePanel.java
    ├── AddEmployeeDialog.java
    ├── TransactionPanel.java
    ├── AddTransactionDialog.java
    ├── ChangePasswordDialog.java
    └── LogoutDialog.java


Note: Dialog classes are referenced but not included in the snippet – they are assumed to exist.

---

## Getting Started

### Prerequisites

- Java 17 or higher (Java 8+ should work, but java.time is used).
- Any IDE (IntelliJ IDEA, Eclipse, NetBeans) or plain javac/java commands.

### Running the Application

1. Clone the repository (or copy the source files).
2. Open the project in your IDE.
3. Ensure all DAO implementations are correctly wired (they currently use in-memory lists – replace with actual database logic if needed).
4. Locate the main class (presumably BankApp or MainFrame – not provided in snippets, but the UI panels are designed to be inserted into a JFrame with a sidebar navigation).
5. Run the main class.

If you have no main class, create one:

```java
public class BankApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bank Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setContentPane(new DashboardPanel()); // or switch via tabs
            frame.setVisible(true);
        });
    }
}

Module Overview
Dashboard
Displays four summary cards: Total Employees, Customers, Accounts, Transactions.

Shows the 10 most recent transactions in a table with status badges.

Includes a welcome message and admin role label.

Customers
List of customers with ID, Name, Phone, Address.

Add – opens a dialog to enter name, phone, address.

Edit – modifies existing customer details.

Delete – removes a customer after confirmation.

Search – filters by name or phone.

Accounts
Table columns: Account No, Customer ID, Account Type, Balance, Status, Actions.

Add – launches AddAccountDialog (associates with an existing customer).

Edit – updates customer ID, type, balance, status.

Delete – removes an account.

Search – filters across all fields.

Balance is formatted as currency.

Employees
Table: ID, Name, Username, Phone, Password (hidden), Status (Active/Inactive), Actions.

Add – dialog for full name, username, password, phone, status.

Edit – updates all fields (password shown as masked with a toggle eye button).

Delete – confirmation before removal.

Search – by name, username, or phone.

Transactions
Table: ID, Account No, Type (Deposit/Withdrawal/Transfer), Amount, Date, Status, Actions.

Add – dialog to select account, type, amount, and status.

Edit – modifies the same fields.

Delete – removes a transaction.

Search – by ID, account, or type.

Amount formatted as currency, status with colored bullet points.

UI/UX Highlights
Consistent Color Scheme: #0066CC primary blue, light grey background, white cards.

Rounded Components: Custom RoundedButtonUI and RoundedPanel for soft corners.

Hover & Selection Feedback: Buttons light up on hover; table rows highlight on selection.

Placeholder Text: Search fields show a hint that disappears on focus.

Pagination: Always shows < and > buttons, with a block of up to 5 page numbers (or smart ellipsis) – exactly matching EmployeePanel and CustomersPanel.

Status Badges:

Account: Active (green) / Inactive (red)

Transaction: Success (green) / Pending (orange) / Failed (red)

Future Improvements
Database Integration – Replace in-memory DAOs with JDBC for persistence.

Login & Authentication – Secure login with hashed passwords.

User Roles – Differentiate admin, teller, and manager views.

Export Data – CSV/PDF export for reports.

Charts – Visualize transaction trends over time.

Theme Switching – Light/Dark mode support.

License
This project is licensed under the MIT License – feel free to use, modify, and distribute it.
