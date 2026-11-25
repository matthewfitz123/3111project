package com.hkust.marketplace;

import com.hkust.marketplace.data.PersistenceService;
import com.hkust.marketplace.services.BorrowScheduler;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * HKUST Marketplace - Main Application Entry Point
 * Frontend-only demo / skeleton for student development.
 * -
 * This application provides a complete UI skeleton for the Buyer, Seller and Admin portals.
 * Data is held in memory while the app runs and saved to the local SQLite database
 * on every store change and again when the app closes.
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

        // last chance save if the jvm is stopped without a normal window close
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                saveSafely()));
    }

    @Override
    public void stop() {
        BorrowScheduler.shutdown();
        saveSafely();
    }

    private static void saveSafely() {
        try {
            PersistenceService.save(DummyDataService.getInstance());
        } catch (RuntimeException ex) {
            System.err.println("[app] could not save the database: " + ex.getMessage());
        }
    }
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch(args);
    }
}
