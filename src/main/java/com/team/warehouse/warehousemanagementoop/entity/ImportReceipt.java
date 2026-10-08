package com.team.warehouse.warehousemanagementoop.entity;

public class ImportReceipt extends StockReceipt {

    private Supplier supplier;

    @Override
    public String getPartnerName() {
        if (supplier == null || supplier.getName() == null) {
            return "";
        }
        return supplier.getName();
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
}