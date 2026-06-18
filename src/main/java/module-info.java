module com.smartlibrary.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires mysql.connector.j;

    exports com.smartlibrary.app;
    exports com.smartlibrary.model;

    opens com.smartlibrary.controller to javafx.fxml;
    opens com.smartlibrary.model to javafx.base;
}
