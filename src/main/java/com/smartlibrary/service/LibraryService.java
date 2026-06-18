package com.smartlibrary.service;

import com.smartlibrary.config.Database;
import com.smartlibrary.dao.BookDao;
import com.smartlibrary.dao.DashboardDao;
import com.smartlibrary.dao.LoanDao;
import com.smartlibrary.dao.jdbc.JdbcBookDao;
import com.smartlibrary.dao.jdbc.JdbcDashboardDao;
import com.smartlibrary.dao.jdbc.JdbcLoanDao;
import com.smartlibrary.model.Book;
import com.smartlibrary.model.DashboardMetrics;
import com.smartlibrary.model.Loan;
import com.smartlibrary.model.LoanStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LibraryService {
    private final BookDao bookDao = new JdbcBookDao();
    private final LoanDao loanDao = new JdbcLoanDao();
    private final DashboardDao dashboardDao = new JdbcDashboardDao();

    public List<Book> getBooks() throws SQLException {
        return bookDao.findAll();
    }

    public List<Book> searchBooks(String query, String category, Boolean availableOnly) throws SQLException {
        return bookDao.search(query, category, availableOnly);
    }

    public List<Book> getAvailableBooks() throws SQLException {
        return bookDao.findAvailable();
    }

    public List<String> getCategories() throws SQLException {
        return bookDao.findCategories();
    }

    public Book saveBook(Book book) throws SQLException {
        return bookDao.save(book);
    }

    public void updateBook(Book book) throws SQLException {
        bookDao.update(book);
    }

    public void deleteBook(int id) throws SQLException {
        bookDao.delete(id);
    }

    public List<Loan> getLoans() throws SQLException {
        refreshLateLoans();
        return loanDao.findAll();
    }

    public List<Loan> searchLoans(String query, LoanStatus status) throws SQLException {
        refreshLateLoans();
        return loanDao.search(query, status);
    }

    public Loan createLoan(Loan loan) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            try {
                connection.setAutoCommit(false);
                bookDao.decrementQuantity(connection, loan.getBookId());
                loan.setStatus(LoanStatus.ACTIVE);
                Loan savedLoan = loanDao.save(connection, loan);
                connection.commit();
                return savedLoan;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void updateLoan(Loan originalLoan, Loan editedLoan) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (isOpenLoan(originalLoan) && originalLoan.getBookId() != editedLoan.getBookId()) {
                    bookDao.incrementQuantity(connection, originalLoan.getBookId());
                    bookDao.decrementQuantity(connection, editedLoan.getBookId());
                }
                loanDao.update(connection, editedLoan);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void returnLoan(Loan loan) throws SQLException {
        if (loan == null || loan.getStatus() == LoanStatus.RETURNED) {
            return;
        }
        try (Connection connection = Database.getConnection()) {
            try {
                connection.setAutoCommit(false);
                bookDao.incrementQuantity(connection, loan.getBookId());
                loanDao.markReturned(connection, loan.getId());
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void deleteLoan(Loan loan) throws SQLException {
        if (loan == null) {
            return;
        }
        try (Connection connection = Database.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (isOpenLoan(loan)) {
                    bookDao.incrementQuantity(connection, loan.getBookId());
                }
                loanDao.delete(connection, loan.getId());
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public DashboardMetrics getMetrics() throws SQLException {
        refreshLateLoans();
        return dashboardDao.metrics();
    }

    public Map<String, Integer> getBooksByCategory() throws SQLException {
        return dashboardDao.booksByCategory();
    }

    public Map<String, Integer> getLoansByStatus() throws SQLException {
        refreshLateLoans();
        return dashboardDao.loansByStatus();
    }

    public Map<String, Integer> getMonthlyLoans() throws SQLException {
        return dashboardDao.monthlyLoans();
    }

    public List<String> getPopularBooks(int limit) throws SQLException {
        return dashboardDao.popularBooks(limit);
    }

    public void refreshLateLoans() throws SQLException {
        loanDao.refreshLateLoans();
    }

    private boolean isOpenLoan(Loan loan) {
        return loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.LATE;
    }
}
