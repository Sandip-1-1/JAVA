package com.inventory.exception;

public class InsufficientStockException extends Exception {
    public InsufficientStockException(int requestedQuantity, int availableQuantity) {
        super("Cannot remove " + requestedQuantity + " item(s). Only "
                + availableQuantity + " item(s) are available.");
    }
}
