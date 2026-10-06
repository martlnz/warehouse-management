package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Supplier;
import com.team.warehouse.warehousemanagementoop.service.PartnerService;
import com.team.warehouse.warehousemanagementoop.service.SupplierService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.Optional;
public class PartnerEditFormController {

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    private final SupplierService supplierService = new SupplierService();
    private Supplier currentSupplier;
    private Runnable onSaveSuccess;

    public void setSupplierData(Supplier supplier, Runnable onSaveSuccess) {
        this.currentSupplier = supplier;
        this.onSaveSuccess = onSaveSuccess;
        if (supplier != null) {
            txtName.setText(supplier.getName() != null ? supplier.getName() : "");
            txtPhone.setText(supplier.getPhone() != null ? supplier.getPhone() : "");
            txtEmail.setText(supplier.getEmail() != null ? supplier.getEmail() : "");
        }
    }

    @FXML
    private void handleSave() {
        if (currentSupplier == null) return;
        currentSupplier.setName(txtName.getText());
        currentSupplier.setPhone(txtPhone.getText());
        currentSupplier.setEmail(txtEmail.getText());

        try {
            supplierService.updateSupplier(currentSupplier);

            if (onSaveSuccess != null) {
                onSaveSuccess.run();
            }
            closeStage();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu / Lưu dữ liệu", e.getMessage());
        }
    }
    @FXML
    private void handleDelete() {
        if (currentSupplier == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa nhà cung cấp '" + currentSupplier.getName() + "' không?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                supplierService.deleteSupplier(currentSupplier.getId());
                if (onSaveSuccess != null) {
                    onSaveSuccess.run();
                }
                closeStage();
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi xóa nhà cung cấp", e.getMessage());
            }
        }
    }
    private void closeStage() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}