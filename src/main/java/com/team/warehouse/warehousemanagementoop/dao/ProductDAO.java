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


     // Trích xuất toàn bộ dữ liệu sản phẩm đang hoạt động

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        // Chỉ lấy những sản phẩm có is_active = 1
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


    //  Tìm kiếm sản phẩm theo tên hoặc mã sản phẩm

    public List<Product> search(String keyword) {
        List<Product> products = new ArrayList<>();
        // Sử dụng LIKE để tìm kiếm tương đối và bỏ qua các sản phẩm đã bị xóa mềm
        String sql = "SELECT * FROM products WHERE (name LIKE ? OR code LIKE ?) AND is_active = 1";

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


    //Thêm sản phẩm mới vào cơ sở dữ liệu

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


     // Cập nhật thông tin sản phẩm

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

     // Xóa sản phẩm : Xoá cứng
    public boolean delete(long id) {
        // Thay đổi câu lệnh SQL để xóa hẳn dòng dữ liệu
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    //Hàm dùng chung: Map dữ liệu từ ResultSet sang đối tượng Product

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

    // Lấy mã sản phẩm được thêm vào cuối cùng (mới nhất) trong CSDL

    public String getLastProductCode() {
        // Lấy sản phẩm có id lớn nhất (mới thêm nhất)
        String sql = "SELECT TOP 1 code FROM products ORDER BY id DESC";

        try (Connection conn = com.team.warehouse.warehousemanagementoop.config.DatabaseConfig.getConnection();
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
     // Cập nhật số lượng tồn kho của sản phẩm (Dùng cho Nhập/Xuất kho
    public boolean updateStock(long productId, int quantityChange) {
        // Lệnh SQL cộng thẳng số lượng thay đổi vào số lượng hiện tại
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityChange);
            pstmt.setLong(2, productId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}