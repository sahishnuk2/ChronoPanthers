module com.example.chronopanthers {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.example.chronopanthers to javafx.fxml;
    exports com.example.chronopanthers;
}