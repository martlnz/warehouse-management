package com.team.warehouse.warehousemanagementoop.entity;

/**
 * Phiếu xuất kho: thêm khách hàng so với StockReceipt.
 */
public class ExportReceipt extends StockReceipt {

    private Customer customer;

    @Override
    public String getPartnerName() {
        if (customer == null || customer.getName() == null) {
            return "";
        }
        return customer.getName();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}