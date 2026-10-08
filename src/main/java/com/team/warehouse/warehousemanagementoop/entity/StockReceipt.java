package com.team.warehouse.warehousemanagementoop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class StockReceipt {

    public static final String STATUS_COMPLETED = "COMPLETED";

    private long id;
    private String code; // mã hiển thị: NK001 (nhập) / XK001 (xuất)
    private LocalDateTime createdDate;
    private User createdBy;
    private String note;
    private String status;
    private List<ReceiptDetail> details = new ArrayList<>();

    public abstract String getPartnerName();

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