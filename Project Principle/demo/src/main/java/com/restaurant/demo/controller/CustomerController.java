package com.restaurant.demo.controller;

import com.restaurant.demo.dto.AuthResponseDto;
import com.restaurant.demo.dto.CustomerLoginDto;
import com.restaurant.demo.dto.CustomerRegistrationDto;
import com.restaurant.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/customers")
@Validated
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true", maxAge = 3600)
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    /**
     * Register a new customer
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerCustomer(@Valid @RequestBody CustomerRegistrationDto registrationDto) {
        // Additional validation for password confirmation
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        AuthResponseDto response = customerService.registerCustomer(registrationDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticate customer login
     * Stores customer ID in HTTP session for server-side validation
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginCustomer(@Valid @RequestBody CustomerLoginDto loginDto, HttpSession session) {
        logger.info("Login attempt for user: {}, sessionId: {}", loginDto.getUsernameOrEmail(), session.getId());
        
        AuthResponseDto response = customerService.loginCustomer(loginDto);
        
        // Store customer ID in HTTP session for server-side validation
        session.setAttribute("customerId", response.getCustomerId());
        session.setAttribute("username", response.getUsername());
        
        logger.info("Login successful - customerId: {}, username: {}, sessionId: {}", 
            response.getCustomerId(), response.getUsername(), session.getId());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get customer profile by ID
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<AuthResponseDto> getCustomerProfile(
            @PathVariable @Positive(message = "Customer ID must be positive") Long customerId) {

        var customer = customerService.findCustomerById(customerId);
        if (customer.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        AuthResponseDto response = new AuthResponseDto(
                "profile-token-" + customer.get().getId(),
                customer.get().getId(),
                customer.get().getUsername(),
                customer.get().getEmail(),
                java.time.LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update customer profile
     */
    @PutMapping("/{customerId}")
    public ResponseEntity<AuthResponseDto> updateCustomer(
            @PathVariable @Positive(message = "Customer ID must be positive") Long customerId,
            @Valid @RequestBody CustomerRegistrationDto updateDto) {

        AuthResponseDto response = customerService.updateCustomer(customerId, updateDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Check if username is available
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(
            @RequestParam @NotBlank(message = "Username is required") String username) {

        boolean isAvailable = customerService.isUsernameAvailable(username);
        return new ResponseEntity<>(isAvailable, HttpStatus.OK);
    }

    /**
     * Check if email is available
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(
            @RequestParam @NotBlank(message = "Email is required") String email) {

        boolean isAvailable = customerService.isEmailAvailable(email);
        return new ResponseEntity<>(isAvailable, HttpStatus.OK);
    }

    /**
     * Validate password strength
     */
    @PostMapping("/validate-password")
    public ResponseEntity<Boolean> validatePassword(
            @RequestParam @NotBlank(message = "Password is required") String password) {

        boolean isValid = customerService.isValidPassword(password);
        return new ResponseEntity<>(isValid, HttpStatus.OK);
    }
}
