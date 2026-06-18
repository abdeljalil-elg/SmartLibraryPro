package com.smartlibrary.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showSplash();
    }

    public static void showSplash() throws IOException {
        Parent root = load("/com/smartlibrary/view/splash.fxml");
        Scene scene = new Scene(root, 960, 620);
        scene.getStylesheets().add(stylesheet());
        AppContext.get().preferences().apply(scene);
        primaryStage.setTitle("SmartLibrary Pro");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showMain() throws IOException {
        Parent root = load("/com/smartlibrary/view/main.fxml");
        Scene scene = new Scene(root, 1320, 820);
        scene.getStylesheets().add(stylesheet());
        AppContext.get().preferences().apply(scene);
        primaryStage.setTitle("SmartLibrary Pro - Bibliothèque intelligente");
        primaryStage.setMinWidth(1120);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private static Parent load(String resource) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(MainApp.class.getResource(resource), resource));
    }

    private static String stylesheet() {
        return Objects.requireNonNull(MainApp.class.getResource("/com/smartlibrary/style/styles.css")).toExternalForm();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
