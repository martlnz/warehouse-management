package com.team.warehouse.warehousemanagementoop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu form "Tạo phiếu xuất kho": khách hàng + ghi chú + nhiều dòng sản phẩm.
 */
public class ExportReceiptDTO {

    private long customerId;
    private long createdById;
    private String note;
    private List<ReceiptDetailDTO> details = new ArrayList<>();

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptDetailDTO detail : details) {
            total = total.add(detail.getSubtotal());
        }
        return total;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(long createdById) {
        this.createdById = createdById;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<ReceiptDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<ReceiptDetailDTO> details) {
        this.details = details;
    }
}