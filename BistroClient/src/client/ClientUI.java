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

//import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import common.Table;
import common.User;
import common.WaitingList;

public class ClientUI extends Application {
	public RepresentativeUI repUI;
	public ClientController controller;
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

    
    //
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
    public void showRoleSelectionScreen() {
        mainLayout.getChildren().clear(); 

        Label header = new Label("Select Your Role");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // 1. Casual Diner
        Button btnCasual = new Button("Casual Diner");
        styleRoleButton(btnCasual, "#4CAF50"); // Green

        // 2. Customer (Updated to Subscriber)
        Button btnCustomer = new Button("Subscriber"); // Changed label to Subscriber
        styleRoleButton(btnCustomer, "#2196F3"); // Blue

        // 3. Representative
        Button btnRep = new Button("Representative");
        styleRoleButton(btnRep, "#FF9800"); // Orange

        // 4. Manager
        Button btnManager = new Button("Manager");
        styleRoleButton(btnManager, "#9C27B0"); // Purple

        // Actions
        btnCasual.setOnAction(e -> {
            CasualUI casualScreen = new CasualUI(mainLayout, this);
            casualScreen.start();
        });

        // --- NEW SUBSCRIBER ACTION ADDED HERE ---
        btnCustomer.setOnAction(e -> {
            SubscriberUI subscriberScreen = new SubscriberUI(mainLayout, this);
            subscriberScreen.start();
        });

        btnRep.setOnAction(e -> {
        	this.repUI = new RepresentativeUI(mainLayout, this); 
        	this.repUI.start();
        });
     // 4. Manager (Launches Login -> Manager Dashboard)
        btnManager.setOnAction(e -> {
            //  ManagerUI inherits from RepUI but has extra buttons
             ManagerUI managerScreen = new ManagerUI(mainLayout, this);
             managerScreen.start();
        });
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

    public void updateOrderTable(ArrayList<Order> orders) {
        // Not used yet in this screen, but method kept for controller compatibility
        /*
        table.getItems().clear();
        table.getItems().addAll(orders);
        */
    }
    public void refreshTableData(ArrayList<Table> tables) {
        // This passes the data to the specific screen
        if (repUI != null) {
            repUI.updateTableData(tables);
        } else {
            System.out.println("Error: RepresentativeUI is null. Make sure you saved the reference when opening the dashboard.");
        }
    }
    public void refreshSubscriberData(ArrayList<User> subscribers) {
        if (repUI != null) {
            repUI.updateSubscriberData(subscribers);
        }
    }

    public void refreshOrderData(ArrayList<Order> orders) {
        if (repUI != null) {
            repUI.updateOrdersData(orders);
        } else {
            System.out.println("Error: RepresentativeUI is not open, cannot update orders.");
        }
    }

    public void refreshWaitingListData(ArrayList<WaitingList> list) {
        if (repUI != null) {
            repUI.updateWaitingListData(list);
        }
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