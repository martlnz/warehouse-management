package com.team.warehouse.warehousemanagementoop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardDTO {

    private int totalProducts;
    private BigDecimal totalStockValue = BigDecimal.ZERO;
    private int lowStockCount;
    private List<MonthlyStatDTO> monthlyStats = new ArrayList<>();

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public BigDecimal getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(BigDecimal totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public List<MonthlyStatDTO> getMonthlyStats() {
        return monthlyStats;
    }

    public void setMonthlyStats(List<MonthlyStatDTO> monthlyStats) {
        this.monthlyStats = monthlyStats;
    }
}