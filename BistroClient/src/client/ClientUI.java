package client;

import common.BistroSchedule;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class ClientUI extends Application {

	private Stage primaryStage;
    // Reference to the active Representative/Manager screen
    // This allows us to pass data (tables, orders) from the server to the screen.
    public RepresentativeUI repUI;
    public User currentUser;
    public CheckoutUI checkoutUI;
    
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
    	this.primaryStage = primaryStage;
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
    
    
    public void openSubscriberDashboard() {
        Platform.runLater(() -> {
            SubscriberUI subScreen = new SubscriberUI(mainLayout, this);
            
            // 1. Define the correct back action
            Runnable backAction;
            if (repUI != null) {
                backAction = () -> repUI.restoreDashboard();
            } else {
                backAction = () -> showRoleSelectionScreen();
            }
            
            if (currentUser != null && currentUser.getSubscriberNumber() != null) {
                // 2. PASS 'backAction' HERE (You were passing showRoleSelectionScreen directly)
                subScreen.showDashboardScreen(
                    currentUser.getUsername(), 
                    currentUser.getSubscriberNumber(), 
                    backAction // <--- CHANGE THIS
                );
            } else {
                showAlert("Error", "Subscriber data is missing.");
            }
        });
    }
    
    
    public void openCasualDashboard() {
        Platform.runLater(() -> {
            CasualUI casualScreen = new CasualUI(mainLayout, this);
            
            // 1. Define the correct back action
            Runnable backAction;
            if (repUI != null) {
                backAction = () -> repUI.restoreDashboard();
            } else {
                backAction = () -> showRoleSelectionScreen();
            }
            
            if (currentUser != null) {
                // 2. PASS 'backAction' HERE
                casualScreen.showOptionsScreen(
                    currentUser.getPhoneNumber(), 
                    backAction // <--- CHANGE THIS
                );
            } else {
                showAlert("Error", "User data is missing.");
            }
        });
    }
    
    
    public void openRepresentativeDashboard(User user) {
        Platform.runLater(() -> {
            // 1. Initialize RepresentativeUI if null (though it should exist from login)
            if (repUI == null) {
                repUI = new RepresentativeUI(mainLayout, this);
            }
            
            // 2. Open the dashboard
            repUI.showDashboardScreen(user.getUsername());
        });
    }

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
    public void refreshScheduleData(ArrayList<BistroSchedule> schedule) {
        // Only update if the Representative screen is currently active/loaded
        if (repUI != null) {
            repUI.updateScheduleData(schedule);
        }
    }
    
    public void activateBillView(String code) {
        if (checkoutUI != null) {
            // Triggers the next screen in CheckoutUI
            checkoutUI.fetchBillDetails(code);
        }
        else {
        	System.out.print("hhhhh");
        }
    }
    
    public void setCheckoutUI(CheckoutUI checkoutUI) {
    	this.checkoutUI = checkoutUI;
    }
    
    
    
    /**
     * Displays a professional "Digital Card" window with a live QR Code.
     */
    public void showDigitalCard(User user) {
        javafx.stage.Stage cardStage = new javafx.stage.Stage();
        cardStage.setTitle("Subscriber Card");

        // --- 1. The Card Container (Styled to look like a plastic card) ---
        javafx.scene.layout.VBox cardBox = new javafx.scene.layout.VBox(10);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPadding(new Insets(20));
        // Gold gradient background for a "Premium" feel
        cardBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #DAA520, #FFD700); " +
                         "-fx-background-radius: 15; " +
                         "-fx-border-color: #B8860B; -fx-border-width: 2; -fx-border-radius: 15; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);");

        // --- 2. Bistro Logo / Header ---
        Label lblTitle = new Label("BISTRO SUBSCRIBER");
        lblTitle.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.WHITE);

        // --- 3. The QR Code (Generated on the fly) ---
        // This API takes the ID and returns a QR image. No libraries needed.
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + user.getSubscriberNumber();
        javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView(qrUrl);
        qrView.setFitWidth(120);
        qrView.setFitHeight(120);

        // --- 4. User Details ---
        Label lblName = new Label(user.getFirstName() + " " + user.getLastName());
        lblName.setFont(Font.font("Arial", 16));
        
        Label lblNum = new Label("ID: " + user.getSubscriberNumber());
        lblNum.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 20));

        // --- 5. Close Button ---
        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> cardStage.close());
        btnClose.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-cursor: hand;");

        // Assembly
        cardBox.getChildren().addAll(lblTitle, qrView, lblName, lblNum, btnClose);

        javafx.scene.Scene scene = new javafx.scene.Scene(cardBox, 320, 420);
        scene.setFill(Color.TRANSPARENT); // Important for rounded corners
        cardStage.setScene(scene);
        cardStage.initStyle(javafx.stage.StageStyle.TRANSPARENT); // Removes OS window borders
        cardStage.show();
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