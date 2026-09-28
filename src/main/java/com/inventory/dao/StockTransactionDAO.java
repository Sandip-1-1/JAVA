package com.inventory.dao;

import com.inventory.model.StockTransaction;
import com.inventory.model.TransactionType;
import com.inventory.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO {
    public StockTransaction recordTransaction(StockTransaction transaction) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return recordTransaction(connection, transaction);
        }
    }

    public StockTransaction recordTransaction(Connection connection,
                                              StockTransaction transaction) throws SQLException {
        String sql = "INSERT INTO stock_transactions "
                + "(product_id, transaction_type, quantity, transaction_date) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, transaction.getProductId());
            statement.setString(2, transaction.getTransactionType().name());
            statement.setInt(3, transaction.getQuantity());
            statement.setString(4, transaction.getTransactionDate().toString());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
            return transaction;
        }
    }

    public List<StockTransaction> getAllTransactions() throws SQLException {
        String sql = "SELECT id, product_id, transaction_type, quantity, transaction_date "
                + "FROM stock_transactions ORDER BY transaction_date DESC, id DESC";
        List<StockTransaction> transactions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                transactions.add(mapTransaction(resultSet));
            }
        }
        return transactions;
    }

    public List<StockTransaction> getTransactionsByProduct(int productId) throws SQLException {
        String sql = "SELECT id, product_id, transaction_type, quantity, transaction_date "
                + "FROM stock_transactions WHERE product_id = ? "
                + "ORDER BY transaction_date DESC, id DESC";
        List<StockTransaction> transactions = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
            }
        }
        return transactions;
    }

    private StockTransaction mapTransaction(ResultSet resultSet) throws SQLException {
        return new StockTransaction(
                resultSet.getInt("id"),
                resultSet.getInt("product_id"),
                TransactionType.valueOf(resultSet.getString("transaction_type")),
                resultSet.getInt("quantity"),
                LocalDateTime.parse(resultSet.getString("transaction_date").replace(' ', 'T'))
        );
    }
}
