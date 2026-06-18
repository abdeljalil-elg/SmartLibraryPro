package com.smartlibrary.config;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

public class UserPreferences {
    private static final String KEY_THEME = "theme";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_FONT_SIZE = "fontSize";
    private static final String KEY_ANIMATIONS = "animations";

    private final Preferences preferences = Preferences.userRoot().node("com.smartlibrary.pro");

    public String getTheme() {
        return preferences.get(KEY_THEME, "dark");
    }

    public void setTheme(String theme) {
        preferences.put(KEY_THEME, theme);
    }

    public String getAccentColor() {
        return preferences.get(KEY_ACCENT, "#4F7CFF");
    }

    public void setAccentColor(String accentColor) {
        preferences.put(KEY_ACCENT, accentColor);
    }

    public double getFontSize() {
        return preferences.getDouble(KEY_FONT_SIZE, 14.0);
    }

    public void setFontSize(double fontSize) {
        preferences.putDouble(KEY_FONT_SIZE, fontSize);
    }

    public boolean animationsEnabled() {
        return preferences.getBoolean(KEY_ANIMATIONS, true);
    }

    public void setAnimationsEnabled(boolean enabled) {
        preferences.putBoolean(KEY_ANIMATIONS, enabled);
    }

    public void reset() {
        setTheme("dark");
        setAccentColor("#4F7CFF");
        setFontSize(14.0);
        setAnimationsEnabled(true);
    }

    public void apply(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        scene.getRoot().getStyleClass().removeAll("light-theme", "dark-theme");
        scene.getRoot().getStyleClass().add(getTheme().equals("light") ? "light-theme" : "dark-theme");
        scene.getRoot().setStyle("-app-accent: " + getAccentColor() + "; -fx-font-size: " + getFontSize() + "px;");
    }
}
