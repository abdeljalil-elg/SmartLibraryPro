package com.smartlibrary.dao.jdbc;

import com.smartlibrary.config.Database;
import com.smartlibrary.dao.DashboardDao;
import com.smartlibrary.model.DashboardMetrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class JdbcDashboardDao implements DashboardDao {
    @Override
    public DashboardMetrics metrics() throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM livres) AS total_livres,
                    (SELECT COUNT(*) FROM livres WHERE disponible = TRUE) AS livres_disponibles,
                    (SELECT COUNT(*) FROM livres WHERE disponible = FALSE) AS livres_indisponibles,
                    (SELECT COUNT(*) FROM emprunts WHERE statut IN ('ACTIF', 'RETARD')) AS emprunts_actifs,
                    (SELECT COUNT(*) FROM emprunts WHERE statut = 'RETARD') AS retards,
                    (SELECT COALESCE(SUM(quantite), 0) FROM livres) AS exemplaires_disponibles,
                    (SELECT COUNT(*) FROM emprunts WHERE statut IN ('ACTIF', 'RETARD')) AS exemplaires_empruntes
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return new DashboardMetrics(
                        resultSet.getInt("total_livres"),
                        resultSet.getInt("livres_disponibles"),
                        resultSet.getInt("livres_indisponibles"),
                        resultSet.getInt("emprunts_actifs"),
                        resultSet.getInt("retards"),
                        resultSet.getInt("exemplaires_disponibles") + resultSet.getInt("exemplaires_empruntes"),
                        resultSet.getInt("exemplaires_empruntes")
                );
            }
            return new DashboardMetrics(0, 0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public Map<String, Integer> booksByCategory() throws SQLException {
        return readMap("SELECT categorie AS label, COUNT(*) AS value FROM livres GROUP BY categorie ORDER BY value DESC");
    }

    @Override
    public Map<String, Integer> loansByStatus() throws SQLException {
        return readMap("SELECT statut AS label, COUNT(*) AS value FROM emprunts GROUP BY statut ORDER BY value DESC");
    }

    @Override
    public Map<String, Integer> monthlyLoans() throws SQLException {
        String sql = """
                SELECT DATE_FORMAT(date_emprunt, '%Y-%m') AS label, COUNT(*) AS value
                FROM emprunts
                GROUP BY DATE_FORMAT(date_emprunt, '%Y-%m')
                ORDER BY label ASC
                LIMIT 12
                """;
        return readMap(sql);
    }

    @Override
    public List<String> popularBooks(int limit) throws SQLException {
        String sql = "SELECT titre, auteur, nombre_emprunts FROM livres ORDER BY nombre_emprunts DESC, titre ASC LIMIT ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> books = new ArrayList<>();
                while (resultSet.next()) {
                    books.add(resultSet.getString("titre") + " - " + resultSet.getString("auteur")
                            + " (" + resultSet.getInt("nombre_emprunts") + " emprunts)");
                }
                return books;
            }
        }
    }

    private Map<String, Integer> readMap(String sql) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<String, Integer> values = new LinkedHashMap<>();
            while (resultSet.next()) {
                String label = resultSet.getString("label");
                values.put(label == null || label.isBlank() ? "Non classé" : label, resultSet.getInt("value"));
            }
            return values;
        }
    }
}
