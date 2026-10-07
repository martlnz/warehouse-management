package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Supplier;
import com.team.warehouse.warehousemanagementoop.service.PartnerService;
import com.team.warehouse.warehousemanagementoop.service.SupplierService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class PartnerFormController {

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    private final SupplierService supplierService = new SupplierService();
    private Runnable onSaveSuccess;

    public void setOnSaveSuccess(Runnable onSaveSuccess) {
        this.onSaveSuccess = onSaveSuccess;
    }

    @FXML
    private void handleSave() {
        Supplier supplier = new Supplier();
        supplier.setName(txtName.getText());
        supplier.setPhone(txtPhone.getText());
        supplier.setEmail(txtEmail.getText());

        try {
            supplierService.addSupplier(supplier);

            if (onSaveSuccess != null) {
                onSaveSuccess.run();
            }


            ButtonType btnContinue = new ButtonType("Tiếp tục", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnExit = new ButtonType("Thoát", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Thêm nhà cung cấp mới thành công!\nBạn muốn thêm tiếp?");
            alert.getButtonTypes().setAll(btnContinue, btnExit);


            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == btnContinue) {

                txtName.clear();
                txtPhone.clear();
                txtEmail.clear();
                txtName.requestFocus();
            } else {

                closeStage();
            }

        }
        catch (RuntimeException e) {
        showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu / Lưu dữ liệu", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
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