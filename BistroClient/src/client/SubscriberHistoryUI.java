package client;

import java.util.ArrayList;

import common.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * The SubscriberHistoryUI class is responsible for displaying the order history
 * for a specific subscriber.
 * <p>
 * It renders a table view containing details of past orders such as date, time, 
 * number of guests, and the current status of the order.
 * * @author Group 6
 * @version 1.0
 */
public class SubscriberHistoryUI {

    /** The main layout container where the UI will be rendered. */
    private VBox mainLayout;
    
    /** The main application instance used for navigation and shared data. */
    private ClientUI mainUI;
    
    /** A Runnable callback to execute when the user navigates back to the previous screen. */
    private Runnable onBack;      
    
    /** The ID of the subscriber whose history is currently being viewed. */
    private String subscriberID;  
    
    /** The data list used to populate the JavaFX TableView. */
    private ObservableList<Order> historyList;

    /**
     * Constructs the SubscriberHistoryUI instance.
     *
     * @param mainLayout   The main layout container where the UI will be rendered.
     * @param mainUI       The main application instance.
     * @param onBack       A Runnable callback to execute when the user navigates back.
     * @param subscriberID The unique ID of the subscriber whose history is being viewed.
     * @param historyData  The list of Order objects retrieved from the server.
     */
    public SubscriberHistoryUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, String subscriberID, ArrayList<Order> historyData) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.subscriberID = subscriberID;
        this.historyList = FXCollections.observableArrayList(historyData);
    }

    /**
     * Starts the UI by building and displaying the history table.
     */
    public void start() {
        showHistoryTable();
    }

    /**
     * Clears the layout and constructs the visual elements for the history view.
     * Includes the header, the table with specific columns, and the back button.
     */
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
        // Change type from <HistoryItem> to <Order>
        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(300);

        // 1. Order Number
        TableColumn<Order, Integer> colId = new TableColumn<>("Order #");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

        // 2. Date
        TableColumn<Order, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        // 3. Time
        TableColumn<Order, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));

        // 4. Guests
        TableColumn<Order, Integer> colGuests = new TableColumn<>("Guests");
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfDiners"));

        // 5. Status
        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Status Styling: Custom CellFactory to color-code the status text
        colStatus.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("FINISHED".equalsIgnoreCase(item) || "APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("CANCELLED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: orange;");
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colDate, colTime, colGuests, colStatus);

        // --- Load Real Data ---
        table.setItems(historyList);

        // --- Buttons ---
        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        VBox content = new VBox(15, header, subHeader, table, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }


    /**
     * Simple inner class originally used for Mock Data.
     * Currently unused in favor of the common.Order entity.
     */
    public static class HistoryItem {
        /** The unique order ID. */
        private String orderId;
        
        /** The date of the order. */
        private String date;
        
        /** The time of the order. */
        private String time;
        
        /** The number of guests. */
        private String guests;
        
        /** The status of the order. */
        private String status;

        /**
         * Constructs a HistoryItem.
         * @param orderId Order ID.
         * @param date Date string.
         * @param time Time string.
         * @param guests Guest count string.
         * @param status Status string.
         */
        public HistoryItem(String orderId, String date, String time, String guests, String status) {
            this.orderId = orderId;
            this.date = date;
            this.time = time;
            this.guests = guests;
            this.status = status;
        }

        /** @return The order ID. */
        public String getOrderId() { return orderId; }
        /** @return The date. */
        public String getDate() { return date; }
        /** @return The time. */
        public String getTime() { return time; }
        /** @return The number of guests. */
        public String getGuests() { return guests; }
        /** @return The status. */
        public String getStatus() { return status; }
    }
}