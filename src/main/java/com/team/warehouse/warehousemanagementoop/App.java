package com.team.warehouse.warehousemanagementoop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/login.fxml")
        );

        Scene scene = new Scene(root);



        stage.setTitle("Hệ thống quản lý kho");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        System.out.println(
                App.class.getResource("/css/App.css")
        );

        launch(args);
    }
}