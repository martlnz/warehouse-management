package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.util.SceneNavigator;
import com.team.warehouse.warehousemanagementoop.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML private AnchorPane contentArea;

    @FXML
    public void initialize() {
        onDashboardMenuClick();
    }

    @FXML
    private void onDashboardMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/dashboard/dashboard.fxml");
    }

    @FXML
    private void onProductMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/product/product-list.fxml");
    }

    @FXML
    private void onCategoryMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/category/category-list.fxml");
    }

    @FXML
    private void onSupplierMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/partner/supplier-list.fxml");
    }

    @FXML
    private void onCustomerMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/partner/customer-list.fxml");
    }

    @FXML
    private void onImportMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/import/import-list.fxml");
    }

    @FXML
    private void onExportMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/export/export-list.fxml");
    }

    @FXML
    private void onUserMenuClick() {
        SceneNavigator.loadInto(contentArea, "/fxml/user/user-management.fxml");
    }

    @FXML
    private void onLogoutClick() throws Exception {
        SessionManager.clear();
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.setMaximized(false);
        stage.setScene(new Scene(loginRoot));
        stage.centerOnScreen();
    }
}