package com.restaurant.demo.exception;

public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }

    public CustomerAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CustomerAlreadyExistsException withEmail(String email) {
        return new CustomerAlreadyExistsException("Customer already exists with email: " + email);
    }

    public static CustomerAlreadyExistsException withUsername(String username) {
        return new CustomerAlreadyExistsException("Customer already exists with username: " + username);
    }
}
