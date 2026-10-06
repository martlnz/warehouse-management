package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.service.ProductService;
import com.team.warehouse.warehousemanagementoop.util.AlertHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private TableView<Product> productTableView;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Long> categoryColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Void> actionColumn;

    private ProductService productService;
    private ObservableList<Product> productObservableList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productService = new ProductService();
        setupTableColumns();
        setupEventHandlers();
        loadProductData();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // -Dịch ID thành tên danh mục
        categoryColumn.setCellFactory(column -> new TableCell<Product, Long>() {
            @Override
            protected void updateItem(Long categoryId, boolean empty) {
                super.updateItem(categoryId, empty);
                if (empty || categoryId == null) {
                    setText(null);
                } else {
                    String categoryName = "Không xác định";
                    switch (categoryId.intValue()) {
                        case 1: categoryName = "Đồ Gia Dụng"; break;
                        case 2: categoryName = "Đồ Điện Tử"; break;
                        case 3: categoryName = "Thực Phẩm"; break;
                        case 4: categoryName = "Thời Trang"; break;
                        case 5: categoryName = "Nhu Yếu Phẩm"; break;
                        case 6: categoryName = "Trang Sức"; break;
                    }
                    setText(categoryName);
                }
            }
        });
        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox pane = new HBox(5, btnEdit, btnDelete);

                    {
                        btnEdit.setOnAction(event -> {
                            Product selectedProduct = getTableView().getItems().get(getIndex());
                            handleEditProduct(selectedProduct);
                        });

                        btnDelete.setOnAction(event -> {
                            Product selectedProduct = getTableView().getItems().get(getIndex());
                            handleDeleteProduct(selectedProduct);
                        });
                        btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                        btnEdit.setMinWidth(55);
                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                        btnDelete.setMinWidth(55);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    private void loadProductData() {
        try {
            List<Product> products = productService.getAllProducts();
            productObservableList = FXCollections.observableArrayList(products);
            productTableView.setItems(productObservableList);
        } catch (Exception e) {
            AlertHelper.showError("Lỗi hệ thống", "Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(event -> {
            String keyword = searchTextField.getText();
            handleSearchProduct(keyword);
        });

        // Đã sửa: Gọi hàm mở cửa sổ nổi thay vì dùng SceneNavigator
        addButton.setOnAction(event -> {
            ProductFormController.currentProductToEdit = null;
            openFormPopup("Thêm sản phẩm mới");
        });
    }

    private void handleSearchProduct(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadProductData();
            return;
        }

        try {
            List<Product> searchResults = productService.searchProducts(keyword.trim());
            productObservableList.setAll(searchResults);
        } catch (Exception e) {
            AlertHelper.showError("Lỗi tìm kiếm", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // Đã sửa: Gọi hàm mở cửa sổ nổi thay vì dùng SceneNavigator
    private void handleEditProduct(Product product) {
        ProductFormController.currentProductToEdit = product;
        openFormPopup("Sửa thông tin sản phẩm");
    }

    private void handleDeleteProduct(Product product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa sản phẩm: '" + product.getName() + "' không?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                productObservableList.remove(product);

                Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
                successDialog.setTitle("Thành công");
                successDialog.setHeaderText(null);
                successDialog.setContentText("Xóa sản phẩm thành công!");
                successDialog.showAndWait();

            } catch (Exception e) {
                AlertHelper.showError("Lỗi xóa dữ liệu", "Lỗi: " + e.getMessage());
            }
        }
    }


    // Cấu hình và bật cửa sổ nổi (Popup/Modal)

    private void openFormPopup(String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product/product-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);


            stage.setScene(new Scene(root, 400, 420));
            stage.setResizable(false);

            stage.showAndWait();


            loadProductData();

        } catch (Exception e) {
            AlertHelper.showError("Lỗi hiển thị", "Không thể mở form: " + e.getMessage());
            e.printStackTrace();
        }
    }
}