package com.restaurant.demo.service;

import com.restaurant.demo.model.CartItem;

import com.restaurant.demo.repository.CartItemRepository;
import org.springframework.stereotype.Service;


import com.restaurant.demo.model.Customer;
import com.restaurant.demo.repository.CartItemRepository;
import com.restaurant.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
        
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Helper method to get customer by ID
    private Customer getCustomerById(Long customerId) {
        if (customerId == null) {
            throw new RuntimeException("Customer ID is required");
        }
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
    }

    // Public methods that accept customerId
    public CartItem addToCart(Long customerId, String itemName, BigDecimal itemPrice, Integer quantity) {
        Customer customer = getCustomerById(customerId);
        return addToCart(customer, itemName, itemPrice, quantity);
    }

    public CartItem updateCartItemQuantity(Long cartItemId, Long customerId, Integer quantity) {
        Customer customer = getCustomerById(customerId);
        return updateQuantity(cartItemId, quantity, customer);
    }

    public void removeFromCart(Long cartItemId, Long customerId) {
        Customer customer = getCustomerById(customerId);
        removeFromCart(cartItemId, customer);
    }

    public List<CartItem> getCartItems(Long customerId) {
        Customer customer = getCustomerById(customerId);
        return getCartByCustomer(customer);
    }

    public CartItem getCartItem(Long cartItemId, Long customerId) {
        Customer customer = getCustomerById(customerId);
        return getCartItem(cartItemId, customer).orElse(null);
    }

    public void clearCart(Long customerId) {
        Customer customer = getCustomerById(customerId);
        clearCart(customer);
    }

    public BigDecimal calculateCartTotal(Long customerId) {
        Customer customer = getCustomerById(customerId);
        List<CartItem> items = getCartByCustomer(customer);
        return items.stream()
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Existing methods that work with Customer objects
    public List<CartItem> getCartByCustomer(Customer customer) {
        if (customer == null) {
            throw new RuntimeException("Customer authentication required");
        }
        return cartItemRepository.findByCustomer(customer);
    }

    public CartItem addToCart(Customer customer, String name, BigDecimal price, int quantity) {
        if (customer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        // Validate initial quantity
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        if (quantity > 99) {
            throw new RuntimeException("Quantity cannot exceed 99");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCustomerAndItemName(customer, name);

        if (existingItem.isPresent()) {
            // Item exists, increase quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Apply quantity validation
            if (newQuantity > 99) {
                newQuantity = 99; // Cap at maximum
            }

            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            // Item doesn't exist, create new entry
            CartItem newItem = new CartItem(customer, name, price, quantity);
            return cartItemRepository.save(newItem);
        }
    }

    public CartItem incrementQuantity(Long itemId, Customer authenticatedCustomer) {
        if (authenticatedCustomer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        Optional<CartItem> optionalItem = cartItemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem item = optionalItem.get();

        // Validate ownership
        if (!item.getCustomer().getId().equals(authenticatedCustomer.getId())) {
            throw new RuntimeException("Access denied: Item does not belong to authenticated customer");
        }

        int newQuantity = item.getQuantity() + 1;

        // Apply maximum quantity validation
        if (newQuantity > 99) {
            throw new RuntimeException("Quantity cannot exceed 99");
        }

        item.setQuantity(newQuantity);
        return cartItemRepository.save(item);
    }

    public CartItem decrementQuantity(Long itemId, Customer authenticatedCustomer) {
        if (authenticatedCustomer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        Optional<CartItem> optionalItem = cartItemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem item = optionalItem.get();

        // Validate ownership
        if (!item.getCustomer().getId().equals(authenticatedCustomer.getId())) {
            throw new RuntimeException("Access denied: Item does not belong to authenticated customer");
        }

        if (item.getQuantity() <= 1) {
            throw new RuntimeException("Quantity cannot be less than 1");
        }

        item.setQuantity(item.getQuantity() - 1);
        return cartItemRepository.save(item);
    }

    public CartItem updateQuantity(Long itemId, int quantity, Customer authenticatedCustomer) {
        if (authenticatedCustomer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        if (quantity > 99) {
            throw new RuntimeException("Quantity cannot exceed 99");
        }

        Optional<CartItem> optionalItem = cartItemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem item = optionalItem.get();

        // Validate ownership
        if (!item.getCustomer().getId().equals(authenticatedCustomer.getId())) {
            throw new RuntimeException("Access denied: Item does not belong to authenticated customer");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void removeFromCart(Long itemId, Customer authenticatedCustomer) {
        if (authenticatedCustomer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        Optional<CartItem> optionalItem = cartItemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem item = optionalItem.get();

        // Validate ownership
        if (!item.getCustomer().getId().equals(authenticatedCustomer.getId())) {
            throw new RuntimeException("Access denied: Item does not belong to authenticated customer");
        }

        cartItemRepository.deleteById(itemId);
    }

    public void clearCart(Customer customer) {
        if (customer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        List<CartItem> customerItems = cartItemRepository.findByCustomer(customer);
        cartItemRepository.deleteAll(customerItems);
    }

    public Optional<CartItem> getCartItem(Long itemId, Customer authenticatedCustomer) {
        if (authenticatedCustomer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        Optional<CartItem> optionalItem = cartItemRepository.findById(itemId);
        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();
            // Validate ownership
            if (!item.getCustomer().getId().equals(authenticatedCustomer.getId())) {
                throw new RuntimeException("Access denied: Item does not belong to authenticated customer");
            }
        }
        return optionalItem;
    }

    public CartItem updateCartItem(CartItem cartItem, Customer authenticatedCustomer) {
        if (authenticatedCustomer == null) {
            throw new RuntimeException("Customer authentication required");
        }

        if (cartItem.getId() != null) {
            // Validate ownership for existing items
            Optional<CartItem> existingItem = cartItemRepository.findById(cartItem.getId());
            if (existingItem.isPresent() &&
                !existingItem.get().getCustomer().getId().equals(authenticatedCustomer.getId())) {
                throw new RuntimeException("Access denied: Item does not belong to authenticated customer");
            }
        }

        // Ensure the item belongs to the authenticated customer
        cartItem.setCustomer(authenticatedCustomer);
        return cartItemRepository.save(cartItem);
    }

    // Deprecated methods for backward compatibility
    @Deprecated
    public List<CartItem> getCartByCustomerId(int customerId) {
        throw new UnsupportedOperationException("Use getCartByCustomer(Customer customer) instead");
    }

    @Deprecated
    public CartItem addToCart(int customerId, String name, int price, int quantity) {
        CartItem newItem = new CartItem(customerId, name, price, quantity);
        return cartItemRepository.save(newItem);
        throw new UnsupportedOperationException("Use addToCart(Customer customer, String name, int price, int quantity) instead");
    }

    @Deprecated
    public CartItem incrementQuantity(int itemId) {
        throw new UnsupportedOperationException("Use incrementQuantity(Long itemId, Customer authenticatedCustomer) instead");
    }

    @Deprecated
    public CartItem decrementQuantity(int itemId) {
        throw new UnsupportedOperationException("Use decrementQuantity(Long itemId, Customer authenticatedCustomer) instead");
    }

    @Deprecated
    public CartItem updateQuantity(int itemId, int quantity) {
        throw new UnsupportedOperationException("Use updateQuantity(Long itemId, int quantity, Customer authenticatedCustomer) instead");
    }

    @Deprecated
    public void removeFromCart(int itemId) {
        throw new UnsupportedOperationException("Use removeFromCart(Long itemId, Customer authenticatedCustomer) instead");
    }

    @Deprecated
    public void clearCart(int customerId) {
        throw new UnsupportedOperationException("Use clearCart(Customer customer) instead");
    }

    @Deprecated
    public Optional<CartItem> getCartItem(int itemId) {
        throw new UnsupportedOperationException("Use getCartItem(Long itemId, Customer authenticatedCustomer) instead");
    }
}