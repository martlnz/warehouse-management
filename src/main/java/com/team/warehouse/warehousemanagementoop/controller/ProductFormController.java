package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.service.ProductService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductFormController implements Initializable {


    public static Product currentProductToEdit = null;

    @FXML private TextField codeTextField;
    @FXML private TextField nameTextField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField quantityTextField;
    @FXML private TextField priceTextField;
    @FXML private CheckBox activeCheckBox;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ProductService productService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productService = new ProductService();

        categoryComboBox.getItems().addAll("Đồ Gia Dụng", "Đồ Điện Tử", "Thực Phẩm", "Thời Trang", "Nhu Yếu Phẩm", "Trang Sức");
        categoryComboBox.getSelectionModel().selectFirst();

        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        loadDataIfEditing();
    }

    private void loadDataIfEditing() {
        if (currentProductToEdit != null) {
            //CHẾ ĐỘ SỬA: Lấy thông tin cũ
            codeTextField.setText(currentProductToEdit.getCode());
            codeTextField.setDisable(true);
            nameTextField.setText(currentProductToEdit.getName());

            if (currentProductToEdit.getCategoryId() > 0) {
                categoryComboBox.getSelectionModel().select((int) currentProductToEdit.getCategoryId() - 1);
            }

            quantityTextField.setText(String.valueOf(currentProductToEdit.getQuantity()));
            quantityTextField.setDisable(true);

            priceTextField.setText(String.valueOf(currentProductToEdit.getPrice()));
            activeCheckBox.setSelected(currentProductToEdit.isActive());
        } else {
            // CHẾ ĐỘ THÊM MỚI: Tự động sinh mã và gán tồn kho
            String nextCode = productService.generateNextProductCode();
            codeTextField.setText(nextCode);
            codeTextField.setDisable(true);

            quantityTextField.setText("0");
            quantityTextField.setDisable(true);

            activeCheckBox.setSelected(true);
        }
    }

    private void handleSave() {
        try {
            long categoryId = categoryComboBox.getSelectionModel().getSelectedIndex() + 1;
            int quantity = Integer.parseInt(quantityTextField.getText().trim());
            double price = Double.parseDouble(priceTextField.getText().trim());
            boolean isActive = activeCheckBox.isSelected();

            if (currentProductToEdit == null) {
                //CHẾ ĐỘ THÊM MỚI
                Product product = new Product();
                product.setCode(codeTextField.getText().trim());
                product.setName(nameTextField.getText().trim());
                product.setCategoryId(categoryId);
                product.setQuantity(quantity);
                product.setPrice(price);
                product.setActive(isActive);

                productService.addProduct(product);
                showSuccessAlert("Thành công", "Thêm sản phẩm thành công!");
            } else {
                //CHẾ ĐỘ CẬP NHẬT (SỬA)
                currentProductToEdit.setName(nameTextField.getText().trim());
                currentProductToEdit.setCategoryId(categoryId);
                currentProductToEdit.setQuantity(quantity);
                currentProductToEdit.setPrice(price);
                currentProductToEdit.setActive(isActive);

                productService.updateProduct(currentProductToEdit);
                showSuccessAlert("Thành công", "Cập nhật sản phẩm thành công!");
            }

            closeWindow();
        } catch (NumberFormatException e) {
            showErrorAlert("Lỗi nhập liệu", "Số lượng và đơn giá bắt buộc phải là số hợp lệ!");
        } catch (Exception e) {
            showErrorAlert("Lỗi hệ thống", e.getMessage());
        }
    }

    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        currentProductToEdit = null;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}