package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.dao.SupplierDAO;
import com.team.warehouse.warehousemanagementoop.entity.Supplier;

import java.util.List;
import java.util.regex.Pattern;

public class PartnerService {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@gmail\\.com$";

    private static final String PHONE_REGEX = "^0\\d{9}$";

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.getAllSuppliers();
    }

    public boolean addSupplier(Supplier supplier) {

        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống!");
        }
        supplier.setName(supplier.getName().trim());

        if (supplier.getPhone() != null && !supplier.getPhone().trim().isEmpty()) {
            String phone = supplier.getPhone().trim();
            if (!Pattern.matches(PHONE_REGEX, phone)) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ!\n(Phải có đúng 10 chữ số và bắt đầu bằng số 0).");
            }
            supplier.setPhone(phone);
        } else {
            supplier.setPhone(null);
        }
        if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
            String email = supplier.getEmail().trim();
            if (!Pattern.matches(EMAIL_REGEX, email)) {
                throw new IllegalArgumentException("Email không hợp lệ!\n(Email bắt buộc phải có định dạng @gmail.com).");
            }
            supplier.setEmail(email);
        } else {
            supplier.setEmail(null);
        }

        boolean success = supplierDAO.addSupplier(supplier);
        if (!success) {
            throw new RuntimeException("Lưu vào cơ sở dữ liệu thất bại!\n(Có thể do Email này đã tồn tại trong hệ thống).");
        }
        return true;
    }
}