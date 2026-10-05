package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Supplier;
import com.team.warehouse.warehousemanagementoop.service.PartnerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class PartnerController {

    @FXML
    private TextField searchTextField;
    @FXML
    private TableView<Supplier> supplierTableView;
    @FXML
    private TableColumn<Supplier, Integer> idColumn;
    @FXML
    private TableColumn<Supplier, String> nameColumn;
    @FXML
    private TableColumn<Supplier, String> phoneColumn;
    @FXML
    private TableColumn<Supplier, String> emailColumn;
    @FXML
    private TableColumn<Supplier, String> actionColumn;

    private final PartnerService supplierService = new PartnerService();
    private final ObservableList<Supplier> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("=== INITIALIZE ĐÃ CHẠY ===");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        supplierTableView.setPlaceholder(new Label("Không tìm thấy nhà cung cấp"));
        setupSearch();
        loadTableData();
    }

    private void loadTableData() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        masterData.setAll(suppliers);
    }

    private void setupSearch() {
        FilteredList<Supplier> filteredData = new FilteredList<>(masterData, p -> true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(supplier -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.trim().toLowerCase();
                return supplier.getName() != null && supplier.getName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Supplier> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(supplierTableView.comparatorProperty());
        supplierTableView.setItems(sortedData);
    }
    @FXML
    private void handleOpenAddForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/partner/supplier-form.fxml"));
            Parent root = loader.load();
            PartnerFormController formController = loader.getController();
            formController.setOnSaveSuccess(this::loadTableData);
            Stage stage = new Stage();
            stage.setTitle("Thêm nhà cung cấp mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}