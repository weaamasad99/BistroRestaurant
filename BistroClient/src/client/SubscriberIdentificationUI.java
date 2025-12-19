package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SubscriberIdentificationUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;      // Action to go back
    private String userIdentifier; // Subscriber ID or Phone

    public SubscriberIdentificationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String userIdentifier) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.userIdentifier = userIdentifier;
    }

    public void start() {
        showIdentifyForm();
    }

    private void showIdentifyForm() {
        mainLayout.getChildren().clear();

        // --- Header ---
        Label header = new Label("Identify at Table");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("User: " + userIdentifier);
        subHeader.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // --- Form ---
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // Confirmation Code Input
        Label lblCode = new Label("Confirmation Code:");
        TextField txtCode = new TextField();
        txtCode.setPromptText("e.g. 1001");
        txtCode.setMaxWidth(120);

        grid.add(lblCode, 0, 0);
        grid.add(txtCode, 1, 0);

        // --- Buttons ---
        
        // ENTER BUTTON
        Button btnEnter = new Button("Enter / Get Table");
        btnEnter.setPrefWidth(200);
        btnEnter.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // LOST CODE BUTTON (Requirement)
        Button btnLostCode = new Button("Lost Code?");
        btnLostCode.setPrefWidth(200);
        btnLostCode.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // BACK BUTTON
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        // --- Logic: Enter ---
        btnEnter.setOnAction(e -> {
            String code = txtCode.getText().trim();
            if (code.isEmpty()) {
                mainUI.showAlert("Error", "Please enter your Confirmation Code.");
            } else {
                simulateIdentification(code);
            }
        });

        // --- Logic: Lost Code ---
        btnLostCode.setOnAction(e -> {
            mainUI.showAlert("Code Recovery", 
                "A copy of your confirmation code has been sent via SMS/Email to: " + userIdentifier);
        });

        VBox content = new VBox(20, header, subHeader, grid, btnEnter, btnLostCode, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(450);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    private void simulateIdentification(String code) {
        // Mock Logic based on requirements:
        // 1. Check if Code exists (Mock: accept any number)
        // 2. Check if Table is Ready OR Customer needs to wait
        
        try {
            Integer.parseInt(code); // Validate number
        } catch (NumberFormatException e) {
            mainUI.showAlert("Error", "Code must be a number.");
            return;
        }

        boolean isTableReady = Math.random() > 0.3; // 70% chance ready

        if (isTableReady) {
            // Case: Table Ready
            int tableNum = (int)(Math.random() * 20) + 1;
            mainUI.showAlert("Welcome!", 
                "Your table is ready!\n\n" +
                "Please proceed to TABLE #" + tableNum + "\n" + 
                "Enjoy your meal!");
            onBack.run(); // Return to dashboard after success
        } else {
            // Case: Delay / Not Ready Yet
            mainUI.showAlert("Please Wait", 
                "Your table is not ready yet (Previous customer delay).\n" +
                "Please wait in the waiting area.\n" +
                "We will notify you immediately when it clears.");
        }
    }
}