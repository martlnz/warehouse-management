package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.dto.ImportReceiptDTO;
import com.team.warehouse.warehousemanagementoop.dto.ReceiptDetailDTO;
import com.team.warehouse.warehousemanagementoop.entity.ImportReceipt;
import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.entity.ReceiptDetail;
import com.team.warehouse.warehousemanagementoop.entity.Supplier;
import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;
import com.team.warehouse.warehousemanagementoop.service.ImportService;
import com.team.warehouse.warehousemanagementoop.util.AlertHelper;
import com.team.warehouse.warehousemanagementoop.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class ImportReceiptController implements Initializable {

    private static final String FORM_FXML = "/fxml/import/import-form.fxml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private TableView<ImportReceipt> importTableView;
    @FXML private TableColumn<ImportReceipt, String> idColumn;
    @FXML private TableColumn<ImportReceipt, String> dateColumn;
    @FXML private TableColumn<ImportReceipt, String> supplierColumn;
    @FXML private TableColumn<ImportReceipt, String> createdByColumn;
    @FXML private TableColumn<ImportReceipt, String> statusColumn;
    @FXML private TableColumn<ImportReceipt, Void> actionColumn;

    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private TextArea noteTextArea;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField quantityTextField;
    @FXML private TextField unitPriceTextField;
    @FXML private Button addLineButton;
    @FXML private TableView<ReceiptDetailDTO> detailTableView;
    @FXML private TableColumn<ReceiptDetailDTO, String> lineProductColumn;
    @FXML private TableColumn<ReceiptDetailDTO, String> lineQuantityColumn;
    @FXML private TableColumn<ReceiptDetailDTO, String> lineUnitPriceColumn;
    @FXML private TableColumn<ReceiptDetailDTO, String> lineSubtotalColumn;
    @FXML private TableColumn<ReceiptDetailDTO, Void> lineRemoveColumn;
    @FXML private Label totalLabel;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private final ImportService importService = new ImportService();
    private final ObservableList<ImportReceipt> receiptList = FXCollections.observableArrayList();
    private final ObservableList<ReceiptDetailDTO> detailList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (importTableView != null) {
            initListView();
        }
        if (detailTableView != null) {
            initFormView();
        }
    }

    private void initListView() {
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().getCreatedDate())));
        supplierColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartnerName()));
        createdByColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedByName()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        actionColumn.setCellFactory(column -> new TableCell<ImportReceipt, Void>() {
            private final Button viewButton = new Button("Xem");
            private final Button deleteButton = new Button("Xóa");
            private final HBox container = new HBox(5, viewButton, deleteButton);

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                viewButton.setMinWidth(60);

                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                deleteButton.setMinWidth(50);

                container.setAlignment(Pos.CENTER);

                viewButton.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> deleteReceipt(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        importTableView.setItems(receiptList);

        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadReceipts(newVal);
        });

        searchButton.setOnAction(e -> loadReceipts(searchTextField.getText()));

        addButton.setOnAction(e -> onAddButtonClick());

        loadReceipts("");
    }

    private void loadReceipts(String keyword) {
        try {
            receiptList.setAll(importService.searchBySupplierName(keyword));
        } catch (DataAccessException e) {
            showDataError("Lỗi tải dữ liệu", e);
        }
    }

    private void onAddButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_FXML));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Tạo phiếu nhập kho");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(addButton.getScene().getWindow());
            dialog.setScene(new Scene(root, 680, 560));
            dialog.showAndWait();

            loadReceipts(searchTextField.getText());
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Lỗi", "Không mở được form tạo phiếu nhập");
        }
    }

    private void showDetail(ImportReceipt row) {
        try {
            ImportReceipt receipt = importService.findById(row.getId());
            if (receipt == null) {
                AlertHelper.showError("Lỗi", "Không tìm thấy phiếu nhập " + row.getDisplayCode());
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/import/import-detail-dialog.fxml"));
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(loader.load());
            ImportDetailController controller = loader.getController();
            controller.setReceiptData(receipt);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Chi tiết phiếu nhập");
            dialog.setHeaderText("Mã phiếu: " + receipt.getDisplayCode());
            dialog.setDialogPane(dialogPane);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (IOException e) {
            AlertHelper.showError("Lỗi", "Không thể nạp giao diện chi tiết phiếu nhập!");
            e.printStackTrace();
        } catch (DataAccessException e) {
            showDataError("Lỗi tải dữ liệu", e);
        }
    }

    private void deleteReceipt(ImportReceipt receipt) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa phiếu nhập " + receipt.getDisplayCode() + "?");
        confirm.setContentText("Hành động này sẽ làm giảm lượng tồn kho tương ứng.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    importService.deleteImportReceipt(receipt.getId());
                    AlertHelper.showInfo("Thành công", "Đã xóa phiếu nhập.");
                    loadReceipts(searchTextField.getText());
                } catch (DataAccessException e) {
                    showDataError("Lỗi xóa phiếu", e);
                }
            }
        });
    }

    private void initFormView() {
        makeSearchable(supplierComboBox, importService.getAllSuppliers(), Supplier::getName);
        makeSearchable(productComboBox, importService.getActiveProducts(),
                product -> product.getCode() + " - " + product.getName());

        unitPriceTextField.setPromptText("Giá nhập");

        lineProductColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        lineQuantityColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        lineUnitPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getUnitPrice())));
        lineSubtotalColumn.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getSubtotal())));

        lineRemoveColumn.setCellFactory(column -> new TableCell<ReceiptDetailDTO, Void>() {
            private final Button removeButton = new Button("Xóa");
            {
                removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                removeButton.setOnAction(e -> {
                    detailList.remove(getTableView().getItems().get(getIndex()));
                    updateTotal();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        detailTableView.setItems(detailList);
        addLineButton.setOnAction(e -> onAddLineButtonClick());
        saveButton.setOnAction(e -> onSaveButtonClick());
        cancelButton.setOnAction(e -> onCancelButtonClick());
        updateTotal();
    }

    private void onAddLineButtonClick() {
        Product product = getChosen(productComboBox);
        if (product == null) {
            AlertHelper.showError("Thiếu dữ liệu", "Vui lòng gõ để tìm và chọn 1 sản phẩm trong danh sách gợi ý");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityTextField.getText().trim());
        } catch (NumberFormatException e) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Số lượng phải là số nguyên");
            return;
        }
        if (quantity <= 0) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Số lượng phải lớn hơn 0");
            return;
        }

        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(unitPriceTextField.getText().trim());
        } catch (NumberFormatException e) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Vui lòng nhập đơn giá nhập (là một số, VD: 12000)");
            return;
        }
        if (unitPrice.signum() < 0) {
            AlertHelper.showError("Dữ liệu không hợp lệ", "Đơn giá không được âm");
            return;
        }

        for (ReceiptDetailDTO line : detailList) {
            if (line.getProductId() == product.getId()) {
                AlertHelper.showError("Sản phẩm đã có trong phiếu",
                        "Hãy xóa dòng cũ của \"" + product.getName() + "\" nếu muốn nhập lại số lượng/đơn giá.");
                return;
            }
        }

        detailList.add(new ReceiptDetailDTO(product.getId(), product.getName(), quantity, unitPrice));
        clearChoice(productComboBox);
        quantityTextField.clear();
        unitPriceTextField.clear();
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptDetailDTO line : detailList) {
            total = total.add(line.getSubtotal());
        }
        totalLabel.setText(formatMoney(total));
    }

    private void onSaveButtonClick() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            AlertHelper.showError("Chưa đăng nhập", "Vui lòng đăng nhập lại để tạo phiếu");
            return;
        }

        Supplier supplier = getChosen(supplierComboBox);
        ImportReceiptDTO dto = new ImportReceiptDTO();
        dto.setSupplierId(supplier == null ? 0 : supplier.getId());
        dto.setCreatedById(currentUser.getId());
        dto.setNote(noteTextArea.getText() == null ? "" : noteTextArea.getText().trim());
        dto.setDetails(new ArrayList<>(detailList));

        try {
            ImportReceipt receipt = importService.createImportReceipt(dto);
            AlertHelper.showInfo("Thành công", "Đã lưu phiếu nhập " + receipt.getDisplayCode());
            closeDialog();
        } catch (IllegalArgumentException e) {
            AlertHelper.showError("Dữ liệu không hợp lệ", e.getMessage());
        } catch (DataAccessException e) {
            showDataError("Lưu phiếu thất bại", e);
        }
    }

    private void onCancelButtonClick() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    private <T> void makeSearchable(ComboBox<T> comboBox, List<T> allItems, Function<T, String> label) {
        FilteredList<T> filtered = new FilteredList<>(FXCollections.observableArrayList(allItems), item -> true);
        comboBox.setEditable(true);
        comboBox.setItems(filtered);
        comboBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : label.apply(item);
            }
            @Override
            public T fromString(String text) {
                if (text != null) {
                    for (T item : allItems) {
                        if (label.apply(item).equalsIgnoreCase(text.trim())) {
                            return item;
                        }
                    }
                }
                return null;
            }
        });

        TextField editor = comboBox.getEditor();
        comboBox.showingProperty().addListener((obs, wasShowing, showing) -> {
            T value = comboBox.getValue();
            if (showing && value != null && label.apply(value).equals(editor.getText())) {
                filtered.setPredicate(item -> true);
            }
        });

        editor.textProperty().addListener((obs, oldText, newText) -> {
            T selected = comboBox.getValue();
            if (selected != null && label.apply(selected).equals(newText)) {
                return;
            }
            String keyword = removeAccent(newText);
            filtered.setPredicate(item -> keyword.isEmpty() || removeAccent(label.apply(item)).contains(keyword));
            if (editor.isFocused() && !keyword.isEmpty() && !filtered.isEmpty() && !comboBox.isShowing()) {
                comboBox.show();
            }
        });
    }

    private <T> T getChosen(ComboBox<T> comboBox) {
        T value = comboBox.getValue();
        String text = comboBox.getEditor().getText();
        if (value != null && comboBox.getConverter().toString(value).equals(text)) {
            return value;
        }
        return comboBox.getConverter().fromString(text);
    }

    private <T> void clearChoice(ComboBox<T> comboBox) {
        comboBox.setValue(null);
        comboBox.getEditor().clear();
    }

    private String removeAccent(String text) {
        if (text == null) {
            return "";
        }
        String temp = Normalizer.normalize(text.trim().toLowerCase(), Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd');
    }

    private void showDataError(String title, DataAccessException e) {
        e.printStackTrace();
        Throwable cause = e.getCause();
        String detail = cause == null ? e.getMessage()
                : e.getMessage() + "\n\nNguyên nhân: " + cause.getMessage();
        AlertHelper.showError(title, detail);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMAT);
    }

    private String formatMoney(BigDecimal value) {
        return new DecimalFormat("#,##0.##").format(value == null ? BigDecimal.ZERO : value);
    }
}