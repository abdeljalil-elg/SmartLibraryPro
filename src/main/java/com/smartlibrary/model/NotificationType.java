package com.smartlibrary.model;

public enum NotificationType {
    SUCCESS("Succès"),
    ERROR("Erreur"),
    WARNING("Avertissement"),
    INFO("Information");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
