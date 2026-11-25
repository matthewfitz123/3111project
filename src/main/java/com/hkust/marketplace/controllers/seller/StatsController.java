package com.hkust.marketplace.controllers.seller;

import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.services.StatsService;
import com.hkust.marketplace.services.StatsService.ListingStat;
import com.hkust.marketplace.services.StatsService.SellerStats;
import com.hkust.marketplace.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * seller stats screen (step 28, task 2.8)
 * shows real totals plus a bar chart per listing and a pie chart of outcomes
 */
public class StatsController {

    @FXML private Label emptyLabel;
    @FXML private HBox metricsBox;
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            render(StatsService.computeForSeller(user));
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Could not load stats." : ex.getMessage()).showAndWait();
        }
    }

    private void render(SellerStats stats) {
        emptyLabel.setVisible(stats.getListingCount() == 0);
        emptyLabel.setManaged(stats.getListingCount() == 0);

        metricsBox.getChildren().addAll(
                metricCard("Listings", String.valueOf(stats.getListingCount()), "#003974"),
                metricCard("Total Views", String.valueOf(stats.getTotalViews()), "#1565c0"),
                metricCard("Offers Received", String.valueOf(stats.getTotalOffers()), "#ed6c02"),
                metricCard("Completed Deals", String.valueOf(stats.getCompletedDeals()), "#2e7d32"),
                metricCard("Avg Rating",
                        stats.getRatingCount() == 0 ? "No reviews"
                                : String.format("%.1f / 5", stats.getAverageRating()),
                        "#A68137"));

        renderBarChart(stats);
        renderPieChart(stats);
    }

    // one category per listing, three series so views/offers/deals compare easily
    private void renderBarChart(SellerStats stats) {
        XYChart.Series<String, Number> views = new XYChart.Series<>();
        views.setName("Views");
        XYChart.Series<String, Number> offers = new XYChart.Series<>();
        offers.setName("Offers");
        XYChart.Series<String, Number> deals = new XYChart.Series<>();
        deals.setName("Deals");

        for (ListingStat row : stats.getPerListing()) {
            String label = row.getTitle() != null && row.getTitle().length() > 24
                    ? row.getTitle().substring(0, 21) + "..." : row.getTitle();
            views.getData().add(new XYChart.Data<>(label, row.getViews()));
            offers.getData().add(new XYChart.Data<>(label, row.getOffers()));
            deals.getData().add(new XYChart.Data<>(label, row.getDeals()));
        }
        // an empty chart still renders, the empty label explains why
        if (stats.getPerListing().isEmpty()) {
            views.getData().add(new XYChart.Data<>("No listings yet", 0));
            offers.getData().add(new XYChart.Data<>("No listings yet", 0));
            deals.getData().add(new XYChart.Data<>("No listings yet", 0));
        }
        barChart.getData().setAll(FXCollections.observableArrayList(views, offers, deals));
    }

    // deal outcomes when deals exist, otherwise the listing status mix
    private void renderPieChart(SellerStats stats) {
        pieChart.getData().clear();
        if (!stats.getDealOutcomes().isEmpty()) {
            pieChart.setTitle("Deal Outcomes");
            for (Map.Entry<DealStatus, Integer> entry : stats.getDealOutcomes().entrySet()) {
                if (entry.getValue() > 0) {
                    pieChart.getData().add(new PieChart.Data(
                            dealStatusLabel(entry.getKey()), entry.getValue()));
                }
            }
            return;
        }
        pieChart.setTitle("Listing Status");
        for (Map.Entry<ListingStatus, Integer> entry : stats.getStatusCounts().entrySet()) {
            if (entry.getValue() > 0) {
                pieChart.getData().add(new PieChart.Data(
                        entry.getKey().name().replace('_', ' '), entry.getValue()));
            }
        }
    }

    private String dealStatusLabel(DealStatus status) {
        return switch (status) {
            case PENDING -> "Pending";
            case ACTIVE -> "Active";
            case COMPLETED -> "Completed";
            case RETURNED -> "Returned";
            case CANCELLED -> "Cancelled";
            case EXPIRED -> "Expired";
        };
    }

    private VBox metricCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setPadding(new Insets(16));
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        val.setWrapText(true);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");
        card.getChildren().addAll(val, lbl);
        return card;
    }
}
