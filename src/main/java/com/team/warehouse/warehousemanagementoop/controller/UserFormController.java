package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Role;
import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserFormController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Role> cbRole;
    @FXML private CheckBox chkActive;

    private final UserService userService = new UserService();
    private Runnable onUserSavedCallback;

    @FXML
    public void initialize() {
        cbRole.getItems().addAll(Role.values());
        cbRole.setValue(Role.NHAN_VIEN_KHO);
    }

    public void setOnUserSavedCallback(Runnable callback) {
        this.onUserSavedCallback = callback;
    }

    @FXML
    private void handleSave() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = txtPassword.getText().trim();
        Role selectedRole = cbRole.getValue();
        boolean isActive = chkActive.isSelected();

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty() || selectedRole == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setFullName(fullName);
            newUser.setPassword(password);
            newUser.setRole(selectedRole);
            newUser.setActive(isActive);

             userService.addUser(newUser);

            if (onUserSavedCallback != null) {
                onUserSavedCallback.run();
            }

            closeStage();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm người dùng thành công!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}