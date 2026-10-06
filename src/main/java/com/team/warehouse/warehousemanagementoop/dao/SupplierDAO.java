package com.team.warehouse.warehousemanagementoop.dao;

import com.team.warehouse.warehousemanagementoop.config.DatabaseConfig;
import com.team.warehouse.warehousemanagementoop.entity.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setPhone(rs.getString("phone"));
                supplier.setEmail(rs.getString("email"));
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, phone, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getName());

            if (supplier.getPhone() != null && !supplier.getPhone().trim().isEmpty()) {
                pstmt.setString(2, supplier.getPhone());
            } else {
                pstmt.setNull(2, Types.NVARCHAR);
            }

            if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
                pstmt.setString(3, supplier.getEmail());
            } else {
                pstmt.setNull(3, Types.NVARCHAR);
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("LỖI SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, phone = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getName());

            if (supplier.getPhone() != null && !supplier.getPhone().trim().isEmpty()) {
                pstmt.setString(2, supplier.getPhone());
            } else {
                pstmt.setNull(2, Types.NVARCHAR);
            }

            if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
                pstmt.setString(3, supplier.getEmail());
            } else {
                pstmt.setNull(3, Types.NVARCHAR);
            }

            pstmt.setInt(4, supplier.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("LỖI SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSupplier(int id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("LỖI SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
