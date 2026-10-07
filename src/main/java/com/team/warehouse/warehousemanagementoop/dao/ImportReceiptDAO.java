package com.team.warehouse.warehousemanagementoop.dao;

import com.team.warehouse.warehousemanagementoop.config.DatabaseConfig;
import com.team.warehouse.warehousemanagementoop.dto.ImportReceiptDTO;
import com.team.warehouse.warehousemanagementoop.dto.ReceiptDetailDTO;
import com.team.warehouse.warehousemanagementoop.entity.ImportReceipt;
import com.team.warehouse.warehousemanagementoop.entity.ReceiptDetail;
import com.team.warehouse.warehousemanagementoop.entity.StockReceipt;
import com.team.warehouse.warehousemanagementoop.entity.Supplier;
import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Thao tác trên bảng stock_receipts (lọc receipt_type = 'IMPORT') và receipt_details.
 * Các hàm nhận Connection dùng chung 1 transaction do ImportService quản lý.
 */
public class ImportReceiptDAO {

    private static final String SELECT_RECEIPT =
            "SELECT r.id, r.created_date, r.note, r.status, "
                    + "u.id AS user_id, COALESCE(u.full_name, u.username) AS user_name, "
                    + "s.id AS supplier_id, s.name AS supplier_name "
                    + "FROM stock_receipts r "
                    + "LEFT JOIN users u ON r.created_by = u.id "
                    + "LEFT JOIN suppliers s ON r.supplier_id = s.id "
                    + "WHERE r.receipt_type = 'IMPORT' ";

    private static final String ORDER_BY = " ORDER BY r.created_date DESC, r.id DESC";

    /** Danh sách phiếu nhập (mới nhất trước), chưa kèm dòng chi tiết. */
    public List<ImportReceipt> findAll() throws DataAccessException {
        return query(SELECT_RECEIPT + ORDER_BY, null);
    }

    /** Tìm phiếu nhập theo tên nhà cung cấp. */
    public List<ImportReceipt> searchBySupplierName(String keyword) throws DataAccessException {
        return query(SELECT_RECEIPT + "AND s.name LIKE ? " + ORDER_BY, "%" + keyword + "%");
    }

    /** Lấy 1 phiếu nhập kèm các dòng chi tiết (tự mở và đóng kết nối). */
    public ImportReceipt findById(long id) throws DataAccessException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw new DataAccessException("Không mở được kết nối cơ sở dữ liệu", e);
        }
    }

    /** Lấy 1 phiếu nhập kèm các dòng chi tiết trên Connection được truyền vào. Trả về null nếu không có. */
    public ImportReceipt findById(Connection conn, long id) throws DataAccessException {
        String sql = SELECT_RECEIPT + "AND r.id = ?";
        try {
            ImportReceipt receipt = null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        receipt = mapRow(rs);
                    }
                }
            }
            if (receipt != null) {
                receipt.setDetails(findDetails(conn, id));
            }
            return receipt;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi truy vấn phiếu nhập #" + id, e);
        }
    }

    /**
     * Lưu phiếu nhập + các dòng chi tiết (chưa cộng tồn kho - việc đó do Service gọi ProductDAO).
     * Trả về mã phiếu vừa tạo.
     */
    public long insert(Connection conn, ImportReceiptDTO dto) throws DataAccessException {
        String sqlReceipt = "INSERT INTO stock_receipts (receipt_type, created_by, supplier_id, note, status) "
                + "VALUES ('IMPORT', ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO receipt_details (receipt_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";
        try {
            long receiptId;
            try (PreparedStatement stmt = conn.prepareStatement(sqlReceipt, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, dto.getCreatedById());
                stmt.setLong(2, dto.getSupplierId());
                stmt.setString(3, dto.getNote());
                stmt.setString(4, StockReceipt.STATUS_COMPLETED);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không lấy được mã phiếu vừa tạo");
                    }
                    receiptId = keys.getLong(1);
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlDetail)) {
                for (ReceiptDetailDTO detail : dto.getDetails()) {
                    stmt.setLong(1, receiptId);
                    stmt.setLong(2, detail.getProductId());
                    stmt.setInt(3, detail.getQuantity());
                    stmt.setBigDecimal(4, detail.getUnitPrice());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            return receiptId;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi lưu phiếu nhập", e);
        }
    }

    private List<ImportReceipt> query(String sql, String param) throws DataAccessException {
        List<ImportReceipt> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) {
                stmt.setString(1, param);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi truy vấn danh sách phiếu nhập", e);
        }
        return result;
    }

    private List<ReceiptDetail> findDetails(Connection conn, long receiptId) throws SQLException {
        String sql = "SELECT d.id, d.product_id, p.name AS product_name, d.quantity, d.unit_price "
                + "FROM receipt_details d "
                + "LEFT JOIN products p ON d.product_id = p.id "
                + "WHERE d.receipt_id = ? ORDER BY d.id";
        List<ReceiptDetail> details = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, receiptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReceiptDetail detail = new ReceiptDetail();
                    detail.setId(rs.getLong("id"));
                    detail.setReceiptId(receiptId);
                    detail.setProductId(rs.getLong("product_id"));
                    detail.setProductName(rs.getString("product_name"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getBigDecimal("unit_price"));
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private ImportReceipt mapRow(ResultSet rs) throws SQLException {
        ImportReceipt receipt = new ImportReceipt();
        receipt.setId(rs.getLong("id"));
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            receipt.setCreatedDate(createdDate.toLocalDateTime());
        }
        receipt.setNote(rs.getString("note"));
        receipt.setStatus(rs.getString("status"));

        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setFullName(rs.getString("user_name"));
        receipt.setCreatedBy(user);

        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("supplier_id"));
        supplier.setName(rs.getString("supplier_name"));
        receipt.setSupplier(supplier);
        return receipt;
    }
}