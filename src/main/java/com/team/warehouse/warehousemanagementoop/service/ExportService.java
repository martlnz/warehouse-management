package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.config.DatabaseConfig;
import com.team.warehouse.warehousemanagementoop.dao.CustomerDAO;
import com.team.warehouse.warehousemanagementoop.dao.ExportReceiptDAO;
import com.team.warehouse.warehousemanagementoop.dao.ProductDAO;
import com.team.warehouse.warehousemanagementoop.dto.ExportReceiptDTO;
import com.team.warehouse.warehousemanagementoop.dto.ReceiptDetailDTO;
import com.team.warehouse.warehousemanagementoop.entity.Customer;
import com.team.warehouse.warehousemanagementoop.entity.ExportReceipt;
import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;
import com.team.warehouse.warehousemanagementoop.exception.InsufficientStockException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Luật nghiệp vụ xuất kho: validate phiếu, KIỂM TRA tồn kho, lưu phiếu và TRỪ tồn kho
 * trong 1 transaction (xem mục 9 của tài liệu kỹ thuật).
 */
public class ExportService {

    private final ExportReceiptDAO exportReceiptDAO = new ExportReceiptDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    // ===== Dữ liệu cho màn hình danh sách =====

    public List<ExportReceipt> findAll() throws DataAccessException {
        return exportReceiptDAO.findAll();
    }

    /** Từ khóa rỗng thì trả về toàn bộ danh sách. */
    public List<ExportReceipt> searchByCustomerName(String keyword) throws DataAccessException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return exportReceiptDAO.findAll();
        }
        return exportReceiptDAO.searchByCustomerName(keyword.trim());
    }

    public ExportReceipt findById(long id) throws DataAccessException {
        return exportReceiptDAO.findById(id);
    }

    // ===== Dữ liệu cho combobox của form =====

    public List<Customer> getAllCustomers() {
        return customerDAO.getAll();
    }

    /** Chỉ cho chọn sản phẩm đang hoạt động. */
    public List<Product> getActiveProducts() {
        List<Product> result = new ArrayList<>();
        for (Product product : productDAO.findAll()) {
            if (product.isActive()) {
                result.add(product);
            }
        }
        return result;
    }

    // ===== Tạo phiếu xuất =====

    /**
     * Tạo phiếu xuất: kiểm tra đủ tồn kho -> lưu phiếu -> trừ tồn kho.
     * Trả về phiếu vừa tạo (kèm chi tiết).
     *
     * @throws IllegalArgumentException   dữ liệu phiếu không hợp lệ
     * @throws InsufficientStockException có sản phẩm không đủ tồn kho (đã rollback)
     * @throws DataAccessException        lỗi truy cập cơ sở dữ liệu (đã rollback)
     */
    public ExportReceipt createExportReceipt(ExportReceiptDTO dto)
            throws DataAccessException, InsufficientStockException {
        validate(dto);
        long receiptId = saveInTransaction(dto);
        return exportReceiptDAO.findById(receiptId);
    }

    private void validate(ExportReceiptDTO dto) {
        if (dto.getCreatedById() <= 0) {
            throw new IllegalArgumentException("Chưa xác định được người tạo phiếu (chưa đăng nhập?)");
        }
        if (dto.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng");
        }
        if (dto.getNote() != null && dto.getNote().length() > 500) {
            throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự");
        }
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Phiếu xuất phải có ít nhất 1 sản phẩm");
        }
        Set<Long> productIds = new HashSet<>();
        for (ReceiptDetailDTO detail : dto.getDetails()) {
            if (detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng của \"" + detail.getProductName() + "\" phải lớn hơn 0");
            }
            if (detail.getUnitPrice() == null || detail.getUnitPrice().signum() < 0) {
                throw new IllegalArgumentException("Đơn giá của \"" + detail.getProductName() + "\" không hợp lệ");
            }
            // Không cho trùng sản phẩm để việc kiểm tra tồn từng dòng luôn đúng
            if (!productIds.add(detail.getProductId())) {
                throw new IllegalArgumentException("Sản phẩm \"" + detail.getProductName() + "\" bị trùng trong phiếu");
            }
        }
    }

    /** Transaction thủ công: kiểm tra tồn + lưu phiếu + trừ tồn dùng chung 1 Connection; lỗi thì rollback. */
    private long saveInTransaction(ExportReceiptDTO dto) throws DataAccessException, InsufficientStockException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Kiểm tra tồn kho từng sản phẩm (khóa dòng sản phẩm đến khi commit/rollback)
            for (ReceiptDetailDTO detail : dto.getDetails()) {
                int currentStock = productDAO.getQuantityForUpdate(conn, detail.getProductId());
                if (currentStock < 0) {
                    throw new DataAccessException("Không tìm thấy sản phẩm \"" + detail.getProductName() + "\"");
                }
                if (currentStock < detail.getQuantity()) {
                    throw new InsufficientStockException("Sản phẩm \"" + detail.getProductName()
                            + "\" không đủ tồn kho (còn " + currentStock + ", cần xuất " + detail.getQuantity() + ")");
                }
            }

            // 2. Lưu phiếu xuất + trừ tồn kho (cùng 1 Connection, cùng 1 transaction)
            long receiptId = exportReceiptDAO.insert(conn, dto);
            for (ReceiptDetailDTO detail : dto.getDetails()) {
                boolean updated = productDAO.updateStock(conn, detail.getProductId(), -detail.getQuantity());
                if (!updated) {
                    throw new DataAccessException("Không trừ được tồn kho sản phẩm \"" + detail.getProductName() + "\"");
                }
            }

            conn.commit(); // Thành công -> lưu vĩnh viễn
            return receiptId;

        } catch (InsufficientStockException | DataAccessException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException("Tạo phiếu xuất thất bại: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}