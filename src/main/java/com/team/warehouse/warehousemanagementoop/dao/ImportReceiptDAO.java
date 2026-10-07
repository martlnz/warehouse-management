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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class ImportReceiptDAO {

    private static final String SELECT_RECEIPT =
            "SELECT r.id, r.receipt_code, r.created_date, r.note, r.status, "
                    + "u.id AS user_id, COALESCE(u.full_name, u.username) AS user_name, "
                    + "s.id AS supplier_id, s.name AS supplier_name "
                    + "FROM stock_receipts r "
                    + "LEFT JOIN users u ON r.created_by = u.id "
                    + "LEFT JOIN suppliers s ON r.supplier_id = s.id "
                    + "WHERE r.receipt_type = 'IMPORT' ";

    private static final String CODE_PREFIX = "NK";
    private static final String ORDER_BY = " ORDER BY r.created_date DESC, r.id DESC";

    public List<ImportReceipt> findAll() throws DataAccessException {
        return query(SELECT_RECEIPT + ORDER_BY, null);
    }

    public List<ImportReceipt> searchBySupplierName(String keyword) throws DataAccessException {
        return query(SELECT_RECEIPT + "AND s.name LIKE ? " + ORDER_BY, "%" + keyword + "%");
    }

    public ImportReceipt findById(long id) throws DataAccessException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw new DataAccessException("Không mở được kết nối cơ sở dữ liệu", e);
        }
    }

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

    public long insert(Connection conn, ImportReceiptDTO dto) throws DataAccessException {
        String sqlReceipt = "INSERT INTO stock_receipts (receipt_type, receipt_code, created_by, supplier_id, note, status) "
                + "VALUES ('IMPORT', ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO receipt_details (receipt_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";
        try {
            String code = nextCode(conn);
            long receiptId;
            try (PreparedStatement stmt = conn.prepareStatement(sqlReceipt, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, code);
                stmt.setLong(2, dto.getCreatedById());
                stmt.setLong(3, dto.getSupplierId());
                stmt.setString(4, dto.getNote());
                stmt.setString(5, StockReceipt.STATUS_COMPLETED);
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

    public void delete(Connection conn, long id) throws SQLException {
        String sqlDetail = "DELETE FROM receipt_details WHERE receipt_id = ?";
        String sqlReceipt = "DELETE FROM stock_receipts WHERE id = ?";

        try (PreparedStatement stmtDetail = conn.prepareStatement(sqlDetail)) {
            stmtDetail.setLong(1, id);
            stmtDetail.executeUpdate();
        }
        try (PreparedStatement stmtReceipt = conn.prepareStatement(sqlReceipt)) {
            stmtReceipt.setLong(1, id);
            stmtReceipt.executeUpdate();
        }
    }

    private String nextCode(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(receipt_code, " + (CODE_PREFIX.length() + 1) + ", 20) AS INT)), 0) + 1 "
                + "FROM stock_receipts WITH (UPDLOCK, HOLDLOCK) WHERE receipt_type = 'IMPORT'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return String.format("%s%03d", CODE_PREFIX, rs.getInt(1));
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
        receipt.setCode(rs.getString("receipt_code"));
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

    public Map<YearMonth, Integer> sumQuantityByMonth(LocalDate startDate) throws DataAccessException {
        String sql = "SELECT YEAR(r.created_date) AS yr, MONTH(r.created_date) AS mn, SUM(d.quantity) AS total "
                + "FROM stock_receipts r "
                + "JOIN receipt_details d ON r.id = d.receipt_id "
                + "WHERE r.receipt_type = 'IMPORT' AND r.status = 'COMPLETED' AND r.created_date >= ? "
                + "GROUP BY YEAR(r.created_date), MONTH(r.created_date)";

        Map<YearMonth, Integer> result = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(startDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    YearMonth month = YearMonth.of(rs.getInt("yr"), rs.getInt("mn"));
                    result.put(month, rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi thống kê số lượng nhập theo tháng", e);
        }
        return result;
    }
}