package com.team.warehouse.warehousemanagementoop.dao;

import com.team.warehouse.warehousemanagementoop.config.DatabaseConfig;
import com.team.warehouse.warehousemanagementoop.entity.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Product product = mapResultSetToEntity(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> search(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = 1 AND (name LIKE ? OR code LIKE ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = mapResultSetToEntity(rs);
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean insert(Product product) {
        String sql = "INSERT INTO products (code, name, category_id, quantity, price, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getCode());
            pstmt.setString(2, product.getName());
            pstmt.setLong(3, product.getCategoryId());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getPrice());
            pstmt.setBoolean(6, product.isActive());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET code = ?, name = ?, category_id = ?, quantity = ?, price = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getCode());
            pstmt.setString(2, product.getName());
            pstmt.setLong(3, product.getCategoryId());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getPrice());
            pstmt.setBoolean(6, product.isActive());
            pstmt.setLong(7, product.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa sản phẩm: Đổi thành xóa mềm (ẩn khỏi hệ thống nhưng không báo lỗi do khóa ngoại của phiếu)
    public boolean delete(long id) {
        String sql = "UPDATE products SET is_active = 0 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Product mapResultSetToEntity(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setCode(rs.getString("code"));
        product.setName(rs.getString("name"));
        product.setCategoryId(rs.getLong("category_id"));
        product.setQuantity(rs.getInt("quantity"));
        product.setPrice(rs.getDouble("price"));
        product.setActive(rs.getBoolean("is_active"));
        return product;
    }

    public String getLastProductCode() {
        String sql = "SELECT TOP 1 code FROM products ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("code");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStock(Connection conn, long productId, int quantityChange) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityChange);
            pstmt.setLong(2, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int getQuantityForUpdate(Connection conn, long productId) throws SQLException {
        String sql = "SELECT quantity FROM products WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("quantity") : -1;
            }
        }
    }
}