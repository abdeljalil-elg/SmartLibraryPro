package com.smartlibrary.model;

public enum LoanStatus {
    ACTIVE("ACTIF", "Actif"),
    RETURNED("RETOURNE", "Retourné"),
    LATE("RETARD", "En retard");

    private final String databaseValue;
    private final String label;

    LoanStatus(String databaseValue, String label) {
        this.databaseValue = databaseValue;
        this.label = label;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public String getLabel() {
        return label;
    }

    public static LoanStatus fromDatabase(String value) {
        if (value == null) {
            return ACTIVE;
        }
        for (LoanStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
