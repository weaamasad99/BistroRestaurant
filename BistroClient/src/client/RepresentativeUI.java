package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class RepresentativeUI {

    private VBox mainLayout;
    private ClientUI mainUI; // Reference back to main

    public RepresentativeUI(VBox mainLayout, ClientUI mainUI) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
    }

    public void start() {
        showLoginScreen();
    }

    /**
     * SCREEN 1: Representative Login
     */
    private void showLoginScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Staff Login");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblInstruction = new Label("Please enter staff credentials:");

        // Username Field
        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Employee Username");
        txtUsername.setMaxWidth(300);
        txtUsername.setStyle("-fx-font-size: 14px;");

        // Password Field
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        txtPassword.setMaxWidth(300);
        txtPassword.setStyle("-fx-font-size: 14px;");

        Button btnLogin = new Button("Login");
        btnLogin.setPrefWidth(150);
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");

        // Navigation: Back to Main Role Selection
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        // Action: Login Logic
        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText().trim();
            String pass = txtPassword.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                mainUI.showAlert("Invalid Input", "Please enter username and password.");
            } else {
                // Future: Validate against DB
                showDashboardScreen(user);
            }
        });

        VBox content = new VBox(15, header, lblInstruction, txtUsername, txtPassword, btnLogin, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    /**
     * SCREEN 2: Representative Dashboard
     */
    private void showDashboardScreen(String username) {
        mainLayout.getChildren().clear();

        Label header = new Label("Staff Dashboard");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Logged in as: " + username);
        subHeader.setTextFill(Color.GRAY);

        // --- SECTION 1: Customer Service (Acting on behalf of client) And Registerations ---
        Label lblService = new Label("Customer Service");
        lblService.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3; -fx-underline: true;");
     // 1. Register New Customer
        Button btnRegister = createOptionButton("Register New Customer", "👤");
        btnRegister.setOnAction(e -> mainUI.showAlert("Registration", "Open Customer Registration Form... (Not Implemented)"));
        
        // 2. New Reservation (Uses ReservationUI)
        Button btnNewRes = createOptionButton("New Reservation (For Client)", "📅");
        btnNewRes.setOnAction(e -> {
            // Define return path
            Runnable onBack = () -> showDashboardScreen(username);
            // Open ReservationUI passing a placeholder ID for the client
            ReservationUI resUI = new ReservationUI(mainLayout, mainUI, onBack, "Client-Via-Rep"); 
            resUI.start();
        });

        // 3. Waiting List (Uses WaitingListUI)
        Button btnWaiting = createOptionButton("Add to Waiting List", "⏳");
        btnWaiting.setOnAction(e -> {
            Runnable onBack = () -> showDashboardScreen(username);
            // Open WaitingListUI (Assuming Casual/Walk-in mode = true)
            WaitingListUI waitScreen = new WaitingListUI(mainLayout, mainUI, onBack, "Client-Via-Rep", true); 
            waitScreen.start();
        });
        
        // 4. Check-In (Uses IdentificationUI)
        Button btnCheckIn = createOptionButton("Check-In / Identify Client", "📋");
        btnCheckIn.setOnAction(e -> {
            Runnable onBack = () -> showDashboardScreen(username);
            IdentificationUI idScreen = new IdentificationUI(mainLayout, mainUI, onBack, "Client-Via-Rep");
            idScreen.start();
        });

        // 5. Checkout Uses CheckoutUI 
        Button btnCheckout = createOptionButton("Process Payment / Checkout", "💳");
        btnCheckout.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand;");
        btnCheckout.setOnAction(e -> {
             Runnable onBack = () -> showDashboardScreen(username);
             // Ensure CheckoutUI exists in your package or use Alert for now
             CheckoutUI checkoutScreen = new CheckoutUI(mainLayout, mainUI, onBack);
             checkoutScreen.start();
        });

        // --- SECTION 2: Management ---
        Label lblManage = new Label("Management & Report");
        lblManage.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-underline: true;");

        // 1. Manage Tables
        Button btnTables = createOptionButton("Manage Tables", "▦");
        btnTables.setOnAction(e -> mainUI.showAlert("System", "Opening Table Management... (Not Implemented)"));
     // 2. Manage Manage Opening Hours
        Button btnHours = createOptionButton("Manage Opening Hours", "⏰");
        btnHours.setOnAction(e -> mainUI.showAlert("Management", "Open Hours Management Screen..."));
        
        Label lblInfo = new Label("Views & Live Data");
        lblInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0; -fx-underline: true;");
     // 1. View Active Orders 
        Button btnViewOrders = createOptionButton("View Active Orders", "🍜");
        btnViewOrders.setOnAction(e -> mainUI.showAlert("View Data", "Fetching Active Orders... (Not Implemented)"));

        // 2. View Waiting List 
        Button btnViewWaitList = createOptionButton("View Full Waiting List", "📜");
        btnViewWaitList.setOnAction(e -> mainUI.showAlert("View Data", "Fetching Waiting List... (Not Implemented)"));

        // 3. View Current Diners
        Button btnCurrentDiners = createOptionButton("View Current Diners (Active)", "🥘");
        btnCurrentDiners.setOnAction(e -> {

             mainUI.showAlert("System", "Fetching Active Diners List...");
             //Future
        });
     // 4. View Subscribers
        Button btnViewSubscribers = createOptionButton("View All Subscribers", "👥");
        btnViewSubscribers.setOnAction(e -> mainUI.showAlert("Data", "Fetch Subscriber List..."));

     // --- Layout Assembly ---
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        
       
        container.getChildren().addAll(
            // Section 1: Customer Service
            lblService,btnRegister, btnNewRes, btnWaiting, btnCheckIn, btnCheckout,
            new Separator(),
            
            // Section 2: Management
            lblManage, btnTables, btnHours, // <--- Added btnHours here
            new Separator(),
            
            // Section 3: Views
            lblInfo,                
            btnCurrentDiners, 
            btnViewSubscribers,
            btnViewWaitList,
            btnViewOrders
            
        );

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #ddd; -fx-text-fill: black; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> mainUI.showRoleSelectionScreen());

        VBox content = new VBox(15, header, subHeader, new Separator(), container, new Separator(), btnLogout);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400); 
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    // Helper method for styling buttons
    private Button createOptionButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(280);
        btn.setPrefHeight(35);
        btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 13px; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            if (!text.contains("Payment") && !text.contains("Checkout")) 
                btn.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbb; -fx-font-size: 13px; -fx-cursor: hand;");
        });
        btn.setOnMouseExited(e -> {
            if (!text.contains("Payment") && !text.contains("Checkout"))
                btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 13px; -fx-cursor: hand;");
        });

        return btn;
    }
}