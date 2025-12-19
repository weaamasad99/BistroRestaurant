package client;

import common.Order;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
// import javafx.scene.control.cell.PropertyValueFactory; // Commented out
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ClientUI extends Application {

    private ClientController controller;
    private VBox mainLayout; // Main container for swapping views

    // Connection Fields
    private TextField txtIp;
    private TextField txtPort;
    private Label lblStatus;
    private Button btnConnect; 

    // --- OLD FIELDS (Commented out per request) ---
    /*
    private TableView<Order> table;
    private Button btnLoadOrders; 
    private TextField txtEditGuests;
    private DatePicker datePicker;
    private Label lblSelectedOrder;
    private Order currentSelectedOrder;
    private Button btnUpdate;
    */

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new ClientController(this);
        primaryStage.setTitle("Bistro Management System");

        // Use a VBox as the main layout that we will clear and refill
        mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #f4f4f4;");
        mainLayout.setPadding(new Insets(20));

        // --- NEW FLOW: Start with Connection Screen ---
        showConnectionScreen();

        // --- OLD PROTOTYPE CODE (Commented out) ---
        /*
        // 1. Top Panel (Connection + Load Action)
        HBox topPanel = createTopPanel();

        // 2. Center Panel (Table)
        table = new TableView<>();
        setupTableColumns();
        
        // Listener: When a user clicks a row, populate the bottom form
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateEditForm(newSelection);
            }
        });

        // 3. Bottom Panel (Edit Form)
        VBox bottomPanel = createEditBox();

        // Main Layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(topPanel, table, bottomPanel);
        root.setStyle("-fx-background-color: #f4f4f4;");

        Scene scene = new Scene(root, 900, 650);
        */

        // Set the scene with our dynamic mainLayout
        Scene scene = new Scene(mainLayout, 600, 500);
        primaryStage.setScene(scene);
        
        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            try { stop(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        primaryStage.show();
    }

    // =========================================================
    // SCREEN 1: Connection UI
    // =========================================================
    private void showConnectionScreen() {
        mainLayout.getChildren().clear(); // Clear any existing content

        Label header = new Label("Connect to Server");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        txtIp = new TextField("localhost");
        txtIp.setPromptText("IP Address");
        txtIp.setMaxWidth(300);
        
        txtPort = new TextField("5555");
        txtPort.setPromptText("Port");
        txtPort.setMaxWidth(300);

        btnConnect = new Button("Connect");
        btnConnect.setPrefWidth(150);
        btnConnect.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        lblStatus = new Label("Disconnected");
        lblStatus.setTextFill(Color.RED);
        lblStatus.setFont(Font.font("Arial", 14));

        // Connect Action
        btnConnect.setOnAction(e -> {
            String ip = txtIp.getText().trim();
            String portStr = txtPort.getText().trim();

            if (ip.isEmpty() || portStr.isEmpty()) {
                showAlert("Input Error", "Please enter both IP Address and Port.");
                return;
            }

            btnConnect.setDisable(true);
            lblStatus.setText("Connecting...");
            lblStatus.setTextFill(Color.ORANGE);

            // Thread for connection
            new Thread(() -> {
                boolean success = false;
                try {
                    int port = Integer.parseInt(portStr);
                    success = controller.connect(ip, port);
                } catch (NumberFormatException ex) {
                    System.err.println("Error parsing port: " + ex.getMessage());
                    success = false;
                }

                final boolean isConnected = success;

                Platform.runLater(() -> {
                    btnConnect.setDisable(false); 

                    if (isConnected) {
                        lblStatus.setText("Connected");
                        lblStatus.setTextFill(Color.GREEN);
                        
                        // --- NAVIGATION: Go to Role Selection ---
                        showRoleSelectionScreen();

                    } else {
                        lblStatus.setText("Connection Failed");
                        lblStatus.setTextFill(Color.RED);
                        showAlert("Connection Error", "Failed to connect to server.");
                    }
                });
            }).start();
        });

        VBox content = new VBox(15, header, new Label("Host:"), txtIp, new Label("Port:"), txtPort, btnConnect, lblStatus);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    // =========================================================
    // SCREEN 2: Role Selection (The 4 Buttons)
    // =========================================================
    private void showRoleSelectionScreen() {
        mainLayout.getChildren().clear(); 

        Label header = new Label("Select Your Role");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // 1. Casual Diner
        Button btnCasual = new Button("Casual Diner");
        styleRoleButton(btnCasual, "#4CAF50"); // Green

        // 2. Customer
        Button btnCustomer = new Button("Customer");
        styleRoleButton(btnCustomer, "#2196F3"); // Blue

        // 3. Representative
        Button btnRep = new Button("Representative");
        styleRoleButton(btnRep, "#FF9800"); // Orange

        // 4. Manager
        Button btnManager = new Button("Manager");
        styleRoleButton(btnManager, "#9C27B0"); // Purple

        // Actions (Placeholder for now)
        btnCasual.setOnAction(e -> showAlert("Navigating", "Going to Casual Diner Screen..."));
        btnCustomer.setOnAction(e -> showAlert("Navigating", "Going to Customer Screen..."));
        btnRep.setOnAction(e -> showAlert("Navigating", "Going to Rep Screen..."));
        btnManager.setOnAction(e -> showAlert("Navigating", "Going to Manager Screen..."));

        VBox menuBox = new VBox(20, header, btnCasual, btnCustomer, btnRep, btnManager);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);
        menuBox.setPadding(new Insets(30));
        menuBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(menuBox);
    }

    private void styleRoleButton(Button btn, String colorHex) {
        btn.setPrefWidth(250);
        btn.setPrefHeight(45);
        btn.setFont(new Font("Arial", 16));
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    // =========================================================
    // OLD PROTOTYPE METHODS (Commented Out)
    // =========================================================

    /*
    private HBox createTopPanel() {
        // ... Old top panel logic ...
        return new HBox(); 
    }

    private void setupTableColumns() {
        TableColumn<Order, Integer> colId = new TableColumn<>("Order #");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        
        TableColumn<Order, Date> colDate = new TableColumn<>("Order Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDate.setPrefWidth(120);

        TableColumn<Order, Integer> colGuests = new TableColumn<>("Guests");
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        
        TableColumn<Order, Integer> colSub = new TableColumn<>("Subscriber ID");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));

        table.getColumns().addAll(colId, colDate, colGuests, colSub);
        table.setPlaceholder(new Label("No orders loaded. Click 'Load Orders' after connecting."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private VBox createEditBox() {
        Label header = new Label("Update Selected Order");
        header.setFont(new Font("Arial", 16));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        lblSelectedOrder = new Label("Select an order from the table to edit");
        lblSelectedOrder.setTextFill(Color.GRAY);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));
        
        txtEditGuests = new TextField();
        txtEditGuests.setPromptText("Enter 1-15");
        
        datePicker = new DatePicker();
        datePicker.setPromptText("Pick new date");

        grid.add(new Label("Number of Guests:"), 0, 0);
        grid.add(txtEditGuests, 1, 0);
        grid.add(new Label("New Order Date:"), 0, 1);
        grid.add(datePicker, 1, 1);

        btnUpdate = new Button("Update Order");
        btnUpdate.setDisable(true);
        btnUpdate.setPrefWidth(150);
        btnUpdate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        btnUpdate.setOnAction(e -> handleUpdateLogic());

        VBox box = new VBox(10, header, lblSelectedOrder, grid, btnUpdate);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-background-radius: 5;");
        return box;
    }

    private void populateEditForm(Order order) {
        currentSelectedOrder = order;
        lblSelectedOrder.setText("Editing Order #" + order.getOrderNumber());
        lblSelectedOrder.setTextFill(Color.BLACK);
        
        txtEditGuests.setText(String.valueOf(order.getNumberOfGuests()));
        if(order.getOrderDate() != null)
            datePicker.setValue(order.getOrderDate().toLocalDate());
        
        btnUpdate.setDisable(false);
    }

    private void handleUpdateLogic() {
        if (currentSelectedOrder == null) return;

        try {
            // Validation 1: Guests
            int guests = Integer.parseInt(txtEditGuests.getText());
            if (guests < 1 || guests > 15) {
                showAlert("Validation Error", "Guests must be between 1 and 15.");
                return;
            }

            // Validation 2: Date
            LocalDate newDate = datePicker.getValue();
            LocalDate originalDate = currentSelectedOrder.getOrderDate().toLocalDate();

            if (newDate == null) {
                showAlert("Validation Error", "Please select a date.");
                return;
            }
            
            if (newDate.isBefore(originalDate)) {
                 showAlert("Validation Error", "New date cannot be earlier than the original date.");
                 return;
            }

            long daysDiff = ChronoUnit.DAYS.between(originalDate, newDate);
            if (daysDiff > 31) { 
                showAlert("Validation Error", "You can only postpone the order by up to 1 month.");
                return;
            }

            // Create updated object
            Order updatedOrder = new Order(
                currentSelectedOrder.getOrderNumber(),
                Date.valueOf(newDate),
                guests,
                currentSelectedOrder.getConfirmationCode(),
                currentSelectedOrder.getSubscriberId(),
                currentSelectedOrder.getDateOfPlacingOrder()
            );

            // Send to server
            controller.updateOrder(updatedOrder);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Number of guests must be a valid integer.");
        }
    }
    */

    public void updateOrderTable(ArrayList<Order> orders) {
        // Not used yet in this screen, but method kept for controller compatibility
        /*
        table.getItems().clear();
        table.getItems().addAll(orders);
        */
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleServerDisconnect() {
        showAlert("Connection Lost", "The server has stopped or crashed.\nRestart client.");
        // Optional: Go back to connection screen
        showConnectionScreen();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stopping client...");
        if (controller != null) {
            controller.disconnect(); 
        }
        super.stop();
        System.exit(0); 
    }
}