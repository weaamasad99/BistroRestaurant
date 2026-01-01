package client;

import controllers.SubscriberController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SubscriberUI {

    private VBox mainLayout;
    private ClientUI mainUI; 
    private SubscriberController controller; 

    public SubscriberUI(VBox mainLayout, ClientUI mainUI) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.controller = new SubscriberController(mainUI.controller);
    }

    public void start() {
        showLoginScreen();
    }

    /**
     * SCREEN 1: Subscriber Login (Username & Int ID)
     */
    private void showLoginScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Subscriber Login");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblInstruction = new Label("Please enter your credentials:");

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        txtUsername.setMaxWidth(300);

        TextField txtId = new TextField();
        txtId.setPromptText("Subscriber ID (Numbers only)");
        txtId.setMaxWidth(300);

        // Optional: Force the text field to only accept numbers while typing
        txtId.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtId.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Button btnLogin = new Button("Login");
        btnLogin.setPrefWidth(150);
        btnLogin.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        // --- LOGIN ACTION ---
        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText().trim();
            String idString = txtId.getText().trim();

            if (user.isEmpty() || idString.isEmpty()) {
                mainUI.showAlert("Invalid Input", "Please enter both Username and Subscriber ID.");
                return;
            }

            try {
                // Parse ID to Integer
                int subscriberId = Integer.parseInt(idString);

                // 1. Send request to server
                controller.login(user, subscriberId);

                // The screen will ONLY change when ClientController receives "LOGIN_RESPONSE"

            } catch (NumberFormatException ex) {
                mainUI.showAlert("Input Error", "Subscriber ID must be a number.");
            }
        });

        VBox content = new VBox(15, header, lblInstruction, txtUsername, txtId, btnLogin, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    /**
     * SCREEN 2: Dashboard
     */
    public void showDashboardScreen(String username, int id, Runnable onExit) { 
        mainLayout.getChildren().clear();

        Label header = new Label("Welcome, " + username);
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Subscriber #" + id);
        subHeader.setTextFill(Color.GRAY);
        
        Runnable stayHere = () -> showDashboardScreen(username, id, onExit);
        
        Button btnProfile = createOptionButton("My Profile", "👤");
        btnProfile.setStyle("-fx-background-color: #E0F7FA; -fx-border-color: #006064; -fx-font-size: 14px;");
        
        btnProfile.setOnAction(e -> {
            // Check if we have the full user object
            if (mainUI.currentUser != null) {
                SubscriberProfileUI profileScreen = new SubscriberProfileUI(mainLayout, mainUI, stayHere, mainUI.currentUser);
                profileScreen.start();
            } else {
                mainUI.showAlert("Error", "User data not loaded.");
            }
        });

        Button btnReservation = createOptionButton("Make Reservation", "📅");
        btnReservation.setOnAction(e -> {
            // Note: ReservationUI might need updating to accept 'int id' if it uses it
            ReservationUI resUI = new ReservationUI(mainLayout, mainUI, stayHere, username);
            resUI.start();
        });

        Button btnWaitingList = createOptionButton("Enter Waiting List", "⏳");
        btnWaitingList.setOnAction(e -> {
            // Passing int ID formatted as String for now, or update WaitingListUI to take int
            WaitingListUI waitScreen = new WaitingListUI(mainLayout, mainUI, stayHere, String.valueOf(id), false);
            waitScreen.start();
        });

        Button btnIdentify = createOptionButton("Check-In", "📋");
        btnIdentify.setOnAction(e -> {
             // Passing int ID formatted as String for now
            IdentificationUI identifyScreen = new IdentificationUI(mainLayout, mainUI, stayHere, String.valueOf(id));
            identifyScreen.start();
        });

        Button btnHistory = createOptionButton("Order History", "📜");
        btnHistory.setOnAction(e -> {
            controller.getHistory(id);
        });

        Button btnCheckout = createOptionButton("Check Out", "💳");
        btnCheckout.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnCheckout.setOnAction(e -> {
            CheckoutUI checkoutScreen = new CheckoutUI(mainLayout, mainUI, stayHere);
            checkoutScreen.start();
        });

        VBox actionsBox = new VBox(10, btnProfile, btnReservation, btnWaitingList, btnIdentify, btnHistory, new Separator(), btnCheckout);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(15, 0, 0, 0));

        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> { if(onExit != null) onExit.run(); else mainUI.showRoleSelectionScreen(); });

        VBox content = new VBox(15, header, subHeader, actionsBox, btnLogout);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    private Button createOptionButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(250);
        btn.setPrefHeight(40);
        return btn;
    }
}