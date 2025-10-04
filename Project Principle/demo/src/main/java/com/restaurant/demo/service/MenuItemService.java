package com.restaurant.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.restaurant.demo.model.MenuItem;
import com.restaurant.demo.repository.MenuItemRepo;

@Service
public class MenuItemService {
    @Autowired
    private MenuItemRepo menuItemRepo;

    // Fetch all active menu items for customer/employee views
    public List<MenuItem> getActiveMenuItems() {
        return menuItemRepo.findByActiveTrue();
    }

    // add new menuItem
    public MenuItem addMenuItem(MenuItem menuItem) {
        return menuItemRepo.save(menuItem);
    }

    // Remove a menu item by id
    public void deleteMenuItem(Long id) {
        menuItemRepo.deleteById(id);
    }

    public Optional<MenuItem> findById(Long id) {
        return menuItemRepo.findById(id);
    }

    public Optional<MenuItem> updateMenuItem(Long id, MenuItem menuItem) {
        return menuItemRepo.findById(id).map(existing -> {
            existing.setName(menuItem.getName());
            existing.setPrice(menuItem.getPrice());
            existing.setCategory(menuItem.getCategory());
            existing.setDescription(menuItem.getDescription());
            existing.setActive(menuItem.isActive());
            if (StringUtils.hasText(menuItem.getOrderStatus())) {
                existing.setOrderStatus(menuItem.getOrderStatus().trim());
            }
            return menuItemRepo.save(existing);
        });
    }

    public Optional<MenuItem> updateOrderStatus(Long id, String status) {
        if (!StringUtils.hasText(status)) {
            return Optional.empty();
        }
        String trimmed = status.trim();
        return menuItemRepo.findById(id).map(existing -> {
            existing.setOrderStatus(trimmed);
            return menuItemRepo.save(existing);
        });
    }
}
