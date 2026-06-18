package com.smartlibrary.controller;

import com.smartlibrary.app.AppContext;
import com.smartlibrary.model.Book;
import com.smartlibrary.model.NotificationType;
import com.smartlibrary.util.Animations;
import com.smartlibrary.util.DialogUtils;
import com.smartlibrary.util.Validators;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BooksController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private CheckBox availableOnlyCheckBox;
    @FXML private ToggleGroup availabilityGroup;
    @FXML private RadioButton allRadio;
    @FXML private RadioButton availableRadio;
    @FXML private RadioButton unavailableRadio;
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Number> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Number> quantityColumn;
    @FXML private TableColumn<Book, String> statusColumn;
    @FXML private Label formTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private ComboBox<String> categoryInput;
    @FXML private TextField publisherField;
    @FXML private TextField yearField;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TextArea descriptionArea;
    @FXML private ProgressBar stockProgressBar;

    private final ObservableList<Book> books = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureTable();
        configureForm();
        configureFilters();
        refreshCategories();
        refreshBooks();
    }

    @FXML
    private void onNewBook() {
        clearForm();
        Animations.pulse(formTitleLabel, AppContext.get().preferences().animationsEnabled());
    }

    @FXML
    private void onSaveBook() {
        try {
            Book book = buildBookFromForm();
            Book selected = selectedBook();
            if (selected == null) {
                AppContext.get().library().saveBook(book);
                AppContext.get().notify(NotificationType.SUCCESS, "Livre ajouté", book.getTitle() + " est maintenant dans le catalogue.");
            } else {
                book.setId(selected.getId());
                AppContext.get().library().updateBook(book);
                AppContext.get().notify(NotificationType.SUCCESS, "Livre modifié", book.getTitle() + " a été mis à jour.");
            }
            refreshCategories();
            refreshBooks();
            clearForm();
        } catch (IllegalArgumentException exception) {
            DialogUtils.warning("Formulaire incomplet", exception.getMessage());
        } catch (SQLException exception) {
            DialogUtils.error("Erreur MySQL", exception.getMessage());
            AppContext.get().notify(NotificationType.ERROR, "Sauvegarde impossible", exception.getMessage());
        }
    }

    @FXML
    private void onDeleteBook() {
        Book selected = selectedBook();
        if (selected == null) {
            DialogUtils.warning("Aucun livre sélectionné", "Sélectionnez un livre dans le tableau.");
            return;
        }
        if (!DialogUtils.confirm("Supprimer le livre", "Voulez-vous supprimer \"" + selected.getTitle() + "\" ?")) {
            return;
        }
        try {
            AppContext.get().library().deleteBook(selected.getId());
            AppContext.get().notify(NotificationType.SUCCESS, "Livre supprimé", selected.getTitle() + " a été retiré du catalogue.");
            refreshBooks();
            clearForm();
        } catch (SQLException exception) {
            DialogUtils.error("Suppression impossible", "Vérifiez qu'aucun emprunt ne référence ce livre.\n" + exception.getMessage());
        }
    }

    @FXML
    private void onExportBooks() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les livres en CSV");
        chooser.setInitialFileName("livres-smartlibrary.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        File file = chooser.showSaveDialog(booksTable.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            AppContext.get().exporter().exportBooks(file, new ArrayList<>(books));
            AppContext.get().notify(NotificationType.SUCCESS, "Export terminé", file.getName() + " a été généré.");
        } catch (Exception exception) {
            DialogUtils.error("Export impossible", exception.getMessage());
        }
    }

    @FXML
    private void refreshBooks() {
        try {
            books.setAll(AppContext.get().library().searchBooks(
                    searchField.getText(),
                    categoryFilter.getValue(),
                    availabilityFilter()));
        } catch (SQLException exception) {
            books.clear();
            AppContext.get().notify(NotificationType.ERROR, "Catalogue indisponible",
                    "Impossible de charger les livres depuis MySQL.");
        }
    }

    private void configureTable() {
        booksTable.setItems(books);
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusLabel()));
        booksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldBook, newBook) -> populateForm(newBook));
    }

    private void configureForm() {
        categoryInput.setEditable(true);
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 1));
        quantitySpinner.valueProperty().addListener((observable, oldValue, newValue) -> updateStockProgress(newValue));
    }

    private void configureFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshBooks());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshBooks());
        availableOnlyCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshBooks());
        availabilityGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> refreshBooks());
        allRadio.setSelected(true);
    }

    private void refreshCategories() {
        try {
            List<String> categories = AppContext.get().library().getCategories();
            categoryFilter.getItems().setAll("Toutes");
            categoryFilter.getItems().addAll(categories);
            categoryFilter.setValue("Toutes");
            categoryInput.getItems().setAll(categories);
        } catch (SQLException exception) {
            categoryFilter.getItems().setAll("Toutes");
            categoryFilter.setValue("Toutes");
        }
    }

    private Boolean availabilityFilter() {
        if (availableOnlyCheckBox.isSelected() || availableRadio.isSelected()) {
            return true;
        }
        if (unavailableRadio.isSelected()) {
            return false;
        }
        return null;
    }

    private Book buildBookFromForm() {
        Book book = new Book();
        book.setTitle(Validators.required(titleField.getText(), "Titre"));
        book.setAuthor(Validators.required(authorField.getText(), "Auteur"));
        book.setIsbn(isbnField.getText());
        book.setCategory(Validators.required(categoryInput.getEditor().getText(), "Catégorie"));
        book.setPublisher(publisherField.getText());
        book.setPublicationYear(Validators.year(yearField.getText()));
        book.setQuantity(quantitySpinner.getValue());
        book.setDescription(descriptionArea.getText());
        book.setAvailable(book.getQuantity() > 0);
        return book;
    }

    private void populateForm(Book book) {
        if (book == null) {
            return;
        }
        formTitleLabel.setText("Modifier le livre");
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        isbnField.setText(book.getIsbn());
        categoryInput.setValue(book.getCategory());
        categoryInput.getEditor().setText(book.getCategory());
        publisherField.setText(book.getPublisher());
        yearField.setText(book.getPublicationYear() == 0 ? "" : Integer.toString(book.getPublicationYear()));
        quantitySpinner.getValueFactory().setValue(book.getQuantity());
        descriptionArea.setText(book.getDescription());
        updateStockProgress(book.getQuantity());
    }

    private void clearForm() {
        booksTable.getSelectionModel().clearSelection();
        formTitleLabel.setText("Nouveau livre");
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        categoryInput.setValue(null);
        categoryInput.getEditor().clear();
        publisherField.clear();
        yearField.clear();
        quantitySpinner.getValueFactory().setValue(1);
        descriptionArea.clear();
        updateStockProgress(1);
    }

    private void updateStockProgress(Number quantity) {
        stockProgressBar.setProgress(Math.min(1.0, quantity.doubleValue() / 10.0));
    }

    private Book selectedBook() {
        return booksTable.getSelectionModel().getSelectedItem();
    }
}
