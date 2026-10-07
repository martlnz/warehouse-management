package com.team.warehouse.warehousemanagementoop.dao;

import com.team.warehouse.warehousemanagementoop.config.DatabaseConfig;
import com.team.warehouse.warehousemanagementoop.entity.Customer;

import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerDAO {

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, phone, email FROM customers";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToCustomer(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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
        try (Connection conn = DatabaseConfig.getConnection();
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

    public boolean add(Customer customer) {
        return insert(customer);
    }

    public boolean insert(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
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

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
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

    // Xử lý set NULL phiếu xuất trước khi xóa khách hàng để tránh lỗi Foreign Key
    public boolean delete(Long id) {
        String sqlUpdate = "UPDATE stock_receipts SET customer_id = NULL WHERE customer_id = ?";
        String sqlDelete = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlUpdate);
                 PreparedStatement pstmt2 = conn.prepareStatement(sqlDelete)) {

                pstmt1.setLong(1, id);
                pstmt1.executeUpdate();

                pstmt2.setLong(1, id);
                int rows = pstmt2.executeUpdate();

                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByPhone(String phone) {
        String sql = "SELECT 1 FROM customers WHERE phone = ?";
        try (Connection conn = DatabaseConfig.getConnection();
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