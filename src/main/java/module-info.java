module smg.switchjava {
    requires javafx.controls;
    requires javafx.fxml;

    opens smg.switchjava to javafx.fxml;
    exports smg.switchjava;
}