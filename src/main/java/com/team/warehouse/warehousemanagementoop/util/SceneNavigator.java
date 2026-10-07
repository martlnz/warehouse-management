package com.team.warehouse.warehousemanagementoop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class SceneNavigator {

    /**
     * Vùng nội dung của main-layout (fx:id="contentArea").
     * MainLayoutController đăng ký 1 lần khi khởi tạo, để các màn hình con (list -> form -> list...)
     * tự chuyển màn hình được mà không cần giữ tham chiếu tới contentArea.
     */
    private static AnchorPane mainContentArea;

    public static void setMainContentArea(AnchorPane contentArea) {
        mainContentArea = contentArea;
    }

    /** Dùng cho sidebar (MainLayoutController): nạp 1 view vào contentArea được chỉ định. */
    public static void loadInto(AnchorPane contentArea, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            throw new RuntimeException("Không load được màn hình: " + fxmlPath, e);
        }
    }

    /**
     * Dùng cho các màn hình con: chuyển sang 1 màn hình khác ngay trong contentArea của main-layout
     * (VD: bấm "Tạo phiếu" ở danh sách -> mở form; lưu xong -> quay lại danh sách).
     */
    public static void navigateTo(String fxmlPath) {
        if (mainContentArea == null) {
            throw new IllegalStateException(
                    "Chưa đăng ký contentArea — MainLayoutController phải gọi SceneNavigator.setMainContentArea(...)");
        }
        loadInto(mainContentArea, fxmlPath);
    }
}