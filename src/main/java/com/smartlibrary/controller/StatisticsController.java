package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.model.DashboardMetrics;
import com.smartlibrary.model.NotificationType;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

import java.sql.SQLException;
import java.util.Map;

public class StatisticsController {
    @FXML private PieChart categoriesPieChart;
    @FXML private BarChart<String, Number> statusBarChart;
    @FXML private LineChart<String, Number> evolutionLineChart;
    @FXML private ListView<String> insightsListView;
    @FXML private ProgressBar availabilityProgressBar;
    @FXML private Label availabilityLabel;

    @FXML
    private void initialize() {
        refreshStatistics();
    }

    @FXML
    private void refreshStatistics() {
        try {
            DashboardMetrics metrics = AppContext.get().library().getMetrics();
            fillPie(AppContext.get().library().getBooksByCategory());
            fillStatusBar(AppContext.get().library().getLoansByStatus());
            fillEvolution(AppContext.get().library().getMonthlyLoans());
            availabilityProgressBar.setProgress(metrics.availabilityRate());
            availabilityLabel.setText(Math.round(metrics.availabilityRate() * 100) + "% de titres disponibles");
            insightsListView.getItems().setAll(
                    "Catalogue: " + metrics.totalBooks() + " titres suivis",
                    "Stock: " + metrics.totalCopies() + " exemplaires au total",
                    "Circulation: " + metrics.borrowedCopies() + " exemplaires actuellement sortis",
                    "Priorité: " + metrics.lateLoans() + " emprunts à relancer"
            );
        } catch (SQLException exception) {
            AppContext.get().notify(NotificationType.ERROR, "Statistiques indisponibles", exception.getMessage());
        }
    }

    private void fillPie(Map<String, Integer> values) {
        categoriesPieChart.getData().setAll(values.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .toList());
    }

    private void fillStatusBar(Map<String, Integer> values) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Emprunts");
        values.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));
        statusBarChart.getData().setAll(series);
    }

    private void fillEvolution(Map<String, Integer> values) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Volume mensuel");
        values.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));
        evolutionLineChart.getData().setAll(series);
    }
}
