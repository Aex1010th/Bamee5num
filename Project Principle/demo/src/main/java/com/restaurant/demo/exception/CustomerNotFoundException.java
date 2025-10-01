package com.restaurant.demo.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CustomerNotFoundException byId(Long id) {
        return new CustomerNotFoundException("Customer not found with ID: " + id);
    }

    public static CustomerNotFoundException byUsername(String username) {
        return new CustomerNotFoundException("Customer not found with username: " + username);
    }

    public static CustomerNotFoundException byEmail(String email) {
        return new CustomerNotFoundException("Customer not found with email: " + email);
    }
}
