package client;

import common.Order;
import controllers.CasualController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import java.util.ArrayList;

/**
 * The IdentificationUI class handles the "Check-In" process for customers arriving at the restaurant.
 * <p>
 * It supports two modes of operation:
 * <ul>
 * <li><b>Subscriber Mode:</b> Fetches and displays a list of today's reservations for the user to select from.</li>
 * <li><b>Casual/Manual Mode:</b> Provides a text field for the user to manually enter their confirmation code.</li>
 * </ul>
 */
public class IdentificationUI {

    private VBox mainLayout;
    private ClientUI mainUI;       
    private Runnable onBack;      
    private String userIdentifier;    
    private CasualController casualController;
    private boolean isSubscriber; 

    // UI Elements
    private VBox contentBox;
    private ComboBox<Order> cmbOrders;
    private TextField txtBookingId;
    private Button btnCheckIn;
    private Label lblInstruction;

    /**
     * Constructs the IdentificationUI instance.
     *
     * @param mainLayout     The main layout container where the UI will be rendered.
     * @param mainUI         The main application instance.
     * @param onBack         A Runnable callback to execute when the user navigates back.
     * @param userIdentifier The identifier for the user (e.g., Phone Number or ID).
     * @param isSubscriber   Flag indicating if the user is a registered subscriber.
     */
    public IdentificationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String userIdentifier, boolean isSubscriber) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.userIdentifier = userIdentifier;
        this.isSubscriber = isSubscriber; 
        this.casualController = new CasualController(mainUI.controller);
        
        // Register this instance so ClientController can update it
        this.mainUI.currentIdentificationUI = this;
    }
    
    /**
     * Starts the identification process.
     * Checks if the user is a subscriber to determine whether to fetch daily orders
     * or default immediately to manual input mode.
     */
    public void start() {
        showIdentificationForm();
        
        // Determine mode based on user type
        if (isSubscriber) {
            // If Subscriber: Fetch list of today's orders from server
            casualController.getDailyOrders(userIdentifier);
        } else {
            // If Casual: Show manual input immediately
            enableManualMode();
        }
    }

    /**
     * Initializes and displays the visual form elements.
     * Sets up the ComboBox for subscribers and the TextField for manual entry,
     * handling their initial visibility states.
     */
    private void showIdentificationForm() {
        mainLayout.getChildren().clear();

        Label header = new Label("Check-In");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        lblInstruction = new Label("Please wait...");
        lblInstruction.setPadding(new Insets(10,0,0,0));

        // --- 1. Smart Select (Subscriber) ---
        // Dropdown for Subscribers to select existing reservations.
        cmbOrders = new ComboBox<>();
        cmbOrders.setPromptText("Select your reservation...");
        cmbOrders.setPrefWidth(300);
        cmbOrders.setVisible(false);
        cmbOrders.setManaged(false); 
        
        // Format dropdown text: "19:00 - 4 Guests"
        cmbOrders.setConverter(new StringConverter<Order>() {
            @Override
            public String toString(Order o) {
                if (o == null) return "";
                return o.getOrderTime().toString().substring(0, 5) + " - " + o.getNumberOfDiners() + " Guests";
            }
            @Override
            public Order fromString(String string) { return null; }
        });

        // --- 2. Manual Entry (Casual) ---
        // Text field for manual code entry (used by Casual users or as fallback).
        txtBookingId = new TextField();
        txtBookingId.setPromptText("Enter Confirmation Code");
        txtBookingId.setMaxWidth(300);
        txtBookingId.setStyle("-fx-font-size: 16px; -fx-padding: 10;");
        txtBookingId.setVisible(false); 
        txtBookingId.setManaged(false);

        // --- Buttons ---
        btnCheckIn = new Button("Get Table");
        btnCheckIn.setPrefWidth(200);
        btnCheckIn.setPrefHeight(40);
        btnCheckIn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnLostCode = new Button("Lost Code?");
        btnLostCode.setPrefWidth(200);
        btnLostCode.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLostCode.setOnAction(e -> {
        	 casualController.recoverLostCode(userIdentifier);
             mainUI.showAlert("Retrieving Code", "We are sending the order details to: " + userIdentifier);
        });
        
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> {
            mainUI.currentIdentificationUI = null;
            onBack.run();
        });

        btnCheckIn.setOnAction(e -> handleCheckIn());

        contentBox = new VBox(20, header, lblInstruction, cmbOrders, txtBookingId, btnCheckIn, btnLostCode, btnBack);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(450);
        contentBox.setPadding(new Insets(30));
        contentBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(contentBox);
    }
    
    /**
     * Switches the UI to manual mode.
     * Hides the order selection dropdown and displays the manual text input field.
     */
    private void enableManualMode() {
        lblInstruction.setText("Please enter your Confirmation Code:");
        cmbOrders.setVisible(false);
        cmbOrders.setManaged(false);
        txtBookingId.setVisible(true);
        txtBookingId.setManaged(true);
    }
    
    /**
     * Callback method used by the Controller to update the list of orders.
     * If orders are found, it populates the dropdown. If no orders are found,
     * it automatically reverts to manual mode.
     *
     * @param orders The list of orders retrieved from the server.
     */
    public void updateOrderList(ArrayList<Order> orders) {
        Platform.runLater(() -> {
            if (orders != null && !orders.isEmpty()) {
                // Show List
                lblInstruction.setText("Select your reservation for today:");
                cmbOrders.getItems().setAll(orders);
                cmbOrders.setVisible(true);
                cmbOrders.setManaged(true);
                cmbOrders.getSelectionModel().selectFirst();
            } else {
                // No orders found? Fallback to manual
                enableManualMode();
                lblInstruction.setText("No active bookings found for today. Enter code manually:");
            }
        });
    }

    /**
     * Handles the "Get Table" button action.
     * Retrieves the confirmation code from either the selected dropdown item
     * or the text field, validates it, and triggers the check-in process.
     */
    private void handleCheckIn() {
        String codeToCheck = "";

        // Check if we are using the dropdown or text field
        if (cmbOrders.isVisible() && cmbOrders.getValue() != null) {
            codeToCheck = cmbOrders.getValue().getConfirmationCode();
        } else {
            codeToCheck = txtBookingId.getText().trim();
        }

        if (codeToCheck.isEmpty()) {
            mainUI.showAlert("Error", "No reservation selected or code entered.");
            return;
        }

        casualController.checkIn(codeToCheck);
    }
}