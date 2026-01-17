package client;

import controllers.CasualController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * The CancelReservationUI class represents the user interface that allows
 * a casual client to cancel an existing reservation.
 * It provides a form for entering a confirmation code and handles the UI logic
 * for submitting the cancellation request.
 */
public class CancelReservationUI {
	private VBox mainLayout;
    private ClientUI mainUI;       
    private Runnable onBack;      // Action to go back
    private CasualController casualController;

    /**
     * Constructs a new CancelReservationUI instance.
     *
     * @param mainLayout The main layout container where the UI will be rendered.
     * @param mainUI     The main application instance, used for global state and alerts.
     * @param onBack     A Runnable callback to execute when the user navigates back.
     */
    public CancelReservationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.casualController = new CasualController(mainUI.controller);
    }
    
    /**
     * Initializes and displays the cancellation form.
     */
    public void start() {
        showCancelForm();
    }

    /**
     * Clears the main layout and constructs the visual elements for the cancellation form.
     * This includes the header, input field for the confirmation code, and action buttons.
     */
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

        // --- Cancellation Logic ---
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