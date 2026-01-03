package client;

import controllers.CasualController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class CancelReservationUI {
	private VBox mainLayout;
    private ClientUI mainUI;       
    private Runnable onBack;      // Action to go back
    private CasualController casualController;

    public CancelReservationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.casualController = new CasualController(mainUI.controller);
    }
    
    public void start() {
        showCancelForm();
    }

    private void showCancelForm() {
        mainLayout.getChildren().clear();

        // --- Header ---
        Label header = new Label("Cancel Reservation");
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
        Button btnCancel = new Button("Cancel");
        btnCancel.setPrefWidth(200);
        btnCancel.setPrefHeight(40);
        btnCancel.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        
        // Navigation: Back to Casual Options
        btnBack.setOnAction(e -> onBack.run());

        // --- Check-In Logic ---
        btnCancel.setOnAction(e -> {
            String bookingId = txtBookingId.getText().trim();
            int userId = mainUI.currentUser.getUserId();

            if (bookingId.isEmpty()) {
                mainUI.showAlert("Error", "Please enter a Confirmation Code.");
            } else {
            	casualController.cancelReservation(bookingId, userId);
            }
        });

        VBox content = new VBox(20, header, instruction, txtBookingId, btnCancel, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(450);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }
}
