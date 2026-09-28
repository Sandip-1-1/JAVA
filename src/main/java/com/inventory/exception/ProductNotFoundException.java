package com.inventory.exception;

public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(int productId) {
        super("Product with ID " + productId + " was not found.");
    }
}
