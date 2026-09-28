package com.inventory;

import com.inventory.dao.ProductDAO;
import com.inventory.exception.DuplicateProductException;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.model.StockTransaction;
import com.inventory.service.InventoryService;
import com.inventory.util.DatabaseConnection;
import com.inventory.util.InputValidator;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Scanner scanner = new Scanner(System.in);
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryService inventoryService = new InventoryService();

    public static void main(String[] args) {
        try {
            DatabaseConnection.initializeDatabase();
            new Main().run();
        } catch (SQLException e) {
            System.out.println("The application could not start: " + e.getMessage());
        }
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt(scanner, "Enter choice: ");

            try {
                switch (choice) {
                    case 1:
                        addProduct();
                        break;
                    case 2:
                        viewProducts();
                        break;
                    case 3:
                        searchProducts();
                        break;
                    case 4:
                        viewProductById();
                        break;
                    case 5:
                        updateProduct();
                        break;
                    case 6:
                        deleteProduct();
                        break;
                    case 7:
                        stockIn();
                        break;
                    case 8:
                        stockOut();
                        break;
                    case 9:
                        viewTransactionHistory();
                        break;
                    case 10:
                        viewLowStockProducts();
                        break;
                    case 11:
                        viewInventoryValueReport();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Thank you for using the Inventory Management System.");
                        break;
                    default:
                        System.out.println("Invalid menu choice. Please try again.");
                }
            } catch (ProductNotFoundException | DuplicateProductException
                     | InsufficientStockException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }

            if (running) {
                System.out.println();
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println("========================================");
        System.out.println("      INVENTORY MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Add Product");
        System.out.println("2. View Products");
        System.out.println("3. Search Products");
        System.out.println("4. View Product by ID");
        System.out.println("5. Update Product");
        System.out.println("6. Delete Product");
        System.out.println("7. Stock In");
        System.out.println("8. Stock Out");
        System.out.println("9. View Transaction History");
        System.out.println("10. View Low Stock Products");
        System.out.println("11. Inventory Value Report");
        System.out.println("0. Exit");
    }

    private void addProduct() throws SQLException, DuplicateProductException {
        System.out.println("\n--- Add Product ---");
        Product product = readProductDetails(0);
        productDAO.addProduct(product);
        System.out.println("Product added successfully with ID " + product.getId() + ".");
    }

    private void viewProducts() throws SQLException {
        System.out.println("\nSort products by:");
        System.out.println("1. Name");
        System.out.println("2. Quantity");
        System.out.println("3. Unit price");
        int sortChoice = InputValidator.readInt(scanner, "Enter sort choice: ");

        List<Product> products;
        if (sortChoice == 2) {
            products = inventoryService.getProductsSortedByQuantity();
        } else if (sortChoice == 3) {
            products = inventoryService.getProductsSortedByUnitPrice();
        } else {
            if (sortChoice != 1) {
                System.out.println("Invalid sort choice. Sorting by name.");
            }
            products = inventoryService.getProductsSortedByName();
        }
        printProducts(products);
    }

    private void searchProducts() throws SQLException {
        String term = InputValidator.readRequiredString(
                scanner, "Enter product name, SKU, or category: ");
        printProducts(productDAO.searchProducts(term));
    }

    private void viewProductById() throws SQLException, ProductNotFoundException {
        int productId = InputValidator.readPositiveInt(scanner, "Enter product ID: ");
        printProduct(productDAO.getProductById(productId));
    }

    private void updateProduct()
            throws SQLException, ProductNotFoundException, DuplicateProductException {
        int productId = InputValidator.readPositiveInt(scanner, "Enter product ID to update: ");
        Product existingProduct = productDAO.getProductById(productId);
        System.out.println("Current product:");
        printProduct(existingProduct);
        System.out.println("Enter all updated values:");

        Product updatedProduct = readProductDetails(productId);
        productDAO.updateProduct(updatedProduct);
        System.out.println("Product updated successfully.");
    }

    private void deleteProduct() throws SQLException, ProductNotFoundException {
        int productId = InputValidator.readPositiveInt(scanner, "Enter product ID to delete: ");
        Product product = productDAO.getProductById(productId);
        printProduct(product);
        String confirmation = InputValidator.readRequiredString(
                scanner, "Delete this product? (y/n): ");
        if (confirmation.equalsIgnoreCase("y")) {
            productDAO.deleteProduct(productId);
            System.out.println("Product deleted successfully.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    private void stockIn() throws SQLException, ProductNotFoundException {
        int productId = InputValidator.readPositiveInt(scanner, "Enter product ID: ");
        Product product = productDAO.getProductById(productId);
        System.out.println("Current quantity: " + product.getQuantity());
        int quantity = InputValidator.readPositiveInt(scanner, "Enter stock-in quantity: ");
        Product updatedProduct = inventoryService.stockIn(productId, quantity);
        System.out.println("Stock added. New quantity: " + updatedProduct.getQuantity());
    }

    private void stockOut()
            throws SQLException, ProductNotFoundException, InsufficientStockException {
        int productId = InputValidator.readPositiveInt(scanner, "Enter product ID: ");
        Product product = productDAO.getProductById(productId);
        System.out.println("Current quantity: " + product.getQuantity());
        int quantity = InputValidator.readPositiveInt(scanner, "Enter stock-out quantity: ");
        Product updatedProduct = inventoryService.stockOut(productId, quantity);
        System.out.println("Stock removed. New quantity: " + updatedProduct.getQuantity());
    }

    private void viewTransactionHistory() throws SQLException, ProductNotFoundException {
        System.out.println("\n1. View all transactions");
        System.out.println("2. View transactions for one product");
        int choice = InputValidator.readInt(scanner, "Enter choice: ");

        if (choice == 1) {
            printTransactions(inventoryService.getAllTransactions());
        } else if (choice == 2) {
            int productId = InputValidator.readPositiveInt(scanner, "Enter product ID: ");
            printTransactions(inventoryService.getTransactionsByProduct(productId));
        } else {
            System.out.println("Invalid transaction-history choice.");
        }
    }

    private void viewLowStockProducts() throws SQLException {
        System.out.println("\n========================================");
        System.out.println("            LOW STOCK REPORT");
        System.out.println("========================================");
        List<Product> products = inventoryService.getLowStockProducts();
        if (products.isEmpty()) {
            System.out.println("No products are currently low in stock.");
            return;
        }
        System.out.printf("%-12s %-24s %10s %15s%n",
                "SKU", "Product", "Quantity", "Reorder Level");
        System.out.println("-----------------------------------------------------------------");
        for (Product product : products) {
            System.out.printf("%-12s %-24s %10d %15d%n",
                    product.getSku(), product.getName(), product.getQuantity(),
                    product.getReorderThreshold());
        }
    }

    private void viewInventoryValueReport() throws SQLException {
        List<Product> products = inventoryService.getProductsSortedByName();
        Map<String, Double> categoryTotals = inventoryService.getCategoryValueTotals();

        System.out.println("\n========================================");
        System.out.println("        INVENTORY VALUE REPORT");
        System.out.println("========================================");
        if (products.isEmpty()) {
            System.out.println("No products are available.");
            return;
        }

        List<String> categories = new ArrayList<>(categoryTotals.keySet());
        categories.sort(String.CASE_INSENSITIVE_ORDER);
        for (String category : categories) {
            System.out.println("\nCategory: " + category);
            System.out.printf("%-24s %8s %12s %14s%n",
                    "Product", "Qty", "Unit Price", "Value");
            System.out.println("--------------------------------------------------------------");
            for (Product product : products) {
                if (product.getCategory().equals(category)) {
                    double value = product.getQuantity() * product.getUnitPrice();
                    System.out.printf("%-24s %8d %12.2f %14.2f%n",
                            product.getName(), product.getQuantity(),
                            product.getUnitPrice(), value);
                }
            }
            System.out.printf("Category Total: %.2f%n", categoryTotals.get(category));
        }

        System.out.println("========================================");
        System.out.printf("TOTAL INVENTORY VALUE: %.2f%n",
                inventoryService.getTotalInventoryValue(products));
        System.out.println("========================================");
    }

    private Product readProductDetails(int productId) {
        String name = InputValidator.readRequiredString(scanner, "Name: ");
        String sku = InputValidator.readRequiredString(scanner, "SKU: ");
        String category = InputValidator.readRequiredString(scanner, "Category: ");
        int quantity = InputValidator.readNonNegativeInt(scanner, "Quantity: ");
        double unitPrice = InputValidator.readNonNegativeDouble(scanner, "Unit price: ");
        int reorderThreshold = InputValidator.readNonNegativeInt(
                scanner, "Reorder threshold: ");
        return new Product(productId, name, sku, category, quantity,
                unitPrice, reorderThreshold);
    }

    private void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.printf("%-5s %-22s %-12s %-18s %8s %12s %10s%n",
                "ID", "Name", "SKU", "Category", "Qty", "Price", "Reorder");
        System.out.println("----------------------------------------------------------------------------------------");
        for (Product product : products) {
            System.out.printf("%-5d %-22s %-12s %-18s %8d %12.2f %10d%n",
                    product.getId(), product.getName(), product.getSku(),
                    product.getCategory(), product.getQuantity(),
                    product.getUnitPrice(), product.getReorderThreshold());
        }
    }

    private void printProduct(Product product) {
        List<Product> products = new ArrayList<>();
        products.add(product);
        printProducts(products);
    }

    private void printTransactions(List<StockTransaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No stock transactions found.");
            return;
        }
        System.out.printf("%-6s %-11s %-12s %10s %-20s%n",
                "ID", "Product ID", "Type", "Quantity", "Date");
        System.out.println("---------------------------------------------------------------");
        for (StockTransaction transaction : transactions) {
            System.out.printf("%-6d %-11d %-12s %10d %-20s%n",
                    transaction.getId(), transaction.getProductId(),
                    transaction.getTransactionType(), transaction.getQuantity(),
                    transaction.getTransactionDate().format(DATE_FORMAT));
        }
    }
}
