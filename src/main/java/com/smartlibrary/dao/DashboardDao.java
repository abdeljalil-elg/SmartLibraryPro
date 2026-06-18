package com.smartlibrary.dao;

import com.smartlibrary.model.DashboardMetrics;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DashboardDao {
    DashboardMetrics metrics() throws SQLException;

    Map<String, Integer> booksByCategory() throws SQLException;

    Map<String, Integer> loansByStatus() throws SQLException;

    Map<String, Integer> monthlyLoans() throws SQLException;

    List<String> popularBooks(int limit) throws SQLException;
}
