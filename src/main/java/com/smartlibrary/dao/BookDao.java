package com.smartlibrary.dao;

import com.smartlibrary.model.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface BookDao extends CrudDao<Book> {
    List<Book> search(String query, String category, Boolean availableOnly) throws SQLException;

    List<Book> findAvailable() throws SQLException;

    List<Book> findPopular(int limit) throws SQLException;

    List<String> findCategories() throws SQLException;

    void decrementQuantity(Connection connection, int bookId) throws SQLException;

    void incrementQuantity(Connection connection, int bookId) throws SQLException;
}
