package com.inventory.model;

import java.time.LocalDateTime;

public class StockTransaction implements Validatable {
    private int id;
    private int productId;
    private TransactionType transactionType;
    private int quantity;
    private LocalDateTime transactionDate;

    public StockTransaction(int productId, TransactionType transactionType, int quantity,
                            LocalDateTime transactionDate) {
        this(0, productId, transactionType, quantity, transactionDate);
    }

    public StockTransaction(int id, int productId, TransactionType transactionType,
                            int quantity, LocalDateTime transactionDate) {
        this.id = id;
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
        validate();
    }

    @Override
    public void validate() {
        if (id < 0) {
            throw new IllegalArgumentException("Transaction ID cannot be negative.");
        }
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive.");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Transaction quantity must be positive.");
        }
        if (transactionDate == null) {
            throw new IllegalArgumentException("Transaction date is required.");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Transaction ID cannot be negative.");
        }
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive.");
        }
        this.productId = productId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Transaction quantity must be positive.");
        }
        this.quantity = quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        if (transactionDate == null) {
            throw new IllegalArgumentException("Transaction date is required.");
        }
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "StockTransaction{" +
                "id=" + id +
                ", productId=" + productId +
                ", transactionType=" + transactionType +
                ", quantity=" + quantity +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
