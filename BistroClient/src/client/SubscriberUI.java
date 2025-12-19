package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SubscriberUI {

    private VBox mainLayout;
    private ClientUI mainUI; // Reference back to main to handle navigation

    public SubscriberUI(VBox mainLayout, ClientUI mainUI) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
    }

    public void start() {
        showLoginScreen();
    }

    /**
     * SCREEN 1: Subscriber Login (Username & ID)
     */
    private void showLoginScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Subscriber Login");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblInstruction = new Label("Please enter your credentials:");

        // Username Field
        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        txtUsername.setMaxWidth(300);
        txtUsername.setStyle("-fx-font-size: 14px;");

        // ID Field
        TextField txtId = new TextField();
        txtId.setPromptText("Subscriber ID");
        txtId.setMaxWidth(300);
        txtId.setStyle("-fx-font-size: 14px;");

        Button btnLogin = new Button("Login");
        btnLogin.setPrefWidth(150);
        btnLogin.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");

        // Navigation: Go back to Role Selection
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        // Action: Validate & Login
        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText().trim();
            String id = txtId.getText().trim();

            if (user.isEmpty() || id.isEmpty()) {
                mainUI.showAlert("Invalid Input", "Please enter both Username and Subscriber ID.");
            } else {
                // Future: Send to server for real authentication
                showDashboardScreen(user, id);
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
     * SCREEN 2: Dashboard with Options + History
     */
    private void showDashboardScreen(String username, String id) {
        mainLayout.getChildren().clear();

        Label header = new Label("Welcome, " + username);
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Subscriber #" + id);
        subHeader.setTextFill(Color.GRAY);

        // 1. Make Reservation
        Button btnReservation = createOptionButton("Make Reservation", "📅");
        btnReservation.setOnAction(e -> mainUI.showAlert("Action", "Opening Reservation Form..."));

        // 2. Enter Waiting List
        Button btnWaitingList = createOptionButton("Enter Waiting List", "⏳");
        btnWaitingList.setOnAction(e -> mainUI.showAlert("Action", "Joining Waiting List..."));

        // 3. Identify
        Button btnIdentify = createOptionButton("Identify at Table", "🆔");
        btnIdentify.setOnAction(e -> mainUI.showAlert("Action", "Identifying..."));

        // 4. Order History (Exclusive to Subscriber)
        Button btnHistory = createOptionButton("Order History", "📜");
        btnHistory.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-font-size: 14px; -fx-cursor: hand;");
        btnHistory.setOnAction(e -> mainUI.showAlert("Action", "Fetching History... (To be implemented)"));

        // 5. Check Out
        Button btnCheckout = createOptionButton("Check Out (10% Off)", "💳");
        btnCheckout.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnCheckout.setOnAction(e -> mainUI.showAlert("Action", "Proceeding to Checkout with discount..."));

        VBox actionsBox = new VBox(10, btnReservation, btnWaitingList, btnIdentify, btnHistory, new Separator(), btnCheckout);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(15, 0, 0, 0));

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #ddd; -fx-text-fill: black;");
        btnLogout.setOnAction(e -> mainUI.showRoleSelectionScreen());

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
        btn.setPrefHeight(40); // Slightly smaller height to fit more buttons
        btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 14px; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            if (!text.contains("Check Out") && !text.contains("History")) // Don't override special styles
                btn.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbb; -fx-font-size: 14px; -fx-cursor: hand;");
        });
        btn.setOnMouseExited(e -> {
            if (!text.contains("Check Out") && !text.contains("History"))
                btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 14px; -fx-cursor: hand;");
        });

        return btn;
    }
}