package client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SubscriberHistoryUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;      // Action to go back
    private String subscriberID;  // The ID of the subscriber

    public SubscriberHistoryUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String subscriberID) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.subscriberID = subscriberID;
    }

    public void start() {
        showHistoryTable();
    }

    @SuppressWarnings("unchecked")
    private void showHistoryTable() {
        mainLayout.getChildren().clear();

        // --- Header ---
        Label header = new Label("Order History");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Subscriber ID: " + subscriberID);
        subHeader.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // --- Table View ---
        TableView<HistoryItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(300);

        // 1. Order Number Column
        TableColumn<HistoryItem, String> colId = new TableColumn<>("Order #");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        // 2. Date Column
        TableColumn<HistoryItem, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 3. Time Column
        TableColumn<HistoryItem, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        // 4. Guests Column
        TableColumn<HistoryItem, String> colGuests = new TableColumn<>("Guests");
        colGuests.setCellValueFactory(new PropertyValueFactory<>("guests"));

        // 5. Status Column
        TableColumn<HistoryItem, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Custom styling for Status to make it look nice
        colStatus.setCellFactory(column -> new TableCell<HistoryItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Finished")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("Cancelled")) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: orange;");
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colDate, colTime, colGuests, colStatus);

        // --- Load Data ---
        // In the future: fetchHistoryFromServer(subscriberID);
        table.setItems(getMockData());

        // --- Buttons ---
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        VBox content = new VBox(15, header, subHeader, table, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600); // Wider for table
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    // --- Mock Data Generator ---
    private ObservableList<HistoryItem> getMockData() {
        ObservableList<HistoryItem> list = FXCollections.observableArrayList();
        list.add(new HistoryItem("1001", "2024-12-01", "19:00", "4", "Finished"));
        list.add(new HistoryItem("1025", "2024-12-15", "20:30", "2", "Finished"));
        list.add(new HistoryItem("1042", "2025-01-05", "18:00", "6", "Cancelled"));
        list.add(new HistoryItem("1055", "2025-01-20", "21:00", "3", "Approved"));
        return list;
    }

    // --- Simple Inner Class for Data ---
    public static class HistoryItem {
        private String orderId;
        private String date;
        private String time;
        private String guests;
        private String status;

        public HistoryItem(String orderId, String date, String time, String guests, String status) {
            this.orderId = orderId;
            this.date = date;
            this.time = time;
            this.guests = guests;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getGuests() { return guests; }
        public String getStatus() { return status; }
    }
}