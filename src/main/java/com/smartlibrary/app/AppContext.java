package com.smartlibrary.app;

import com.smartlibrary.config.UserPreferences;
import com.smartlibrary.controller.MainController;
import com.smartlibrary.model.NotificationType;
import com.smartlibrary.service.CsvExportService;
import com.smartlibrary.service.LibraryService;
import com.smartlibrary.service.NotificationCenter;

public final class AppContext {
    private static final AppContext INSTANCE = new AppContext();

    private final LibraryService libraryService = new LibraryService();
    private final CsvExportService csvExportService = new CsvExportService();
    private final NotificationCenter notificationCenter = new NotificationCenter();
    private final UserPreferences userPreferences = new UserPreferences();
    private MainController mainController;

    private AppContext() {
    }

    public static AppContext get() {
        return INSTANCE;
    }

    public LibraryService library() {
        return libraryService;
    }

    public CsvExportService exporter() {
        return csvExportService;
    }

    public NotificationCenter notifications() {
        return notificationCenter;
    }

    public UserPreferences preferences() {
        return userPreferences;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void notify(NotificationType type, String title, String message) {
        notificationCenter.add(type, title, message);
        if (mainController != null) {
            mainController.showToast(type, title, message);
        }
    }
}
