package com.smartlibrary.dao.jdbc;

import com.smartlibrary.config.Database;
import com.smartlibrary.dao.LoanDao;
import com.smartlibrary.model.Loan;
import com.smartlibrary.model.LoanStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLoanDao implements LoanDao {
    private static final String SELECT_WITH_BOOK = """
            SELECT e.*, l.titre AS livre_titre
            FROM emprunts e
            LEFT JOIN livres l ON l.id = e.livre_id
            """;

    @Override
    public List<Loan> findAll() throws SQLException {
        String sql = SELECT_WITH_BOOK + " ORDER BY e.created_at DESC";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Loan> loans = new ArrayList<>();
            while (resultSet.next()) {
                loans.add(mapLoan(resultSet));
            }
            return loans;
        }
    }

    @Override
    public Optional<Loan> findById(int id) throws SQLException {
        String sql = SELECT_WITH_BOOK + " WHERE e.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapLoan(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public Loan save(Loan loan) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            return save(connection, loan);
        }
    }

    @Override
    public Loan save(Connection connection, Loan loan) throws SQLException {
        String sql = """
                INSERT INTO emprunts (livre_id, emprunteur_nom, emprunteur_email, emprunteur_telephone,
                                      date_emprunt, date_retour_prevue, date_retour_effective, statut, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindLoan(statement, loan);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    loan.setId(keys.getInt(1));
                }
            }
            return loan;
        }
    }

    @Override
    public void update(Loan loan) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            update(connection, loan);
        }
    }

    @Override
    public void update(Connection connection, Loan loan) throws SQLException {
        String sql = """
                UPDATE emprunts
                SET livre_id = ?, emprunteur_nom = ?, emprunteur_email = ?, emprunteur_telephone = ?,
                    date_emprunt = ?, date_retour_prevue = ?, date_retour_effective = ?,
                    statut = ?, notes = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindLoan(statement, loan);
            statement.setInt(10, loan.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            delete(connection, id);
        }
    }

    @Override
    public void delete(Connection connection, int loanId) throws SQLException {
        String sql = "DELETE FROM emprunts WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, loanId);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Loan> search(String query, LoanStatus status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_WITH_BOOK).append(" WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql.append("""
                     AND (LOWER(e.emprunteur_nom) LIKE ?
                          OR LOWER(e.emprunteur_email) LIKE ?
                          OR LOWER(e.emprunteur_telephone) LIKE ?
                          OR LOWER(l.titre) LIKE ?)
                    """);
            String likeQuery = "%" + query.toLowerCase().trim() + "%";
            parameters.add(likeQuery);
            parameters.add(likeQuery);
            parameters.add(likeQuery);
            parameters.add(likeQuery);
        }
        if (status != null) {
            sql.append(" AND e.statut = ?");
            parameters.add(status.getDatabaseValue());
        }
        sql.append(" ORDER BY e.created_at DESC");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Loan> loans = new ArrayList<>();
                while (resultSet.next()) {
                    loans.add(mapLoan(resultSet));
                }
                return loans;
            }
        }
    }

    @Override
    public void markReturned(Connection connection, int loanId) throws SQLException {
        String sql = """
                UPDATE emprunts
                SET statut = ?, date_retour_effective = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LoanStatus.RETURNED.getDatabaseValue());
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.setInt(3, loanId);
            statement.executeUpdate();
        }
    }

    @Override
    public void refreshLateLoans() throws SQLException {
        String sql = """
                UPDATE emprunts
                SET statut = ?
                WHERE statut = ? AND date_retour_prevue < CURRENT_DATE AND date_retour_effective IS NULL
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LoanStatus.LATE.getDatabaseValue());
            statement.setString(2, LoanStatus.ACTIVE.getDatabaseValue());
            statement.executeUpdate();
        }
    }

    private void bindLoan(PreparedStatement statement, Loan loan) throws SQLException {
        statement.setInt(1, loan.getBookId());
        statement.setString(2, loan.getBorrowerName());
        statement.setString(3, loan.getBorrowerEmail());
        statement.setString(4, loan.getBorrowerPhone());
        statement.setDate(5, Date.valueOf(loan.getLoanDate()));
        statement.setDate(6, Date.valueOf(loan.getDueDate()));
        if (loan.getReturnDate() == null) {
            statement.setDate(7, null);
        } else {
            statement.setDate(7, Date.valueOf(loan.getReturnDate()));
        }
        statement.setString(8, loan.getStatus().getDatabaseValue());
        statement.setString(9, loan.getNotes());
    }

    private Loan mapLoan(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();
        loan.setId(resultSet.getInt("id"));
        loan.setBookId(resultSet.getInt("livre_id"));
        loan.setBookTitle(resultSet.getString("livre_titre"));
        loan.setBorrowerName(resultSet.getString("emprunteur_nom"));
        loan.setBorrowerEmail(resultSet.getString("emprunteur_email"));
        loan.setBorrowerPhone(resultSet.getString("emprunteur_telephone"));
        loan.setLoanDate(toLocalDate(resultSet.getDate("date_emprunt")));
        loan.setDueDate(toLocalDate(resultSet.getDate("date_retour_prevue")));
        loan.setReturnDate(toLocalDate(resultSet.getDate("date_retour_effective")));
        loan.setStatus(LoanStatus.fromDatabase(resultSet.getString("statut")));
        loan.setNotes(resultSet.getString("notes"));
        loan.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        loan.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return loan;
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
