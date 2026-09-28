package com.inventory.dao;

import com.inventory.exception.DuplicateProductException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public Product addProduct(Product product) throws SQLException, DuplicateProductException {
        String sql = "INSERT INTO products "
                + "(name, sku, category, quantity, unit_price, reorder_threshold) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            setProductParameters(statement, product);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
            }
            return product;
        } catch (SQLException e) {
            if (isDuplicateSku(e)) {
                throw new DuplicateProductException(product.getSku());
            }
            throw e;
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        String sql = "SELECT id, name, sku, category, quantity, unit_price, "
                + "reorder_threshold FROM products";
        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
        }
        return products;
    }

    public Product getProductById(int productId)
            throws SQLException, ProductNotFoundException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return getProductById(connection, productId);
        }
    }

    public Product getProductById(Connection connection, int productId)
            throws SQLException, ProductNotFoundException {
        String sql = "SELECT id, name, sku, category, quantity, unit_price, "
                + "reorder_threshold FROM products WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProduct(resultSet);
                }
            }
        }
        throw new ProductNotFoundException(productId);
    }

    public List<Product> searchProducts(String searchTerm) throws SQLException {
        String sql = "SELECT id, name, sku, category, quantity, unit_price, "
                + "reorder_threshold FROM products "
                + "WHERE LOWER(name) LIKE LOWER(?) OR LOWER(sku) LIKE LOWER(?) "
                + "OR LOWER(category) LIKE LOWER(?)";
        String pattern = "%" + searchTerm.trim() + "%";
        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }
        return products;
    }

    public void updateProduct(Product product)
            throws SQLException, ProductNotFoundException, DuplicateProductException {
        String sql = "UPDATE products SET name = ?, sku = ?, category = ?, quantity = ?, "
                + "unit_price = ?, reorder_threshold = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setProductParameters(statement, product);
            statement.setInt(7, product.getId());
            if (statement.executeUpdate() == 0) {
                throw new ProductNotFoundException(product.getId());
            }
        } catch (SQLException e) {
            if (isDuplicateSku(e)) {
                throw new DuplicateProductException(product.getSku());
            }
            throw e;
        }
    }

    public void deleteProduct(int productId) throws SQLException, ProductNotFoundException {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            if (statement.executeUpdate() == 0) {
                throw new ProductNotFoundException(productId);
            }
        }
    }

    public void updateQuantity(Connection connection, int productId, int newQuantity)
            throws SQLException, ProductNotFoundException {
        String sql = "UPDATE products SET quantity = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, productId);
            if (statement.executeUpdate() == 0) {
                throw new ProductNotFoundException(productId);
            }
        }
    }

    private void setProductParameters(PreparedStatement statement, Product product)
            throws SQLException {
        statement.setString(1, product.getName().trim());
        statement.setString(2, product.getSku().trim());
        statement.setString(3, product.getCategory().trim());
        statement.setInt(4, product.getQuantity());
        statement.setDouble(5, product.getUnitPrice());
        statement.setInt(6, product.getReorderThreshold());
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("sku"),
                resultSet.getString("category"),
                resultSet.getInt("quantity"),
                resultSet.getDouble("unit_price"),
                resultSet.getInt("reorder_threshold")
        );
    }

    private boolean isDuplicateSku(SQLException exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains("UNIQUE constraint failed: products.sku");
    }
}
