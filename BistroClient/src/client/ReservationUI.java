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
import java.sql.Date;
import java.sql.Time;

import common.Message;
import common.Order;
import controllers.CasualController;

public class ReservationUI {

    private CasualController casualController;
    private VBox mainLayout;
    private ClientUI mainUI; 
    private Runnable onBack;
    private String phoneNumber;
    
    private ComboBox<String> timeComboBox;
    private Button btnSubmit;

    public ReservationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String phoneNumber) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.phoneNumber = phoneNumber;
        this.casualController = new CasualController(mainUI.controller);
    }

    public void start() {
        // This ensures that if a Rep just changed hours, we get the latest version.
        Message refreshMsg = new Message(common.TaskType.GET_SCHEDULE, null);
        mainUI.controller.accept(refreshMsg);

        showReservationForm();
    }

    private void showReservationForm() {
        mainLayout.getChildren().clear();

        Label header = new Label("New Reservation");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Booking for: " + phoneNumber);
        subHeader.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // 1. Date Picker
        Label lblDate = new Label("Date:");
        DatePicker datePicker = new DatePicker();
        
        // Block past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // 2. Time ComboBox
        Label lblTime = new Label("Time:");
        timeComboBox = new ComboBox<>();
        timeComboBox.setPromptText("Select a Date first");
        timeComboBox.setDisable(true);

        // 3. Guests
        Label lblGuests = new Label("Guests:");
        TextField txtGuests = new TextField();
        txtGuests.setPromptText("1-15");
        txtGuests.setMaxWidth(80);

        grid.add(lblDate, 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(lblTime, 0, 1);
        grid.add(timeComboBox, 1, 1);
        grid.add(lblGuests, 0, 2);
        grid.add(txtGuests, 1, 2);

        btnSubmit = new Button("Check Availability");
        btnSubmit.setPrefWidth(200);
        btnSubmit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSubmit.setDisable(true); 

        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        // --- INSTANT OFFLINE CHECK ---
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // 1. Ask ClientUI for the hours (No Server Call Needed!)
                String hours = mainUI.getOfflineHours(newVal);
                populateTimeList(hours);
            }
        });

        // Submit Logic
        btnSubmit.setOnAction(e -> {
            LocalDate localdate = datePicker.getValue();
            String timeStr = timeComboBox.getValue();
            String guestsStr = txtGuests.getText().trim();

            if (localdate != null && timeStr != null && !guestsStr.isEmpty()) {
                Date date = Date.valueOf(localdate);
                // Ensure HH:mm:ss
                String validTimeStr = timeStr.length() == 5 ? timeStr + ":00" : timeStr;
                Time time = Time.valueOf(validTimeStr); 
                
                try {
                    int guests = Integer.parseInt(guestsStr);
                    casualController.requestReservation(new Order(this.mainUI.currentUser.getUserId(), date, time, guests));
                } catch (NumberFormatException ex) {
                    mainUI.showAlert("Error", "Guests must be a number");
                }
            } else {
                mainUI.showAlert("Missing Info", "Please fill all fields");
            }
        });

        VBox content = new VBox(20, header, subHeader, grid, btnSubmit, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(450);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    private void populateTimeList(String range) {
        timeComboBox.getItems().clear();

        if ("CLOSED".equals(range)) {
            timeComboBox.setPromptText("Closed this day");
            timeComboBox.setDisable(true);
            btnSubmit.setDisable(true);
            return;
        }

        try {
            // Range format: "08:00-14:00" or "08:00:00-14:00:00"
            String[] parts = range.split("-");
            
            // Clean up seconds if present
            String startStr = parts[0].trim();
            String endStr = parts[1].trim();

            LocalTime start = LocalTime.parse(startStr);
            LocalTime end = LocalTime.parse(endStr);

            List<String> slots = new ArrayList<>();
            // Last seating 1 hour before close
            LocalTime lastSeating = end.minusMinutes(60); 

            while (!start.isAfter(lastSeating)) {
                slots.add(start.toString());
                start = start.plusMinutes(30);
            }

            if (slots.isEmpty()) {
                 timeComboBox.setPromptText("Fully Booked / Closing Soon");
                 timeComboBox.setDisable(true);
                 btnSubmit.setDisable(true);
            } else {
                timeComboBox.setItems(FXCollections.observableArrayList(slots));
                timeComboBox.setPromptText("Select Time");
                timeComboBox.setDisable(false);
                btnSubmit.setDisable(false);
            }

        } catch (Exception e) {
            System.err.println("Error parsing time range: " + range);
            e.printStackTrace();
        }
    }
}