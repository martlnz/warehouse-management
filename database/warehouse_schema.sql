CREATE DATABASE warehouse_db;
GO

USE warehouse_db;
GO

-- 1. Tạo bảng users
CREATE TABLE users (
                       id BIGINT IDENTITY(1,1) PRIMARY KEY,
                       username NVARCHAR(50) UNIQUE NOT NULL,
                       password NVARCHAR(255) NOT NULL,
                       full_name NVARCHAR(100),
                       role NVARCHAR(20) NOT NULL,
                       is_active BIT DEFAULT 1
);

-- 2. Tạo bảng categories
CREATE TABLE categories (
                            id BIGINT IDENTITY(1,1) PRIMARY KEY,
                            name NVARCHAR(100) NOT NULL
);

-- 3. Tạo bảng products
CREATE TABLE products (
                          id BIGINT IDENTITY(1,1) PRIMARY KEY,
                          code NVARCHAR(50) UNIQUE NOT NULL,
                          name NVARCHAR(200) NOT NULL,
                          category_id BIGINT FOREIGN KEY REFERENCES categories(id),
                          quantity INT NOT NULL DEFAULT 0,
                          price DECIMAL(18,2) NOT NULL,
                          is_active BIT DEFAULT 1
);

-- 4. Tạo bảng suppliers
CREATE TABLE suppliers (
                           id BIGINT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(200) NOT NULL,
                           phone NVARCHAR(20),
                           email NVARCHAR(100) UNIQUE
);

-- 5. Tạo bảng customers
CREATE TABLE customers (
                           id BIGINT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(200) NOT NULL,
                           phone NVARCHAR(20),
                           email NVARCHAR(100)
);

-- 6. Tạo bảng stock_receipts (Đã tích hợp cột receipt_code từ file migration)
CREATE TABLE stock_receipts (
                                id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                receipt_type NVARCHAR(10) NOT NULL,
                                receipt_code NVARCHAR(20) NULL,
                                created_date DATETIME NOT NULL DEFAULT GETDATE(),
                                created_by BIGINT FOREIGN KEY REFERENCES users(id),
                                supplier_id BIGINT NULL FOREIGN KEY REFERENCES suppliers(id),
                                customer_id BIGINT NULL FOREIGN KEY REFERENCES customers(id),
                                note NVARCHAR(500),
                                status NVARCHAR(20) NOT NULL
);
GO

-- Tạo Unique Index cho mã phiếu (Tích hợp từ file migration)
CREATE UNIQUE INDEX UX_stock_receipts_code ON stock_receipts(receipt_code) WHERE receipt_code IS NOT NULL;
GO

-- 7. Tạo bảng receipt_details
CREATE TABLE receipt_details (
                                 id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                 receipt_id BIGINT FOREIGN KEY REFERENCES stock_receipts(id),
                                 product_id BIGINT FOREIGN KEY REFERENCES products(id),
                                 quantity INT NOT NULL,
                                 unit_price DECIMAL(18,2) NOT NULL
);
GO

-- =====================================================================
-- CHÈN DỮ LIỆU MẶC ĐỊNH
-- =====================================================================

-- Thêm các danh mục mặc định
INSERT INTO categories (name)
VALUES 
    (N'Đồ Gia Dụng'),
    (N'Đồ Điện Tử'),
    (N'Thực Phẩm'),
    (N'Thời Trang'),
    (N'Nhu Yếu Phẩm'),
    (N'Trang Sức');
GO

-- Thêm tài khoản admin mặc định
INSERT INTO users (username, password, full_name, role, is_active)
VALUES 
    ('admin', '123456', N'Quản trị viên hệ thống', 'ADMIN', 1);
GO