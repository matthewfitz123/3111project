package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.services.DealHistoryService;
import com.hkust.marketplace.utils.DummyDataService;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * admin all-deals record (task 3.5)
 * title, buyer, seller, dates, type and status, with search and filters
 */
public class AllDealsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Button clearButton;
    @FXML private Label countLabel;
    @FXML private TableView<Deal> dealTable;

    private List<Deal> allDeals = List.of();

    @FXML
    public void initialize() {
        allDeals = DealHistoryService.allDealsForAdmin();

        categoryFilter.setItems(FXCollections.observableArrayList("All Categories"));
        categoryFilter.getItems().addAll(DummyDataService.CATEGORIES);
        categoryFilter.getSelectionModel().selectFirst();

        typeFilter.setItems(FXCollections.observableArrayList("All Types",
                DealHistoryService.adminTypeLabel(ListingType.SELL),
                DealHistoryService.adminTypeLabel(ListingType.LEND),
                DealHistoryService.adminTypeLabel(ListingType.GIVEAWAY)));
        typeFilter.getSelectionModel().selectFirst();

        statusFilter.setItems(FXCollections.observableArrayList("All Statuses"));
        statusFilter.getItems().addAll(Arrays.stream(DealStatus.values())
                .map(DealHistoryService::statusLabel).toList());
        statusFilter.getSelectionModel().selectFirst();

        fromDate.valueProperty().addListener((obs, oldDate, newDate) -> refresh());
        toDate.valueProperty().addListener((obs, oldDate, newDate) -> refresh());
        categoryFilter.setOnAction(e -> refresh());
        typeFilter.setOnAction(e -> refresh());
        statusFilter.setOnAction(e -> refresh());
        searchField.textProperty().addListener((obs, oldText, newText) -> refresh());
        clearButton.setOnAction(e -> clearFilters());

        dealTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        addColumn("Item Title", deal -> text(deal.getListingTitle()));
        addColumn("Buyer Username", deal -> text(deal.getBuyerUsername()));
        addColumn("Seller Username", deal -> text(deal.getSellerUsername()));
        addColumn("Type", DealHistoryService::adminTypeLabel);
        addColumn("Start Date", deal -> format(DealHistoryService.startDate(deal)));
        addColumn("Complete/Return Date", deal -> format(DealHistoryService.endDate(deal)));
        addColumn("Status", DealHistoryService::statusLabel);

        refresh();
    }

    private void refresh() {
        List<Deal> filtered = allDeals.stream()
                .filter(deal -> DealHistoryService.matches(deal,
                        fromDate.getValue(), toDate.getValue(), categoryFilter.getValue(),
                        typeFilter.getValue(), statusFilter.getValue(), searchField.getText(), true))
                .toList();
        dealTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " of " + allDeals.size() + " deal(s)");
        dealTable.setPlaceholder(new Label(allDeals.isEmpty()
                ? "No deals yet."
                : "No deals match these filters."));
    }

    private void clearFilters() {
        fromDate.setValue(null);
        toDate.setValue(null);
        categoryFilter.getSelectionModel().selectFirst();
        typeFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
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
