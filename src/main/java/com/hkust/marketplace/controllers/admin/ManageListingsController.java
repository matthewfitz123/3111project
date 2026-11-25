package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.services.NotificationService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * admin manage-all-listings screen (step 27)
 * embedded inside the admin dashboard content pane
 * admins can edit any field, add listings on behalf of a seller,
 * and remove listings with a required reason
 */
public class ManageListingsController {

    @FXML private VBox listingsContainer;

    @FXML
    public void initialize() {
        render();
    }

    private void render() {
        listingsContainer.getChildren().clear();

        Button addBtn = new Button("+ Add Listing (on behalf of seller)");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showListingForm(null));
        listingsContainer.getChildren().add(addBtn);

        List<Listing> all = new ArrayList<>(DummyDataService.getInstance().getAllListings());
        all.sort(Comparator.comparing(Listing::getPostedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        TableView<Listing> table = new TableView<>(FXCollections.observableArrayList(all));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No listings yet."));

        addColumn(table, "Title", Listing::getTitle);
        addColumn(table, "Seller", l -> l.getSellerFullName() + " (@" + l.getSellerUsername() + ")");
        addColumn(table, "Category", Listing::getCategory);
        addColumn(table, "Type", Listing::getTypeDisplay);
        addColumn(table, "Status", l -> l.getStatus().name().replace('_', ' '));
        addColumn(table, "Price", Listing::getPriceDisplay);
        addColumn(table, "Views", l -> String.valueOf(l.getViewCount()));
        addColumn(table, "Posted", l -> l.getPostedAt() == null ? ""
                : l.getPostedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        TableColumn<Listing, Node> actions = new TableColumn<>("Actions");
        actions.setCellValueFactory(c -> new SimpleObjectProperty<>(actionButtons(c.getValue())));
        actions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Node item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : item);
            }
        });
        table.getColumns().add(actions);

        listingsContainer.getChildren().add(table);
    }

    private void addColumn(TableView<Listing> table, String header, Function<Listing, String> getter) {
        TableColumn<Listing, String> col = new TableColumn<>(header);
        col.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        table.getColumns().add(col);
    }

    private Node actionButtons(Listing l) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Button edit = new Button("Edit");
        edit.getStyleClass().addAll("btn-outline", "btn-sm");
        edit.setOnAction(e -> showListingForm(l));

        Button remove = new Button("Remove");
        remove.getStyleClass().addAll("btn-danger", "btn-sm");
        remove.setDisable(l.getStatus() == ListingStatus.REMOVED);
        remove.setOnAction(e -> removeListing(l));

        box.getChildren().addAll(edit, remove);
        return box;
    }

    /**
     * one form for both "add on behalf of seller" (existing == null)
     * and editing any field of an existing listing
     */
    private void showListingForm(Listing existing) {
        if (!requireAdmin()) {
            return;
        }
        boolean isAdd = existing == null;
        DummyDataService data = DummyDataService.getInstance();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isAdd ? "Add Listing" : "Edit Listing");
        dialog.setHeaderText(isAdd ? "Create a listing on behalf of a seller"
                : "Edit: " + existing.getTitle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        List<User> sellers = data.getUsersByRole(UserRole.SELLER);
        ComboBox<User> sellerBox = new ComboBox<>(FXCollections.observableArrayList(sellers));
        sellerBox.setCellFactory(v -> userListCell());
        sellerBox.setButtonCell(userListCell());
        if (!sellers.isEmpty()) {
            if (isAdd) {
                sellerBox.getSelectionModel().selectFirst();
            } else {
                sellers.stream()
                        .filter(u -> u.getUsername().equalsIgnoreCase(existing.getSellerUsername()))
                        .findFirst()
                        .ifPresent(sellerBox::setValue);
            }
        }

        TextField titleField = new TextField(isAdd ? "" : existing.getTitle());
        TextArea descriptionArea = new TextArea(isAdd ? "" : existing.getDescription());
        descriptionArea.setPrefRowCount(3);

        ComboBox<String> categoryBox = new ComboBox<>(
                FXCollections.observableArrayList(DummyDataService.CATEGORIES));
        categoryBox.setValue(isAdd ? DummyDataService.CATEGORIES.get(0) : existing.getCategory());

        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("Sell", "Lend", "Give Away"));
        typeBox.setValue(isAdd ? "Sell" : typeToLabel(existing.getType()));

        ComboBox<String> conditionBox = new ComboBox<>(
                FXCollections.observableArrayList(DummyDataService.CONDITIONS));
        conditionBox.setValue(isAdd ? DummyDataService.CONDITIONS.get(0) : existing.getCondition());

        TextField priceField = new TextField(isAdd || existing.getPrice() == null
                ? "" : String.format("%.0f", existing.getPrice()));
        TextField daysField = new TextField(isAdd || existing.getLendingDays() == null
                ? "" : String.valueOf(existing.getLendingDays()));

        ComboBox<String> meetupBox = new ComboBox<>(
                FXCollections.observableArrayList(DummyDataService.HKUST_LOCATIONS));
        meetupBox.setValue(isAdd ? DummyDataService.HKUST_LOCATIONS.get(0) : existing.getMeetupLocation());

        ComboBox<ListingStatus> statusBox = new ComboBox<>(
                FXCollections.observableArrayList(ListingStatus.values()));
        statusBox.setValue(isAdd ? ListingStatus.PENDING_REVIEW : existing.getStatus());

        // price only applies to sell, lending days only to lend
        Runnable syncTypeFields = () -> {
            boolean sell = "Sell".equals(typeBox.getValue());
            boolean lend = "Lend".equals(typeBox.getValue());
            priceField.setDisable(!sell);
            daysField.setDisable(!lend);
        };
        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> syncTypeFields.run());
        syncTypeFields.run();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(9);
        int row = 0;
        grid.add(new Label("Seller"), 0, row);
        grid.add(sellerBox, 1, row++);
        grid.add(new Label("Title"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Category"), 0, row);
        grid.add(categoryBox, 1, row++);
        grid.add(new Label("Type"), 0, row);
        grid.add(typeBox, 1, row++);
        grid.add(new Label("Condition"), 0, row);
        grid.add(conditionBox, 1, row++);
        grid.add(new Label("Price (HK$)"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Lending days"), 0, row);
        grid.add(daysField, 1, row++);
        grid.add(new Label("Meet-up"), 0, row);
        grid.add(meetupBox, 1, row++);
        grid.add(new Label("Status"), 0, row);
        grid.add(statusBox, 1, row++);
        grid.add(new Label("Description"), 0, row);
        grid.add(descriptionArea, 1, row);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button != ButtonType.OK) {
                return;
            }
            User seller = sellerBox.getValue();
            String title = titleField.getText() == null ? "" : titleField.getText().trim();
            if (seller == null) {
                new Alert(Alert.AlertType.WARNING, "Pick a seller for this listing.").showAndWait();
                return;
            }
            if (title.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Title cannot be empty.").showAndWait();
                return;
            }
            if (typeBox.getValue() == null || categoryBox.getValue() == null
                    || conditionBox.getValue() == null || meetupBox.getValue() == null
                    || statusBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING,
                        "Pick a type, category, condition, meet-up spot and status.").showAndWait();
                return;
            }

            ListingType type = labelToType(typeBox.getValue());
            Double price = null;
            Integer days = null;
            if (type == ListingType.SELL) {
                price = parsePositiveDouble(priceField.getText());
                if (price == null) {
                    new Alert(Alert.AlertType.WARNING, "Enter a price above zero.").showAndWait();
                    return;
                }
            }
            if (type == ListingType.LEND) {
                days = parsePositiveInt(daysField.getText());
                if (days == null) {
                    new Alert(Alert.AlertType.WARNING, "Enter a lending period above zero.").showAndWait();
                    return;
                }
            }

            Listing l = isAdd ? new Listing() : existing;
            l.setTitle(title);
            l.setDescription(descriptionArea.getText() == null ? "" : descriptionArea.getText().trim());
            l.setCategory(categoryBox.getValue());
            l.setType(type);
            l.setCondition(conditionBox.getValue());
            l.setMeetupLocation(meetupBox.getValue());
            l.setPrice(type == ListingType.SELL ? price : null);
            l.setLendingDays(type == ListingType.LEND ? days : null);
            l.setStatus(statusBox.getValue());
            l.setSellerUsername(seller.getUsername());
            l.setSellerFullName(seller.getFullName());

            if (isAdd) {
                data.addListing(l);
                data.logAudit(actor(), "LISTING_CREATED",
                        "'" + title + "' for @" + seller.getUsername() + " status=" + l.getStatus());
            } else {
                data.updateListing(l);
                data.logAudit(actor(), "LISTING_EDITED",
                        l.getId() + " '" + title + "' status=" + l.getStatus());
            }
            render();
        });
    }

    /**
     * admin removal: requires a reason, keeps the listing as REMOVED
     * and notifies every buyer with a pending offer or deal on it
     */
    private void removeListing(Listing l) {
        if (!requireAdmin()) {
            return;
        }
        DummyDataService data = DummyDataService.getInstance();
        Set<String> buyers = affectedBuyers(l);

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Remove Listing");
        conf.setHeaderText("Remove: " + l.getTitle());
        conf.setContentText("The listing stays in history with status REMOVED."
                + (buyers.isEmpty() ? ""
                : "\n" + buyers.size() + " buyer(s) with pending offers or deals will be notified."));
        if (conf.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // reason is required so keep asking until the admin gives one or cancels
        Optional<String> reason = promptForReason();
        while (reason.isPresent() && reason.get().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Reason is required.").showAndWait();
            reason = promptForReason();
        }
        if (reason.isEmpty()) {
            return;
        }

        l.setStatus(ListingStatus.REMOVED);
        l.setRejectionReason(reason.get());
        l.setReviewedAt(LocalDateTime.now());
        data.updateListing(l);

        NotificationService.listingRemoved(l, reason.get());
        String notice = "'" + l.getTitle() + "' was removed by an admin: " + reason.get();
        for (String buyer : buyers) {
            NotificationService.notify(buyer, "Listing Removed", notice, "System");
        }
        data.logAudit(actor(), "LISTING_REMOVED",
                l.getId() + " '" + l.getTitle() + "' reason='" + reason.get() + "'");
        render();
    }

    /**
     * buyers holding a pending/countered offer or a pending/active deal
     * on this listing — they are the ones affected by a removal
     */
    private Set<String> affectedBuyers(Listing l) {
        DummyDataService data = DummyDataService.getInstance();
        Set<String> buyers = new LinkedHashSet<>();
        for (Offer offer : data.getOffersForListing(l.getId())) {
            if (offer.getStatus() == OfferStatus.PENDING
                    || offer.getStatus() == OfferStatus.COUNTERED) {
                buyers.add(offer.getBuyerUsername());
            }
        }
        for (Deal deal : data.getAllDeals()) {
            if (l.getId().equals(deal.getListingId())
                    && (deal.getStatus() == DealStatus.PENDING
                    || deal.getStatus() == DealStatus.ACTIVE)) {
                buyers.add(deal.getBuyerUsername());
            }
        }
        buyers.remove(l.getSellerUsername());
        return buyers;
    }

    private ListCell<User> userListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getFullName() + " (@" + item.getUsername() + ")");
            }
        };
    }

    private String typeToLabel(ListingType type) {
        return switch (type) {
            case SELL -> "Sell";
            case LEND -> "Lend";
            case GIVEAWAY -> "Give Away";
        };
    }

    private ListingType labelToType(String label) {
        return switch (label) {
            case "Lend" -> ListingType.LEND;
            case "Give Away" -> ListingType.GIVEAWAY;
            default -> ListingType.SELL;
        };
    }

    private boolean requireAdmin() {
        try {
            SessionManager.requireRole(UserRole.ADMIN);
            return true;
        } catch (SecurityException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            return false;
        }
    }

    private String actor() {
        User admin = SessionManager.getCurrentUser();
        return admin == null ? "admin" : admin.getUsername();
    }

    private Optional<String> promptForReason() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Removal Reason");
        dialog.setHeaderText("Please provide a reason for removing this listing");
        dialog.setContentText("Reason:");
        return dialog.showAndWait();
    }

    // tiny parsers so bad input becomes a validation error, not a crash
    private Double parsePositiveDouble(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parsePositiveInt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
