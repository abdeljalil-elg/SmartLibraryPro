package com.smartlibrary.controller;

import com.smartlibrary.util.DialogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class HelpController {
    @FXML private ListView<String> shortcutsListView;

    @FXML
    private void initialize() {
        shortcutsListView.getItems().setAll(
                "Ctrl + D : ouvrir le tableau de bord",
                "Ctrl + L : ouvrir la gestion des livres",
                "Ctrl + E : ouvrir la gestion des emprunts",
                "Ctrl + , : ouvrir les paramètres",
                "Entrée dans la recherche : filtrage instantané automatique"
        );
    }

    @FXML
    private void showSupportDialog() {
        DialogUtils.info("Support", "Pour une démonstration: importez database/init_db.sql, puis lancez mvn javafx:run.");
    }
}
