package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class UserManagementController {
    @FXML
    private TableView<User> userTableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    @FXML private Button addButton;
    @FXML private Button searchButton;

    private final UserService userService = new UserService();
    public void initialize(){
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(column -> new TableCell<User, Boolean>() {

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    User user = getTableRow().getItem();
                    setText(user.isActive() ? "Hoạt động" : "Bị khóa");
                    setStyle(user.isActive() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        if (addButton != null) {
            addButton.setOnAction(event -> openAddUser());
        }
        loadData();
    }
    private void openAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/user_form.fxml"));
            Parent root = loader.load();

            UserFormController formController = loader.getController();
            formController.setOnUserSavedCallback(this::loadData);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Thêm người dùng mới");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể mở form thêm người dùng: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }
    private void loadData() {
        List<User> list = userService.getAllUsers();
        ObservableList<User> observableList = FXCollections.observableArrayList(list);
        userTableView.setItems(observableList);
    }
}
