package com.hkust.marketplace.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * central navigation helper
 * loads fxml screens and remembers the primary stage
 */
public class SceneManager {

    private static Stage primaryStage;
    private static final String FXML_BASE = "/fxml/";

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * opens a scene without giving anything to its controller
     */
    public static void switchScene(String fxmlName) {
        load(fxmlName, null);
    }

    /**
     * opens a scene and hands the controller to the initializer
     * useful when a screen needs an id before it can render
     */
    public static <T> void switchScene(String fxmlName, Consumer<T> initializer) {
        load(fxmlName, initializer);
    }

    // both public methods end up here so the loading code only lives once
    @SuppressWarnings("unchecked")
    private static <T> void load(String fxmlName, Consumer<T> initializer) {
        try {
            if (primaryStage == null) {
                throw new IllegalStateException("SceneManager not initialized – primaryStage is null");
            }

            // find the fxml next to the other resources
            URL resource = SceneManager.class.getResource(FXML_BASE + fxmlName);
            if (resource == null) {
                throw new IOException("FXML resource not found on classpath: " + FXML_BASE + fxmlName);
            }

            System.out.println("[SceneManager] Loading: " + resource);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            // reuse the scene so the stylesheet sticks around
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1200, 780);
                URL css = SceneManager.class.getResource("/css/styles.css");
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            // let the caller pass an id or other setup to the controller
            if (initializer != null) {
                T controller = (T) loader.getController();
                if (controller != null) {
                    initializer.accept(controller);
                }
            }
            System.out.println("[SceneManager] Switched to: " + fxmlName);
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            String msg = "Failed to load: " + fxmlName + "\n\n" + cause.getClass().getSimpleName()
                    + ": " + cause.getMessage();
            System.err.println("[SceneManager] " + msg);

            // show a dialog so the error is visible without reading the console
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Navigation Error");
                alert.setHeaderText("Could not open screen");
                alert.setContentText(msg);
                alert.showAndWait();
            } catch (Exception ignored) {
                // if even the alert fails we still printed to console
            }
            throw new RuntimeException(msg, e);
        }
    }

    public static void showWelcome() {
        switchScene("WelcomeView.fxml");
    }

    public static void showBuyerLogin() {
        switchScene("buyer/BuyerLoginView.fxml");
    }

    public static void showBuyerRegister() {
        switchScene("buyer/BuyerRegisterView.fxml");
    }

    public static void showBuyerDashboard() {
        switchScene("buyer/BuyerDashboardView.fxml");
    }

    public static void showSellerLogin() {
        switchScene("seller/SellerLoginView.fxml");
    }

    public static void showSellerRegister() {
        switchScene("seller/SellerRegisterView.fxml");
    }

    public static void showSellerDashboard() {
        switchScene("seller/SellerDashboardView.fxml");
    }

    public static void showAdminLogin() {
        switchScene("admin/AdminLoginView.fxml");
    }

    public static void showAdminDashboard() {
        switchScene("admin/AdminDashboardView.fxml");
    }
}
