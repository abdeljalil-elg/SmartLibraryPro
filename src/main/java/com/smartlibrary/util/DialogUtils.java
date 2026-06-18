package com.smartlibrary.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class DialogUtils {
    private DialogUtils() {
    }

    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void error(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static void warning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    public static void aboutDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("À propos de SmartLibrary Pro");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        VBox content = new VBox(10,
                new Label("SmartLibrary Pro"),
                new Label("Application JavaFX MVC connectée à MySQL."),
                new Label("Gestion professionnelle des livres, emprunts, statistiques et préférences."));
        content.setPrefWidth(420);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
