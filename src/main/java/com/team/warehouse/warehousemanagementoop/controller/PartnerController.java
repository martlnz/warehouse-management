package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.entity.Customer;
import com.team.warehouse.warehousemanagementoop.service.PartnerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class PartnerController {
    // 1. KHAI BÁO BIẾN GIAO DIỆN & SERVICE
    @FXML private TableView<Customer> customerTableView;
    @FXML private TableColumn<Customer, String> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, Void> actionColumn;
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;

    private final PartnerService partnerService = new PartnerService();

    // Style giao diện
    private final String styleEditNormal = "-fx-background-color: #0969da; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-padding: 4px 10px; -fx-cursor: hand;";
    private final String styleEditHover  = "-fx-background-color: #0451a5; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-padding: 4px 10px; -fx-cursor: hand;";

    private final String styleDeleteNormal = "-fx-background-color: #cf222e; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-padding: 4px 10px; -fx-cursor: hand;";
    private final String styleDeleteHover  = "-fx-background-color: #a40e26; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-padding: 4px 10px; -fx-cursor: hand;";

    // 2. KHỞI TẠO (INITIALIZE)
    @FXML
    public void initialize() {
        setupTableColumns();
        loadCustomerData();

        if (customerTableView != null) {
            customerTableView.setPlaceholder(new Label("Không tìm thấy khách hàng nào!"));
        }
        if (searchButton != null) {
            searchButton.setOnAction(e -> handleSearchAction());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((obs, oldVal, newVal) -> handleSearchAction());
            searchTextField.setOnAction(e -> handleSearchAction());
        }
    }

    // 3. CẤU HÌNH CÁC CỘT TRONG BẢNG (TABLEVIEW)
    private void setupTableColumns() {
        if (idColumn != null) {
            idColumn.setPrefWidth(120);
            idColumn.setCellValueFactory(cell ->
                    new SimpleStringProperty(formatCustomerId(cell.getValue().getId())));
        }
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        }
        if (phoneColumn != null) {
            phoneColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        }
        if (emailColumn != null) {
            emailColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        }

        // Cấu hình cột nút bấm Sửa / Xóa
        if (actionColumn != null) {
            actionColumn.setCellFactory(param -> new TableCell<Customer, Void>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Customer customer = getTableRow().getItem();
                        setGraphic(createActionButtons(customer));
                    }
                }
            });
        }
    }

    // 4. TẠO CÁC NÚT BẤM SỬA / XÓA CHO MỖI DÒNG
    private HBox createActionButtons(Customer customer) {
        Button btnEdit = new Button("Sửa");
        Button btnDelete = new Button("Xóa");

        applyCustomButtonStyle(btnEdit, styleEditNormal, styleEditHover);
        applyCustomButtonStyle(btnDelete, styleDeleteNormal, styleDeleteHover);

        btnEdit.setOnAction(e -> handleEditCustomer(customer));
        btnDelete.setOnAction(e -> handleDeleteCustomer(customer));

        HBox pane = new HBox(6, btnEdit, btnDelete);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    // 5. XỬ LÝ SỬA THÔNG TIN KHÁCH HÀNG
    private void handleEditCustomer(Customer customer) {
        if (customer == null) return;

        String oldName = customer.getName();
        String oldPhone = customer.getPhone();
        String oldEmail = customer.getEmail();

        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin khách hàng");
        dialog.setHeaderText("Cập nhật thông tin cho khách hàng ID: " + customer.getId());

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtName = new TextField(oldName);
        TextField txtPhone = new TextField(oldPhone);
        TextField txtEmail = new TextField(oldEmail);

        grid.add(new Label("Tên khách hàng:"), 0, 0); grid.add(txtName, 1, 0);
        grid.add(new Label("Số điện thoại:"), 0, 1); grid.add(txtPhone, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(txtEmail, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(ActionEvent.ACTION, e -> {
            String newPhone = txtPhone.getText().trim();
            String newEmail = txtEmail.getText().trim();

            if (!newPhone.equals(oldPhone) && partnerService.isPhoneExist(newPhone)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", null, "Số điện thoại (" + newPhone + ") đã tồn tại ở khách hàng khác!");
                e.consume();
                return;
            }

            if (!validateInput(txtName.getText().trim(), newPhone, newEmail)) {
                e.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                customer.setName(txtName.getText().trim());
                customer.setPhone(txtPhone.getText().trim());
                customer.setEmail(txtEmail.getText().trim());
                return customer;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCustomer -> {
            boolean success = partnerService.updateCustomer(updatedCustomer);
            if (success) {
                StringBuilder detail = new StringBuilder();
                if (!oldName.equals(updatedCustomer.getName())) {
                    detail.append("• Tên: '").append(oldName).append("' ➔ '").append(updatedCustomer.getName()).append("'\n");
                }
                if (!oldPhone.equals(updatedCustomer.getPhone())) {
                    detail.append("• SĐT: '").append(oldPhone).append("' ➔ '").append(updatedCustomer.getPhone()).append("'\n");
                }
                if (!oldEmail.equals(updatedCustomer.getEmail())) {
                    detail.append("• Email: '").append(oldEmail).append("' ➔ '").append(updatedCustomer.getEmail()).append("'\n");
                }

                if (detail.length() == 0) detail.append("Không có thay đổi nào!");

                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Sửa thông tin khách hàng ID " + customer.getId() + " Thành Công!",
                        "Chi tiết thay đổi:\n" + detail);

                loadCustomerData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", null, "Cập nhật thất bại!");
            }
        });
    }

    // 6. XỬ LÝ XÓA KHÁCH HÀNG
    private void handleDeleteCustomer(Customer customer) {
        if (customer == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa khách hàng này ?");
        confirm.setContentText("ID: " + customer.getId() + "\nTên: " + customer.getName() + "\nSố điện thoại: " + customer.getPhone() + "\nEmail: " + customer.getEmail());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = partnerService.deleteCustomer(customer.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Xóa khách hàng thành công!",
                            "Đã xóa :\n• ID: " + customer.getId()
                                    + "\n• Tên: " + customer.getName()
                                    + "\n• SĐT: " + customer.getPhone()
                                    + "\n• Email: " + customer.getEmail());

                    loadCustomerData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", null, "Xóa thất bại!");
                }
            }
        });
    }

    // 7. XỬ LÝ THÊM KHÁCH HÀNG MỚI
    public void handleAddCustomer() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Thêm khách hàng mới");
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(); txtName.setPromptText("Tên khách hàng");
        TextField txtPhone = new TextField(); txtPhone.setPromptText("Số điện thoại");
        TextField txtEmail = new TextField(); txtEmail.setPromptText("Email");

        grid.add(new Label("Tên khách hàng:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Số điện thoại:"), 0, 1);
        grid.add(txtPhone, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);

        Button btnSave = new Button("Lưu");
        Button btnCancel = new Button("Hủy");

        btnCancel.setOnAction(e -> dialogStage.close());

        btnSave.setOnAction(event -> {
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();

            if (!validateInput(name, phone, email)) return;

            if (partnerService.isPhoneExist(phone)) {
                showAlert(Alert.AlertType.ERROR, "Thông báo trùng lặp", "Không thể thêm khách hàng", "Số điện thoại (" + phone + ") đã tồn tại!");
                return;
            }

            try {
                Customer c = new Customer();
                c.setName(name);
                c.setPhone(phone);
                c.setEmail(email);

                partnerService.addCustomer(c);

                showAlert(Alert.AlertType.INFORMATION, "Thành công", null, "Thêm khách hàng thành công!");

                loadCustomerData();
                dialogStage.close();

            } catch (Throwable t) {
                System.err.println("-> [LỖI THỰC SỰ]: " + t.getMessage());
                t.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không lưu được vào CSDL", t.toString());
            }
        });

        HBox buttonBox = new HBox(10, btnSave, btnCancel);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 3);

        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.sizeToScene();
        dialogStage.showAndWait();
    }

    // 8. CÁC HÀM BỔ TRỢ (HELPER METHODS)
    private void loadCustomerData() {
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() {
                return partnerService.getAllCustomers();
            }
        };
        task.setOnSucceeded(e -> {
            List<Customer> list = task.getValue();
            if (list != null && customerTableView != null) {
                customerTableView.setItems(FXCollections.observableArrayList(list));
                customerTableView.refresh();
            }
        });
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            if (t != null) t.printStackTrace();
        });
        new Thread(task).start();
    }

    private boolean validateInput(String name, String phone, String email) {
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Vui lòng nhập tên khách hàng!");
            return false;
        }
        if (!name.matches("^[\\p{L}\\s]+$")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Tên khách hàng chỉ được chứa chữ cái, không được chứa số hay ký tự đặc biệt!");
            return false;
        }

        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Vui lòng nhập số điện thoại!");
            return false;
        }
        if (!phone.startsWith("0")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Số điện thoại phải bắt đầu bằng số 0!");
            return false;
        }
        if (!phone.matches("^\\d{10}$")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Số điện thoại phải gồm đúng 10 chữ số!");
            return false;
        }
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Vui lòng nhập email!");
            return false;
        }
        if (!email.toLowerCase().matches("^[a-z0-9._%+-]+@gmail\\.com$")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", null, "Email không hợp lệ! Phải đúng định dạng abc@gmail.com");
            return false;
        }
        return true;
    }

    private String formatCustomerId(Long id) {
        if (id == null) return "KH000";
        return String.format("KH%03d", id);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleSearchAction() {
        if (searchTextField == null) return;
        String keyword = searchTextField.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomerData();
            return;
        }
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() {
                return partnerService.searchCustomers(keyword);
            }
        };
        task.setOnSucceeded(e -> {
            List<Customer> result = task.getValue();
            if (customerTableView != null) {
                if (result == null || result.isEmpty()) {
                    customerTableView.getItems().clear();
                } else {
                    customerTableView.setItems(FXCollections.observableArrayList(result));
                }
                customerTableView.refresh();
            }
        });
        task.setOnFailed(e -> {
            Throwable t = task.getException();
            if (t != null) t.printStackTrace();
        });
        new Thread(task).start();
    }

    private void applyCustomButtonStyle(Button button, String normalStyle, String hoverStyle) {
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
}