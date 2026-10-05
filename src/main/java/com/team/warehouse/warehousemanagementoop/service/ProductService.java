package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.dao.ProductDAO;
import com.team.warehouse.warehousemanagementoop.entity.Product;

import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }


    //  Lấy danh sách tất cả sản phẩm đang hoạt động
    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }


     // Tìm kiếm sản phẩm theo tên hoặc mã

    public List<Product> searchProducts(String keyword) throws Exception {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new Exception("Từ khóa tìm kiếm không được để trống!");
        }
        return productDAO.search(keyword.trim());
    }


     // Thêm sản phẩm mới với các quy tắc kiểm tra (Validation)

    public void addProduct(Product product) throws Exception {
        validateProductData(product);

        // Mặc định khi thêm mới thì trạng thái là hoạt động
        product.setActive(true);

        boolean isSuccess = productDAO.insert(product);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể thêm sản phẩm vào cơ sở dữ liệu. Vui lòng kiểm tra lại mã sản phẩm có thể bị trùng!");
        }
    }


     // Cập nhật thông tin sản phẩm

    public void updateProduct(Product product) throws Exception {
        if (product.getId() <= 0) {
            throw new Exception("Lỗi: Không xác định được sản phẩm cần cập nhật!");
        }

        validateProductData(product);

        boolean isSuccess = productDAO.update(product);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Cập nhật sản phẩm thất bại. Sản phẩm có thể đã bị xóa hoặc mã bị trùng!");
        }
    }


    //  Xóa sản phẩm (Soft delete)

    public void deleteProduct(long id) throws Exception {
        if (id <= 0) {
            throw new Exception("Lỗi: Mã ID sản phẩm không hợp lệ!");
        }

        boolean isSuccess = productDAO.delete(id);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Xóa sản phẩm thất bại!");
        }
    }


     // Hàm dùng chung: Kiểm tra tính hợp lệ của dữ liệu đầu vào

    private void validateProductData(Product product) throws Exception {
        if (product.getCode() == null || product.getCode().trim().isEmpty()) {
            throw new Exception("Mã sản phẩm không được để trống!");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new Exception("Tên sản phẩm không được để trống!");
        }
        if (product.getCategoryId() <= 0) {
            throw new Exception("Vui lòng chọn danh mục cho sản phẩm!");
        }
        if (product.getQuantity() < 0) {
            throw new Exception("Số lượng tồn kho không được là số âm!");
        }
        if (product.getPrice() < 0) {
            throw new Exception("Đơn giá không được là số âm!");
        }
    }
}