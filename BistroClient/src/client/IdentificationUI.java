package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class IdentificationUI {

    private VBox mainLayout;
    private ClientUI mainUI;       
    private CasualUI parentUI;     
    private String phoneNumber;    

    public IdentificationUI(VBox mainLayout, ClientUI mainUI, CasualUI parentUI, String phoneNumber) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.parentUI = parentUI;
        this.phoneNumber = phoneNumber;
    }

    public void start() {
        showIdentificationForm();
    }

    private void showIdentificationForm() {
        mainLayout.getChildren().clear();

        // --- Header ---
        Label header = new Label("Check-In");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label instruction = new Label("Please enter your Confirmation Code:");
        instruction.setPadding(new Insets(10,0,0,0));

        // --- Form Fields ---
        TextField txtBookingId = new TextField();
        txtBookingId.setPromptText("Enter Confirmation Code");
        txtBookingId.setMaxWidth(300);
        txtBookingId.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        // --- Buttons ---
        Button btnCheckIn = new Button("Get My Table");
        btnCheckIn.setPrefWidth(200);
        btnCheckIn.setPrefHeight(40);
        btnCheckIn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // Scan QR Button (Mockup)
        Button btnScanQR = new Button("📷 Scan QR Code");
        btnScanQR.setPrefWidth(200);
        btnScanQR.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-cursor: hand;");
        btnScanQR.setOnAction(e -> mainUI.showAlert("Camera", "Scanning feature would open camera here..."));

        // Lost Code Button (NEW)
        Button btnLostCode = new Button("❓ Lost Code");
        btnLostCode.setPrefWidth(200);
        btnLostCode.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLostCode.setOnAction(e -> {
             // Logic: Usually this would trigger an SMS to the phone number
             mainUI.showAlert("Retrieving Code", "We are sending the reservation details to: " + phoneNumber);
        });
        
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        
        // Navigation: Back to Casual Options
        btnBack.setOnAction(e -> parentUI.showOptionsScreen(phoneNumber));

        // --- Check-In Logic ---
        btnCheckIn.setOnAction(e -> {
            String bookingId = txtBookingId.getText().trim();

            if (bookingId.isEmpty()) {
                mainUI.showAlert("Error", "Please enter a Confirmation Code.");
            } else {
                // TODO: Send request to Server (CLIENT_IDENTIFY)
                // If server approves -> Show "Your Table is #5"
                // If denied -> Show "Reservation not found or too early."
                
                mainUI.showAlert("Identifying...", 
                    "Sending Request for Confirmation Code: " + bookingId + "\n" +
                    "(Server validation coming next)");
            }
        });

        VBox content = new VBox(20, header, instruction, txtBookingId, btnCheckIn, btnScanQR, btnLostCode, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(450);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }
}