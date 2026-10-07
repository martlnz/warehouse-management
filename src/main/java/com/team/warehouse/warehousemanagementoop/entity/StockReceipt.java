package com.team.warehouse.warehousemanagementoop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiếu kho (abstract) - cha chung của ImportReceipt và ExportReceipt.
 * Ánh xạ bảng stock_receipts (cột receipt_type phân biệt IMPORT / EXPORT).
 */
public abstract class StockReceipt {

    public static final String STATUS_COMPLETED = "COMPLETED";

    private long id;
    private String code; // mã hiển thị: NK001 (nhập) / XK001 (xuất)
    private LocalDateTime createdDate;
    private User createdBy;
    private String note;
    private String status;
    private List<ReceiptDetail> details = new ArrayList<>();

    /** Tên đối tác của phiếu: phiếu nhập -> nhà cung cấp, phiếu xuất -> khách hàng (đa hình). */
    public abstract String getPartnerName();

    /** Tổng tiền của phiếu = tổng thành tiền các dòng chi tiết (dùng chung cho cả 2 loại phiếu). */
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptDetail detail : details) {
            total = total.add(detail.getSubtotal());
        }
        return total;
    }

    public String getCreatedByName() {
        if (createdBy == null || createdBy.getFullName() == null) {
            return "";
        }
        return createdBy.getFullName();
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /** Mã phiếu để hiển thị: NK001 / XK001. Phiếu cũ chưa có mã thì hiện tạm "#id". */
    public String getDisplayCode() {
        return code == null || code.isEmpty() ? "#" + id : code;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ReceiptDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ReceiptDetail> details) {
        this.details = details;
    }
}