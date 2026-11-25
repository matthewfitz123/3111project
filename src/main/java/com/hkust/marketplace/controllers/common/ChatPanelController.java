package com.hkust.marketplace.controllers.common;

import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.ChatThread;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.services.NotificationService;
import com.hkust.marketplace.services.OfferService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * private chat panel for one listing (step 14)
 * it creates or finds the thread for this listing and buyer
 * then shows the messages with a search box and a send box
 */
public class ChatPanelController {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM HH:mm");

    @FXML private VBox messageList;
    @FXML private ScrollPane messageScroll;
    @FXML private TextField searchField;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Button makeOfferButton;
    @FXML private Label chatHint;

    private Listing listing;
    private ChatThread thread;
    private boolean canPost;
    private boolean canOffer;
    private String searchKeyword = "";

    @FXML
    public void initialize() {
        // the detail screen calls setListing once it has the real listing
        chatHint.setText("open a listing to start chatting");
        messageField.setDisable(true);
        sendButton.setDisable(true);
        makeOfferButton.setDisable(true);
    }

    /**
     * points the panel at a listing and finds the right thread
     * buyers get their own thread, sellers get the thread a buyer already started
     */
    public void setListing(Listing listing) {
        this.listing = listing;
        this.thread = null;
        this.canPost = false;
        this.canOffer = false;
        this.searchKeyword = "";
        searchField.clear();

        User current = SessionManager.getCurrentUser();
        if (current == null || listing == null) {
            chatHint.setText("sign in to use chat");
            updateInputState();
            refresh();
            return;
        }

        if (current.getRole() == UserRole.BUYER) {
            // buyers always get their own thread for this listing
            thread = DummyDataService.getInstance().getOrCreateThread(
                    listing.getId(), current.getUsername());
            canPost = true;
            // only buyers can offer, and only while the listing is live and not a giveaway
            canOffer = listing.getStatus() == ListingStatus.LIVE
                    && listing.getType() != ListingType.GIVEAWAY;
            chatHint.setText("chatting about '" + listing.getTitle() + "' with @"
                    + thread.getSellerUsername());
        } else if (current.getRole() == UserRole.SELLER
                && listing.getSellerUsername() != null
                && listing.getSellerUsername().equalsIgnoreCase(current.getUsername())) {
            // sellers can only reply to a thread a buyer already started
            for (ChatThread candidate : DummyDataService.getInstance()
                    .getThreadsForUser(current.getUsername())) {
                if (listing.getId().equals(candidate.getListingId())) {
                    thread = candidate;
                    break;
                }
            }
            canPost = thread != null;
            chatHint.setText(canPost
                    ? "replying to @" + thread.getBuyerUsername()
                    : "no buyer has started a chat about this listing yet");
        } else {
            chatHint.setText("only the buyer and seller can post here");
        }

        updateInputState();
        refresh();
    }

    private void updateInputState() {
        messageField.setDisable(!canPost);
        sendButton.setDisable(!canPost);
        makeOfferButton.setDisable(!canOffer);
    }

    /**
     * sends the text in the box and pings the other person
     */
    @FXML
    private void sendMessage() {
        if (!canPost || thread == null) {
            chatHint.setText("you cannot post in this thread");
            return;
        }
        String text = messageField.getText() == null ? "" : messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        User sender = SessionManager.getCurrentUser();
        if (sender == null) {
            return;
        }

        ChatMessage message = new ChatMessage();
        message.setThreadId(thread.getId());
        message.setSenderUsername(sender.getUsername());
        message.setContent(text);
        DummyDataService.getInstance().addChatMessage(message);

        // the other side gets a notification so they know to check chat
        String recipient = sender.getUsername().equalsIgnoreCase(thread.getBuyerUsername())
                ? thread.getSellerUsername() : thread.getBuyerUsername();
        NotificationService.newChatMessage(message, recipient);

        messageField.clear();
        refresh();
    }

    /**
     * formal offer from inside the chat (step 17)
     * reuses OfferService so offer rules stay in one place
     */
    @FXML
    private void makeOffer() {
        if (!canOffer || thread == null) {
            chatHint.setText("offers are only for live listings");
            return;
        }
        User buyer = SessionManager.getCurrentUser();
        if (buyer == null) {
            return;
        }

        if (listing.getType() == ListingType.SELL) {
            TextInputDialog dialog = new TextInputDialog(
                    listing.getPrice() == null ? "" : String.format("%.0f", listing.getPrice()));
            dialog.setTitle("Make an Offer");
            dialog.setHeaderText("Offer for: " + listing.getTitle());
            dialog.setContentText("Your price (HK$):");
            dialog.showAndWait().ifPresent(text -> {
                try {
                    double price = Double.parseDouble(text.trim());
                    submitChatOffer(buyer, price, null, null);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.WARNING, "Enter a valid price.").showAndWait();
                }
            });
            return;
        }

        // LEND offers need a start and end date
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Borrow Request");
        dialog.setHeaderText("Request to borrow: " + listing.getTitle());

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(
                listing.getLendingDays() == null ? 7 : listing.getLendingDays()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Start date"), 0, 0);
        grid.add(startPicker, 1, 0);
        grid.add(new Label("End date"), 0, 1);
        grid.add(endPicker, 1, 1);
        Label hint = new Label("minimum borrow is 5 seconds for the demo, same day is fine");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");
        grid.add(hint, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType send = new ButtonType("Send request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(send, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(button -> {
            if (button == send) {
                submitChatOffer(buyer, null, startPicker.getValue(), endPicker.getValue());
            }
        });
    }

    // creates the offer through the service then posts the linked chat message
    private void submitChatOffer(User buyer, Double price, LocalDate start, LocalDate end) {
        try {
            Offer offer = OfferService.submitOffer(listing, buyer, price, start, end, "Sent via chat");
            ChatMessage message = new ChatMessage();
            message.setThreadId(thread.getId());
            message.setSenderUsername(buyer.getUsername());
            message.setFormalOffer(true);
            message.setOfferId(offer.getId());
            message.setContent(offer.getType() == ListingType.SELL
                    ? "[Offer] HK$" + String.format("%.0f", offer.getOfferPrice())
                    : "[Borrow request] " + offer.getBorrowStart() + " to " + offer.getBorrowEnd());
            DummyDataService.getInstance().addChatMessage(message);
            refresh();
            new Alert(Alert.AlertType.INFORMATION,
                    "Offer sent to @" + thread.getSellerUsername() + ".").showAndWait();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
        }
    }

    /**
     * searches the current thread for a keyword
     */
    @FXML
    private void handleSearch() {
        searchKeyword = searchField.getText() == null ? "" : searchField.getText().trim();
        refresh();
    }

    /**
     * clears the search and shows the whole thread again
     */
    @FXML
    private void clearSearch() {
        searchField.clear();
        searchKeyword = "";
        refresh();
    }

    /**
     * used by the detail screen chat button
     */
    public void focusInput() {
        if (canPost) {
            messageField.requestFocus();
        }
    }

    private void refresh() {
        messageList.getChildren().clear();
        if (thread == null) {
            Label empty = new Label("no messages yet");
            empty.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
            messageList.getChildren().add(empty);
            return;
        }

        List<ChatMessage> messages = DummyDataService.getInstance()
                .getMessagesForThread(thread.getId());
        if (!searchKeyword.isEmpty()) {
            // the store search is global so filter it down to this thread
            messages = DummyDataService.getInstance().searchMessages(searchKeyword).stream()
                    .filter(m -> thread.getId().equals(m.getThreadId()))
                    .toList();
        }

        if (messages.isEmpty()) {
            Label empty = new Label(searchKeyword.isEmpty()
                    ? "no messages yet - say hello"
                    : "nothing matched that search");
            empty.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
            messageList.getChildren().add(empty);
            return;
        }

        for (ChatMessage message : messages) {
            messageList.getChildren().add(buildMessageRow(message));
        }
        // jump to the newest message after the layout pass
        Platform.runLater(() -> messageScroll.setVvalue(1.0));
    }

    private HBox buildMessageRow(ChatMessage message) {
        boolean mine = message.getSenderUsername() != null
                && message.getSenderUsername().equalsIgnoreCase(SessionManager.getCurrentUsername());

        String name = message.getSenderUsername();
        User sender = DummyDataService.getInstance().findUser(name);
        if (sender != null && sender.getFullName() != null && !sender.getFullName().isBlank()) {
            name = sender.getFullName();
        }

        Label header = new Label((mine ? "you" : name) + "  ·  "
                + (message.getSentAt() == null ? "" : TIME_FORMAT.format(message.getSentAt())));
        header.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 10px;");

        // formal offers already carry their own "[Offer]"/"[Borrow request]" label in the content
        Label body = new Label(message.getContent() == null ? "" : message.getContent());
        body.setWrapText(true);
        body.setStyle("-fx-background-color: " + (mine ? "#e8f0f8" : "#f5f7fa") + ";"
                + " -fx-background-radius: 8; -fx-padding: 7 10;"
                + " -fx-text-fill: #1a1a2e; -fx-font-size: 13px;");

        VBox bubble = new VBox(2, header, body);
        bubble.setMaxWidth(430);
        HBox row = new HBox(bubble);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }
}
