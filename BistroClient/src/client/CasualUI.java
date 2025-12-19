package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CasualUI {

    private VBox mainLayout;
    private ClientUI mainUI; // Reference back to main to handle navigation "Back"

    public CasualUI(VBox mainLayout, ClientUI mainUI) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
    }

    public void start() {
        showPhoneInputScreen();
    }

    /**
     * SCREEN 1: Phone Input
     */
    private void showPhoneInputScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Casual Diner Identification");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblInstruction = new Label("Please enter your phone number to continue:");
        
        TextField txtPhone = new TextField();
        txtPhone.setPromptText("050-0000000");
        txtPhone.setMaxWidth(300);
        txtPhone.setStyle("-fx-font-size: 14px;");

        Button btnContinue = new Button("Continue");
        btnContinue.setPrefWidth(150);
        btnContinue.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        
        // Navigation: Go back to Role Selection in MainUI
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        // Action: Validate
        btnContinue.setOnAction(e -> {
            String phone = txtPhone.getText().trim();
            if (phone.isEmpty() || !phone.matches("\\d+")) { 
                mainUI.showAlert("Invalid Input", "Please enter a valid phone number (digits only).");
            }
            else if (!phone.startsWith("05") || phone.length() != 10) {
            	mainUI.showAlert("Invalid Input", "Please enter a valid phone number");
            } 
            else {
                showOptionsScreen(phone);
            }
        });

        VBox content = new VBox(20, header, lblInstruction, txtPhone, btnContinue, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    /**
     * SCREEN 2: Dashboard with 4 Options
     */
    public void showOptionsScreen(String phoneNumber) {
        mainLayout.getChildren().clear();

        Label header = new Label("Hello");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label subHeader = new Label("What would you like to do?");
        subHeader.setTextFill(Color.GRAY);

        // 1. Make Reservation
        Button btnReservation = createOptionButton("Make Reservation", "📅");
        btnReservation.setOnAction(e -> {
            ReservationUI resUI = new ReservationUI(mainLayout, mainUI, this, phoneNumber);
            resUI.start();
        });

        // 2. Enter Waiting List
        Button btnWaitingList = createOptionButton("Enter Waiting List", "⏳");
        btnWaitingList.setOnAction(e -> mainUI.showAlert("Action", "Joining Waiting List... (To be implemented)"));

        // 3. Identify
        Button btnIdentify = createOptionButton("Check-In", "📋");
        btnIdentify.setOnAction(e -> {
        	Runnable onBack = () -> showOptionsScreen(phoneNumber);
        	
            IdentificationUI identifyUI = new IdentificationUI(mainLayout, mainUI, onBack, phoneNumber);
            identifyUI.start();
        });

        // 4. Check Out
        Button btnCheckout = createOptionButton("Check Out", "💳");
        btnCheckout.setOnAction(e -> mainUI.showAlert("Action", "Proceeding to Checkout... (To be implemented)"));

        VBox actionsBox = new VBox(15, btnReservation, btnWaitingList, btnIdentify, new Separator(), btnCheckout);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(20, 0, 0, 0));

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
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 14px; -fx-cursor: hand;");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbb; -fx-font-size: 14px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 14px; -fx-cursor: hand;"));
        
        return btn;
    }
}