package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    private final AuthService authService = new AuthService();
    @FXML
    private void onLoginButtonClick() {
        if (usernameField.getText() == null || usernameField.getText().isBlank()) {
            errorLabel.setText("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (passwordField.getText() == null || passwordField.getText().isBlank()) {
            errorLabel.setText("Vui lòng nhập tên đăng nhập");
            return;
        }

        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            boolean isSuccess = authService.login(username, password);
            if(isSuccess){
                Parent mainLayout = FXMLLoader.load(getClass().getResource("/fxml/main-layout.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.getScene().setRoot(mainLayout);
                stage.setMaximized(true);
            }
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}