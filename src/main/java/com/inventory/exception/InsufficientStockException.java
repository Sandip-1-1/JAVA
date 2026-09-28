package com.inventory.exception;

public class InsufficientStockException extends Exception {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(int requestedQuantity, int availableQuantity) {
        super("Cannot remove " + requestedQuantity + " item(s). Only "
                + availableQuantity + " item(s) are available.");
    }
}
