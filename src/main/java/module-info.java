module com.stockmaster {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.prefs;
    requires org.xerial.sqlitejdbc;
    requires org.apache.pdfbox;
    requires org.slf4j;
    requires org.controlsfx.controls;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.stockmaster to javafx.fxml;
    opens com.stockmaster.controller to javafx.fxml;
    opens com.stockmaster.model to javafx.base;
    opens com.stockmaster.auth to javafx.fxml;
    opens com.stockmaster.i18n to java.prefs;

    exports com.stockmaster;
    exports com.stockmaster.controller;
    exports com.stockmaster.model;
    exports com.stockmaster.db;
    exports com.stockmaster.dao;
    exports com.stockmaster.auth;
    exports com.stockmaster.service;
    exports com.stockmaster.hardware;
    exports com.stockmaster.i18n;
}
