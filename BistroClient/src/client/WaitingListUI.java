package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.sql.Date;
import java.sql.Time;
import java.util.Optional;

import common.WaitingList;
import controllers.CasualController;

/**
 * The WaitingListUI class provides the user interface for customers to join or leave the waiting list.
 * <p>
 * This screen is used when no tables are immediately available. It allows both casual diners 
 * and subscribers to enter their details (number of diners) and be added to the queue.
 * It also provides functionality to leave the waiting list if the customer changes their mind.
 */
public class WaitingListUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;      // Action to go back to the previous menu
    private String userIdentifier; // Subscriber ID or Phone Number
    private boolean isCasual;     // Flag to toggle fields
    private CasualController casualController;

    /**
     * Constructs the WaitingListUI instance.
     *
     * @param mainLayout     The main layout container where the UI will be rendered.
     * @param mainUI         The main application instance.
     * @param onBack         A Runnable callback to execute when the user navigates back.
     * @param userIdentifier The unique identifier for the user (Phone Number for casual, Subscriber ID for subscribers).
     * @param isCasual       Flag indicating if the user is a casual diner (true) or a subscriber (false).
     */
    public WaitingListUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String userIdentifier, boolean isCasual) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.userIdentifier = userIdentifier;
        this.isCasual = isCasual;
        this.casualController = new CasualController(mainUI.controller);
    }

    /**
     * Starts the waiting list interface by building and displaying the form.
     */
    public void start() {
        showWaitingListForm();
    }

    /**
     * Constructs and displays the waiting list form.
     * <p>
     * The form includes:
     * <ul>
     * <li>Input for the number of diners (1-15).</li>
     * <li>Optional email field for casual diners.</li>
     * <li>"Join Waiting List" button to submit the request.</li>
     * <li>"Leave / Cancel" button to remove the user from the list.</li>
     * </ul>
     */
    private void showWaitingListForm() {
        mainLayout.getChildren().clear();

        // --- Header ---
        Label header = new Label("Waiting List");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label(isCasual ? "Customer Phone: " + userIdentifier : "Subscriber ID: " + userIdentifier);
        subHeader.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // --- Form Section ---
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // 1. Number of Diners (Required for everyone)
        Label lblGuests = new Label("Number of Diners:");
        TextField txtGuests = new TextField();
        txtGuests.setPromptText("1-15");
        txtGuests.setMaxWidth(80);

        grid.add(lblGuests, 0, 0);
        grid.add(txtGuests, 1, 0);

        // 2. Extra fields for Casual Customers (Email)
        // Note: Phone is already passed as 'userIdentifier'
        TextField txtEmail = new TextField();
        if (isCasual) {
            Label lblEmail = new Label("Email (Optional):");
            txtEmail.setPromptText("email@example.com");
            
            grid.add(lblEmail, 0, 1);
            grid.add(txtEmail, 1, 1);
        }

        // --- Action Buttons ---
        
        // JOIN BUTTON
        Button btnJoin = new Button("Join Waiting List");
        btnJoin.setPrefWidth(200);
        btnJoin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // LEAVE / CANCEL BUTTON (Requested Requirement)
        Button btnLeave = new Button("Leave / Cancel Waiting List");
        btnLeave.setPrefWidth(200);
        btnLeave.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // BACK BUTTON
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        // --- Logic: Join ---
        btnJoin.setOnAction(e -> {
        	String guestsStr = txtGuests.getText().trim();
            if (validateInput(guestsStr)) {
                int diners = Integer.parseInt(guestsStr);
                
                WaitingList wl = new WaitingList();
                wl.setNumOfDiners(diners);
                long now = System.currentTimeMillis();

                wl.setDateRequested(new Date(now));
                wl.setTimeRequested(new Time(now));
                wl.setStatus("WAITING");
                
                // We assume mainUI.currentUser was set during the identification phase
                if (mainUI.currentUser != null) {
                    wl.setUserId(mainUI.currentUser.getUserId()); 
                    
                    // Send only the object
                    casualController.enterWaitingList(wl);
                    onBack.run();
                } else {
                    mainUI.showAlert("Error", "User not identified. Please try logging in again.");
                }
            }
        });

        btnLeave.setOnAction(e -> {
            if (mainUI.currentUser != null) {
                // We send the User ID. The server checks if this user is in the list.
                casualController.exitWaitingList(mainUI.currentUser.getUserId());
            } else {
                mainUI.showAlert("Error", "System cannot identify you. Please log in again.");
            }
        });

        VBox content = new VBox(20, header, subHeader, grid, btnJoin, new Separator(), btnLeave, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(450);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    /**
     * Requirement: "The customer can exit the waiting list at any time"
     * <p>
     * Displays a dialog prompting the user for a Confirmation Code to verify the cancellation.
     * <br><b>Note:</b> This is an alternative flow to the direct "Leave" button above.
     */
    private void showCancellationDialog() {	
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancel Waiting List");
        dialog.setHeaderText("Leave Waiting List");
        dialog.setContentText("Please enter your Confirmation Code:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            if (code.trim().isEmpty()) {
                mainUI.showAlert("Error", "Code cannot be empty.");
            } else {
                // Future: Send cancellation to server
                mainUI.showAlert("Success", "Request to cancel code " + code + " sent.\nYou have been removed from the list.");
                onBack.run();
            }
        });
    }

    /**
     * Validates the input for the number of guests.
     *
     * @param guestsStr The input string from the text field.
     * @return {@code true} if the input is a number between 1 and 15; {@code false} otherwise.
     */
    private boolean validateInput(String guestsStr) {
        try {
            int guests = Integer.parseInt(guestsStr);
            if (guests < 1 || guests > 15) {
                mainUI.showAlert("Error", "Guests must be between 1 and 15.");
                return false;
            }
        } catch (NumberFormatException e) {
            mainUI.showAlert("Error", "Please enter a valid number of guests.");
            return false;
        }
        return true;
    }

    /**
     * Simulates the server-side logic for joining the waiting list (Mock/Prototype logic).
     * <p>
     * This method is preserved for testing purposes. In the live system, the 
     * {@link CasualController#enterWaitingList(WaitingList)} method is used instead.
     *
     * @param guests The number of guests requested.
     */
    private void simulateJoinServer(String guests) {
        // Logic described in requirements: 
        // 1. Check if table available immediately -> Go to table
        // 2. If not -> Generate code + SMS/Email
        
        boolean isAvailableImmediately = Math.random() > 0.7; // Mock logic

        if (isAvailableImmediately) {
            mainUI.showAlert("Table Ready!", 
                "Good news! A table for " + guests + " is available immediately.\n" +
                "Please proceed to Table #" + ((int)(Math.random() * 10) + 1));
        } else {
            int code = (int)(Math.random() * 9000) + 1000;
            mainUI.showAlert("Added to List", 
                "No tables are currently free.\n" +
                "You have been added to the Waiting List.\n\n" +
                "YOUR CODE: " + code + "\n\n" +
                "We will notify you via SMS/Email when ready.\n" +
                "(You have 15 mins to arrive once notified)");
        }
        onBack.run();
    }
}