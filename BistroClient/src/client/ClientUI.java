package client;

import common.Order;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableView<Order> table;
    
    // Connection Fields
    private TextField txtIp;
    private TextField txtPort;
    private Label lblStatus;
    private Button btnLoadOrders; // New Button requested

    // Edit Fields
    private TextField txtEditGuests;
    private DatePicker datePicker;
    private Label lblSelectedOrder;
    private Order currentSelectedOrder;
    private Button btnUpdate;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new ClientController(this);
        primaryStage.setTitle("Bistro Client Management");

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
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Closing client...");
            if (controller != null) {
                controller.disconnect(); // מנתק את החיבור לשרת
            }
            Platform.exit(); 
            System.exit(0);  
        });
        primaryStage.show();
    }

    private HBox createTopPanel() {
        // Connection Inputs
        txtIp = new TextField("localhost");
        txtIp.setPromptText("IP Address");
        txtIp.setPrefWidth(120);
        
        txtPort = new TextField("5555");
        txtPort.setPromptText("Port");
        txtPort.setPrefWidth(60);

        Button btnConnect = new Button("Connect");
        btnConnect.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        lblStatus = new Label("Disconnected");
        lblStatus.setTextFill(Color.RED);
        lblStatus.setFont(Font.font("Arial", 14));

        // The requested "Get Orders" Option
        btnLoadOrders = new Button("Load Orders");
        btnLoadOrders.setDisable(true); // Disabled until connected
        btnLoadOrders.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- CONNECT BUTTON ACTION (Threaded to prevent freezing) ---
        btnConnect.setOnAction(e -> {
            String ip = txtIp.getText().trim();
            String portStr = txtPort.getText().trim();

            // 1. Basic Validation
            if (ip.isEmpty() || portStr.isEmpty()) {
                showAlert("Input Error", "Please enter both IP Address and Port.");
                return;
            }

            // 2. Update UI to "Connecting..." state and disable button to prevent double-clicks
            btnConnect.setDisable(true);
            lblStatus.setText("Connecting...");
            lblStatus.setTextFill(Color.ORANGE);

            // 3. Run connection logic in a separate thread to avoid freezing the GUI
            new Thread(() -> {
                boolean success = false;
                try {
                    int port = Integer.parseInt(portStr);
                    // This is the blocking call that might take time if IP is wrong
                    success = controller.connect(ip, port);
                } catch (NumberFormatException ex) {
                    System.err.println("Error parsing port: " + ex.getMessage());
                    success = false;
                }

                // Capture result for the UI thread
                final boolean isConnected = success;

                // 4. Update the GUI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    // Re-enable the connect button (or keep disabled if connected)
                    btnConnect.setDisable(false); 

                    if (isConnected) {
                        // --- SUCCESS ---
                        lblStatus.setText("Connected");
                        lblStatus.setTextFill(Color.GREEN);
                        
                        // Lock connection fields
                        txtIp.setDisable(true);
                        txtPort.setDisable(true);
                        btnConnect.setDisable(true); // Disable connect button after success
                        
                        // Enable the functionality button
                        btnLoadOrders.setDisable(false);
                    } else {
                        // --- FAILURE ---
                        lblStatus.setText("Connection Failed");
                        lblStatus.setTextFill(Color.RED);
                        showAlert("Connection Error", "Failed to connect to server.\nPlease check the IP address and ensure the server is running.");
                    }
                });
            }).start(); // Start the background thread
        });

        // Load Orders Action
        btnLoadOrders.setOnAction(e -> {
            controller.getAllOrders();
        });

        // Layout container
        HBox box = new HBox(15, 
            new Label("Host:"), txtIp, 
            new Label("Port:"), txtPort, 
            btnConnect, 
            new Separator(javafx.geometry.Orientation.VERTICAL), // Visual separator
            btnLoadOrders,
            lblStatus
        );
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        return box;
    }

    private void setupTableColumns() {
        TableColumn<Order, Integer> colId = new TableColumn<>("Order #");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        
        TableColumn<Order, Date> colDate = new TableColumn<>("Order Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDate.setPrefWidth(120);

        TableColumn<Order, Integer> colGuests = new TableColumn<>("Guests");
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        
        // Additional columns for context
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

    public void updateOrderTable(ArrayList<Order> orders) {
        table.getItems().clear();
        table.getItems().addAll(orders);
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}