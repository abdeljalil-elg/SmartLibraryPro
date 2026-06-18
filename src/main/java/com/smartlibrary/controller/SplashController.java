package com.smartlibrary.controller;

import com.smartlibrary.app.MainApp;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

import java.io.IOException;

public class SplashController {
    @FXML private ProgressBar progressBar;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label loadingLabel;

    @FXML
    private void initialize() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0),
                        new KeyValue(progressIndicator.progressProperty(), 0)),
                new KeyFrame(Duration.millis(550), event -> loadingLabel.setText("Connexion au catalogue..."),
                        new KeyValue(progressBar.progressProperty(), 0.35),
                        new KeyValue(progressIndicator.progressProperty(), 0.35)),
                new KeyFrame(Duration.millis(1150), event -> loadingLabel.setText("Préparation du tableau de bord..."),
                        new KeyValue(progressBar.progressProperty(), 0.72),
                        new KeyValue(progressIndicator.progressProperty(), 0.72)),
                new KeyFrame(Duration.millis(1750), event -> loadingLabel.setText("Bienvenue dans SmartLibrary Pro"),
                        new KeyValue(progressBar.progressProperty(), 1),
                        new KeyValue(progressIndicator.progressProperty(), 1))
        );
        timeline.setOnFinished(event -> {
            try {
                MainApp.showMain();
            } catch (IOException exception) {
                throw new IllegalStateException("Impossible de charger l'application principale.", exception);
            }
        });
        timeline.play();
    }
}
