package com.hkust.marketplace.controllers.buyer;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.services.DealHistoryService;
import com.hkust.marketplace.services.ReviewService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * buyer deal history screen (task 1.10)
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
        addReviewColumn();

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

    private void addReviewColumn() {
        TableColumn<Deal, Void> column = new TableColumn<>("Review");
        column.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button("Leave review");

            {
                button.getStyleClass().addAll("btn-gold", "btn-sm");
                button.setOnAction(e -> showReviewDialog(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                Deal deal = empty ? null : getTableRow().getItem();
                if (deal == null || !canReview(deal)) {
                    setGraphic(deal != null && deal.isRated() ? new Label("Reviewed") : null);
                    return;
                }
                setGraphic(button);
            }
        });
        dealTable.getColumns().add(column);
    }

    private void showReviewDialog(Deal deal) {
        if (deal == null || !canReview(deal)) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rate Seller");
        dialog.setHeaderText("How was your deal for '" + deal.getListingTitle() + "'?");

        ComboBox<Integer> ratingBox = new ComboBox<>(FXCollections.observableArrayList(5, 4, 3, 2, 1));
        ratingBox.setValue(5);
        ratingBox.setButtonCell(starCell());
        ratingBox.setCellFactory(list -> starCell());

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Optional comment for the seller");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);

        VBox content = new VBox(8, new Label("Rating"), ratingBox, new Label("Comment"), commentArea);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType submit = new ButtonType("Submit review", ButtonBar.ButtonData.OK_DONE);
        ButtonType later = new ButtonType("Later", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submit, later);

        if (dialog.showAndWait().orElse(later) != submit) {
            return;
        }
        try {
            ReviewService.submitReview(deal, SessionManager.getCurrentUser(),
                    ratingBox.getValue() == null ? 0 : ratingBox.getValue(), commentArea.getText());
            new Alert(Alert.AlertType.INFORMATION, "Thanks, your review was posted.").showAndWait();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Could not post the review." : ex.getMessage()).showAndWait();
        }
        historyDeals = DealHistoryService.buyerHistory(SessionManager.getCurrentUser().getUsername());
        refresh();
    }

    private static boolean canReview(Deal deal) {
        return !deal.isRated()
                && (deal.getStatus() == DealStatus.COMPLETED || deal.getStatus() == DealStatus.RETURNED);
    }

    private static ListCell<Integer> starCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null
                        : "★".repeat(value) + "☆".repeat(5 - value) + "  (" + value + ")");
            }
        };
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
