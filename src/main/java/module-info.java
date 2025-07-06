module com.example.chronopanthers {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires java.net.http;

    opens com.example.chronopanthers to javafx.fxml;
    exports com.example.chronopanthers;
}