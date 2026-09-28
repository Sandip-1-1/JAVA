package com.inventory.exception;

public class DuplicateProductException extends Exception {
    public DuplicateProductException(String sku) {
        super("A product with SKU '" + sku + "' already exists.");
    }
}
