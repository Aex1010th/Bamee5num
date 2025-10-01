package com.restaurant.demo.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(String message) {
        super(message);
    }

    public CartItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CartItemNotFoundException byId(Long id) {
        return new CartItemNotFoundException("Cart item not found with ID: " + id);
    }

    public static CartItemNotFoundException byCustomerAndItem(Long customerId, String itemName) {
        return new CartItemNotFoundException("Cart item '" + itemName + "' not found for customer ID: " + customerId);
    }
}
