package com.restaurant.demo.service;

import com.restaurant.demo.dto.AuthResponseDto;
import com.restaurant.demo.dto.CustomerLoginDto;
import com.restaurant.demo.dto.CustomerRegistrationDto;
import com.restaurant.demo.exception.CustomerAlreadyExistsException;
import com.restaurant.demo.exception.CustomerNotFoundException;
import com.restaurant.demo.exception.InvalidCredentialsException;
import com.restaurant.demo.model.Customer;
import com.restaurant.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new customer with validation
     */
    public AuthResponseDto registerCustomer(CustomerRegistrationDto registrationDto) {
        // Check if username already exists
        if (customerRepository.existsByUsername(registrationDto.getUsername())) {
            throw CustomerAlreadyExistsException.withUsername(registrationDto.getUsername());
        }

        // Check if email already exists
        if (customerRepository.existsByEmail(registrationDto.getEmail())) {
            throw CustomerAlreadyExistsException.withEmail(registrationDto.getEmail());
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setUsername(registrationDto.getUsername());
        customer.setEmail(registrationDto.getEmail());
        customer.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        customer.setName(registrationDto.getName());
        customer.setPhone(registrationDto.getPhone());

        // Save customer
        Customer savedCustomer = customerRepository.save(customer);

        return new AuthResponseDto(
            "registration-token-" + savedCustomer.getId(),
            savedCustomer.getId(),
            savedCustomer.getUsername(),
            savedCustomer.getEmail(),
            LocalDateTime.now()
        );
    }

    /**
     * Authenticate customer login
     */
    public AuthResponseDto loginCustomer(CustomerLoginDto loginDto) {
        // Find customer by username or email
        Optional<Customer> customerOpt = findCustomerByUsernameOrEmail(loginDto.getUsernameOrEmail());

        if (customerOpt.isEmpty()) {
            throw InvalidCredentialsException.forLogin();
        }

        Customer customer = customerOpt.get();

        // Check password
        if (!passwordEncoder.matches(loginDto.getPassword(), customer.getPasswordHash())) {
            throw InvalidCredentialsException.forLogin();
        }

        return new AuthResponseDto(
            "login-token-" + customer.getId(),
            customer.getId(),
            customer.getUsername(),
            customer.getEmail(),
            LocalDateTime.now()
        );
    }

    /**
     * Find customer by ID
     */
    public Optional<Customer> findCustomerById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    /**
     * Find customer by username or email
     */
    public Optional<Customer> findCustomerByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by username first
        Optional<Customer> customer = customerRepository.findByUsername(usernameOrEmail);

        // If not found by username, try by email
        if (customer.isEmpty()) {
            customer = customerRepository.findByEmail(usernameOrEmail);
        }

        return customer;
    }

    /**
     * Validate password strength
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Check for at least one lowercase, uppercase, digit, and special character
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$");
    }

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !customerRepository.existsByUsername(username);
    }

    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !customerRepository.existsByEmail(email);
    }

    /**
     * Update customer profile
     */
    public AuthResponseDto updateCustomer(Long customerId, CustomerRegistrationDto updateDto) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);

        if (customerOpt.isEmpty()) {
            throw CustomerNotFoundException.byId(customerId);
        }

        Customer customer = customerOpt.get();

        // Check if username is being changed and if it's available
        if (!customer.getUsername().equals(updateDto.getUsername()) &&
            !isUsernameAvailable(updateDto.getUsername())) {
            throw CustomerAlreadyExistsException.withUsername(updateDto.getUsername());
        }

        // Check if email is being changed and if it's available
        if (!customer.getEmail().equals(updateDto.getEmail()) &&
            !isEmailAvailable(updateDto.getEmail())) {
            throw CustomerAlreadyExistsException.withEmail(updateDto.getEmail());
        }

        // Update customer fields
        customer.setUsername(updateDto.getUsername());
        customer.setEmail(updateDto.getEmail());
        customer.setName(updateDto.getName());
        customer.setPhone(updateDto.getPhone());

        // Update password if provided
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            customer.setPasswordHash(passwordEncoder.encode(updateDto.getPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);

        return new AuthResponseDto(
            "update-token-" + updatedCustomer.getId(),
            updatedCustomer.getId(),
            updatedCustomer.getUsername(),
            updatedCustomer.getEmail(),
            LocalDateTime.now()
        );
    }
}
