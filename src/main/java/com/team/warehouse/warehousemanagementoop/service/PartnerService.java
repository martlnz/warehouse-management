package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.dao.CustomerDAO;
import com.team.warehouse.warehousemanagementoop.entity.Customer;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.stream.Collectors;

public class PartnerService {
    private final CustomerDAO customerDAO = new CustomerDAO();

    // Lấy toàn bộ danh sách khách hàng
    public List<Customer> getAllCustomers() {
        return customerDAO.getAll();
    }

    // Tìm kiếm khách hàng theo tên (nếu chuỗi rỗng thì lấy tất cả)
    public List<Customer> searchCustomers(String keyword) {
        return customerDAO.searchCustomers(keyword);
    }

    // Thêm khách hàng mới (xóa khoảng trắng thừa ở đầu/cuối)
    public boolean addCustomer(Customer customer) {
        return customer != null && customerDAO.insert(cleanData(customer));
    }

    // Xóa khách hàng theo ID
    public boolean deleteCustomer(Long id) {
        return id != null && customerDAO.delete(id);
    }

    // Cập nhật thông tin khách hàng
    public boolean updateCustomer(Customer customer) {
        return customer != null && customer.getId() != null && customerDAO.update(cleanData(customer));
    }

    // Kiểm tra số điện thoại đã tồn tại chưa
    public boolean isPhoneExist(String phone) {
        return phone != null && customerDAO.existsByPhone(phone.trim());
    }

    // --- HÀM PHỤ BẮT LỖI TÊN & LÀM SẠCH ---
    private boolean isValidName(Customer c) {
        // Tên không rỗng và KHÔNG chứa số/ký tự đặc biệt
        return c != null && c.getName() != null && c.getName().trim().matches("^[\\p{L}\\s]+$");
    }

    // Hàm phụ: Chuẩn hóa dữ liệu đầu vào (tránh dính khoảng trắng thừa)
    private Customer cleanData(Customer c) {
        if (c.getName() != null) c.setName(c.getName().trim());
        if (c.getPhone() != null) c.setPhone(c.getPhone().trim());
        if (c.getEmail() != null) c.setEmail(c.getEmail().trim());
        return c;
    }
}