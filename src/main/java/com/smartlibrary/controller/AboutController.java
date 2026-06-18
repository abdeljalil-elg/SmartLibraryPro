package com.smartlibrary.controller;

import com.smartlibrary.util.DialogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

public class AboutController {
    @FXML private ProgressBar qualityProgressBar;

    @FXML
    private void initialize() {
        qualityProgressBar.setProgress(0.96);
    }

    @FXML
    private void openDialog() {
        DialogUtils.aboutDialog();
    }
}
