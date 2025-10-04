package com.restaurant.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restaurant.demo.model.MenuItem;
import com.restaurant.demo.repository.MenuItemRepo;

@Service
public class MenuItemService {
    @Autowired
    private MenuItemRepo menuItemRepo;

    // ค้นหารายการเมนูที่เปิดใช้งาน
    public List<MenuItem> getActiveMenuItems() {
        return menuItemRepo.findByActiveTrue();
    }

    // เพิ่มเมนูใหม่
    public MenuItem addMenuItem(MenuItem menuItem) {
        return menuItemRepo.save(menuItem);
    }

    // ลบเมนูตาม ID
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
            return menuItemRepo.save(existing);
        });
    }

}
