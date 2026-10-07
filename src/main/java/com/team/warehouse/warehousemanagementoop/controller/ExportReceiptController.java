package com.team.warehouse.warehousemanagementoop.controller;

import com.team.warehouse.warehousemanagementoop.dto.ExportReceiptDTO;
import com.team.warehouse.warehousemanagementoop.dto.ReceiptDetailDTO;
import com.team.warehouse.warehousemanagementoop.entity.Customer;
import com.team.warehouse.warehousemanagementoop.entity.ExportReceipt;
import com.team.warehouse.warehousemanagementoop.entity.Product;
import com.team.warehouse.warehousemanagementoop.entity.ReceiptDetail;
import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.exception.DataAccessException;
import com.team.warehouse.warehousemanagementoop.exception.InsufficientStockException;
import com.team.warehouse.warehousemanagementoop.service.ExportService;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

/**
 * Controller dùng cho CẢ 2 màn hình: export-list.fxml và export-form.fxml.
 * Mỗi lần load FXML sẽ tạo 1 controller mới; control nào không có trong FXML đang load thì sẽ là null,
 * nên initialize() dựa vào đó để biết đang ở màn hình nào.
 * Sự kiện của nút được gán bằng code (setOnAction) nên FXML không cần khai báo onAction.
 * Form tạo phiếu được mở trong 1 cửa sổ popup (modal) đè lên màn hình danh sách.
 */
public class ExportReceiptController implements Initializable {

    private static final String FORM_FXML = "/fxml/export/export-form.fxml";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== export-list.fxml =====
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private TableView<ExportReceipt> exportTableView;
    @FXML private TableColumn<ExportReceipt, String> idColumn;
    @FXML private TableColumn<ExportReceipt, String> dateColumn;
    @FXML private TableColumn<ExportReceipt, String> customerColumn;
    @FXML private TableColumn<ExportReceipt, String> createdByColumn;
    @FXML private TableColumn<ExportReceipt, String> statusColumn;
    @FXML private TableColumn<ExportReceipt, Void> actionColumn;

    // ===== export-form.fxml =====
    @FXML private ComboBox<Customer> customerComboBox;
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

    private final ExportService exportService = new ExportService();
    private final ObservableList<ExportReceipt> receiptList = FXCollections.observableArrayList();
    private final ObservableList<ReceiptDetailDTO> detailList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (exportTableView != null) {
            initListView();
        }
        if (detailTableView != null) {
            initFormView();
        }
    }

    // =====================================================================
    // MÀN HÌNH DANH SÁCH
    // =====================================================================

    private void initListView() {
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().getCreatedDate())));
        customerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartnerName()));
        createdByColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedByName()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Cột "Thao tác": nút Xem chi tiết phiếu
        actionColumn.setCellFactory(column -> new TableCell<ExportReceipt, Void>() {
            private final Button viewButton = new Button("Xem");

            {
                viewButton.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        exportTableView.setItems(receiptList);
        searchButton.setOnAction(e -> onSearchButtonClick());
        addButton.setOnAction(e -> onAddButtonClick());

        loadReceipts("");
    }

    private void loadReceipts(String keyword) {
        try {
            receiptList.setAll(exportService.searchByCustomerName(keyword));
        } catch (DataAccessException e) {
            showDataError("Lỗi tải dữ liệu", e);
        }
    }

    private void onSearchButtonClick() {
        loadReceipts(searchTextField.getText());
    }

    /** Mở form tạo phiếu trong popup; đóng popup xong thì làm mới danh sách. */
    private void onAddButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FORM_FXML));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Tạo phiếu xuất kho");
            dialog.initModality(Modality.APPLICATION_MODAL); // khóa cửa sổ chính cho tới khi đóng popup
            dialog.initOwner(addButton.getScene().getWindow());
            dialog.setScene(new Scene(root, 680, 560));
            dialog.showAndWait();

            loadReceipts(searchTextField.getText()); // popup đóng -> nạp lại danh sách
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Lỗi", "Không mở được form tạo phiếu xuất");
        }
    }

    private void showDetail(ExportReceipt row) {
        try {
            ExportReceipt receipt = exportService.findById(row.getId());
            if (receipt == null) {
                AlertHelper.showError("Lỗi", "Không tìm thấy phiếu xuất " + row.getDisplayCode());
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Phiếu xuất ").append(receipt.getDisplayCode()).append("\n");
            sb.append("Khách hàng: ").append(receipt.getPartnerName()).append("\n");
            sb.append("Ghi chú: ").append(receipt.getNote() == null ? "" : receipt.getNote()).append("\n\n");
            for (ReceiptDetail detail : receipt.getDetails()) {
                sb.append("- ").append(detail.getProductName())
                        .append(" | SL: ").append(detail.getQuantity())
                        .append(" | Đơn giá: ").append(formatMoney(detail.getUnitPrice()))
                        .append(" | Thành tiền: ").append(formatMoney(detail.getSubtotal()))
                        .append("\n");
            }
            sb.append("\nTổng tiền: ").append(formatMoney(receipt.getTotalAmount()));
            AlertHelper.showInfo("Chi tiết phiếu xuất", sb.toString());
        } catch (DataAccessException e) {
            showDataError("Lỗi tải dữ liệu", e);
        }
    }

    // =====================================================================
    // MÀN HÌNH FORM TẠO PHIẾU
    // =====================================================================

    private void initFormView() {
        // Ô gõ để tìm: gõ chữ -> danh sách gợi ý tự lọc, rồi chọn 1 mục trong gợi ý
        makeSearchable(customerComboBox, exportService.getAllCustomers(), Customer::getName);
        // Hiển thị kèm tồn kho hiện tại để người dùng biết còn bao nhiêu (Service vẫn kiểm tra lại khi lưu)
        makeSearchable(productComboBox, exportService.getActiveProducts(),
                product -> product.getCode() + " - " + product.getName() + " (tồn: " + product.getQuantity() + ")");

        // Chọn sản phẩm thì gợi ý sẵn đơn giá (vẫn sửa lại được)
        productComboBox.valueProperty().addListener((obs, oldProduct, newProduct) -> {
            if (newProduct != null) {
                unitPriceTextField.setText(
                        BigDecimal.valueOf(newProduct.getPrice()).stripTrailingZeros().toPlainString());
            }
        });

        lineProductColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        lineQuantityColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        lineUnitPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getUnitPrice())));
        lineSubtotalColumn.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getSubtotal())));

        // Cột cuối: nút Xóa dòng
        lineRemoveColumn.setCellFactory(column -> new TableCell<ReceiptDetailDTO, Void>() {
            private final Button removeButton = new Button("Xóa");

            {
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
            AlertHelper.showError("Dữ liệu không hợp lệ", "Đơn giá phải là số");
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

        Customer customer = getChosen(customerComboBox);
        ExportReceiptDTO dto = new ExportReceiptDTO();
        dto.setCustomerId(customer == null ? 0 : customer.getId());
        dto.setCreatedById(currentUser.getId());
        dto.setNote(noteTextArea.getText() == null ? "" : noteTextArea.getText().trim());
        dto.setDetails(new ArrayList<>(detailList));

        try {
            ExportReceipt receipt = exportService.createExportReceipt(dto);
            AlertHelper.showInfo("Thành công", "Đã lưu phiếu xuất " + receipt.getDisplayCode());
            closeDialog();
        } catch (IllegalArgumentException e) {
            AlertHelper.showError("Dữ liệu không hợp lệ", e.getMessage());
        } catch (InsufficientStockException e) {
            // Ở lại form để người dùng chỉnh lại số lượng
            AlertHelper.showError("Không đủ tồn kho", e.getMessage());
        } catch (DataAccessException e) {
            showDataError("Lưu phiếu thất bại", e);
        }
    }

    private void onCancelButtonClick() {
        closeDialog();
    }

    /** Đóng cửa sổ popup chứa form hiện tại. */
    private void closeDialog() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    // =====================================================================
    // Ô "gõ để tìm" cho ComboBox
    // =====================================================================

    /**
     * Biến ComboBox thành ô "gõ để tìm": gõ chữ vào ô, danh sách gợi ý tự lọc theo chữ vừa gõ
     * (không phân biệt hoa/thường, gõ không dấu vẫn tìm được tiếng Việt).
     * Vẫn phải chọn 1 mục trong gợi ý, vì phiếu lưu theo mã (id) chứ không lưu theo chữ gõ vào.
     */
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
                // Gõ trùng đúng tên của 1 mục thì coi như đã chọn mục đó
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

        // Mở danh sách khi đang có 1 mục được chọn -> hiện đầy đủ để đổi sang mục khác
        comboBox.showingProperty().addListener((obs, wasShowing, showing) -> {
            T value = comboBox.getValue();
            if (showing && value != null && label.apply(value).equals(editor.getText())) {
                filtered.setPredicate(item -> true);
            }
        });

        editor.textProperty().addListener((obs, oldText, newText) -> {
            T selected = comboBox.getValue();
            if (selected != null && label.apply(selected).equals(newText)) {
                return; // chữ do ComboBox tự điền khi chọn 1 mục -> không lọc lại
            }
            String keyword = removeAccent(newText);
            filtered.setPredicate(item -> keyword.isEmpty() || removeAccent(label.apply(item)).contains(keyword));
            if (editor.isFocused() && !keyword.isEmpty() && !filtered.isEmpty() && !comboBox.isShowing()) {
                comboBox.show();
            }
        });
    }

    /**
     * Mục người dùng đang chọn/gõ trong ô, lấy theo chữ đang hiện trong ô.
     * Trả về null nếu chữ đó không khớp mục nào (VD: gõ dở hoặc gõ sai tên).
     */
    private <T> T getChosen(ComboBox<T> comboBox) {
        T value = comboBox.getValue();
        String text = comboBox.getEditor().getText();
        if (value != null && comboBox.getConverter().toString(value).equals(text)) {
            return value;
        }
        return comboBox.getConverter().fromString(text);
    }

    /** Xóa lựa chọn và cả chữ đã gõ trong ô. */
    private <T> void clearChoice(ComboBox<T> comboBox) {
        comboBox.setValue(null);
        comboBox.getEditor().clear();
    }

    /** Bỏ dấu tiếng Việt và đưa về chữ thường: "Nhà Cung Cấp" -> "nha cung cap". */
    private String removeAccent(String text) {
        if (text == null) {
            return "";
        }
        String temp = Normalizer.normalize(text.trim().toLowerCase(), Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd');
    }

    // =====================================================================
    // Tiện ích hiển thị
    // =====================================================================

    /** Hiện lỗi DB kèm nguyên nhân gốc (SQLException) và in stack trace ra console để dễ tìm lỗi. */
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