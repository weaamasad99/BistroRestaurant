package client;

import common.*;
import controllers.RepresentativeController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The RepresentativeUI class serves as the main boundary for restaurant staff (Representatives).
 * <p>
 * It provides the dashboard interface for:
 * <ul>
 * <li>Managing restaurant resources (Tables, Opening Hours).</li>
 * <li>Viewing operational data (Orders, Waiting List, Current Diners).</li>
 * <li>Registering new subscribers.</li>
 * <li>Accessing client dashboards on behalf of customers.</li>
 * </ul>
 * This class interacts with the server via the {@link RepresentativeController}.
 */
public class RepresentativeUI {

    protected VBox mainLayout;
    protected ClientUI mainUI; 
    protected RepresentativeController controller; // Specific Controller

    private String currentUsername;

    // --- UI Fields ---
    protected Button registerNewSubscriberButton;
    protected Button viewSubscriberButton;
    protected Button manageTablesButton;
    protected Button editOpeningHoursButton;
    protected Button viewOrdersButton;
    
    protected Button accessSubscriberDashButton;
    protected Button accessCasualDashButton;

    protected Button viewCurrentDinersButton;
    protected Button viewWaitingListButton;

    // --- Data Views ---
    private TableView<Table> tablesView;
    private TableView<User> subscribersView;
    private TableView<WaitingList> waitingListView;
    private TableView<Order> activeOrdersView;
    private TableView<BistroSchedule> specialDatesView; 
    private Map<String, DayRow> regularScheduleRows = new HashMap<>();
    private Map<String, BistroSchedule> cachedScheduleMap = new HashMap<>();

    /**
     * Helper class to hold UI references for a single day in the schedule editor.
     * Contains the checkbox for closed status and dropdowns for open/close times.
     */
    private class DayRow {
        CheckBox isClosed;
        ComboBox<String> openTime;
        ComboBox<String> closeTime;

        public DayRow(CheckBox c, ComboBox<String> o, ComboBox<String> cl) {
            this.isClosed = c; this.openTime = o; this.closeTime = cl;
        }
    }

    /**
     * Constructs the RepresentativeUI instance.
     *
     * @param mainLayout The main layout container for the application.
     * @param mainUI     The main application instance used for global navigation.
     */
    public RepresentativeUI(VBox mainLayout, ClientUI mainUI) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        // Initialize the specific controller using the main network controller
        this.controller = new RepresentativeController(mainUI.controller);
    }

    /**
     * Starts the representative interface by displaying the login screen.
     */
    public void start() {
        showLoginScreen();
    }

    /**
     * Displays the staff login form.
     * Authenticates the user via the server and routes them to the dashboard upon success.
     */
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
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLogin.setPrefWidth(150);

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true;");
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText();
            String pass = txtPassword.getText();
            
            if (user.isEmpty() || pass.isEmpty()) {
                mainUI.showAlert("Error", "Please enter username and password.");
                return;
            }
            
            // Send Login Request to Server
            controller.staffLogin(user, pass);
            
        });

        VBox content = new VBox(15, header, txtUsername, txtPassword, btnLogin, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        mainLayout.getChildren().add(content);
    }

    /**
     * Displays the main dashboard for the logged-in representative.
     * Sets up navigation buttons for various administrative tasks.
     *
     * @param username The username of the currently logged-in staff member.
     */
    protected void showDashboardScreen(String username) {
        this.currentUsername = username;
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
        

        // NAVIGATION 
        registerNewSubscriberButton.setOnAction(e -> registerNewSubscriber(username));


        // --- 3. Management Section ---
        Label lblMgmt = sectionTitle("Management", "#4CAF50");
        manageTablesButton = createWideButton("Manage Tables", "▦");
        editOpeningHoursButton = createWideButton("Manage Opening Hours", "⏰");
        
        manageTablesButton.setOnAction(e -> updateTableDetails());
        editOpeningHoursButton.setOnAction(e -> setOpeningHours());

        // --- 4. View Section ---
        Label lblView = sectionTitle("View", "#4CAF50");
        viewCurrentDinersButton = createWideButton("View Current Diners (Active)", "🍽");
        viewOrdersButton = createWideButton("View All Orders", "🍜"); 
        viewWaitingListButton = createWideButton("View Full Waiting List", "⏳");
        viewSubscriberButton = createWideButton("View All Subscribers", "👥");

        viewCurrentDinersButton.setOnAction(e -> displayCurrentDiners());
        viewOrdersButton.setOnAction(e -> displayAllOrders());
        viewWaitingListButton.setOnAction(e -> displayWaitingList());
        viewSubscriberButton.setOnAction(e -> displaySubscribers());

        // Layout Assembly
        VBox groupAccess = new VBox(8, lblAccess, accessSubscriberDashButton, accessCasualDashButton); groupAccess.setAlignment(Pos.CENTER);
        VBox groupOps = new VBox(8, lblOps, registerNewSubscriberButton); groupOps.setAlignment(Pos.CENTER);
        VBox groupMgmt = new VBox(8, lblMgmt, manageTablesButton, editOpeningHoursButton); groupMgmt.setAlignment(Pos.CENTER);
        VBox groupView = new VBox(8, lblView, viewCurrentDinersButton, viewOrdersButton, viewWaitingListButton, viewSubscriberButton); groupView.setAlignment(Pos.CENTER);

        VBox centralContainer = new VBox(20, groupAccess, new Separator(), groupOps, new Separator(), groupMgmt, new Separator(), groupView);
        centralContainer.setAlignment(Pos.CENTER);
        addManagerContent(centralContainer); // Hook for ManagerUI subclass

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
     * Hook method to allow subclasses (e.g., ManagerUI) to inject additional content
     * into the dashboard container.
     *
     * @param container The VBox container of the dashboard.
     */
    protected void addManagerContent(VBox container) {}

    // --- Actions ---

    /**
     * Navigates to the Subscriber Registration screen.
     * @param returnUser The username to return to after registration completes/cancels.
     */
    public void registerNewSubscriber(String returnUser) {
        // Assuming SubscriberRegistrationUI exists and works
        SubscriberRegistrationUI regUI = new SubscriberRegistrationUI(mainLayout, mainUI, () -> showDashboardScreen(returnUser));
        regUI.start();
    }

    // =================================================================================
    // 1. MANAGE TABLES UI (Integrated with DB)
    // =================================================================================
    
    /**
     * Displays the Table Management screen.
     * Allows the staff to Add, Update (seats), or Remove tables from the system.
     * Automatically requests the current table list from the server upon loading.
     */
    public void updateTableDetails() {
        mainLayout.getChildren().clear();

        Label header = new Label("Manage Tables");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        tablesView = new TableView<>();
        
        TableColumn<Table, Integer> idCol = new TableColumn<>("Table ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        
        TableColumn<Table, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        
        TableColumn<Table, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        tablesView.getColumns().addAll(idCol, seatsCol, statusCol);
        tablesView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TextField txtId = new TextField(); 
        txtId.setPromptText("ID"); 
        txtId.setPrefWidth(80);
        
        TextField txtSeats = new TextField(); 
        txtSeats.setPromptText("Seats"); 
        txtSeats.setPrefWidth(80);

        // Selection Listener
        tablesView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(String.valueOf(newVal.getTableId()));
                txtSeats.setText(String.valueOf(newVal.getSeats()));
            }
        });

        // --- ADD BUTTON ---
        Button btnAdd = new Button("Add New Table");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(txtId.getText());
                int seats = Integer.parseInt(txtSeats.getText());
                Table newTable = new Table(id, seats, "AVAILABLE");
                
                // Use Controller
                controller.addTable(newTable);
                refreshTableRequest();
            } catch (Exception ex) { mainUI.showAlert("Error", "Invalid Input (ID/Seats must be numbers)"); }
        });

        // --- UPDATE SEATS BUTTON ---
        Button btnUpdate = new Button("Update Seats Only");
        btnUpdate.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnUpdate.setOnAction(e -> {
            Table selected = tablesView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    selected.setSeats(Integer.parseInt(txtSeats.getText()));
                    
                    // Use Controller
                    controller.updateTable(selected);
                    refreshTableRequest();
                    
                    tablesView.getSelectionModel().clearSelection();
                    txtId.clear();
                    txtSeats.clear();
                } catch (Exception ex) { mainUI.showAlert("Error", "Invalid Seat Number"); }
            } else {
                mainUI.showAlert("Error", "Please select a table from the list first.");
            }
        });

        // --- REMOVE BUTTON ---
        Button btnRemove = new Button("Remove");
        btnRemove.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        btnRemove.setOnAction(e -> {
            Table selected = tablesView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Use Controller
                controller.removeTable(selected.getTableId());
                refreshTableRequest();
            }
        });

        // Layout
        javafx.scene.layout.HBox inputRow = new javafx.scene.layout.HBox(10, new Label("ID:"), txtId, new Label("Seats:"), txtSeats); 
        inputRow.setAlignment(Pos.CENTER);
        
        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(10, btnAdd, btnUpdate, btnRemove); 
        btnRow.setAlignment(Pos.CENTER);
        
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, tablesView, new Separator(), new Label("Actions:"), inputRow, btnRow, new Separator(), btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
        
        refreshTableRequest();
    }

    private void refreshTableRequest() {
        controller.getAllTables();
    }
    
    /**
     * Callback method: Updates the table view with data received from the server.
     * @param tables The list of Table objects.
     */
    public void updateTableData(ArrayList<Table> tables) {
        Platform.runLater(() -> {
            if (tablesView != null) {
                tablesView.getItems().setAll(tables);
            }
        });
    }

    // =================================================================================
    // 2. OPENING HOURS 
    // =================================================================================
    
    /**
     * Displays the Opening Hours Management screen.
     * Contains tabs for editing the "Regular Weekly Schedule" and "Special Dates".
     */
    public void setOpeningHours() {
        mainLayout.getChildren().clear();
        Label header = new Label("Manage Opening Hours");
        header.setFont(new Font("Arial", 22));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        

        tabPane.getTabs().addAll(
            new Tab("Regular Week", createRegularScheduleContent()), 
            new Tab("Special Dates", createSpecialDatesContent())
        );

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));
        
        // 2. Create the Save Button for Regular Hours
        Button btnSaveRegular = new Button("Save Regular Hours");
        btnSaveRegular.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // 3. Logic: Read from UI Map -> Send to Server
        btnSaveRegular.setOnAction(e -> {
        	ArrayList<BistroSchedule> scheduleList = new ArrayList<>();
            for (java.util.Map.Entry<String, DayRow> entry : regularScheduleRows.entrySet()) {
                String dayName = entry.getKey();
                DayRow row = entry.getValue();
                
                // Create the data object
                BistroSchedule item = new BistroSchedule(
                    dayName,
                    row.openTime.getValue(),
                    row.closeTime.getValue(),
                    row.isClosed.isSelected(),
                    "REGULAR",
                    null
                );
                
                // Send update to server
                	scheduleList.add(item);            
            }
            controller.saveSchedule(scheduleList);
        });

        // 4. Layout
        VBox content = new VBox(15, header, tabPane, btnSaveRegular, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
        
        // 5. Load data immediately to populate the dropdowns
        controller.getSchedule();
    }

    // =================================================================================
    // 3. SUBSCRIBERS (Integrated with DB)
    // =================================================================================
    
    /**
     * Displays the list of all registered subscribers.
     * Fetches data from the server and populates a table view.
     */
    public void displaySubscribers() {
        mainLayout.getChildren().clear();

        Label header = new Label("All Subscribers");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        subscribersView = new TableView<>();
        
        TableColumn<User, Integer> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        
        TableColumn<User, String> fNameCol = new TableColumn<>("First Name");
        fNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<User, String> lNameCol = new TableColumn<>("Last Name");
        lNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        subscribersView.getColumns().addAll(idCol, fNameCol, lNameCol, phoneCol);
        subscribersView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, subscribersView, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(650); 
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);

        // Request Data via Controller
        controller.getAllSubscribers();
    }

    /**
     * Callback method: Updates the subscribers view with data received from the server.
     * @param subscribers The list of User objects (subscribers).
     */
    public void updateSubscriberData(ArrayList<User> subscribers) {
        Platform.runLater(() -> {
            if (subscribersView != null) {
                subscribersView.getItems().setAll(subscribers);
            }
        });
    }

    // =================================================================================
    // 4. WAITING LIST (Integrated with DB)
    // =================================================================================
    
    /**
     * Displays the current Waiting List.
     */
    public void displayWaitingList() {
        mainLayout.getChildren().clear();

        Label header = new Label("Current Waiting List");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        waitingListView = new TableView<>();
        
        TableColumn<WaitingList, Integer> idCol = new TableColumn<>("Wait ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("waitingId"));
        
        TableColumn<WaitingList, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateRequested"));
        
        TableColumn<WaitingList, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeRequested"));
        
        TableColumn<WaitingList, Integer> sizeCol = new TableColumn<>("Diners");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("numOfDiners"));
        
        TableColumn<WaitingList, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        waitingListView.getColumns().addAll(idCol, dateCol, timeCol, sizeCol, statusCol);
        waitingListView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, waitingListView, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);

        // Request Data via Controller
        controller.getWaitingList();
    }
    
    /**
     * Callback method: Updates the waiting list view with data received from the server.
     * @param list The list of WaitingList entries.
     */
    public void updateWaitingListData(ArrayList<WaitingList> list) {
        Platform.runLater(() -> {
            if (waitingListView != null) waitingListView.getItems().setAll(list);
        });
    }

    // =================================================================================
    // 5. CURRENT DINERS (Integrated with DB)
    // =================================================================================
    
    /**
     * Displays the list of currently active diners (orders that are currently seated).
     */
    public void displayCurrentDiners() {
        mainLayout.getChildren().clear();

        Label header = new Label("Active Orders / Diners");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        activeOrdersView = new TableView<>();
        
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order #");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        
        TableColumn<Order, Integer> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        

        TableColumn<Order, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        

        TableColumn<Order, Integer> dinersCol = new TableColumn<>("Diners");
        dinersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfDiners"));

        
        activeOrdersView.getColumns().addAll(orderIdCol, userCol, timeCol,dinersCol );
        activeOrdersView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, activeOrdersView, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(650); 
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
        
        // Request Data via Controller
        controller.getActiveOrders();
    }
    
    // =================================================================================
    // 6. ALL ORDERS (Integrated with DB)
    // =================================================================================
    
    /**
     * Displays the list of all orders (History and Active).
     */
    public void displayAllOrders() {
        mainLayout.getChildren().clear();

        Label header = new Label("All Orders");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        activeOrdersView = new TableView<>();
        
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order #");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        
        TableColumn<Order, Integer> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        
        
        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        
        TableColumn<Order, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Order, Integer> dinersCol = new TableColumn<>("Diners");
        dinersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfDiners"));

        
        activeOrdersView.getColumns().addAll(orderIdCol, userCol, dateCol, timeCol,dinersCol ,statusCol);
        activeOrdersView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, activeOrdersView, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(650); // 
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
        
        // Request Data via Controller
        controller.getAllOrders();
      }
      
    /**
     * Callback method: Updates the orders view with data received from the server.
     * @param orders The list of Order objects.
     */
    public void updateOrdersData(ArrayList<Order> orders) {
        Platform.runLater(() -> {
            if (activeOrdersView != null) activeOrdersView.getItems().setAll(orders);
        });
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
    
    /**
     * Shows a dialog asking for a Subscriber ID.
     * If entered, sends a verification request to the server to potentially access their dashboard.
     */
    private void promptForSubscriberAccess(String returnUser) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Access"); 
        dialog.setHeaderText("Enter Subscriber ID:");
        
        dialog.showAndWait().ifPresent(id -> {
            if(!id.isEmpty()) {
                 // 1. Basic Validation (Optional, but good UX)
                 if (!id.matches("\\d+")) {
                     mainUI.showAlert("Error", "Subscriber ID must be a number.");
                     return;
                 }

                 // 2. SEND REQUEST TO SERVER INSTEAD OF OPENING SCREEN
                 // The ClientController will handle the response (USER_FOUND / USER_NOT_FOUND)
                 controller.checkUserExists(id);
                 
            }
        });
    }

    /**
     * Shows a dialog asking for a Phone Number (Casual User).
     * If entered, sends a verification request to the server.
     */
    private void promptForCasualAccess(String returnUser) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Access"); 
        dialog.setHeaderText("Enter Phone Number:");
        
        dialog.showAndWait().ifPresent(phone -> {
            if(!phone.isEmpty()) {
                 // SEND REQUEST TO SERVER
                 controller.checkUserExists(phone);

            }
        });
    }

    // --- Helpers for Opening Hours ---
    private javafx.collections.ObservableList<String> generateTimeSlots() {
        javafx.collections.ObservableList<String> slots = javafx.collections.FXCollections.observableArrayList();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 30) {
                slots.add(String.format("%02d:%02d", h, m));
            }
        }
        return slots;
    }

    private VBox createRegularScheduleContent() {
        regularScheduleRows.clear(); // Reset map when recreating view
        
        Label lblInfo = new Label("Set standard opening hours:");
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(15); grid.setAlignment(Pos.CENTER); grid.setPadding(new Insets(10));
        
        javafx.collections.ObservableList<String> timeSlots = generateTimeSlots();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        grid.add(new Label("Day"), 0, 0); grid.add(new Label("Status"), 1, 0);
        grid.add(new Label("Open"), 2, 0); grid.add(new Label("Close"), 4, 0);

        for (int i = 0; i < days.length; i++) {
            String dayName = days[i];
            
            //  Use DB data if we have it, otherwise default ---
            String open = "08:00";
            String close = "22:00";
            boolean isClosed = false;

            if (cachedScheduleMap.containsKey(dayName)) {
                BistroSchedule s = cachedScheduleMap.get(dayName);
                if (s.getOpenTime() != null) open = s.getOpenTime();
                if (s.getCloseTime() != null) close = s.getCloseTime();
                isClosed = s.isClosed();
            }
            // ---------------------------------------------------------------

            addSmartHoursRow(grid, i + 1, dayName, open, close, isClosed, timeSlots);
        }
        
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true); 
        scroll.setPrefHeight(350);
        return new VBox(10, lblInfo, scroll);
    }
    
    /**
     * Helper to create a single row for the Regular Schedule Grid.
     * Adds the controls to the UI and registers them in the `regularScheduleRows` map.
     */
    private void addSmartHoursRow(javafx.scene.layout.GridPane grid, int row, String day, String defaultOpen, String defaultClose, boolean isClosed, javafx.collections.ObservableList<String> times) {
        Label lblDay = new Label(day + ":"); 
        CheckBox chkClosed = new CheckBox("Closed"); chkClosed.setSelected(isClosed);
        ComboBox<String> cmbOpen = new ComboBox<>(times); cmbOpen.setValue(defaultOpen); cmbOpen.setPrefWidth(90);
        ComboBox<String> cmbClose = new ComboBox<>(times); cmbClose.setValue(defaultClose); cmbClose.setPrefWidth(90);
        Label lblDash = new Label("-");

        cmbOpen.disableProperty().bind(chkClosed.selectedProperty());
        cmbClose.disableProperty().bind(chkClosed.selectedProperty());

        // *** Save references to the Map ***
        regularScheduleRows.put(day, new DayRow(chkClosed, cmbOpen, cmbClose));

        grid.add(lblDay, 0, row); grid.add(chkClosed, 1, row); grid.add(cmbOpen, 2, row); grid.add(lblDash, 3, row); grid.add(cmbClose, 4, row);
    }
    
    /**
     * Creates the UI content for managing Special Dates (Holidays/Events).
     */
    private VBox createSpecialDatesContent() {
        Label lblInfo = new Label("Define specific dates with different hours:");
        
        specialDatesView = new TableView<>();
        specialDatesView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); 
        specialDatesView.setPrefHeight(200);

        TableColumn<BistroSchedule, String> dateCol = new TableColumn<>("Date"); 
        dateCol.setCellValueFactory(new PropertyValueFactory<>("identifier"));
        
        TableColumn<BistroSchedule, String> hoursCol = new TableColumn<>("Hours"); 
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("hoursString")); 
        
        TableColumn<BistroSchedule, String> eventCol = new TableColumn<>("Event"); 
        eventCol.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        
        specialDatesView.getColumns().addAll(dateCol, hoursCol, eventCol);

        // Inputs
        DatePicker datePicker = new DatePicker(); 
        datePicker.setPromptText("Select Date");
        TextField txtOpen = new TextField(); txtOpen.setPromptText("08:00");
        TextField txtClose = new TextField(); txtClose.setPromptText("22:00");
        TextField txtEvent = new TextField(); txtEvent.setPromptText("Event Name");
        
        Button btnAdd = new Button("Add / Update"); 
        btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        btnAdd.setOnAction(e -> {
            if(datePicker.getValue() != null && !txtOpen.getText().isEmpty()) {
                BistroSchedule newItem = new BistroSchedule(
                    datePicker.getValue().toString(),
                    txtOpen.getText(),
                    txtClose.getText(),
                    false, 
                    "SPECIAL",
                    txtEvent.getText()
                );
                // Save to DB
                controller.saveScheduleItem(newItem);
                controller.getSchedule(); // Refresh
            }
        });
        
        Button btnRemove = new Button("Remove"); 
        btnRemove.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        btnRemove.setOnAction(e -> {
            BistroSchedule selected = specialDatesView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.deleteScheduleItem(selected.getIdentifier());
                controller.getSchedule(); // Refresh
            }
        });
     


        javafx.scene.layout.HBox inputRow = new javafx.scene.layout.HBox(10, datePicker, txtOpen, new Label("-"), txtClose, txtEvent); 
        inputRow.setAlignment(Pos.CENTER);
        
        VBox layout = new VBox(15, lblInfo, specialDatesView, inputRow, new javafx.scene.layout.HBox(10, btnAdd, btnRemove));
        layout.setAlignment(Pos.CENTER); layout.setPadding(new Insets(15));
        return layout;
    }
    
    /**
     * Callback method: Updates the Schedule UI (Regular and Special) with data from server.
     * @param scheduleList The list of schedule items.
     */
    public void updateScheduleData(ArrayList<BistroSchedule> scheduleList) {
        Platform.runLater(() -> {
            
            // 1. Update the Local Cache
            for (BistroSchedule s : scheduleList) {
                cachedScheduleMap.put(s.getIdentifier(), s);
            }

            // 2. Update the UI
            if (specialDatesView != null) specialDatesView.getItems().clear();

            for (BistroSchedule s : scheduleList) {
                
                // Update Regular Days (Dropdowns)
                if ("REGULAR".equals(s.getType())) {
                    DayRow row = regularScheduleRows.get(s.getIdentifier()); 
                    if (row != null) {
                        row.isClosed.setSelected(s.isClosed());
                        if(s.getOpenTime() != null) row.openTime.setValue(s.getOpenTime());
                        if(s.getCloseTime() != null) row.closeTime.setValue(s.getCloseTime());
                    }
                }
                
                // Update Special Dates (Table)
                else if ("SPECIAL".equals(s.getType())) {
                    if (specialDatesView != null) {
                        specialDatesView.getItems().add(s);
                    }
                }
            }
        });
    }
    
    
    
    /**
     * Restores the dashboard view for the currently logged-in representative.
     * Used when returning from a client's view.
     */
    public void restoreDashboard() {
        if (currentUsername != null) {
            showDashboardScreen(currentUsername);
        } else {
            // Fallback if username is lost (shouldn't happen in normal flow)
            mainUI.showRoleSelectionScreen();
        }
    }
    
}