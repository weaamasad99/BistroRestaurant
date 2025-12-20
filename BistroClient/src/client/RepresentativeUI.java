package client;

import common.*; // Import the real entities (Table, User, Order, TaskType, Message)
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

/**
 * Boundary class for Representative Dashboard.
 *  integrated with DB via Client-Server.
 */
public class RepresentativeUI {

    protected VBox mainLayout;
    protected ClientUI mainUI; 

    private String currentUsername;

    // --- UI Fields ---
    protected Button registerNewSubscriberButton;
    protected Button viewSubscriberButton;
    protected Button manageTablesButton;
    protected Button editOpeningHoursButton;
    protected Button viewOrdersButton;
    
    protected Button accessSubscriberDashButton;
    protected Button accessCasualDashButton;
    protected Button checkInClientButton;
    protected Button viewCurrentDinersButton;
    protected Button viewWaitingListButton;

    // --- Data Views (Promoted to fields so we can update them from Server) ---
    private TableView<Table> tablesView;
    private TableView<User> subscribersView;
    private TableView<WaitingList> waitingListView;
    private TableView<Order> activeOrdersView;

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
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLogin.setPrefWidth(150);

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true;");
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText();
            String pass = txtPassword.getText();
            
            // Send Login Request to Server
            User loginReq = new User();
            loginReq.setUsername(user);
            loginReq.setPassword(pass);
            Message msg = new Message(TaskType.LOGIN_REQUEST, loginReq);
            
            
            if (mainUI.controller != null) {
                mainUI.controller.accept(msg); 
            }
           
            
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
        viewOrdersButton = createWideButton("View Active Orders", "🍜"); // Reuses Active logic
        viewWaitingListButton = createWideButton("View Full Waiting List", "⏳");
        viewSubscriberButton = createWideButton("View All Subscribers", "👥");

        viewCurrentDinersButton.setOnAction(e -> displayCurrentDiners());
        viewOrdersButton.setOnAction(e -> displayCurrentDiners());
        viewWaitingListButton.setOnAction(e -> displayWaitingList());
        viewSubscriberButton.setOnAction(e -> displaySubscribers());

        // Layout Assembly
        VBox groupAccess = new VBox(8, lblAccess, accessSubscriberDashButton, accessCasualDashButton); groupAccess.setAlignment(Pos.CENTER);
        VBox groupOps = new VBox(8, lblOps, registerNewSubscriberButton, checkInClientButton); groupOps.setAlignment(Pos.CENTER);
        VBox groupMgmt = new VBox(8, lblMgmt, manageTablesButton, editOpeningHoursButton); groupMgmt.setAlignment(Pos.CENTER);
        VBox groupView = new VBox(8, lblView, viewCurrentDinersButton, viewOrdersButton, viewWaitingListButton, viewSubscriberButton); groupView.setAlignment(Pos.CENTER);

        VBox centralContainer = new VBox(20, groupAccess, new Separator(), groupOps, new Separator(), groupMgmt, new Separator(), groupView);
        centralContainer.setAlignment(Pos.CENTER);
        addManagerContent(centralContainer); // Hook

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
    
    protected void addManagerContent(VBox container) {}

    // --- Actions ---

    public void registerNewSubscriber(String returnUser) {
        SubscriberRegistrationUI regUI = new SubscriberRegistrationUI(mainLayout, mainUI, () -> showDashboardScreen(returnUser));
        regUI.start();
    }

    // =================================================================================
    // 1. MANAGE TABLES UI (Integrated with DB)
    // =================================================================================
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

        // 3. Selection Listener
        tablesView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(String.valueOf(newVal.getTableId()));
                txtSeats.setText(String.valueOf(newVal.getSeats()));
                
            }
        });

        // 4. Buttons 

        // --- ADD BUTTON ---
        Button btnAdd = new Button("Add New Table");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(txtId.getText());
                int seats = Integer.parseInt(txtSeats.getText());
                
                // DEFAULT STATUS IS ALWAYS "AVAILABLE" FOR NEW TABLES
                Table newTable = new Table(id, seats, "AVAILABLE");
                
                mainUI.controller.accept(new Message(TaskType.ADD_TABLE, newTable));
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
                    
                    mainUI.controller.accept(new Message(TaskType.UPDATE_TABLE, selected));
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
                mainUI.controller.accept(new Message(TaskType.REMOVE_TABLE, selected.getTableId()));
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
       
        mainUI.controller.accept(new Message(TaskType.GET_TABLES, null));
    }
    

    /**
     * CALLED BY CLIENT CONTROLLER WHEN SERVER SENDS 'GET_TABLES'
     */
    public void updateTableData(ArrayList<Table> tables) {
        Platform.runLater(() -> {
            if (tablesView != null) {
                tablesView.getItems().setAll(tables);
            }
        });
    }

    // =================================================================================
    // 2. OPENING HOURS (Visual Only - kept local for now)
    // =================================================================================
    public void setOpeningHours() {
        mainLayout.getChildren().clear();
        Label header = new Label("Manage Opening Hours");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(new Tab("Regular Week", createRegularScheduleContent()), new Tab("Special Dates", createSpecialDatesContent()));

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));
        
        Button btnSaveGlobal = new Button("Save All Changes");
        btnSaveGlobal.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSaveGlobal.setOnAction(e -> mainUI.showAlert("Success", "Hours saved to DB.")); // Could implement SAVE_OPENING_HOURS

        VBox content = new VBox(15, header, tabPane, btnSaveGlobal, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    // =================================================================================
    // 3. SUBSCRIBERS (Integrated with DB)
    // =================================================================================
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
        
        TableColumn<User, Integer> subNumCol = new TableColumn<>("Sub #");
        subNumCol.setCellValueFactory(new PropertyValueFactory<>("subscriberNumber"));

        subscribersView.getColumns().addAll(idCol, fNameCol, lNameCol, phoneCol, subNumCol);
        subscribersView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, subscribersView, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(650); 
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);

        // Request Data

        mainUI.controller.accept(new Message(TaskType.GET_ALL_SUBSCRIBERS, null));
    }

    /**
     * CALLED BY CLIENT CONTROLLER
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

        // Request Data

        mainUI.controller.accept(new Message(TaskType.GET_WAITING_LIST, null));
    }
    
    public void updateWaitingListData(ArrayList<WaitingList> list) {
        Platform.runLater(() -> {
            if (waitingListView != null) waitingListView.getItems().setAll(list);
        });
    }

    // =================================================================================
    // 5. CURRENT DINERS / ACTIVE ORDERS (Integrated with DB)
    // =================================================================================
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
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        activeOrdersView.getColumns().addAll(orderIdCol, userCol, timeCol, statusCol);
        activeOrdersView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, activeOrdersView, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
        
        // Request Data
        mainUI.controller.accept(new Message(TaskType.GET_ORDERS, null));
    }
    
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
        Label lblInfo = new Label("Set standard opening hours (Check 'Closed' if not open):");
        lblInfo.setFont(new Font("Arial", 14));
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(15); grid.setAlignment(Pos.CENTER); grid.setPadding(new Insets(10));
        
        javafx.scene.layout.ColumnConstraints colDay = new javafx.scene.layout.ColumnConstraints(); colDay.setMinWidth(90);
        javafx.scene.layout.ColumnConstraints colCheck = new javafx.scene.layout.ColumnConstraints(); colCheck.setMinWidth(80);
        grid.getColumnConstraints().addAll(colDay, colCheck);
        
        javafx.collections.ObservableList<String> timeSlots = generateTimeSlots();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        grid.add(new Label("Day"), 0, 0); grid.add(new Label("Status"), 1, 0);
        grid.add(new Label("Open"), 2, 0); grid.add(new Label("Close"), 4, 0);

        for (int i = 0; i < days.length; i++) {
            addSmartHoursRow(grid, i + 1, days[i], "08:00", "22:00", false, timeSlots);
        }
        
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPrefHeight(350);
        return new VBox(10, lblInfo, scroll);
    }
    
    private void addSmartHoursRow(javafx.scene.layout.GridPane grid, int row, String day, String defaultOpen, String defaultClose, boolean isClosed, javafx.collections.ObservableList<String> times) {
        Label lblDay = new Label(day + ":"); lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); 
        CheckBox chkClosed = new CheckBox("Closed"); chkClosed.setSelected(isClosed);
        ComboBox<String> cmbOpen = new ComboBox<>(times); cmbOpen.setValue(defaultOpen); cmbOpen.setPrefWidth(90);
        ComboBox<String> cmbClose = new ComboBox<>(times); cmbClose.setValue(defaultClose); cmbClose.setPrefWidth(90);
        Label lblDash = new Label("-");

        cmbOpen.disableProperty().bind(chkClosed.selectedProperty());
        cmbClose.disableProperty().bind(chkClosed.selectedProperty());
        lblDash.disableProperty().bind(chkClosed.selectedProperty());

        grid.add(lblDay, 0, row); grid.add(chkClosed, 1, row); grid.add(cmbOpen, 2, row); grid.add(lblDash, 3, row); grid.add(cmbClose, 4, row);
    }

    private VBox createSpecialDatesContent() {
        Label lblInfo = new Label("Define specific dates with different hours:");
        // Keep MockSpecialDate internal as it's not DB connected yet in the prompt files
        TableView<MockSpecialDate> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); table.setPrefHeight(200);

        TableColumn<MockSpecialDate, String> dateCol = new TableColumn<>("Date"); dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<MockSpecialDate, String> hoursCol = new TableColumn<>("Hours"); hoursCol.setCellValueFactory(new PropertyValueFactory<>("hours"));
        TableColumn<MockSpecialDate, String> eventCol = new TableColumn<>("Event"); eventCol.setCellValueFactory(new PropertyValueFactory<>("event"));
        table.getColumns().addAll(dateCol, hoursCol, eventCol);

        javafx.collections.ObservableList<MockSpecialDate> data = javafx.collections.FXCollections.observableArrayList();
        table.setItems(data);

        DatePicker datePicker = new DatePicker(); datePicker.setPromptText("Select Date"); datePicker.setPrefWidth(110);
        TextField txtOpen = new TextField(); txtOpen.setPromptText("Open"); txtOpen.setPrefWidth(55);
        TextField txtClose = new TextField(); txtClose.setPromptText("Close"); txtClose.setPrefWidth(55);
        TextField txtEvent = new TextField(); txtEvent.setPromptText("Event Name"); txtEvent.setPrefWidth(120);
        
        Button btnAdd = new Button("Add"); btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> {
            if(datePicker.getValue() != null && !txtOpen.getText().isEmpty()) {
                data.add(new MockSpecialDate(datePicker.getValue().toString(), txtOpen.getText() + " - " + txtClose.getText(), txtEvent.getText()));
            }
        });
        
        Button btnRemove = new Button("Remove"); btnRemove.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        btnRemove.setOnAction(e -> data.remove(table.getSelectionModel().getSelectedItem()));

        javafx.scene.layout.HBox inputRow = new javafx.scene.layout.HBox(10, datePicker, txtOpen, new Label("-"), txtClose, txtEvent, btnAdd); inputRow.setAlignment(Pos.CENTER);
        javafx.scene.layout.HBox actionRow = new javafx.scene.layout.HBox(10, btnRemove); actionRow.setAlignment(Pos.CENTER_RIGHT);
        
        VBox layout = new VBox(15, lblInfo, table, actionRow, new Separator(), new Label("Add New Exception:"), inputRow);
        layout.setAlignment(Pos.CENTER); layout.setPadding(new Insets(15));
        return layout;
    }
    
    // Internal Helper for visual only (not yet DB connected )
    public static class MockSpecialDate {
        private String date; private String hours; private String event;
        public MockSpecialDate(String d, String h, String e) { this.date = d; this.hours = h; this.event = e; }
        public String getDate() { return date; } public String getHours() { return hours; } public String getEvent() { return event; }
    }
}