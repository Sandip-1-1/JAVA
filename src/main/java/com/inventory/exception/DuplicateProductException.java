package com.inventory.exception;

public class DuplicateProductException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateProductException(String sku) {
        super("A product with SKU '" + sku + "' already exists.");
    }
}
