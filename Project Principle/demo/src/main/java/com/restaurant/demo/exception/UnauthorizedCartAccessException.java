package com.restaurant.demo.exception;

public class UnauthorizedCartAccessException extends RuntimeException {

    public UnauthorizedCartAccessException(String message) {
        super(message);
    }

    public UnauthorizedCartAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UnauthorizedCartAccessException forCartItem(Long cartItemId) {
        return new UnauthorizedCartAccessException("You are not authorized to access cart item with ID: " + cartItemId);
    }

    public static UnauthorizedCartAccessException forCustomerCart(Long customerId) {
        return new UnauthorizedCartAccessException("You are not authorized to access cart for customer ID: " + customerId);
    }
}
