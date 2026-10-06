package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Role;
import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserEditFormController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPassword;
    @FXML private ComboBox<Role> cbRole;
    @FXML private CheckBox chkActive;

    private final UserService userService = new UserService();
    private Runnable onUserSavedCallback;
    private User currentUser;

    @FXML
    public void initialize() {
        cbRole.getItems().addAll(Role.values());
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            txtUsername.setText(user.getUsername());
            txtFullName.setText(user.getFullName());
            txtPassword.setText(user.getPassword());
            cbRole.setValue(user.getRole());
            chkActive.setSelected(user.isActive());
        }
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

        if (username.isEmpty() || fullName.isEmpty() || selectedRole == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            currentUser.setUsername(username);
            currentUser.setFullName(fullName);
            currentUser.setPassword(password);
            currentUser.setRole(selectedRole);
            currentUser.setActive(isActive);

            userService.updateUser(currentUser);

            if (onUserSavedCallback != null) {
                onUserSavedCallback.run();
            }

            closeStage();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin thành công!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật: " + e.getMessage());
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
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}