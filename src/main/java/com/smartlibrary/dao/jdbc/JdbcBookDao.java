package com.smartlibrary.dao.jdbc;

import com.smartlibrary.config.Database;
import com.smartlibrary.dao.BookDao;
import com.smartlibrary.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookDao implements BookDao {
    @Override
    public List<Book> findAll() throws SQLException {
        String sql = "SELECT * FROM livres ORDER BY titre ASC";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (resultSet.next()) {
                books.add(mapBook(resultSet));
            }
            return books;
        }
    }

    @Override
    public Optional<Book> findById(int id) throws SQLException {
        String sql = "SELECT * FROM livres WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBook(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public Book save(Book book) throws SQLException {
        String sql = """
                INSERT INTO livres (titre, auteur, isbn, categorie, editeur, annee_publication,
                                    description, quantite, disponible, nombre_emprunts)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindBook(statement, book);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
            return book;
        }
    }

    @Override
    public void update(Book book) throws SQLException {
        String sql = """
                UPDATE livres
                SET titre = ?, auteur = ?, isbn = ?, categorie = ?, editeur = ?,
                    annee_publication = ?, description = ?, quantite = ?,
                    disponible = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindBook(statement, book);
            statement.setInt(10, book.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM livres WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Book> search(String query, String category, Boolean availableOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM livres WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(titre) LIKE ? OR LOWER(auteur) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(categorie) LIKE ?)");
            String likeQuery = "%" + query.toLowerCase().trim() + "%";
            parameters.add(likeQuery);
            parameters.add(likeQuery);
            parameters.add(likeQuery);
            parameters.add(likeQuery);
        }
        if (category != null && !category.isBlank() && !"Toutes".equalsIgnoreCase(category)) {
            sql.append(" AND categorie = ?");
            parameters.add(category);
        }
        if (availableOnly != null) {
            sql.append(" AND disponible = ?");
            parameters.add(availableOnly);
        }
        sql.append(" ORDER BY titre ASC");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (resultSet.next()) {
                    books.add(mapBook(resultSet));
                }
                return books;
            }
        }
    }

    @Override
    public List<Book> findAvailable() throws SQLException {
        return search("", null, true);
    }

    @Override
    public List<Book> findPopular(int limit) throws SQLException {
        String sql = "SELECT * FROM livres ORDER BY nombre_emprunts DESC, titre ASC LIMIT ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (resultSet.next()) {
                    books.add(mapBook(resultSet));
                }
                return books;
            }
        }
    }

    @Override
    public List<String> findCategories() throws SQLException {
        String sql = "SELECT DISTINCT categorie FROM livres WHERE categorie IS NOT NULL AND categorie <> '' ORDER BY categorie";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(resultSet.getString("categorie"));
            }
            return categories;
        }
    }

    @Override
    public void decrementQuantity(Connection connection, int bookId) throws SQLException {
        int quantity = readQuantityForUpdate(connection, bookId);
        if (quantity <= 0) {
            throw new SQLException("Ce livre n'est plus disponible.");
        }
        int newQuantity = quantity - 1;
        updateQuantity(connection, bookId, newQuantity, true);
    }

    @Override
    public void incrementQuantity(Connection connection, int bookId) throws SQLException {
        int quantity = readQuantityForUpdate(connection, bookId);
        updateQuantity(connection, bookId, quantity + 1, false);
    }

    private int readQuantityForUpdate(Connection connection, int bookId) throws SQLException {
        String sql = "SELECT quantite FROM livres WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("quantite");
                }
                throw new SQLException("Livre introuvable.");
            }
        }
    }

    private void updateQuantity(Connection connection, int bookId, int quantity, boolean countBorrow) throws SQLException {
        String sql = countBorrow
                ? "UPDATE livres SET quantite = ?, disponible = ?, nombre_emprunts = nombre_emprunts + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?"
                : "UPDATE livres SET quantite = ?, disponible = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setBoolean(2, quantity > 0);
            statement.setInt(3, bookId);
            statement.executeUpdate();
        }
    }

    private void bindBook(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, book.getTitle());
        statement.setString(2, book.getAuthor());
        statement.setString(3, book.getIsbn());
        statement.setString(4, book.getCategory());
        statement.setString(5, book.getPublisher());
        statement.setInt(6, book.getPublicationYear());
        statement.setString(7, book.getDescription());
        statement.setInt(8, book.getQuantity());
        statement.setBoolean(9, book.getQuantity() > 0);
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            if (value instanceof Boolean booleanValue) {
                statement.setBoolean(i + 1, booleanValue);
            } else {
                statement.setObject(i + 1, value);
            }
        }
    }

    private Book mapBook(ResultSet resultSet) throws SQLException {
        Book book = new Book();
        book.setId(resultSet.getInt("id"));
        book.setTitle(resultSet.getString("titre"));
        book.setAuthor(resultSet.getString("auteur"));
        book.setIsbn(resultSet.getString("isbn"));
        book.setCategory(resultSet.getString("categorie"));
        book.setPublisher(resultSet.getString("editeur"));
        book.setPublicationYear(resultSet.getInt("annee_publication"));
        book.setDescription(resultSet.getString("description"));
        book.setQuantity(resultSet.getInt("quantite"));
        book.setAvailable(resultSet.getBoolean("disponible"));
        book.setBorrowCount(resultSet.getInt("nombre_emprunts"));
        book.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        book.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return book;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
