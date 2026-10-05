package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.service.ProductService;
import com.team.warehouse.warehousemanagementoop.util.SceneNavigator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductFormController implements Initializable {

    // Biến static nhận dữ liệu từ màn hình danh sách
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

        categoryComboBox.getItems().addAll("Đồ Gia Dụng", "Đồ Điện Tử", "Thực Phẩm");
        categoryComboBox.getSelectionModel().selectFirst();

        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());

        // Kiểm tra xem có đang mở form ở chế độ Sửa hay không
        loadDataIfEditing();
    }

    private void loadDataIfEditing() {
        if (currentProductToEdit != null) {
            codeTextField.setText(currentProductToEdit.getCode());
            codeTextField.setDisable(true); // Khóa không cho sửa Mã SP
            nameTextField.setText(currentProductToEdit.getName());

            if (currentProductToEdit.getCategoryId() > 0) {
                categoryComboBox.getSelectionModel().select((int) currentProductToEdit.getCategoryId() - 1);
            }

            quantityTextField.setText(String.valueOf(currentProductToEdit.getQuantity()));
            priceTextField.setText(String.valueOf(currentProductToEdit.getPrice()));
            activeCheckBox.setSelected(currentProductToEdit.isActive());
        }
    }

    private void handleSave() {
        try {
            long categoryId = categoryComboBox.getSelectionModel().getSelectedIndex() + 1;
            int quantity = Integer.parseInt(quantityTextField.getText().trim());
            double price = Double.parseDouble(priceTextField.getText().trim());
            boolean isActive = activeCheckBox.isSelected();

            if (currentProductToEdit == null) {
                // --- CHẾ ĐỘ THÊM MỚI ---
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
                // --- CHẾ ĐỘ CẬP NHẬT (SỬA) ---
                currentProductToEdit.setName(nameTextField.getText().trim());
                currentProductToEdit.setCategoryId(categoryId);
                currentProductToEdit.setQuantity(quantity);
                currentProductToEdit.setPrice(price);
                currentProductToEdit.setActive(isActive);

                productService.updateProduct(currentProductToEdit);
                showSuccessAlert("Thành công", "Cập nhật sản phẩm thành công!");
            }

            goBackToList();

        } catch (NumberFormatException e) {
            showErrorAlert("Lỗi nhập liệu", "Số lượng và đơn giá bắt buộc phải là số hợp lệ!");
        } catch (Exception e) {
            showErrorAlert("Lỗi hệ thống", e.getMessage());
        }
    }

    private void handleCancel() {
        goBackToList();
    }

    private void goBackToList() {
        try {
            // Xóa rỗng biến tĩnh để form mở lại vào lần sau ở trạng thái Thêm mới
            currentProductToEdit = null;

            AnchorPane contentArea = getParentAnchorPane();
            if (contentArea != null) {
                SceneNavigator.loadInto(contentArea, "/fxml/product/product-list.fxml");
            }
        } catch (Exception e) {
            showErrorAlert("Lỗi điều hướng", "Không thể quay lại danh sách: " + e.getMessage());
        }
    }

    private AnchorPane getParentAnchorPane() {
        if (saveButton.getScene() == null) return null;
        AnchorPane contentArea = (AnchorPane) saveButton.getScene().lookup("#contentArea");
        if (contentArea != null) return contentArea;
        if (saveButton.getScene().getRoot() instanceof AnchorPane) {
            return (AnchorPane) saveButton.getScene().getRoot();
        }
        return null;
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