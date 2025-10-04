package com.restaurant.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.demo.model.MenuItem;
import com.restaurant.demo.service.MenuItemService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeApiController {

    private final MenuItemService menuItemService;

    public EmployeeApiController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    // Employees จัดการเมนู
    @GetMapping("/menu")
    public List<MenuItem> getMenuForEmployee() {
        return menuItemService.getActiveMenuItems();
    }

    @PostMapping("/menu")
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        MenuItem created = menuItemService.addMenuItem(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        return menuItemService.updateMenuItem(id, menuItem)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        if (menuItemService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    // Employee ดูแลการอัปเดตสถานะคำสั่งซื้อ
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<MenuItem> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest request) {
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        String newStatus = request.getStatus().trim();
        return menuItemService.findById(id)
                .flatMap(item -> {
                    item.setOrderStatus(newStatus);
                    return menuItemService.updateMenuItem(id, item);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class OrderStatusUpdateRequest {
        private String status;

        public OrderStatusUpdateRequest() {}

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
