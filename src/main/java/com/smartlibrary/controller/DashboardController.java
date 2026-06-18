package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.model.DashboardMetrics;
import com.smartlibrary.model.NotificationType;
import com.smartlibrary.util.Animations;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

import java.sql.SQLException;
import java.util.Map;

public class DashboardController {
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label unavailableBooksLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label lateLoansLabel;
    @FXML private Label stockLabel;
    @FXML private ProgressBar stockProgressBar;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private PieChart loanStatusPieChart;
    @FXML private BarChart<String, Number> categoryBarChart;
    @FXML private LineChart<String, Number> monthlyLoansLineChart;
    @FXML private ListView<String> popularBooksList;

    @FXML
    private void initialize() {
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        loadingIndicator.setVisible(true);
        try {
            DashboardMetrics metrics = AppContext.get().library().getMetrics();
            boolean animations = AppContext.get().preferences().animationsEnabled();
            Animations.animateCounter(totalBooksLabel, metrics.totalBooks(), animations);
            Animations.animateCounter(availableBooksLabel, metrics.availableBooks(), animations);
            Animations.animateCounter(unavailableBooksLabel, metrics.unavailableBooks(), animations);
            Animations.animateCounter(activeLoansLabel, metrics.activeLoans(), animations);
            Animations.animateCounter(lateLoansLabel, metrics.lateLoans(), animations);
            stockLabel.setText(metrics.borrowedCopies() + " / " + metrics.totalCopies() + " exemplaires empruntés");
            stockProgressBar.setProgress(metrics.totalCopies() == 0 ? 0 : metrics.borrowedCopies() / (double) metrics.totalCopies());
            fillPieChart(AppContext.get().library().getLoansByStatus());
            fillBarChart(AppContext.get().library().getBooksByCategory());
            fillLineChart(AppContext.get().library().getMonthlyLoans());
            popularBooksList.setItems(FXCollections.observableArrayList(AppContext.get().library().getPopularBooks(6)));
        } catch (SQLException exception) {
            AppContext.get().notify(NotificationType.ERROR, "Base de données indisponible",
                    "Importez database/init_db.sql puis vérifiez database.properties.");
            clearDashboard();
        } finally {
            loadingIndicator.setVisible(false);
        }
    }

    private void fillPieChart(Map<String, Integer> data) {
        loanStatusPieChart.getData().setAll(data.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .toList());
    }

    private void fillBarChart(Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Livres");
        data.forEach((category, value) -> series.getData().add(new XYChart.Data<>(category, value)));
        categoryBarChart.getData().setAll(series);
    }

    private void fillLineChart(Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Emprunts");
        data.forEach((month, value) -> series.getData().add(new XYChart.Data<>(month, value)));
        monthlyLoansLineChart.getData().setAll(series);
    }

    private void clearDashboard() {
        totalBooksLabel.setText("0");
        availableBooksLabel.setText("0");
        unavailableBooksLabel.setText("0");
        activeLoansLabel.setText("0");
        lateLoansLabel.setText("0");
        stockLabel.setText("Aucune donnée disponible");
        stockProgressBar.setProgress(0);
        loanStatusPieChart.getData().clear();
        categoryBarChart.getData().clear();
        monthlyLoansLineChart.getData().clear();
        popularBooksList.getItems().clear();
    }
}
