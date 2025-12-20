package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Boundary class for Representative Dashboard.
 */
public class RepresentativeUI {

    protected VBox mainLayout;
    protected ClientUI mainUI; 

    // --- Fields from Class Diagram ---
    protected Button registerNewSubscriberButton;
    protected Button viewSubscriberButton;
    protected Button manageTablesButton;
    protected Button editOpeningHoursButton;
    protected Button viewOrdersButton;
    
    // Additional buttons for the layout
    protected Button accessSubscriberDashButton;
    protected Button accessCasualDashButton;
    protected Button checkInClientButton;
    protected Button viewCurrentDinersButton;
    protected Button viewWaitingListButton;

    public RepresentativeUI(VBox mainLayout, ClientUI mainUI) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
    }

    public void start() {
        showLoginScreen();
    }

    protected void showLoginScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Staff Login");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        TextField txtUsername = new TextField(); 
        txtUsername.setPromptText("Username"); 
        txtUsername.setMaxWidth(300);

        PasswordField txtPassword = new PasswordField(); 
        txtPassword.setPromptText("Password"); 
        txtPassword.setMaxWidth(300);

        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLogin.setPrefWidth(150);

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText();
            String pass = txtPassword.getText();
            if ((user.equals("rep") && pass.equals("1234")) || (user.equals("admin") && pass.equals("admin"))) {
                showDashboardScreen(user);
            } else {
                mainUI.showAlert("Error", "Invalid Credentials (Try: rep/1234)");
            }
        });

        VBox content = new VBox(15, header, txtUsername, txtPassword, btnLogin, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        mainLayout.getChildren().add(content);
    }

    protected void showDashboardScreen(String username) {
        mainLayout.getChildren().clear();

        Label header = new Label("Staff Dashboard");
        header.setFont(new Font("Arial", 26));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label subHeader = new Label("Logged in as: " + username);
        subHeader.setTextFill(Color.GRAY);

        // --- 1. Access Client/Subscriber Section ---
        Label lblAccess = sectionTitle("Access Client/Subscriber", "#E91E63");
        accessSubscriberDashButton = createWideButton("Access Subscriber Dashboard", "🎭");
        accessCasualDashButton = createWideButton("Access Casual Dashboard", "🎭");
        
        accessSubscriberDashButton.setOnAction(e -> promptForSubscriberAccess(username));
        accessCasualDashButton.setOnAction(e -> promptForCasualAccess(username));

        // --- 2. General Operations Section ---
        Label lblOps = sectionTitle("General Operations", "#2196F3");
        registerNewSubscriberButton = createWideButton("Register New Customer", "👤");
        checkInClientButton = createWideButton("Check-In / Identify Client", "📋");

        // NAVIGATION LOGIC
        registerNewSubscriberButton.setOnAction(e -> registerNewSubscriber(username));
        checkInClientButton.setOnAction(e -> {
            IdentificationUI idUI = new IdentificationUI(mainLayout, mainUI, () -> showDashboardScreen(username), "Client-Via-Rep");
            idUI.start();
        });

        // --- 3. Management Section ---
        Label lblMgmt = sectionTitle("Management", "#4CAF50");
        manageTablesButton = createWideButton("Manage Tables", "▦");
        editOpeningHoursButton = createWideButton("Manage Opening Hours", "⏰");
        
        manageTablesButton.setOnAction(e -> updateTableDetails());
        editOpeningHoursButton.setOnAction(e -> setOpeningHours());

        // --- 4. View Section ---
        Label lblView = sectionTitle("View", "#4CAF50");
        viewCurrentDinersButton = createWideButton("View Current Diners (Active)", "🍽");
        viewOrdersButton = createWideButton("View Active Orders", "🍜");
        viewWaitingListButton = createWideButton("View Full Waiting List", "⏳");
        viewSubscriberButton = createWideButton("View All Subscribers", "👥");

        viewCurrentDinersButton.setOnAction(e -> displayCurrentDiners());
        viewOrdersButton.setOnAction(e -> displayWaitingList());
        viewWaitingListButton.setOnAction(e -> displayWaitingList());
        viewSubscriberButton.setOnAction(e -> mainUI.showAlert("Info", "Showing Subscriber List..."));

        // Helper Layouts
        VBox groupAccess = new VBox(8, lblAccess, accessSubscriberDashButton, accessCasualDashButton);
        groupAccess.setAlignment(Pos.CENTER);
        
        VBox groupOps = new VBox(8, lblOps, registerNewSubscriberButton, checkInClientButton);
        groupOps.setAlignment(Pos.CENTER);
        
        VBox groupMgmt = new VBox(8, lblMgmt, manageTablesButton, editOpeningHoursButton);
        groupMgmt.setAlignment(Pos.CENTER);
        
        VBox groupView = new VBox(8, lblView, viewCurrentDinersButton, viewOrdersButton, viewWaitingListButton, viewSubscriberButton);
        groupView.setAlignment(Pos.CENTER);

        // Allow Manager to add content here (Override hook)
        VBox centralContainer = new VBox(20, groupAccess, new Separator(), groupOps, new Separator(), groupMgmt, new Separator(), groupView);
        centralContainer.setAlignment(Pos.CENTER);
        
        // This is where ManagerUI will inject buttons
        addManagerContent(centralContainer);

        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> mainUI.showRoleSelectionScreen());

        VBox content = new VBox(20, header, subHeader, new Separator(), centralContainer, new Separator(), btnLogout);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        mainLayout.getChildren().add(scroll);
    }
    
    /**
     * Hook for ManagerUI to add buttons. Empty in base class.
     */
    protected void addManagerContent(VBox container) {
        // Do nothing in Rep UI
    }

    // --- Actions ---

    public void registerNewSubscriber(String returnUser) {
        SubscriberRegistrationUI regUI = new SubscriberRegistrationUI(mainLayout, mainUI, () -> showDashboardScreen(returnUser));
        regUI.start();
    }

    public void updateTableDetails() {
        mainUI.showAlert("System", "Opening Table Management Screen...");
    }

    public void setOpeningHours() {
        mainUI.showAlert("System", "Opening Hours Editor...");
    }

    public void displayWaitingList() {
        mainUI.showAlert("System", "Fetching Waiting List Data...");
    }

    public void displayCurrentDiners() {
        mainUI.showAlert("System", "Fetching Current Diners...");
    }

    // --- Helpers ---
    protected Button createWideButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(300);
        btn.setPrefHeight(35);
        btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-cursor: hand; -fx-font-size: 13px;");
        return btn;
    }

    private Label sectionTitle(String title, String color) {
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-underline: true;");
        return lbl;
    }
    
    private void promptForSubscriberAccess(String returnUser) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Access"); dialog.setHeaderText("Enter Subscriber ID:");
        dialog.showAndWait().ifPresent(id -> {
            if(!id.isEmpty()) {
                 SubscriberUI subUI = new SubscriberUI(mainLayout, mainUI);
                 subUI.showDashboardScreen("Client(Via Rep)", id, () -> showDashboardScreen(returnUser));
            }
        });
    }

    private void promptForCasualAccess(String returnUser) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Access"); dialog.setHeaderText("Enter Phone Number:");
        dialog.showAndWait().ifPresent(phone -> {
            if(!phone.isEmpty()) {
                 CasualUI casualUI = new CasualUI(mainLayout, mainUI);
                 casualUI.showOptionsScreen(phone, () -> showDashboardScreen(returnUser));
            }
        });
    }
}