module com.example.connect6gameapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.connect6gameapp to javafx.fxml;
    exports com.example.connect6gameapp;
}