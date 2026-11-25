package com.hkust.marketplace.controllers.buyer;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.services.DealHistoryService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * buyer deal history screen (task 1.9)
 * title, seller, date, type and outcome, filtered by date, category and search
 */
public class DealHistoryController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField searchField;
    @FXML private Button clearButton;
    @FXML private Label countLabel;
    @FXML private TableView<Deal> dealTable;

    private List<Deal> historyDeals = List.of();

    @FXML
    public void initialize() {
        User me = SessionManager.getCurrentUser();
        historyDeals = me == null ? List.of() : DealHistoryService.buyerHistory(me.getUsername());

        categoryFilter.setItems(FXCollections.observableArrayList("All Categories"));
        categoryFilter.getItems().addAll(DummyDataService.CATEGORIES);
        categoryFilter.getSelectionModel().selectFirst();

        fromDate.valueProperty().addListener((obs, oldDate, newDate) -> refresh());
        toDate.valueProperty().addListener((obs, oldDate, newDate) -> refresh());
        categoryFilter.setOnAction(e -> refresh());
        searchField.textProperty().addListener((obs, oldText, newText) -> refresh());
        clearButton.setOnAction(e -> clearFilters());

        dealTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        addColumn("Item Title", deal -> text(deal.getListingTitle()));
        addColumn("Seller", DealHistoryService::sellerDisplay);
        addColumn("Category", DealHistoryService::category);
        addColumn("Deal Date", deal -> format(DealHistoryService.historyDate(deal)));
        addColumn("Type", DealHistoryService::typeLabel);
        addColumn("Outcome", DealHistoryService::statusLabel);

        refresh();
    }

    private void refresh() {
        List<Deal> filtered = historyDeals.stream()
                .filter(deal -> DealHistoryService.matches(deal,
                        fromDate.getValue(), toDate.getValue(), categoryFilter.getValue(),
                        null, null, searchField.getText(), false))
                .toList();
        dealTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " of " + historyDeals.size() + " completed deal(s)");
        dealTable.setPlaceholder(new Label(historyDeals.isEmpty()
                ? "No completed deals yet."
                : "No deals match these filters."));
    }

    private void clearFilters() {
        fromDate.setValue(null);
        toDate.setValue(null);
        categoryFilter.getSelectionModel().selectFirst();
        searchField.setText("");
        refresh();
    }

    private void addColumn(String header, Function<Deal, String> getter) {
        TableColumn<Deal, String> column = new TableColumn<>(header);
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
        dealTable.getColumns().add(column);
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String format(LocalDate date) {
        return date == null ? "—" : date.format(DATE_FORMAT);
    }
}
