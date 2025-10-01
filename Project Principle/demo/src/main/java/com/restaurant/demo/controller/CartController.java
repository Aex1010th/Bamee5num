package com.restaurant.demo.controller;

import com.restaurant.demo.model.CartItem;
import com.restaurant.demo.service.CartService;
<<<<<<< HEAD
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
>>>>>>> feature/seperate-customer-cart
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Validated
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true", maxAge = 3600)
public class CartController {

<<<<<<< HEAD
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable int userId) {
        return cartService.getCartByCustomerId(userId);
    }

    @PostMapping("/add")
    public CartItem addToCart(@RequestBody CartItem cartItem) {
        return cartService.addToCart(cartItem);
    }

    @DeleteMapping("/remove/{id}")
    public void removeFromCart(@PathVariable int id) {
        cartService.removeFromCart(id);
=======
    @Autowired
    private CartService cartService;

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestParam @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId,
            @RequestParam @NotBlank(message = "Item name is required")
            @Size(min = 1, max = 100, message = "Item name must be between 1 and 100 characters") String itemName,
            @RequestParam @NotNull(message = "Item price is required")
            @DecimalMin(value = "0.01", message = "Item price must be greater than 0")
            @DecimalMax(value = "9999.99", message = "Item price must not exceed 9999.99") BigDecimal itemPrice,
            @RequestParam @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 100, message = "Quantity must not exceed 100") Integer quantity) {

        CartItem cartItem = cartService.addToCart(customerId, itemName, itemPrice, quantity);
        return new ResponseEntity<>(cartItem, HttpStatus.CREATED);
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable @NotNull(message = "Cart item ID is required")
            @Positive(message = "Cart item ID must be positive") Long cartItemId,
            @RequestParam @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId,
            @RequestParam @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 100, message = "Quantity must not exceed 100") Integer quantity) {

        CartItem cartItem = cartService.updateCartItemQuantity(cartItemId, customerId, quantity);
        return new ResponseEntity<>(cartItem, HttpStatus.OK);
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable @NotNull(message = "Cart item ID is required")
            @Positive(message = "Cart item ID must be positive") Long cartItemId,
            @RequestParam @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId) {

        cartService.removeFromCart(cartItemId, customerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get all cart items for a customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CartItem>> getCartItems(
            @PathVariable @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId) {

        List<CartItem> cartItems = cartService.getCartItems(customerId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    /**
     * Get cart item by ID
     */
    @GetMapping("/{cartItemId}")
    public ResponseEntity<CartItem> getCartItem(
            @PathVariable @NotNull(message = "Cart item ID is required")
            @Positive(message = "Cart item ID must be positive") Long cartItemId,
            @RequestParam @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId) {

        CartItem cartItem = cartService.getCartItem(cartItemId, customerId);
        return new ResponseEntity<>(cartItem, HttpStatus.OK);
    }

    /**
     * Clear all cart items for a customer
     */
    @DeleteMapping("/clear/{customerId}")
    public ResponseEntity<Void> clearCart(
            @PathVariable @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId) {

        cartService.clearCart(customerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get cart total for a customer
     */
    @GetMapping("/total/{customerId}")
    public ResponseEntity<BigDecimal> getCartTotal(
            @PathVariable @NotNull(message = "Customer ID is required")
            @Positive(message = "Customer ID must be positive") Long customerId) {

        BigDecimal total = cartService.calculateCartTotal(customerId);
        return new ResponseEntity<>(total, HttpStatus.OK);
>>>>>>> feature/seperate-customer-cart
    }
}