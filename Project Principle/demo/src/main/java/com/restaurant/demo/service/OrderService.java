package com.restaurant.demo.service;

import com.restaurant.demo.dto.OrderResponseDto;
import com.restaurant.demo.dto.OrderStatusUpdateDto;
import com.restaurant.demo.model.CartItem;
import com.restaurant.demo.model.Customer;
import com.restaurant.demo.repository.CartItemRepository;
import com.restaurant.demo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;

    private static final List<String> ORDER_STATUSES = List.of("Pending", "In Progress", "Cancelled", "Finish");

    public OrderService(CartItemRepository cartItemRepository, CustomerRepository customerRepository) {
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Place an order by converting cart items to pending orders
     * 
     * @param customerId The ID of the customer placing the order
     * @return OrderResponseDto containing the placed order details
     * @throws RuntimeException if customer not found or cart is empty
     */
    public OrderResponseDto placeOrder(Long customerId) {
        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Get all cart items for this customer (assuming items with any status in cart)
        // We'll look for items that are not yet ordered (you may need to adjust this logic)
        List<CartItem> cartItems = cartItemRepository.findByCustomer(customer);

        // Filter only items that are not already in an order status (or adjust based on your requirements)
        // For now, we'll assume we want to place order for items in cart regardless of current status
        List<CartItem> itemsToOrder = cartItems.stream()
                .filter(item -> item.getStatus() == null || item.getStatus().isEmpty() || 
                                item.getStatus().equals("Cart") || item.getStatus().equals("Pending"))
                .collect(Collectors.toList());

        if (itemsToOrder.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // Update all cart items to "Pending" status
        itemsToOrder.forEach(item -> item.setStatus("Pending"));
        cartItemRepository.saveAll(itemsToOrder);

        return buildOrderResponse(customer, itemsToOrder, "Pending");
    }

    /**
     * Get the most recently updated order snapshot for a specific customer.
     * Includes status updates that employees may have applied (Pending, In Progress, Finish, Cancelled).
     *
     * @param customerId The ID of the customer
     * @return OrderResponseDto containing latest order details
     * @throws RuntimeException if customer not found
     */
    public OrderResponseDto getPendingOrdersByCustomerId(Long customerId) {
        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Get latest cart items across tracked statuses (Pending, In Progress, Cancelled, Finish)
        List<CartItem> trackedItems = cartItemRepository
                .findByCustomerAndStatusInOrderByUpdatedAtDesc(customer, ORDER_STATUSES);

        if (trackedItems.isEmpty()) {
            return new OrderResponseDto(
                    customer.getId(),
                    customer.getName(),
                    List.of(),
                    BigDecimal.ZERO,
                    "Pending",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        }

        // Determine the latest status from most recently updated item
        String latestStatus = trackedItems.stream()
                .map(CartItem::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .flatMap(latestUpdate -> trackedItems.stream()
                        .filter(item -> latestUpdate.equals(item.getUpdatedAt()))
                        .map(CartItem::getStatus)
                        .filter(Objects::nonNull)
                        .findFirst())
                .orElseGet(() -> trackedItems.stream()
                        .map(CartItem::getStatus)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("Pending"));

        // Filter items to the ones matching the latest status so we show a single order snapshot
        List<CartItem> latestStatusItems = trackedItems.stream()
                .filter(item -> latestStatus.equals(item.getStatus()))
                .collect(Collectors.toList());

        return buildOrderResponse(customer, latestStatusItems.isEmpty() ? trackedItems : latestStatusItems, latestStatus);
    }

    /**
     * Get all orders filtered by status (for employee view)
     * 
     * @param status The status to filter by (Pending, In Progress, Cancelled, Finish)
     * @return List of OrderResponseDto grouped by customer
     */
    public List<OrderResponseDto> getAllOrdersByStatus(String status) {
        // Validate status
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        // Get all cart items with the specified status
        List<CartItem> items = cartItemRepository.findByStatus(status);

        // Group by customer
        Map<Customer, List<CartItem>> itemsByCustomer = items.stream()
                .collect(Collectors.groupingBy(CartItem::getCustomer));

        // Convert to OrderResponseDto list
        return itemsByCustomer.entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<CartItem> customerItems = entry.getValue();

                    return buildOrderResponse(customer, customerItems, status);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update order status with validation
     * 
     * @param updateDto OrderStatusUpdateDto containing customerId and new status
     * @return OrderResponseDto with updated order
     * @throws RuntimeException if customer not found or validation fails
     */
    public OrderResponseDto updateOrderStatus(OrderStatusUpdateDto updateDto) {
        Long customerId = updateDto.getCustomerId();
        String newStatus = updateDto.getNewStatus();

        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Get all cart items for this customer (we'll update all items to the same status)
        List<CartItem> customerItems = cartItemRepository.findByCustomer(customer);

        if (customerItems.isEmpty()) {
            throw new RuntimeException("No items found for customer ID: " + customerId);
        }

        // Get current status (assuming all items have the same status)
        String currentStatus = customerItems.get(0).getStatus();

        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }

        // Update all items to new status
        customerItems.forEach(item -> item.setStatus(newStatus));
        cartItemRepository.saveAll(customerItems);

        return buildOrderResponse(customer, customerItems, newStatus);
    }

    /**
     * Get count of orders by status (for notification polling)
     * 
     * @param status The status to count
     * @return Count of orders with the specified status (grouped by customer)
     */
    public Long getOrderCountByStatus(String status) {
        // Validate status
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        // Get all cart items with the specified status
        List<CartItem> items = cartItemRepository.findByStatus(status);

        // Count unique customers (each customer represents one order)
        return items.stream()
                .map(CartItem::getCustomer)
                .map(Customer::getId)
                .distinct()
                .count();
    }

    /**
     * Validate if the status is valid
     * 
     * @param status The status to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidStatus(String status) {
        return status != null && ORDER_STATUSES.contains(status);
    }

    /**
     * Validate status transition rules
     * Status transitions: Pending → In Progress → Finish
     * Any status can transition to Cancelled
     * 
     * @param currentStatus The current status
     * @param newStatus The new status
     * @return true if transition is valid, false otherwise
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        // Any status can transition to Cancelled
        if (newStatus.equals("Cancelled")) {
            return true;
        }

        // Status transition rules
        switch (currentStatus) {
            case "Pending":
                return newStatus.equals("In Progress") || newStatus.equals("Cancelled");
            case "In Progress":
                return newStatus.equals("Finish") || newStatus.equals("Cancelled");
            case "Finish":
            case "Cancelled":
                // Cannot change from Finish or Cancelled to other statuses
                return false;
            default:
                return false;
        }
    }

    private OrderResponseDto buildOrderResponse(Customer customer, List<CartItem> items, String statusOverride) {
        List<OrderResponseDto.OrderItemDto> orderItems = items.stream()
                .map(item -> new OrderResponseDto.OrderItemDto(
                        item.getId(),
                        item.getItemName(),
                        item.getItemPrice(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime createdAt = items.stream()
                .map(CartItem::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        LocalDateTime updatedAt = items.stream()
                .map(CartItem::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        String resolvedStatus = statusOverride != null ? statusOverride : items.stream()
                .map(CartItem::getStatus)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Pending");

        return new OrderResponseDto(
                customer.getId(),
                customer.getName(),
                orderItems,
                totalPrice,
                resolvedStatus,
                createdAt,
                updatedAt
        );
    }
}
