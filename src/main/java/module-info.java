module com.lockbox {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.lockbox to javafx.fxml;
    opens com.lockbox.controller to javafx.fxml;

    exports com.lockbox;
    exports com.lockbox.controller;
}