package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.model.NotificationItem;
import com.smartlibrary.model.NotificationType;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class NotificationsController {
    @FXML private ListView<NotificationItem> notificationsListView;

    @FXML
    private void initialize() {
        notificationsListView.setItems(AppContext.get().notifications().notifications());
        notificationsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                getStyleClass().removeAll("notification-success", "notification-error", "notification-warning", "notification-info");
                if (!empty && item != null) {
                    getStyleClass().add("notification-" + item.getType().name().toLowerCase());
                }
            }
        });
    }

    @FXML
    private void clearNotifications() {
        AppContext.get().notifications().clear();
    }

    @FXML
    private void demoSuccess() {
        AppContext.get().notify(NotificationType.SUCCESS, "Opération réussie", "Le toast de succès est animé.");
    }

    @FXML
    private void demoWarning() {
        AppContext.get().notify(NotificationType.WARNING, "Attention", "Un emprunt approche de sa date limite.");
    }

    @FXML
    private void demoError() {
        AppContext.get().notify(NotificationType.ERROR, "Erreur simulée", "Message d'erreur affiché proprement.");
    }

    @FXML
    private void demoInfo() {
        AppContext.get().notify(NotificationType.INFO, "Information", "Le centre de notifications conserve l'historique.");
    }
}
