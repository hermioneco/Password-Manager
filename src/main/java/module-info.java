module com.lockbox.lockbox {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.lockbox.lockbox to javafx.fxml;
    opens com.lockbox.lockbox.controller to javafx.fxml;

    exports com.lockbox.lockbox;
    exports com.lockbox.lockbox.controller;
    requires spring.boot.autoconfigure;
}