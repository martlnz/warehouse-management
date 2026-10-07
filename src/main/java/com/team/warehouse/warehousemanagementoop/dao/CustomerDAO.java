package com.team.warehouse.warehousemanagementoop.dao;

import com.team.warehouse.warehousemanagementoop.entity.Customer;

import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerDAO {

    // Kết nối CSDL SQL Server
    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=warehouse_db;encrypt=false;trustServerCertificate=true;";
        return DriverManager.getConnection(url, "sa", "Warehouse@123");
    }

    // Helper map ResultSet sang đối tượng Customer để tránh lặp code
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }

    // Lấy tất cả khách hàng
    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, phone, email FROM customers";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToCustomer(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tìm kiếm khách hàng theo tên
    // Hàm phụ bỏ dấu tiếng Việt (Ví dụ: "Trần Thị B" -> "tran thi b")
    private String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        String cleanKeyword = removeAccent(keyword.trim().toLowerCase());
        String sql = "SELECT * FROM customers";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String strId = (id != null) ? id.toString() : "";
                String cleanName = removeAccent(name != null ? name.toLowerCase() : "");
                String cleanPhone = (phone != null) ? phone.toLowerCase() : "";
                String cleanEmail = (email != null) ? email.toLowerCase() : "";
                if (strId.contains(cleanKeyword) ||
                        cleanName.contains(cleanKeyword) ||
                        cleanPhone.contains(cleanKeyword) ||
                        cleanEmail.contains(cleanKeyword)) {
                    Customer c = new Customer();
                    c.setId(id);
                    c.setName(name);
                    c.setPhone(phone);
                    c.setEmail(email);
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm khách hàng mới vào CSDL
    // Trong CustomerDAO.java
    public boolean add(Customer customer) {
        return insert(customer); // Gọi lại hàm insert của bạn
    }
    public boolean insert(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật thông tin khách hàng
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setLong(4, customer.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa khách hàng theo ID
    public boolean delete(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra số điện thoại đã tồn tại chưa
    public boolean existsByPhone(String phone) {
        String sql = "SELECT 1 FROM customers WHERE phone = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}