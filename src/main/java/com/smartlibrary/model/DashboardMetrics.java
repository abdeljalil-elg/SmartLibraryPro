package com.smartlibrary.model;

public record DashboardMetrics(
        int totalBooks,
        int availableBooks,
        int unavailableBooks,
        int activeLoans,
        int lateLoans,
        int totalCopies,
        int borrowedCopies
) {
    public double availabilityRate() {
        if (totalBooks == 0) {
            return 0;
        }
        return availableBooks / (double) totalBooks;
    }
}
