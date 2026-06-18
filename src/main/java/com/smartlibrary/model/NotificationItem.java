package com.smartlibrary.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationItem {
    private final NotificationType type;
    private final String title;
    private final String message;
    private final LocalDateTime createdAt;

    public NotificationItem(NotificationType type, String title, String message) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDisplayTime() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    @Override
    public String toString() {
        return "[" + getDisplayTime() + "] " + type.getLabel() + " - " + title + "\n" + message;
    }
}
