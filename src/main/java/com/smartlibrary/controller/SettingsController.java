package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.config.Database;
import com.smartlibrary.config.UserPreferences;
import com.smartlibrary.model.NotificationType;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

public class SettingsController {
    @FXML private ToggleGroup themeGroup;
    @FXML private RadioButton darkRadio;
    @FXML private RadioButton lightRadio;
    @FXML private ColorPicker accentColorPicker;
    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeLabel;
    @FXML private CheckBox animationsCheckBox;
    @FXML private ProgressBar fontPreviewProgressBar;
    @FXML private Label dbUrlLabel;
    @FXML private Label dbUserLabel;
    @FXML private Label connectionStateLabel;

    @FXML
    private void initialize() {
        fontSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateFontPreview());
        loadPreferences();
        testConnection();
    }

    private void loadPreferences() {
        UserPreferences preferences = AppContext.get().preferences();
        darkRadio.setSelected("dark".equals(preferences.getTheme()));
        lightRadio.setSelected("light".equals(preferences.getTheme()));
        accentColorPicker.setValue(Color.web(preferences.getAccentColor()));
        fontSizeSlider.setValue(preferences.getFontSize());
        animationsCheckBox.setSelected(preferences.animationsEnabled());
        dbUrlLabel.setText(Database.config().url());
        dbUserLabel.setText(Database.config().user());
        updateFontPreview();
    }

    @FXML
    private void savePreferences() {
        UserPreferences preferences = AppContext.get().preferences();
        preferences.setTheme(lightRadio.isSelected() ? "light" : "dark");
        preferences.setAccentColor(toHex(accentColorPicker.getValue()));
        preferences.setFontSize(fontSizeSlider.getValue());
        preferences.setAnimationsEnabled(animationsCheckBox.isSelected());
        if (fontSizeLabel.getScene() != null) {
            preferences.apply(fontSizeLabel.getScene());
        }
        AppContext.get().notify(NotificationType.SUCCESS, "Préférences sauvegardées", "Vos réglages seront conservés au prochain lancement.");
    }

    @FXML
    private void resetPreferences() {
        AppContext.get().preferences().reset();
        loadPreferences();
        savePreferences();
    }

    @FXML
    private void testConnection() {
        boolean connected = Database.testConnection();
        connectionStateLabel.setText(connected ? "Connexion MySQL opérationnelle" : "Connexion MySQL indisponible");
        connectionStateLabel.getStyleClass().removeAll("status-online", "status-offline");
        connectionStateLabel.getStyleClass().add(connected ? "status-online" : "status-offline");
    }

    private void updateFontPreview() {
        double value = fontSizeSlider.getValue();
        fontSizeLabel.setText(Math.round(value) + " px");
        fontPreviewProgressBar.setProgress((value - fontSizeSlider.getMin()) / (fontSizeSlider.getMax() - fontSizeSlider.getMin()));
    }

    private String toHex(Color color) {
        int red = (int) Math.round(color.getRed() * 255);
        int green = (int) Math.round(color.getGreen() * 255);
        int blue = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}
