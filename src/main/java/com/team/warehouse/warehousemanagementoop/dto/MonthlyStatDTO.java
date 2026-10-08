package com.team.warehouse.warehousemanagementoop.dto;

public class MonthlyStatDTO {

    private final String monthLabel;
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