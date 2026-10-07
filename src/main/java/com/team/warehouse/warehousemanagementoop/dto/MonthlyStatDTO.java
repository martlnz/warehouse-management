package com.team.warehouse.warehousemanagementoop.dto;

/**
 * Tổng số lượng hàng nhập và xuất của 1 tháng - mỗi đối tượng là 1 cột trên biểu đồ Dashboard.
 */
public class MonthlyStatDTO {

    private final String monthLabel; // VD: "10/2026"
    private final int importQuantity;
    private final int exportQuantity;

    public MonthlyStatDTO(String monthLabel, int importQuantity, int exportQuantity) {
        this.monthLabel = monthLabel;
        this.importQuantity = importQuantity;
        this.exportQuantity = exportQuantity;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public int getImportQuantity() {
        return importQuantity;
    }

    public int getExportQuantity() {
        return exportQuantity;
    }
}