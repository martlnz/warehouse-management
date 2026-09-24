CREATE DATABASE warehouse_db;
GO

USE warehouse_db;
GO

CREATE TABLE users (
                       id BIGINT IDENTITY(1,1) PRIMARY KEY,
                       username NVARCHAR(50) UNIQUE NOT NULL,
                       password NVARCHAR(255) NOT NULL,
                       full_name NVARCHAR(100),
                       role NVARCHAR(20) NOT NULL,
                       is_active BIT DEFAULT 1
);

CREATE TABLE categories (
                            id BIGINT IDENTITY(1,1) PRIMARY KEY,
                            name NVARCHAR(100) NOT NULL
);

CREATE TABLE products (
                          id BIGINT IDENTITY(1,1) PRIMARY KEY,
                          code NVARCHAR(50) UNIQUE NOT NULL,
                          name NVARCHAR(200) NOT NULL,
                          category_id BIGINT FOREIGN KEY REFERENCES categories(id),
                          quantity INT NOT NULL DEFAULT 0,
                          price DECIMAL(18,2) NOT NULL,
                          is_active BIT DEFAULT 1
);

CREATE TABLE suppliers (
                           id BIGINT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(200) NOT NULL,
                           phone NVARCHAR(20),
                           email NVARCHAR(100) UNIQUE
);

CREATE TABLE customers (
                           id BIGINT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(200) NOT NULL,
                           phone NVARCHAR(20),
                           email NVARCHAR(100)
);

CREATE TABLE stock_receipts (
                                id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                receipt_type NVARCHAR(10) NOT NULL,
                                created_date DATETIME NOT NULL DEFAULT GETDATE(),
                                created_by BIGINT FOREIGN KEY REFERENCES users(id),
                                supplier_id BIGINT NULL FOREIGN KEY REFERENCES suppliers(id),
                                customer_id BIGINT NULL FOREIGN KEY REFERENCES customers(id),
                                note NVARCHAR(500),
                                status NVARCHAR(20) NOT NULL
);

CREATE TABLE receipt_details (
                                 id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                 receipt_id BIGINT FOREIGN KEY REFERENCES stock_receipts(id),
                                 product_id BIGINT FOREIGN KEY REFERENCES products(id),
                                 quantity INT NOT NULL,
                                 unit_price DECIMAL(18,2) NOT NULL
);
