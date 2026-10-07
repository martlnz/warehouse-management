package com.team.warehouse.warehousemanagementoop.entity;

/**
 * Phiếu nhập kho: thêm nhà cung cấp so với StockReceipt.
 */
public class ImportReceipt extends StockReceipt {

    private Supplier supplier;

    @Override
    public String getPartnerName() {
        if (supplier == null || supplier.getName() == null) {
            return "";
        }
        return supplier.getName();
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
}