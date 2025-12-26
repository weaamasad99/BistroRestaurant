package client;

import common.Order;
import common.Table;
import common.User;
import common.WaitingList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;

public class ClientUI extends Application {

    // Reference to the active Representative/Manager screen
    // This allows us to pass data (tables, orders) from the server to the screen.
    public RepresentativeUI repUI;
    
    public ClientController controller;
    private VBox mainLayout; 

    // Connection Fields
    private TextField txtIp;
    private TextField txtPort;
    private Label lblStatus;
    private Button btnConnect; 

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new ClientController(this);
        primaryStage.setTitle("Bistro Management System");

        mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #f4f4f4;");
        mainLayout.setPadding(new Insets(20));

        showConnectionScreen();

        Scene scene = new Scene(mainLayout, 600, 500);
        primaryStage.setScene(scene);
        
        primaryStage.setOnCloseRequest(e -> {
            try { stop(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        primaryStage.show();
    }

    // =========================================================
    // SCREEN 1: Connection UI
    // =========================================================
    private void showConnectionScreen() {
        mainLayout.getChildren().clear();

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

            new Thread(() -> {
                boolean success = false;
                try {
                    int port = Integer.parseInt(portStr);
                    success = controller.connect(ip, port);
                } catch (NumberFormatException ex) {
                    System.err.println("Error parsing port: " + ex.getMessage());
                }

                final boolean isConnected = success;

                Platform.runLater(() -> {
                    btnConnect.setDisable(false); 

                    if (isConnected) {
                        lblStatus.setText("Connected");
                        lblStatus.setTextFill(Color.GREEN);
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
    // SCREEN 2: Role Selection
    // =========================================================
    public void showRoleSelectionScreen() {
        mainLayout.getChildren().clear(); 

        Label header = new Label("Select Your Role");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Button btnCasual = new Button("Casual Diner");
        styleRoleButton(btnCasual, "#4CAF50");

        Button btnSubscriber = new Button("Subscriber");
        styleRoleButton(btnSubscriber, "#2196F3");

        Button btnRep = new Button("Representative");
        styleRoleButton(btnRep, "#FF9800");

        Button btnManager = new Button("Manager");
        styleRoleButton(btnManager, "#9C27B0");

        // --- ACTIONS ---

        btnCasual.setOnAction(e -> {
            CasualUI casualScreen = new CasualUI(mainLayout, this);
            casualScreen.start();
        });

        btnSubscriber.setOnAction(e -> {
            SubscriberUI subscriberScreen = new SubscriberUI(mainLayout, this);
            subscriberScreen.start();
        });

        btnRep.setOnAction(e -> {
            // Create Rep UI and save reference so we can update it later
            RepresentativeUI repScreen = new RepresentativeUI(mainLayout, this);
            this.repUI = repScreen; 
            repScreen.start();
        });

        btnManager.setOnAction(e -> {
             // Create Manager UI
             ManagerUI managerScreen = new ManagerUI(mainLayout, this);
             // CRITICAL FIX: Assign managerScreen to repUI. 
             // Since ManagerUI extends RepresentativeUI, this allows us to use the same refresh methods.
             this.repUI = managerScreen; 
             managerScreen.start();
        });

        VBox menuBox = new VBox(20, header, btnCasual, btnSubscriber, btnRep, btnManager);
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
    // DATA REFRESH METHODS (Called by ClientController)
    // =========================================================

    public void refreshTableData(ArrayList<Table> tables) {
        if (repUI != null) {
            repUI.updateTableData(tables);
        }
    }

    public void refreshSubscriberData(ArrayList<User> subscribers) {
        if (repUI != null) {
            repUI.updateSubscriberData(subscribers);
        }
    }

    public void refreshOrderData(ArrayList<Order> orders) {
        if (repUI != null) {
            // Ensure RepresentativeUI has this method!
            repUI.updateOrdersData(orders);
        }
    }

    public void refreshWaitingListData(ArrayList<WaitingList> list) {
        if (repUI != null) {
            // Ensure RepresentativeUI has this method!
            repUI.updateWaitingListData(list);
        }
    }

    // =========================================================
    // UTILS
    // =========================================================

    public void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void handleServerDisconnect() {
        showAlert("Connection Lost", "The server has stopped or crashed.\nRestart client.");
        showConnectionScreen();
    }

    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.disconnect(); 
        }
        super.stop();
        System.exit(0); 
    }
}