-- MySQL Database Setup Script for Restaurant Application
-- Run this script in MySQL Workbench or MySQL command line

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Set character set and collation
ALTER DATABASE restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Drop existing tables if they exist (to recreate with correct structure)
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS customer;

-- Create customers table (plural to match entity)
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
);

-- Create cart_items table (plural to match potential entity naming)
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    item_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_created_at (created_at)
);

-- Insert some sample data for testing (optional)
INSERT INTO customers (name, username, email, phone, password_hash) VALUES
('John Doe', 'johndoe', 'john@example.com', '081-234-5678', '$2a$10$example_hash'),
('Jane Smith', 'janesmith', 'jane@example.com', '089-876-5432', '$2a$10$example_hash');

SHOW TABLES;
