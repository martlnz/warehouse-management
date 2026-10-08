package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.ImportReceipt;
import com.team.warehouse.warehousemanagementoop.entity.ReceiptDetail;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;

public class ImportDetailController {

    @FXML private Label lblSupplier;
    @FXML private Label lblNote;
    @FXML private TableView<ReceiptDetail> detailTable;
    @FXML private TableColumn<ReceiptDetail, String> colName;
    @FXML private TableColumn<ReceiptDetail, String> colQty;
    @FXML private TableColumn<ReceiptDetail, String> colPrice;
    @FXML private TableColumn<ReceiptDetail, String> colSubtotal;
    @FXML private Label lblTotal;

    @FXML
    private void initialize() {
        // Cấu hình cách lấy dữ liệu cho các cột trong bảng
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getUnitPrice())));
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getSubtotal())));
    }

    public void setReceiptData(ImportReceipt receipt) {
        lblSupplier.setText(receipt.getPartnerName());
        lblNote.setText(receipt.getNote() == null ? "" : receipt.getNote());

        detailTable.setItems(FXCollections.observableArrayList(receipt.getDetails()));
        lblTotal.setText("Tổng tiền: " + formatMoney(receipt.getTotalAmount()));
    }

    // Hàm định dạng tiền tệ (hoặc dùng Utility Class nếu có)
    private String formatMoney(BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}
