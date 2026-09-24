module com.team.warehouse.warehousemanagementoop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.team.warehouse.warehousemanagementoop.controller to javafx.fxml;
    exports com.team.warehouse.warehousemanagementoop;
}