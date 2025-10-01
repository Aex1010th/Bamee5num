package com.restaurant.demo.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class AuthResponseDto {

    @NotBlank(message = "Token is required")
    private String token;

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Login timestamp is required")
    private LocalDateTime loginTime;

    // Default constructor
    public AuthResponseDto() {}

    // Constructor with all fields
    public AuthResponseDto(String token, Long customerId, String username, String email, LocalDateTime loginTime) {
        this.token = token;
        this.customerId = customerId;
        this.username = username;
        this.email = email;
        this.loginTime = loginTime;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}
