package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.config.DatabaseConfig;
import com.team.warehouse.warehousemanagementoop.dao.ImportReceiptDAO;
import com.team.warehouse.warehousemanagementoop.dao.ProductDAO;
import com.team.warehouse.warehousemanagementoop.dao.SupplierDAO;
import com.team.warehouse.warehousemanagementoop.dto.ImportReceiptDTO;
import com.team.warehouse.warehousemanagementoop.dto.ReceiptDetailDTO;
import com.team.warehouse.warehousemanagementoop.entity.ImportReceipt;
import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.entity.ReceiptDetail;
import com.team.warehouse.warehousemanagementoop.entity.Supplier;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportService {

    private final ImportReceiptDAO importReceiptDAO = new ImportReceiptDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<ImportReceipt> searchBySupplierName(String keyword) throws DataAccessException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return importReceiptDAO.findAll();
        }
        return importReceiptDAO.searchBySupplierName(keyword.trim());
    }

    public ImportReceipt findById(long id) throws DataAccessException {
        return importReceiptDAO.findById(id);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierDAO.getAllSuppliers();
    }

    public List<Product> getActiveProducts() {
        List<Product> result = new ArrayList<>();
        for (Product product : productDAO.findAll()) {
            if (product.isActive()) {
                result.add(product);
            }
        }
        return result;
    }

    public ImportReceipt createImportReceipt(ImportReceiptDTO dto) throws DataAccessException {
        validate(dto);
        long receiptId = saveInTransaction(dto);
        return importReceiptDAO.findById(receiptId);
    }

    private void validate(ImportReceiptDTO dto) {
        if (dto.getCreatedById() <= 0) {
            throw new IllegalArgumentException("Chưa xác định được người tạo phiếu (chưa đăng nhập?)");
        }
        if (dto.getSupplierId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp");
        }
        if (dto.getNote() != null && dto.getNote().length() > 500) {
            throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự");
        }
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất 1 sản phẩm");
        }
        Set<Long> productIds = new HashSet<>();
        for (ReceiptDetailDTO detail : dto.getDetails()) {
            if (detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng của \"" + detail.getProductName() + "\" phải lớn hơn 0");
            }
            if (detail.getUnitPrice() == null || detail.getUnitPrice().signum() < 0) {
                throw new IllegalArgumentException("Đơn giá của \"" + detail.getProductName() + "\" không hợp lệ");
            }
            if (!productIds.add(detail.getProductId())) {
                throw new IllegalArgumentException("Sản phẩm \"" + detail.getProductName() + "\" bị trùng trong phiếu");
            }
        }
    }

    private long saveInTransaction(ImportReceiptDTO dto) throws DataAccessException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            long receiptId = importReceiptDAO.insert(conn, dto);
            for (ReceiptDetailDTO detail : dto.getDetails()) {
                boolean updated = productDAO.updateStock(conn, detail.getProductId(), detail.getQuantity());
                if (!updated) {
                    throw new DataAccessException("Không tìm thấy sản phẩm \"" + detail.getProductName() + "\"");
                }
            }

            conn.commit();
            return receiptId;

        } catch (DataAccessException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException("Tạo phiếu nhập thất bại: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    public void deleteImportReceipt(long id) throws DataAccessException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            ImportReceipt receipt = importReceiptDAO.findById(conn, id);
            if (receipt == null) {
                throw new DataAccessException("Không tìm thấy phiếu nhập có ID " + id);
            }

            for (ReceiptDetail detail : receipt.getDetails()) {
                int currentStock = productDAO.getQuantityForUpdate(conn, detail.getProductId());
                if (currentStock < detail.getQuantity()) {
                    throw new DataAccessException("Không thể xóa phiếu nhập vì sản phẩm \"" + detail.getProductName() + "\" sẽ bị âm tồn kho.");
                }
                boolean updated = productDAO.updateStock(conn, detail.getProductId(), -detail.getQuantity());
                if (!updated) {
                    throw new DataAccessException("Không trừ được tồn kho sản phẩm \"" + detail.getProductName() + "\"");
                }
            }

            importReceiptDAO.delete(conn, id);

            conn.commit();
        } catch (DataAccessException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException("Xóa phiếu nhập thất bại: " + e.getMessage(), e);
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