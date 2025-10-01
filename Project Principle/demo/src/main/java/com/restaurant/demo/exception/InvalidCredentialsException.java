package com.restaurant.demo.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidCredentialsException forLogin() {
        return new InvalidCredentialsException("Invalid username or password");
    }

    public static InvalidCredentialsException forToken() {
        return new InvalidCredentialsException("Invalid or expired authentication token");
    }
}
