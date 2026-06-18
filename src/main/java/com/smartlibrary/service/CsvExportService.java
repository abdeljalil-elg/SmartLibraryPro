package com.smartlibrary.service;

import com.smartlibrary.model.Book;
import com.smartlibrary.model.Loan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class CsvExportService {
    public void exportBooks(File file, List<Book> books) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("id;titre;auteur;isbn;categorie;editeur;annee;quantite;statut;emprunts");
            writer.newLine();
            for (Book book : books) {
                writer.write(String.join(";",
                        value(book.getId()),
                        csv(book.getTitle()),
                        csv(book.getAuthor()),
                        csv(book.getIsbn()),
                        csv(book.getCategory()),
                        csv(book.getPublisher()),
                        value(book.getPublicationYear()),
                        value(book.getQuantity()),
                        csv(book.getStatusLabel()),
                        value(book.getBorrowCount())));
                writer.newLine();
            }
        }
    }

    public void exportLoans(File file, List<Loan> loans) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("id;livre;emprunteur;email;telephone;date_emprunt;date_retour_prevue;date_retour_effective;statut;notes");
            writer.newLine();
            for (Loan loan : loans) {
                writer.write(String.join(";",
                        value(loan.getId()),
                        csv(loan.getBookTitle()),
                        csv(loan.getBorrowerName()),
                        csv(loan.getBorrowerEmail()),
                        csv(loan.getBorrowerPhone()),
                        csv(loan.getLoanDate()),
                        csv(loan.getDueDate()),
                        csv(loan.getReturnDate()),
                        csv(loan.getStatusLabel()),
                        csv(loan.getNotes())));
                writer.newLine();
            }
        }
    }

    private String value(int number) {
        return Integer.toString(number);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
