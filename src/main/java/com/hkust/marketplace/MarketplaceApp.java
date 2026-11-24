package com.hkust.marketplace;

import com.hkust.marketplace.services.BorrowScheduler;
import com.hkust.marketplace.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * HKUST Marketplace - Main Application Entry Point
 * Frontend-only demo / skeleton for student development.
 *
 * This application provides a complete UI skeleton for the Buyer, Seller and Admin portals.
 * All data is in-memory / dummy. Backend integration points are marked with TODO comments.
 */
public class MarketplaceApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        SceneManager.showWelcome();
        primaryStage.setTitle("HKUST Marketplace");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
        BorrowScheduler.start();
    }

    @Override
    public void stop() {
        BorrowScheduler.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
