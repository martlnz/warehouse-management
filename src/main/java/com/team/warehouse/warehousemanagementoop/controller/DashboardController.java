package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.dto.DashboardDTO;
import com.team.warehouse.warehousemanagementoop.dto.MonthlyStatDTO;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;
import com.team.warehouse.warehousemanagementoop.service.DashboardService;
import com.team.warehouse.warehousemanagementoop.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller của dashboard.fxml. Mỗi lần mở Dashboard từ sidebar, FXML được load lại
 * nên số liệu luôn là mới nhất.
 */
public class DashboardController implements Initializable {

    @FXML private Label totalProductsLabel;
    @FXML private Label totalStockValueLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private BarChart<String, Number> importExportChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis quantityAxis;

    private final DashboardService dashboardService = new DashboardService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboard();
    }

    private void loadDashboard() {
        try {
            DashboardDTO dashboard = dashboardService.getDashboard();
            totalProductsLabel.setText(String.valueOf(dashboard.getTotalProducts()));
            totalStockValueLabel.setText(formatMoney(dashboard.getTotalStockValue()));
            lowStockCountLabel.setText(String.valueOf(dashboard.getLowStockCount()));
            showChart(dashboard.getMonthlyStats());
        } catch (DataAccessException e) {
            AlertHelper.showError("Lỗi tải Dashboard", e.getMessage());
        }
    }

    /** Vẽ biểu đồ cột: mỗi tháng có 1 cột "Nhập kho" và 1 cột "Xuất kho". */
    private void showChart(List<MonthlyStatDTO> stats) {
        XYChart.Series<String, Number> importSeries = new XYChart.Series<>();
        importSeries.setName("Nhập kho");
        XYChart.Series<String, Number> exportSeries = new XYChart.Series<>();
        exportSeries.setName("Xuất kho");

        ObservableList<String> months = FXCollections.observableArrayList();
        int maxQuantity = 0;
        for (MonthlyStatDTO stat : stats) {
            months.add(stat.getMonthLabel());
            importSeries.getData().add(new XYChart.Data<>(stat.getMonthLabel(), stat.getImportQuantity()));
            exportSeries.getData().add(new XYChart.Data<>(stat.getMonthLabel(), stat.getExportQuantity()));
            maxQuantity = Math.max(maxQuantity, Math.max(stat.getImportQuantity(), stat.getExportQuantity()));
        }

        // Giữ đúng thứ tự tháng từ cũ đến mới
        monthAxis.setCategories(months);

        // Dữ liệu còn rất nhỏ thì cố định trục 0..5 bước 1, tránh hiện số lẻ (0.5; 1.5...) trên trục Số lượng
        if (maxQuantity < 5) {
            quantityAxis.setAutoRanging(false);
            quantityAxis.setLowerBound(0);
            quantityAxis.setUpperBound(5);
            quantityAxis.setTickUnit(1);
        } else {
            quantityAxis.setAutoRanging(true);
        }

        importExportChart.getData().clear();
        importExportChart.getData().add(importSeries);
        importExportChart.getData().add(exportSeries);
    }

    private String formatMoney(BigDecimal value) {
        return new DecimalFormat("#,##0").format(value) + " ₫";
    }
}