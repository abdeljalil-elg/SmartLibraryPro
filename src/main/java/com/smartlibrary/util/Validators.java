package com.smartlibrary.util;

public final class Validators {
    private Validators() {
    }

    public static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static String required(String value, String field) {
        if (blank(value)) {
            throw new IllegalArgumentException("Le champ \"" + field + "\" est obligatoire.");
        }
        return value.trim();
    }

    public static int year(String value) {
        if (blank(value)) {
            return 0;
        }
        try {
            int year = Integer.parseInt(value.trim());
            if (year < 1000 || year > 2100) {
                throw new IllegalArgumentException("L'année doit être comprise entre 1000 et 2100.");
            }
            return year;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("L'année doit être numérique.");
        }
    }
}
