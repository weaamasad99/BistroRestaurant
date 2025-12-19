package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.Optional; 

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
     * SCREEN 1: Staff
     */
    private void showLoginScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Staff Login");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblInstruction = new Label("Please enter staff credentials:");

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Employee Username");
        txtUsername.setMaxWidth(300);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        txtPassword.setMaxWidth(300);

        Button btnLogin = new Button("Login");
        btnLogin.setPrefWidth(150);
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");

        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText().trim();
            String pass = txtPassword.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                mainUI.showAlert("Invalid Input", "Please enter username and password.");
            } else {
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

        // ==========================================================
        // SECTION 1: Act As Client 
        // ==========================================================
        Label lblAct = new Label("Access Client/Subscriber");
        lblAct.setStyle("-fx-font-weight: bold; -fx-text-fill: #E91E63; -fx-underline: true;");

        // 1. Act as Subscriber
        Button btnActSub = createOptionButton("Access Subscriber Dashboard", "🎭");
        btnActSub.setOnAction(e -> {
            // Popup Dialog to get ID
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Subscriber Access");
            dialog.setHeaderText("Enter Subscriber ID to manage:");
            dialog.setContentText("ID:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(id -> {
                if(!id.isEmpty()) {
                    SubscriberUI subUI = new SubscriberUI(mainLayout, mainUI);
                    
                    //  We pass a Runnable that brings us back HERE (Rep Dashboard) when done
                    Runnable returnToRep = () -> showDashboardScreen(username);
                    
                    subUI.showDashboardScreen("Client(Via Rep)", id, returnToRep);
                }
            });
        });

        // 2. Act as Casual
        Button btnActCas = createOptionButton("Access Casual Dashboard", "🎭");
        btnActCas.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Casual Diner Access");
            dialog.setHeaderText("Enter Customer Phone Number:");
            dialog.setContentText("Phone:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(phoneinput -> {
            	String phone = phoneinput.trim();
            	if (phone.isEmpty() || !phone.matches("\\d+")) { 
                    mainUI.showAlert("Invalid Input", "Please enter a valid phone number (digits only).");
                }
                else if (!phone.startsWith("05") || phone.length() != 10) {
                    mainUI.showAlert("Invalid Input", "Please enter a valid phone number .");
                } 
                else {
                    // Validation Passed - Open Dashboard
                    CasualUI casualUI = new CasualUI(mainLayout, mainUI);
                    //  Return to Rep Dashboard when done
                    Runnable returnToRep = () -> showDashboardScreen(username);
                    
                    casualUI.showOptionsScreen(phone, returnToRep);
                }
            });
        });

        VBox actBox = new VBox(8, lblAct, btnActSub, btnActCas);
        actBox.setAlignment(Pos.CENTER);


        // ==========================================================
        // SECTION 2: General Operations
        // ==========================================================
        Label lblService = new Label("General Operations");
        lblService.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3; -fx-underline: true;");
        
        Button btnRegister = createOptionButton("Register New Customer", "👤");
        btnRegister.setOnAction(e -> mainUI.showAlert("Registration", "Open Customer Registration Form... (Not Implemented)"));
        
        Button btnCheckIn = createOptionButton("Check-In / Identify Client", "📋");
        btnCheckIn.setOnAction(e -> {
            Runnable onBack = () -> showDashboardScreen(username);
            IdentificationUI idScreen = new IdentificationUI(mainLayout, mainUI, onBack, "Client-Via-Rep");
            idScreen.start();
        });

        VBox opsBox = new VBox(8, lblService, btnRegister, btnCheckIn);
        opsBox.setAlignment(Pos.CENTER);

        
        // ==========================================================
        // SECTION 3: Management & Reports
        // ==========================================================
        Label lblManage = new Label("Management");
        lblManage.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-underline: true;");

        Button btnTables = createOptionButton("Manage Tables", "▦");
        btnTables.setOnAction(e -> mainUI.showAlert("System", "Opening Table Management... (Not Implemented)"));
        
        Button btnHours = createOptionButton("Manage Opening Hours", "⏰");
        btnHours.setOnAction(e -> mainUI.showAlert("Management", "Open Hours Management Screen..."));
        VBox manageBox = new VBox(8, lblManage, btnTables, btnHours);
        manageBox.setAlignment(Pos.CENTER);
        
        Label lblView = new Label("View");
        lblView.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-underline: true;");
        
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

        VBox ViewBox = new VBox(8, lblView, btnCurrentDiners,btnViewOrders,btnViewWaitList,btnViewSubscribers);
        ViewBox.setAlignment(Pos.CENTER);


        // --- Layout Assembly ---
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(actBox, new Separator(), opsBox, new Separator(), manageBox, new Separator(),ViewBox);
        

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

    private Button createOptionButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(300);
        btn.setPrefHeight(35);
        btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 13px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bbb; -fx-font-size: 13px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-font-size: 13px; -fx-cursor: hand;"));
        return btn;
    }
}