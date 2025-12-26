package client;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import common.Order;

public class ReservationUI {

	private CasualController casualController;
    private VBox mainLayout;
    private ClientUI mainUI;      // For alerts and global methods
    private Runnable onBack;    // To go back to the specific Casual Menu
    private String phoneNumber;   // The ID of the user

    public ReservationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String phoneNumber) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.phoneNumber = phoneNumber;
        this.casualController = new CasualController(mainUI.controller);
    }

    public void start() {
        showReservationForm();
    }

    private void showReservationForm() {
        mainLayout.getChildren().clear();

        // --- Header ---
        Label header = new Label("New Reservation");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Booking for: " + phoneNumber);
        subHeader.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // --- Form Fields ---
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // 1. Date
        Label lblDate = new Label("Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        // Block past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // 2. Time (Populate 30-min intervals)
        Label lblTime = new Label("Time:");
        ComboBox<String> timeComboBox = new ComboBox<>();
        timeComboBox.setItems(FXCollections.observableArrayList(generateTimeSlots()));
        timeComboBox.setPromptText("Select Time");

        // 3. Guests
        Label lblGuests = new Label("Guests:");
        TextField txtGuests = new TextField();
        txtGuests.setPromptText("1-15");
        txtGuests.setMaxWidth(80);

        // Add to grid
        grid.add(lblDate, 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(lblTime, 0, 1);
        grid.add(timeComboBox, 1, 1);
        grid.add(lblGuests, 0, 2);
        grid.add(txtGuests, 1, 2);

        // --- Buttons ---
        Button btnSubmit = new Button("Check Availability");
        btnSubmit.setPrefWidth(200);
        btnSubmit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        
        // Navigation: Go back to the Casual Options screen
        btnBack.setOnAction(e -> onBack.run());

        // Submit Logic
        btnSubmit.setOnAction(e -> {
            LocalDate date = datePicker.getValue();
            String time = timeComboBox.getValue();
            String guestsStr = txtGuests.getText().trim();

            if (validateInput(date, time, guestsStr)) {
                // TODO: Send request to Server
            	//casualController.requestReservation(new Order(date, time, Integer.parseInt(guestsStr)));
            	
                mainUI.showAlert("Request Sent", 
                    "Checking availability for:\n" + 
                    date + " at " + time + "\n" + 
                    "Guests: " + guestsStr + "\n\n(Server integration coming next)");
            }
        });

        
        VBox content = new VBox(20, header, subHeader, grid, btnSubmit, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(450);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
        
    }

    // Helper: Generate time slots from 12:00 to 23:00
    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(23, 0);

        while (!start.isAfter(end)) {
            slots.add(start.toString());
            start = start.plusMinutes(30);
        }
        return slots;
    }

    // Helper: Validation Logic
    private boolean validateInput(LocalDate date, String time, String guestsStr) {
        if (date == null) {
            mainUI.showAlert("Error", "Please select a date.");
            return false;
        }
        if (time == null) {
            mainUI.showAlert("Error", "Please select a time.");
            return false;
        }
        try {
            int guests = Integer.parseInt(guestsStr);
            if (guests < 1 || guests > 15) {
                mainUI.showAlert("Error", "Guests must be between 1 and 15.");
                return false;
            }
        } catch (NumberFormatException e) {
            mainUI.showAlert("Error", "Guests must be a valid number.");
            return false;
        }
        return true;
    }
}