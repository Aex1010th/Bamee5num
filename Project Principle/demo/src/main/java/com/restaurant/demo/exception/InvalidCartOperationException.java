package com.restaurant.demo.exception;

public class InvalidCartOperationException extends RuntimeException {

    public InvalidCartOperationException(String message) {
        super(message);
    }

    public InvalidCartOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidCartOperationException invalidQuantity(int quantity) {
        return new InvalidCartOperationException("Invalid quantity: " + quantity + ". Quantity must be greater than 0");
    }

    public static InvalidCartOperationException emptyCart() {
        return new InvalidCartOperationException("Cannot perform operation on empty cart");
    }

    public static InvalidCartOperationException invalidPrice(double price) {
        return new InvalidCartOperationException("Invalid price: " + price + ". Price must be greater than 0");
    }

    public static InvalidCartOperationException duplicateItem(String itemName) {
        return new InvalidCartOperationException("Item '" + itemName + "' already exists in cart. Use update quantity instead");
    }
}
