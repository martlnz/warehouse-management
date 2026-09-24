package com.team.warehouse.warehousemanagementoop.util;

import javafx.scene.control.Alert;

public class AlertHelper {
    public static void showError(String title, String content) {
        new Alert(Alert.AlertType.ERROR, content).showAndWait();
    }
    public static void showInfo(String title, String content) {
        new Alert(Alert.AlertType.INFORMATION, content).showAndWait();
    }
}
