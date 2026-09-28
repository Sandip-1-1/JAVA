package com.inventory.exception;

public class ProductNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(int productId) {
        super("Product with ID " + productId + " was not found.");
    }
}
