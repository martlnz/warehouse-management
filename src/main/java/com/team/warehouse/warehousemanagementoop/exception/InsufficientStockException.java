package com.team.warehouse.warehousemanagementoop.exception;

/**
 * Ném ra khi xuất kho mà số lượng tồn không đủ (xem ExportService).
 */
public class InsufficientStockException extends Exception {

    public InsufficientStockException(String message) {
        super(message);
    }
}