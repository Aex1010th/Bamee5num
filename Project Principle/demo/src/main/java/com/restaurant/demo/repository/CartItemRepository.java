package com.restaurant.demo.repository;

import com.restaurant.demo.model.CartItem;
import com.restaurant.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomer(Customer customer);
    Optional<CartItem> findByCustomerAndItemName(Customer customer, String itemName);

    // Deprecated methods for backward compatibility
    @Deprecated
    default List<CartItem> findByCustomerId(int customerId) {
        // This method is deprecated and should not be used
        throw new UnsupportedOperationException("Use findByCustomer(Customer customer) instead");
    }

    @Deprecated
    default Optional<CartItem> findByCustomerIdAndName(int customerId, String name) {
        // This method is deprecated and should not be used
        throw new UnsupportedOperationException("Use findByCustomerAndItemName(Customer customer, String itemName) instead");
    }
}
