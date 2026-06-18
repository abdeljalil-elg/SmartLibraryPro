package com.smartlibrary.service;

import com.smartlibrary.model.NotificationItem;
import com.smartlibrary.model.NotificationType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificationCenter {
    private final ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();

    public ObservableList<NotificationItem> notifications() {
        return notifications;
    }

    public NotificationItem add(NotificationType type, String title, String message) {
        NotificationItem item = new NotificationItem(type, title, message);
        notifications.add(0, item);
        return item;
    }

    public void clear() {
        notifications.clear();
    }
}
