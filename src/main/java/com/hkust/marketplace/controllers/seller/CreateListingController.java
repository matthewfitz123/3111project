package com.hkust.marketplace.controllers.seller;

import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SceneManager;
import com.hkust.marketplace.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * seller create-listing form (task 2.3)
 * validates fields based on the chosen listing type and stores
 * uploaded photos under /images/uploads/ as classpath-relative paths
 */
public class CreateListingController {

    private static final int MAX_PHOTOS = 6;
    private static final String UPLOADS_DIR = "src/main/resources/images/uploads";
    private static final String CLASSPATH_UPLOADS_DIR = "target/classes/images/uploads";

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> conditionCombo;
    @FXML private TextArea descriptionArea;
    @FXML private Label priceLabel;
    @FXML private TextField priceField;
    @FXML private Label lendingDaysLabel;
    @FXML private TextField lendingDaysField;
    @FXML private ComboBox<String> meetupCombo;
    @FXML private Label photoListLabel;
    @FXML private Label errorLabel;

    private final List<File> selectedPhotos = new ArrayList<>();
    private Runnable onSubmittedCallback;

    /**
     * lets the dashboard decide where to go after a successful submit
     */
    public void setOnSubmittedCallback(Runnable callback) {
        this.onSubmittedCallback = callback;
    }

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Sell", "Lend", "Give Away"));
        categoryCombo.setItems(FXCollections.observableArrayList(DummyDataService.CATEGORIES));
        conditionCombo.setItems(FXCollections.observableArrayList(DummyDataService.CONDITIONS));
        meetupCombo.setItems(FXCollections.observableArrayList(DummyDataService.HKUST_LOCATIONS));

        // price only applies to sell, lending days only to lend
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateTypeFields(newVal));
        updateTypeFields(null);
    }

    private void updateTypeFields(String type) {
        boolean isSell = "Sell".equals(type);
        boolean isLend = "Lend".equals(type);
        priceLabel.setVisible(isSell);
        priceLabel.setManaged(isSell);
        priceField.setDisable(!isSell);
        priceField.setVisible(isSell);
        priceField.setManaged(isSell);
        lendingDaysLabel.setVisible(isLend);
        lendingDaysLabel.setManaged(isLend);
        lendingDaysField.setDisable(!isLend);
        lendingDaysField.setVisible(isLend);
        lendingDaysField.setManaged(isLend);
        if (!isSell) priceField.clear();
        if (!isLend) lendingDaysField.clear();
    }

    @FXML
    private void handleSelectPhotos() {
        hideError();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Photos (up to " + MAX_PHOTOS + ")");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg, *.gif, *.bmp)",
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        List<File> chosen = chooser.showOpenMultipleDialog(SceneManager.getPrimaryStage());
        if (chosen == null || chosen.isEmpty()) {
            return;
        }

        List<File> imageFiles = chosen.stream()
                .filter(this::isImageFile)
                .toList();
        if (imageFiles.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Please select image files only (PNG, JPG, JPEG, GIF, BMP).").showAndWait();
            return;
        }
        if (imageFiles.size() != chosen.size()) {
            new Alert(Alert.AlertType.WARNING,
                    "Non-image files were skipped.").showAndWait();
        }
        if (selectedPhotos.size() + imageFiles.size() > MAX_PHOTOS) {
            new Alert(Alert.AlertType.WARNING,
                    "You can attach at most " + MAX_PHOTOS + " photos.").showAndWait();
            return;
        }
        selectedPhotos.addAll(imageFiles);
        updatePhotoLabel();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg")
                || name.endsWith(".jpeg") || name.endsWith(".gif")
                || name.endsWith(".bmp");
    }

    private void updatePhotoLabel() {
        if (selectedPhotos.isEmpty()) {
            photoListLabel.setText("No photos selected");
        } else {
            StringBuilder sb = new StringBuilder();
            for (File f : selectedPhotos) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(f.getName());
            }
            photoListLabel.setText(sb.toString());
        }
    }

    @FXML
    private void handleSubmit() {
        hideError();

        // only sellers may create listings
        User seller = SessionManager.requireRole(UserRole.SELLER);

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String type = typeCombo.getValue();
        String category = categoryCombo.getValue();
        String condition = conditionCombo.getValue();
        String meetup = meetupCombo.getValue();

        if (type == null) { showError("Please select a listing type."); return; }
        if (title.isEmpty()) { showError("Item name is required."); return; }
        if (category == null) { showError("Please select a category."); return; }
        if (condition == null) { showError("Please select a condition."); return; }
        if (meetup == null) { showError("Please select a meet-up location."); return; }

        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription(descriptionArea.getText() == null ? "" : descriptionArea.getText().trim());
        listing.setCategory(category);
        listing.setCondition(condition);
        listing.setMeetupLocation(meetup);
        listing.setSellerUsername(seller.getUsername());
        listing.setSellerFullName(seller.getFullName());
        // status defaults to PENDING_REVIEW in the Listing constructor

        switch (type) {
            case "Sell" -> {
                listing.setType(ListingType.SELL);
                Double price = parsePrice(priceField.getText());
                if (price == null) {
                    showError("A valid price (positive number) is required for Sell listings.");
                    return;
                }
                listing.setPrice(price);
            }
            case "Lend" -> {
                listing.setType(ListingType.LEND);
                Integer days = parseDays(lendingDaysField.getText());
                if (days == null) {
                    showError("A valid lending duration in days is required for Lend listings.");
                    return;
                }
                listing.setLendingDays(days);
            }
            default -> listing.setType(ListingType.GIVEAWAY);
        }

        // copy photos into the classpath so they can be shown later
        try {
            listing.setPhotoPaths(copyPhotosToUploads());
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to save photos: " + ex.getMessage()).showAndWait();
            return;
        }

        DummyDataService.getInstance().addListing(listing);
        DummyDataService.getInstance().addNotification(new Notification(
                "admin", "New Listing Submitted",
                seller.getFullName() + " submitted '" + listing.getTitle() + "' for review.",
                "System"));

        new Alert(Alert.AlertType.INFORMATION,
                "Listing submitted! Status: Pending Review.\nAdmin will review shortly.")
                .showAndWait();

        Runnable callback = onSubmittedCallback;
        if (callback != null) {
            callback.run();
        } else {
            SceneManager.showSellerDashboard();
        }
    }

    private Double parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            double value = Double.parseDouble(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseDays(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * copies the chosen photos into images/uploads and returns the
     * classpath-relative paths that should be stored on the listing
     */
    private List<String> copyPhotosToUploads() throws IOException {
        List<String> paths = new ArrayList<>();
        if (selectedPhotos.isEmpty()) {
            return paths;
        }

        Path sourceDir = Paths.get(UPLOADS_DIR);
        Files.createDirectories(sourceDir);

        // mirror into target/classes so the running app can load them right away
        Path runtimeDir = Paths.get(CLASSPATH_UPLOADS_DIR);
        boolean mirrorRuntime = Files.isDirectory(runtimeDir.getParent());
        if (mirrorRuntime) {
            Files.createDirectories(runtimeDir);
        }

        long stamp = System.currentTimeMillis();
        int index = 0;
        for (File photo : selectedPhotos) {
            String safeName = photo.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = stamp + "_" + (index++) + "_" + safeName;

            Path target = sourceDir.resolve(fileName);
            Files.copy(photo.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            if (mirrorRuntime) {
                Files.copy(photo.toPath(), runtimeDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            // store classpath-relative path, never the absolute file location
            paths.add("/images/uploads/" + fileName);
        }
        return paths;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
