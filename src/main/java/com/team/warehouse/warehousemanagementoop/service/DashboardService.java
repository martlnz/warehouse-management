package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.dao.ExportReceiptDAO;
import com.team.warehouse.warehousemanagementoop.dao.ImportReceiptDAO;
import com.team.warehouse.warehousemanagementoop.dao.ProductDAO;
import com.team.warehouse.warehousemanagementoop.dto.DashboardDTO;
import com.team.warehouse.warehousemanagementoop.dto.MonthlyStatDTO;
import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tính các số liệu thống kê cho Dashboard. Chỉ đọc dữ liệu, không ghi gì vào CSDL.
 */
public class DashboardService {

    /** Sản phẩm có tồn kho <= ngưỡng này được tính là "sắp hết hàng". */
    private static final int MIN_STOCK_THRESHOLD = 10;

    /** Số tháng hiển thị trên biểu đồ (tính cả tháng hiện tại). */
    private static final int CHART_MONTHS = 6;

    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final ProductDAO productDAO = new ProductDAO();
    private final ImportReceiptDAO importReceiptDAO = new ImportReceiptDAO();
    private final ExportReceiptDAO exportReceiptDAO = new ExportReceiptDAO();

    public DashboardDTO getDashboard() throws DataAccessException {
        DashboardDTO dashboard = new DashboardDTO();
        fillProductSummary(dashboard);
        dashboard.setMonthlyStats(buildMonthlyStats());
        return dashboard;
    }

    /** Tổng số sản phẩm, tổng giá trị tồn kho và số sản phẩm sắp hết hàng (chỉ tính sản phẩm đang hoạt động). */
    private void fillProductSummary(DashboardDTO dashboard) {
        int totalProducts = 0;
        int lowStockCount = 0;
        BigDecimal totalStockValue = BigDecimal.ZERO;

        for (Product product : productDAO.findAll()) {
            if (!product.isActive()) {
                continue;
            }
            totalProducts++;
            BigDecimal value = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(product.getQuantity()));
            totalStockValue = totalStockValue.add(value);
            if (product.getQuantity() <= MIN_STOCK_THRESHOLD) {
                lowStockCount++;
            }
        }

        dashboard.setTotalProducts(totalProducts);
        dashboard.setTotalStockValue(totalStockValue);
        dashboard.setLowStockCount(lowStockCount);
    }

    /** Số lượng nhập/xuất của CHART_MONTHS tháng gần nhất; tháng không có phiếu nào thì tính 0. */
    private List<MonthlyStatDTO> buildMonthlyStats() throws DataAccessException {
        YearMonth firstMonth = YearMonth.now().minusMonths(CHART_MONTHS - 1);
        Map<YearMonth, Integer> importByMonth = importReceiptDAO.sumQuantityByMonth(firstMonth.atDay(1));
        Map<YearMonth, Integer> exportByMonth = exportReceiptDAO.sumQuantityByMonth(firstMonth.atDay(1));

        List<MonthlyStatDTO> stats = new ArrayList<>();
        for (int i = 0; i < CHART_MONTHS; i++) {
            YearMonth month = firstMonth.plusMonths(i);
            stats.add(new MonthlyStatDTO(
                    month.format(MONTH_LABEL_FORMAT),
                    importByMonth.getOrDefault(month, 0),
                    exportByMonth.getOrDefault(month, 0)));
        }
        return stats;
    }
}