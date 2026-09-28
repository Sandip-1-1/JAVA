# Inventory Management System

## Description

The Inventory Management System is a terminal-based Java application for managing products and warehouse stock. It stores products and stock transactions in SQLite, supports complete product CRUD operations, and produces low-stock and inventory-value reports.

## Features

- Add, list, view, search, update, and delete products
- Unique SKU validation
- Product sorting by name, quantity, or unit price
- Stock-in and stock-out operations
- Prevention of negative inventory
- Complete and product-specific stock transaction history
- Low-stock report based on each product's reorder threshold
- Inventory-value report with product, category, and overall totals
- Recoverable console input validation
- Persistent SQLite storage

## Technologies

- Java with Java 11 source compatibility
- Maven
- JDBC
- SQLite
- Xerial SQLite JDBC driver

## Project Structure

```text
.
├── pom.xml
├── schema.sql
├── README.md
└── src/main/java/com/inventory/
    ├── Main.java
    ├── dao/
    │   ├── ProductDAO.java
    │   └── StockTransactionDAO.java
    ├── exception/
    │   ├── DuplicateProductException.java
    │   ├── InsufficientStockException.java
    │   └── ProductNotFoundException.java
    ├── model/
    │   ├── Product.java
    │   ├── StockTransaction.java
    │   ├── TransactionType.java
    │   └── Validatable.java
    ├── service/
    │   └── InventoryService.java
    └── util/
        ├── DatabaseConnection.java
        └── InputValidator.java
```

- `model`: Encapsulated objects representing products and stock transactions.
- `dao`: JDBC code for reading and changing database records.
- `service`: Stock rules, JDBC transactions, sorting, filtering, and report calculations.
- `exception`: Custom exceptions for common business errors.
- `util`: Database setup and reusable console-input validation.
- `Main`: Application startup, menus, and terminal output.

## Database Setup

No separate database server, account, or password is needed. On startup, the application reads the root [`schema.sql`](schema.sql), creates the `data/` directory if necessary, and creates `data/inventory.db` automatically.

Run the application from the project root so it can find `schema.sql`. The database file is local runtime data and is excluded by `.gitignore`.

## How to Run

Requirements:

- JDK 11 or newer
- Maven 3.6 or newer

From a fresh clone:

```bash
git clone <repository-url>
cd FInalProject
mvn compile
mvn exec:java -Dexec.mainClass=com.inventory.Main
```

Maven downloads the SQLite JDBC driver during the first build. On later runs, start the application with the same `mvn exec:java` command.

## Usage

The application displays this main menu:

```text
========================================
      INVENTORY MANAGEMENT SYSTEM
========================================
1. Add Product
2. View Products
3. Search Products
4. View Product by ID
5. Update Product
6. Delete Product
7. Stock In
8. Stock Out
9. View Transaction History
10. View Low Stock Products
11. Inventory Value Report
0. Exit
```

A typical workflow is to add a product, use Stock In or Stock Out when inventory changes, and then view transaction history or reports. Invalid numbers and empty required values are rejected without ending the program.

## Database Schema

### `products`

Stores the product ID, name, unique SKU, category, quantity, unit price, and reorder threshold. Constraints prevent empty required text and negative numeric values.

### `stock_transactions`

Stores the transaction ID, product ID, transaction type, quantity, and date/time. `product_id` is a foreign key to `products.id`; deleting a product also deletes its related transaction history.

Stock changes use a JDBC transaction so updating the product quantity and inserting its history record either both succeed or both roll back.

## OOP Concepts Demonstrated

- **Encapsulation:** `Product` and `StockTransaction` have private fields accessed through constructors, getters, and validated setters.
- **Abstraction:** `Validatable` defines the common ability to validate a domain object's state.
- **Polymorphism:** Both model classes implement their own version of `validate()` through the `Validatable` interface.
- **Inheritance:** The project does not use a class-inheritance hierarchy because one is not needed. Interface implementation provides the required abstraction and polymorphism without forced inheritance.

## Java Collections Framework

- `List` and `ArrayList` hold ordered products and transactions retrieved from SQLite.
- `HashMap` stores inventory-value totals by category.
- `Comparator` sorts products by name, quantity, or unit price.
- Loops filter low-stock products and group products for report output.

SQLite remains the persistent source of truth; collections are used only for in-memory processing and display.

## JDBC

- `Connection` opens sessions with `data/inventory.db`.
- `PreparedStatement` safely supplies values to all CRUD and search SQL.
- `ResultSet` maps query rows to model objects.
- `ProductDAO` implements product create, read, update, delete, and search operations.
- `StockTransactionDAO` inserts and retrieves transaction records.
- Try-with-resources closes connections, statements, and result sets automatically.
- Explicit `commit()` and `rollback()` protect stock quantity and history consistency.

## Exception Handling

- `SQLException` is handled at the console boundary with a readable database-error message.
- `ProductNotFoundException` reports an ID that does not exist.
- `DuplicateProductException` reports an SKU that is already stored.
- `InsufficientStockException` prevents stock-out from making quantity negative.
- `IllegalArgumentException` handles invalid domain values, while `InputValidator` prevents normal input mistakes before model creation.

## Screenshots

Add real terminal screenshots after running the project. Suggested screenshots:

1. Main menu and product listing
2. Stock-in or stock-out followed by transaction history
3. Low-stock or inventory-value report

```text
[Screenshot 1: Main menu and product list]
[Screenshot 2: Stock transaction history]
[Screenshot 3: Inventory report]
```

## Known Limitations

- The application is designed for one local terminal user, not concurrent warehouse users.
- Prices use `double`, which is acceptable for this academic project but is not ideal for financial systems requiring exact decimal arithmetic.
- Deleting a product also deletes its stock transaction history through the database foreign-key rule.
- Editing a product can directly change its quantity; direct CRUD edits are not added to stock transaction history.
- Supplier management, authentication, and date-range filtering are outside the required scope.
