package com.stockmaster;

import com.stockmaster.db.DBManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize Database
        DBManager.initializeDatabase();

        Parent root = loadFXML("MainLayout");

        scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle("StockMaster - Inventory Management");

        // Load Application Icon (Window Title Bar)
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found: /images/logo.png");
        }

        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        try {
            launch();
        } catch (Throwable e) {
            logError(e);
        }
    }

    private static void logError(Throwable e) {
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get(System.getProperty("user.home"), "StockMaster_Error.log");
            String timestamp = java.time.LocalDateTime.now().toString();
            String errorMsg = timestamp + "\n" + e.toString() + "\n";
            for (StackTraceElement ste : e.getStackTrace()) {
                errorMsg += "\tat " + ste.toString() + "\n";
            }
            errorMsg += "\n--------------------------------------------------\n";
            java.nio.file.Files.write(logPath, errorMsg.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }
}
