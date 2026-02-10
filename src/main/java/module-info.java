module com.stockmaster {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.apache.pdfbox;
    requires org.slf4j;
    requires org.controlsfx.controls;

    opens com.stockmaster to javafx.fxml;
    opens com.stockmaster.controller to javafx.fxml;
    opens com.stockmaster.model to javafx.base;

    exports com.stockmaster;
    exports com.stockmaster.controller;
    exports com.stockmaster.model;
    exports com.stockmaster.db;
    exports com.stockmaster.dao;
}
