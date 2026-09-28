package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.StockTransactionDAO;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.model.StockTransaction;
import com.inventory.model.TransactionType;
import com.inventory.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private final ProductDAO productDAO;
    private final StockTransactionDAO transactionDAO;

    public InventoryService() {
        this.productDAO = new ProductDAO();
        this.transactionDAO = new StockTransactionDAO();
    }

    public Product stockIn(int productId, int quantity)
            throws SQLException, ProductNotFoundException {
        validateTransactionQuantity(quantity);
        return changeStock(productId, quantity, TransactionType.STOCK_IN);
    }

    public Product stockOut(int productId, int quantity)
            throws SQLException, ProductNotFoundException, InsufficientStockException {
        validateTransactionQuantity(quantity);

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Product product = productDAO.getProductById(connection, productId);
                if (quantity > product.getQuantity()) {
                    throw new InsufficientStockException(quantity, product.getQuantity());
                }

                int newQuantity = product.getQuantity() - quantity;
                productDAO.updateQuantity(connection, productId, newQuantity);
                recordTransaction(connection, productId, TransactionType.STOCK_OUT, quantity);
                connection.commit();
                product.setQuantity(newQuantity);
                return product;
            } catch (SQLException | ProductNotFoundException
                     | InsufficientStockException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public List<StockTransaction> getAllTransactions() throws SQLException {
        return transactionDAO.getAllTransactions();
    }

    public List<StockTransaction> getTransactionsByProduct(int productId)
            throws SQLException, ProductNotFoundException {
        productDAO.getProductById(productId);
        return transactionDAO.getTransactionsByProduct(productId);
    }

    public List<Product> getProductsSortedByName() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
        return products;
    }

    public List<Product> getProductsSortedByQuantity() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        products.sort(Comparator.comparingInt(Product::getQuantity)
                .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
        return products;
    }

    public List<Product> getProductsSortedByUnitPrice() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        products.sort(Comparator.comparingDouble(Product::getUnitPrice)
                .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
        return products;
    }

    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> lowStockProducts = new ArrayList<>();
        for (Product product : productDAO.getAllProducts()) {
            if (product.getQuantity() <= product.getReorderThreshold()) {
                lowStockProducts.add(product);
            }
        }
        lowStockProducts.sort(Comparator.comparingInt(Product::getQuantity));
        return lowStockProducts;
    }

    public Map<String, Double> getCategoryValueTotals() throws SQLException {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Product product : productDAO.getAllProducts()) {
            double productValue = product.getQuantity() * product.getUnitPrice();
            double currentTotal = categoryTotals.getOrDefault(product.getCategory(), 0.0);
            categoryTotals.put(product.getCategory(), currentTotal + productValue);
        }
        return categoryTotals;
    }

    public double getTotalInventoryValue(List<Product> products) {
        double total = 0.0;
        for (Product product : products) {
            total += product.getQuantity() * product.getUnitPrice();
        }
        return total;
    }

    private Product changeStock(int productId, int quantity, TransactionType type)
            throws SQLException, ProductNotFoundException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Product product = productDAO.getProductById(connection, productId);
                int newQuantity = Math.addExact(product.getQuantity(), quantity);
                productDAO.updateQuantity(connection, productId, newQuantity);
                recordTransaction(connection, productId, type, quantity);
                connection.commit();
                product.setQuantity(newQuantity);
                return product;
            } catch (SQLException | ProductNotFoundException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    private void recordTransaction(Connection connection, int productId,
                                   TransactionType type, int quantity) throws SQLException {
        StockTransaction transaction = new StockTransaction(
                productId, type, quantity, LocalDateTime.now());
        transactionDAO.recordTransaction(connection, transaction);
    }

    private void validateTransactionQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than zero.");
        }
    }

    private void rollback(Connection connection, Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
