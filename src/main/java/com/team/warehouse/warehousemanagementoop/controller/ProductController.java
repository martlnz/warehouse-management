package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.service.ProductService;
import com.team.warehouse.warehousemanagementoop.util.AlertHelper;
import com.team.warehouse.warehousemanagementoop.util.SceneNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    // Ánh xạ các thành phần UI từ product-list.fxml
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

    // Service xử lý logic và danh sách hiển thị
    private ProductService productService;
    private ObservableList<Product> productObservableList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo service
        productService = new ProductService();

        // Thiết lập các cột của bảng
        setupTableColumns();

        // Gắn các sự kiện cho các nút bấm
        setupEventHandlers();

        // Tải dữ liệu từ CSDL khi vừa mở màn hình
        loadProductData();
    }


     // Cấu hình liên kết dữ liệu cho các cột trong TableView

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Cấu hình riêng cho cột "Thao tác" chứa nút Sửa và Xóa
        setupActionColumn();
    }


     // Tạo 2 nút "Sửa" và "Xóa" bên trong mỗi ô của cột actionColumn

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

                        btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
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

     // Tải danh sách sản phẩm từ cơ sở dữ liệu lên giao diện

    private void loadProductData() {
        try {
            // Lấy danh sách sản phẩm thật từ Database thông qua Service
            List<Product> products = productService.getAllProducts();
            productObservableList = FXCollections.observableArrayList(products);
            productTableView.setItems(productObservableList);
        } catch (Exception e) {
            AlertHelper.showError("Lỗi hệ thống", "Không thể tải dữ liệu: " + e.getMessage());
        }
    }


     // Thiết lập sự kiện cho các nút bấm cố định (Tìm kiếm, Thêm mới)

    private void setupEventHandlers() {
        // Gắn sự kiện cho nút tìm kiếm
        searchButton.setOnAction(event -> {
            String keyword = searchTextField.getText();
            handleSearchProduct(keyword);
        });

        // Gắn sự kiện cho nút thêm sản phẩm
        addButton.setOnAction(event -> {
            try {
                AnchorPane contentArea = getParentAnchorPane();
                if (contentArea != null) {
                    SceneNavigator.loadInto(contentArea, "/fxml/product/product-form.fxml");
                } else {
                    AlertHelper.showError("Lỗi giao diện", "Không tìm thấy vùng chứa (AnchorPane) để chuyển trang!");
                }
            } catch (Exception e) {
                AlertHelper.showError("Lỗi điều hướng", "Không thể mở form thêm sản phẩm: " + e.getMessage());
            }
        });
    }

     // Xử lý hành động tìm kiếm sản phẩm

    private void handleSearchProduct(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadProductData(); // Nếu ô tìm kiếm rỗng thì tải lại toàn bộ
            return;
        }

        try {
            // Gọi ProductService để tìm kiếm theo tên hoặc mã SP
            List<Product> searchResults = productService.searchProducts(keyword.trim());
            productObservableList.setAll(searchResults);
        } catch (Exception e) {
            AlertHelper.showError("Lỗi tìm kiếm", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }


     // Xử lý khi nhấn nút Sửa tại 1 dòng cụ thể

    private void handleEditProduct(Product product) {
        try {
            // Truyền sản phẩm được chọn sang Form sửa
            ProductFormController.currentProductToEdit = product;

            AnchorPane contentArea = getParentAnchorPane();
            if (contentArea != null) {
                // Sửa lại đường dẫn ở đây (thêm /fxml/ ở đầu)
                SceneNavigator.loadInto(contentArea, "/fxml/product/product-form.fxml");
            }
        } catch (Exception e) {
            AlertHelper.showError("Lỗi điều hướng", "Không thể mở form cập nhật: " + e.getMessage());
        }
    }




    // Xử lý khi nhấn nút Xóa tại 1 dòng cụ thể

    private void handleDeleteProduct(Product product) {
        // Hiện hộp thoại xác nhận bằng Alert mặc định của JavaFX
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa sản phẩm: '" + product.getName() + "' không?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Gọi ProductService để thực hiện xóa mềm (Soft Delete) trong Database
                productService.deleteProduct(product.getId());

                // Cập nhật lại UI sau khi xóa thành công mà không cần load lại toàn bộ DB
                productObservableList.remove(product);

                // Báo thành công
                Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
                successDialog.setTitle("Thành công");
                successDialog.setHeaderText(null);
                successDialog.setContentText("Xóa sản phẩm thành công!");
                successDialog.showAndWait();

            } catch (Exception e) {
                // Bắt lỗi từ hàm deleteProduct trong Service ném ra
                AlertHelper.showError("Lỗi xóa dữ liệu", "Lỗi: " + e.getMessage());
            }
        }
    }


     // Hàm hỗ trợ lấy AnchorPane bao ngoài danh sách

    private AnchorPane getParentAnchorPane() {
        if (addButton.getScene() == null) return null;

        // Cách 1: Tìm node có id là "contentArea" trong MainLayout
        AnchorPane contentArea = (AnchorPane) addButton.getScene().lookup("#contentArea");
        if (contentArea != null) {
            return contentArea;
        }

        // Cách 2: Lấy thẳng root của Scene nếu root là AnchorPane
        if (addButton.getScene().getRoot() instanceof AnchorPane) {
            return (AnchorPane) addButton.getScene().getRoot();
        }

        return null;
    }
}