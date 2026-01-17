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

    // Constructor accepts isSubscriber flag
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
    
    public void start() {
        showIdentificationForm();
        
        // --- LOGIC SPLIT ---
        if (isSubscriber) {
            // If Subscriber: Fetch list from server
            casualController.getDailyOrders(userIdentifier);
        } else {
            // If Casual: Show manual input immediately
            enableManualMode();
        }
    }

    private void showIdentificationForm() {
        mainLayout.getChildren().clear();

        Label header = new Label("Check-In");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        lblInstruction = new Label("Please wait...");
        lblInstruction.setPadding(new Insets(10,0,0,0));

        // --- 1. Smart Select (Subscriber) ---
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
    
    // Switch to Manual Text Field
    private void enableManualMode() {
        lblInstruction.setText("Please enter your Confirmation Code:");
        cmbOrders.setVisible(false);
        cmbOrders.setManaged(false);
        txtBookingId.setVisible(true);
        txtBookingId.setManaged(true);
    }
    
    // Server Callback (Called by ClientController)
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