package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.app.MainApp;
import com.smartlibrary.config.Database;
import com.smartlibrary.model.NotificationType;
import com.smartlibrary.util.Animations;
import com.smartlibrary.util.DialogUtils;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainController {
    @FXML private BorderPane shell;
    @FXML private StackPane contentPane;
    @FXML private VBox toastContainer;
    @FXML private Label connectionStatusLabel;
    @FXML private Button dashboardButton;
    @FXML private Button booksButton;
    @FXML private Button loansButton;
    @FXML private Button statisticsButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;
    @FXML private Button helpButton;
    @FXML private Button aboutButton;

    private List<Button> navigationButtons;

    @FXML
    private void initialize() {
        AppContext.get().setMainController(this);
        navigationButtons = List.of(dashboardButton, booksButton, loansButton, statisticsButton,
                notificationsButton, settingsButton, helpButton, aboutButton);
        shell.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                AppContext.get().preferences().apply(newScene);
                registerShortcuts();
            }
        });
        refreshConnectionStatus();
        openDashboard();
    }

    @FXML private void openDashboard() { loadPage("dashboard.fxml", dashboardButton); }

    @FXML private void openBooks() { loadPage("books.fxml", booksButton); }

    @FXML private void openLoans() { loadPage("loans.fxml", loansButton); }

    @FXML private void openStatistics() { loadPage("statistics.fxml", statisticsButton); }

    @FXML private void openNotifications() { loadPage("notifications.fxml", notificationsButton); }

    @FXML private void openSettings() { loadPage("settings.fxml", settingsButton); }

    @FXML private void openHelp() { loadPage("help.fxml", helpButton); }

    @FXML private void openAbout() { loadPage("about.fxml", aboutButton); }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    @FXML
    private void onAboutDialog() {
        DialogUtils.aboutDialog();
    }

    @FXML
    private void onRefreshConnection() {
        refreshConnectionStatus();
        showToast(NotificationType.INFO, "Connexion", "État MySQL actualisé.");
    }

    public void showToast(NotificationType type, String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("toast-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("toast-message");

        VBox toast = new VBox(4, titleLabel, messageLabel);
        toast.getStyleClass().addAll("toast", toastClass(type));
        toast.setOpacity(0);
        toast.setTranslateX(24);
        toastContainer.getChildren().add(0, toast);

        if (AppContext.get().preferences().animationsEnabled()) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), toast);
            slideIn.setFromX(24);
            slideIn.setToX(0);
            fadeIn.play();
            slideIn.play();
        } else {
            toast.setOpacity(1);
            toast.setTranslateX(0);
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(done -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        });
        delay.play();
    }

    private void loadPage(String fxml, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    MainApp.class.getResource("/com/smartlibrary/view/" + fxml), fxml));
            Node page = loader.load();
            contentPane.getChildren().setAll(page);
            setActive(activeButton);
            Animations.fadeSlideIn(page, AppContext.get().preferences().animationsEnabled());
        } catch (IOException exception) {
        DialogUtils.error("Navigation impossible", detailedMessage(exception));
    }
    }

    private void setActive(Button activeButton) {
        navigationButtons.forEach(button -> button.getStyleClass().remove("nav-button-active"));
        activeButton.getStyleClass().add("nav-button-active");
    }

    private void refreshConnectionStatus() {
        boolean connected = Database.testConnection();
        connectionStatusLabel.setText(connected ? "MySQL connecté" : "MySQL non connecté");
        connectionStatusLabel.getStyleClass().removeAll("status-online", "status-offline");
        connectionStatusLabel.getStyleClass().add(connected ? "status-online" : "status-offline");
    }

    private String toastClass(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "toast-success";
            case ERROR -> "toast-error";
            case WARNING -> "toast-warning";
            case INFO -> "toast-info";
        };
    }

    private void registerShortcuts() {
        shell.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), this::openDashboard);
        shell.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), this::openBooks);
        shell.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), this::openLoans);
        shell.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN), this::openSettings);
    }
    private String detailedMessage(Throwable throwable) {
        Throwable root = throwable;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        String message = root.getMessage();

        if (message == null || message.isBlank()) {
            message = throwable.getMessage();
        }

        return (message == null || message.isBlank())
                ? root.getClass().getSimpleName()
                : message;
    }
}
