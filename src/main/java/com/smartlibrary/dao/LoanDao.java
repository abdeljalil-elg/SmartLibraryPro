package com.smartlibrary.dao;

import com.smartlibrary.model.Loan;
import com.smartlibrary.model.LoanStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface LoanDao extends CrudDao<Loan> {
    List<Loan> search(String query, LoanStatus status) throws SQLException;

    Loan save(Connection connection, Loan loan) throws SQLException;

    void update(Connection connection, Loan loan) throws SQLException;

    void delete(Connection connection, int loanId) throws SQLException;

    void markReturned(Connection connection, int loanId) throws SQLException;

    void refreshLateLoans() throws SQLException;
}
