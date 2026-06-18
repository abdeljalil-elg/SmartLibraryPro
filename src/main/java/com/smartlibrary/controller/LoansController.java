package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.model.Book;
import com.smartlibrary.model.Loan;
import com.smartlibrary.model.LoanStatus;
import com.smartlibrary.model.NotificationType;
import com.smartlibrary.util.DialogUtils;
import com.smartlibrary.util.Validators;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class LoansController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Loan> loansTable;
    @FXML private TableColumn<Loan, Number> idColumn;
    @FXML private TableColumn<Loan, String> bookColumn;
    @FXML private TableColumn<Loan, String> borrowerColumn;
    @FXML private TableColumn<Loan, String> dueDateColumn;
    @FXML private TableColumn<Loan, String> statusColumn;
    @FXML private ComboBox<Book> bookComboBox;
    @FXML private TextField borrowerNameField;
    @FXML private TextField borrowerEmailField;
    @FXML private TextField borrowerPhoneField;
    @FXML private DatePicker loanDatePicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private TextArea notesArea;
    @FXML private ProgressIndicator busyIndicator;

    private final ObservableList<Loan> loans = FXCollections.observableArrayList();
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureTable();
        configureForm();
        configureFilters();
        refreshBooks();
        refreshLoans();
    }

    @FXML
    private void onNewLoan() {
        clearForm();
    }

    @FXML
    private void onCreateLoan() {
        try {
            Loan loan = buildLoanFromForm();
            AppContext.get().library().createLoan(loan);
            AppContext.get().notify(NotificationType.SUCCESS, "Emprunt créé",
                    loan.getBorrowerName() + " a emprunté " + selectedBookTitle() + ".");
            refreshBooks();
            refreshLoans();
            clearForm();
        } catch (IllegalArgumentException exception) {
            DialogUtils.warning("Formulaire incomplet", exception.getMessage());
        } catch (SQLException exception) {
            DialogUtils.error("Emprunt impossible", exception.getMessage());
            AppContext.get().notify(NotificationType.ERROR, "Stock ou MySQL", exception.getMessage());
        }
    }

    @FXML
    private void onUpdateLoan() {
        Loan selected = selectedLoan();
        if (selected == null) {
            DialogUtils.warning("Aucun emprunt sélectionné", "Sélectionnez un emprunt dans le tableau.");
            return;
        }
        try {
            Loan edited = buildLoanFromForm();
            edited.setId(selected.getId());
            edited.setStatus(selected.getStatus());
            edited.setReturnDate(selected.getReturnDate());
            AppContext.get().library().updateLoan(selected, edited);
            AppContext.get().notify(NotificationType.SUCCESS, "Emprunt modifié", "Les informations d'emprunt ont été mises à jour.");
            refreshBooks();
            refreshLoans();
        } catch (IllegalArgumentException exception) {
            DialogUtils.warning("Formulaire incomplet", exception.getMessage());
        } catch (SQLException exception) {
            DialogUtils.error("Modification impossible", exception.getMessage());
        }
    }

    @FXML
    private void onReturnLoan() {
        Loan selected = selectedLoan();
        if (selected == null) {
            DialogUtils.warning("Aucun emprunt sélectionné", "Sélectionnez un emprunt actif ou en retard.");
            return;
        }
        if (selected.getStatus() == LoanStatus.RETURNED) {
            DialogUtils.info("Déjà retourné", "Cet emprunt est déjà clôturé.");
            return;
        }
        if (!DialogUtils.confirm("Retour de livre", "Confirmer le retour de \"" + selected.getBookTitle() + "\" ?")) {
            return;
        }
        try {
            AppContext.get().library().returnLoan(selected);
            AppContext.get().notify(NotificationType.SUCCESS, "Livre retourné", "Le stock a été réaugmenté automatiquement.");
            refreshBooks();
            refreshLoans();
            clearForm();
        } catch (SQLException exception) {
            DialogUtils.error("Retour impossible", exception.getMessage());
        }
    }

    @FXML
    private void onDeleteLoan() {
        Loan selected = selectedLoan();
        if (selected == null) {
            DialogUtils.warning("Aucun emprunt sélectionné", "Sélectionnez un emprunt à supprimer.");
            return;
        }
        if (!DialogUtils.confirm("Supprimer l'emprunt", "La suppression restaurera le stock si l'emprunt est ouvert. Continuer ?")) {
            return;
        }
        try {
            AppContext.get().library().deleteLoan(selected);
            AppContext.get().notify(NotificationType.SUCCESS, "Emprunt supprimé", "L'emprunt a été retiré de l'historique.");
            refreshBooks();
            refreshLoans();
            clearForm();
        } catch (SQLException exception) {
            DialogUtils.error("Suppression impossible", exception.getMessage());
        }
    }

    @FXML
    private void onExportLoans() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les emprunts en CSV");
        chooser.setInitialFileName("emprunts-smartlibrary.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        File file = chooser.showSaveDialog(loansTable.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            AppContext.get().exporter().exportLoans(file, new ArrayList<>(loans));
            AppContext.get().notify(NotificationType.SUCCESS, "Export terminé", file.getName() + " a été généré.");
        } catch (Exception exception) {
            DialogUtils.error("Export impossible", exception.getMessage());
        }
    }

    @FXML
    private void refreshLoans() {
        busyIndicator.setVisible(true);
        try {
            loans.setAll(AppContext.get().library().searchLoans(searchField.getText(), selectedStatus()));
        } catch (SQLException exception) {
            loans.clear();
            AppContext.get().notify(NotificationType.ERROR, "Emprunts indisponibles",
                    "Impossible de charger les emprunts depuis MySQL.");
        } finally {
            busyIndicator.setVisible(false);
        }
    }

    private void refreshBooks() {
        try {
            books.setAll(AppContext.get().library().getBooks());
            bookComboBox.setItems(books);
        } catch (SQLException exception) {
            books.clear();
        }
    }

    private void configureTable() {
        loansTable.setItems(loans);
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        bookColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        borrowerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBorrowerName()));
        dueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDueDate())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusLabel()));
        loansTable.getSelectionModel().selectedItemProperty().addListener((observable, oldLoan, newLoan) -> populateForm(newLoan));
    }

    private void configureForm() {
        bookComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Book book) {
                return book == null ? "" : book.getTitle() + " - " + book.getAuthor() + " (" + book.getQuantity() + ")";
            }

            @Override
            public Book fromString(String string) {
                return books.stream().filter(book -> toString(book).equals(string)).findFirst().orElse(null);
            }
        });
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 90, 14));
        loanDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(LocalDate.now().plusDays(durationSpinner.getValue()));
        durationSpinner.valueProperty().addListener((observable, oldValue, newValue) -> updateDueDate());
        loanDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateDueDate());
    }

    private void configureFilters() {
        statusFilter.getItems().setAll("Tous", "Actifs", "Retards", "Retournés");
        statusFilter.setValue("Tous");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshLoans());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshLoans());
    }

    private void updateDueDate() {
        if (loanDatePicker.getValue() != null && durationSpinner.getValue() != null) {
            dueDatePicker.setValue(loanDatePicker.getValue().plusDays(durationSpinner.getValue()));
        }
    }

    private Loan buildLoanFromForm() {
        Book selectedBook = bookComboBox.getValue();
        if (selectedBook == null) {
            throw new IllegalArgumentException("Sélectionnez un livre.");
        }
        if (loanDatePicker.getValue() == null || dueDatePicker.getValue() == null) {
            throw new IllegalArgumentException("Les dates d'emprunt et de retour prévu sont obligatoires.");
        }
        Loan loan = new Loan();
        loan.setBookId(selectedBook.getId());
        loan.setBookTitle(selectedBook.getTitle());
        loan.setBorrowerName(Validators.required(borrowerNameField.getText(), "Nom de l'emprunteur"));
        loan.setBorrowerEmail(borrowerEmailField.getText());
        loan.setBorrowerPhone(borrowerPhoneField.getText());
        loan.setLoanDate(loanDatePicker.getValue());
        loan.setDueDate(dueDatePicker.getValue());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNotes(notesArea.getText());
        return loan;
    }

    private void populateForm(Loan loan) {
        if (loan == null) {
            return;
        }
        bookComboBox.setValue(books.stream()
                .filter(book -> book.getId() == loan.getBookId())
                .findFirst()
                .orElse(null));
        borrowerNameField.setText(loan.getBorrowerName());
        borrowerEmailField.setText(loan.getBorrowerEmail());
        borrowerPhoneField.setText(loan.getBorrowerPhone());
        loanDatePicker.setValue(loan.getLoanDate());
        dueDatePicker.setValue(loan.getDueDate());
        notesArea.setText(loan.getNotes());
    }

    private void clearForm() {
        loansTable.getSelectionModel().clearSelection();
        bookComboBox.setValue(null);
        borrowerNameField.clear();
        borrowerEmailField.clear();
        borrowerPhoneField.clear();
        loanDatePicker.setValue(LocalDate.now());
        durationSpinner.getValueFactory().setValue(14);
        dueDatePicker.setValue(LocalDate.now().plusDays(14));
        notesArea.clear();
    }

    private LoanStatus selectedStatus() {
        String value = statusFilter.getValue();
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "Actifs" -> LoanStatus.ACTIVE;
            case "Retards" -> LoanStatus.LATE;
            case "Retournés" -> LoanStatus.RETURNED;
            default -> null;
        };
    }

    private String selectedBookTitle() {
        Book book = bookComboBox.getValue();
        return book == null ? "le livre" : book.getTitle();
    }

    private Loan selectedLoan() {
        return loansTable.getSelectionModel().getSelectedItem();
    }
}
