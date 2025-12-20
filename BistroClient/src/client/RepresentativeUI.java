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

    private String currentUsername;

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
        viewOrdersButton = createWideButton("View Active Orders", "🍜");
        viewWaitingListButton = createWideButton("View Full Waiting List", "⏳");
        viewSubscriberButton = createWideButton("View All Subscribers", "👥");

        viewCurrentDinersButton.setOnAction(e -> displayCurrentDiners());
        viewOrdersButton.setOnAction(e -> displayWaitingList());
        viewWaitingListButton.setOnAction(e -> displayWaitingList());
        viewSubscriberButton.setOnAction(e -> displaySubscribers());

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

 // =================================================================================
    // 1. MANAGE TABLES UI (MOCK)
    // =================================================================================
// =================================================================================
    // 1. MANAGE TABLES UI (Add, Remove, Edit Seats)
    // =================================================================================
    public void updateTableDetails() {
        mainLayout.getChildren().clear();

        Label header = new Label("Manage Tables");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        // 1. Setup Table
        TableView<MockTable> table = new TableView<>();
        
        TableColumn<MockTable, String> idCol = new TableColumn<>("Table ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        
        TableColumn<MockTable, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("seats"));
        
        TableColumn<MockTable, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, seatsCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Mock Data
        javafx.collections.ObservableList<MockTable> data = javafx.collections.FXCollections.observableArrayList(
            new MockTable("1", 4, "Available"),
            new MockTable("2", 2, "Occupied"),
            new MockTable("3", 6, "Reserved"),
            new MockTable("4", 4, "Available")
        );
        table.setItems(data);

        // 2. Input Fields
        TextField txtId = new TextField(); 
        txtId.setPromptText("Table ID"); 
        txtId.setPrefWidth(80);
        
        TextField txtSeats = new TextField(); 
        txtSeats.setPromptText("Seats"); 
        txtSeats.setPrefWidth(80);

        // 3. Selection Listener (Auto-fill inputs when row clicked)
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtId.setText(newSelection.getId());
                txtSeats.setText(String.valueOf(newSelection.getSeats()));
            }
        });

        // 4. Buttons (Add, Update, Remove)
        
        // --- ADD BUTTON ---
        Button btnAdd = new Button("Add New");
        btnAdd.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> {
            try {
                String id = txtId.getText();
                int seats = Integer.parseInt(txtSeats.getText());
                
                // Check if ID exists (Optional simple check)
                boolean exists = data.stream().anyMatch(t -> t.getId().equals(id));
                if (exists) {
                    mainUI.showAlert("Error", "Table ID already exists.");
                } else {
                    data.add(new MockTable(id, seats, "Available"));
                    txtId.clear(); txtSeats.clear();
                }
            } catch (NumberFormatException ex) {
                mainUI.showAlert("Error", "Seats must be a number.");
            }
        });

        // --- UPDATE BUTTON ---
        Button btnUpdate = new Button("Update Selected");
        btnUpdate.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnUpdate.setOnAction(e -> {
            MockTable selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainUI.showAlert("Error", "Select a table to update.");
            } else {
                try {
                    // Update the object
                    selected.setId(txtId.getText());
                    selected.setSeats(Integer.parseInt(txtSeats.getText()));
                    // Refresh table view
                    table.refresh();
                    txtId.clear(); txtSeats.clear();
                } catch (NumberFormatException ex) {
                    mainUI.showAlert("Error", "Seats must be a number.");
                }
            }
        });

        // --- REMOVE BUTTON ---
        Button btnRemove = new Button("Remove Selected");
        btnRemove.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        btnRemove.setOnAction(e -> {
            MockTable selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainUI.showAlert("Error", "Select a table to remove.");
            } else {
                data.remove(selected);
                txtId.clear(); txtSeats.clear();
            }
        });

        // Layout Assembly
        javafx.scene.layout.HBox inputRow = new javafx.scene.layout.HBox(10, new Label("Details:"), txtId, txtSeats);
        inputRow.setAlignment(Pos.CENTER);
        
        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(10, btnAdd, btnUpdate, btnRemove);
        btnRow.setAlignment(Pos.CENTER);

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, table, new Separator(), inputRow, btnRow, new Separator(), btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
    }
    // =================================================================================
    // 2. OPENING HOURS UI (MOCK)
    // =================================================================================
//=================================================================================
// 2. OPENING HOURS UI (Regular + Special Dates)
// =================================================================================
public void setOpeningHours() {
    mainLayout.getChildren().clear();

    Label header = new Label("Manage Opening Hours");
    header.setFont(new Font("Arial", 22));
    header.setStyle("-fx-font-weight: bold;");

    // --- TAB PANE SETUP ---
    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    // TAB 1: Regular Week
    Tab regularTab = new Tab("Regular Week");
    regularTab.setContent(createRegularScheduleContent());

    // TAB 2: Special Dates
    Tab specialTab = new Tab("Special Dates (Holidays/Events)");
    specialTab.setContent(createSpecialDatesContent());

    tabPane.getTabs().addAll(regularTab, specialTab);

    // --- BOTTOM BUTTONS ---
    Button btnBack = new Button("Back to Dashboard");
    btnBack.setOnAction(e -> showDashboardScreen(currentUsername));
    
    // Save Button (Mock Logic)
    Button btnSaveGlobal = new Button("Save All Changes");
    btnSaveGlobal.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
    btnSaveGlobal.setOnAction(e -> mainUI.showAlert("Success", "Regular hours and Special dates saved to DB."));

    VBox content = new VBox(15, header, tabPane, btnSaveGlobal, btnBack);
    content.setAlignment(Pos.CENTER);
    content.setMaxWidth(600);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

    mainLayout.getChildren().add(content);
}
// Generate 30-minute intervals (00:00 - 23:30) ---
private javafx.collections.ObservableList<String> generateTimeSlots() {
    javafx.collections.ObservableList<String> slots = javafx.collections.FXCollections.observableArrayList();
    
    for (int h = 0; h < 24; h++) {
        for (int m = 0; m < 60; m += 30) {
            // Format as HH:mm (e.g., 09:00, 09:30)
            String time = String.format("%02d:%02d", h, m);
            slots.add(time);
        }
    }
    return slots;
}
private void addSmartHoursRow(javafx.scene.layout.GridPane grid, int row, String day, String defaultOpen, String defaultClose, boolean isClosed, javafx.collections.ObservableList<String> times) {
        // 1. Day Label (Ensuring text color is visible)
        Label lblDay = new Label(day + ":");
        lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); 
        
        // 2. Closed Checkbox
        CheckBox chkClosed = new CheckBox("Closed");
        chkClosed.setSelected(isClosed);

        // 3. Time Dropdowns
        ComboBox<String> cmbOpen = new ComboBox<>(times);
        cmbOpen.setValue(defaultOpen);
        cmbOpen.setPrefWidth(90);

        ComboBox<String> cmbClose = new ComboBox<>(times);
        cmbClose.setValue(defaultClose);
        cmbClose.setPrefWidth(90);
        
        Label lblDash = new Label("-");

        // 4. Logic
        cmbOpen.disableProperty().bind(chkClosed.selectedProperty());
        cmbClose.disableProperty().bind(chkClosed.selectedProperty());
        lblDash.disableProperty().bind(chkClosed.selectedProperty());

        // 5. Add to Grid
        grid.add(lblDay, 0, row);
        grid.add(chkClosed, 1, row);
        grid.add(cmbOpen, 2, row);
        grid.add(lblDash, 3, row);
        grid.add(cmbClose, 4, row);
    }

//  Create Regular Schedule Content
    private VBox createRegularScheduleContent() {
        Label lblInfo = new Label("Set standard opening hours (Check 'Closed' if not open):");
        lblInfo.setFont(new Font("Arial", 14));
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); // Space between columns
        grid.setVgap(15); // Space between rows
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));

        // --- FIX: Force Column Widths so "Day" isn't hidden ---
        javafx.scene.layout.ColumnConstraints colDay = new javafx.scene.layout.ColumnConstraints();
        colDay.setMinWidth(90); // Force "Day" column to be at least 90px wide
        
        javafx.scene.layout.ColumnConstraints colCheck = new javafx.scene.layout.ColumnConstraints();
        colCheck.setMinWidth(80); // Force "Status" column

        // Apply constraints
        grid.getColumnConstraints().addAll(colDay, colCheck);

        // Generate time slots
        javafx.collections.ObservableList<String> timeSlots = generateTimeSlots();

        // Add rows for all days
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        // Headers (Styled for visibility)
        Label hDay = new Label("Day"); hDay.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        Label hStatus = new Label("Status"); hStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        Label hOpen = new Label("Open"); hOpen.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        Label hClose = new Label("Close"); hClose.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        grid.add(hDay, 0, 0);
        grid.add(hStatus, 1, 0);
        grid.add(hOpen, 2, 0);
        grid.add(new Label(""), 3, 0); 
        grid.add(hClose, 4, 0);

        for (int i = 0; i < days.length; i++) {
            addSmartHoursRow(grid, i + 1, days[i], "08:00", "22:00", false, timeSlots);
        }
        
        // Wrap in ScrollPane in case the screen is short
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPrefHeight(350);

        VBox layout = new VBox(10, lblInfo, scroll);
        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    private VBox createSpecialDatesContent() {
        Label lblInfo = new Label("Define specific dates with different hours (Holidays, Inventory, etc.):");

        // 1. Table View
        TableView<MockSpecialDate> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);

        TableColumn<MockSpecialDate, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));

        TableColumn<MockSpecialDate, String> hoursCol = new TableColumn<>("Hours");
        hoursCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hours"));

        // CHANGED: Column Name "Event" and Property "event"
        TableColumn<MockSpecialDate, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("event"));

        table.getColumns().addAll(dateCol, hoursCol, eventCol);

        // Mock Data
        javafx.collections.ObservableList<MockSpecialDate> data = javafx.collections.FXCollections.observableArrayList(
            new MockSpecialDate("2025-04-12", "08:00 - 13:00", "Passover Eve"),
            new MockSpecialDate("2025-12-31", "18:00 - 02:00", "New Year's Party")
        );
        table.setItems(data);

        // 2. Add New Entry Form
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");
        datePicker.setPrefWidth(110);
        
        TextField txtOpen = new TextField(); txtOpen.setPromptText("Open"); txtOpen.setPrefWidth(55);
        TextField txtClose = new TextField(); txtClose.setPromptText("Close"); txtClose.setPrefWidth(55);
        
        // CHANGED: Renamed variable to txtEvent
        TextField txtEvent = new TextField(); txtEvent.setPromptText("Event Name"); txtEvent.setPrefWidth(120);
        
        Button btnAdd = new Button("Add");
        btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> {
            if(datePicker.getValue() == null || txtOpen.getText().isEmpty() || txtClose.getText().isEmpty()) {
                mainUI.showAlert("Error", "Please fill all fields.");
            } else {
                String dateStr = datePicker.getValue().toString();
                String hoursStr = txtOpen.getText() + " - " + txtClose.getText();
                // CHANGED: Using txtEvent.getText()
                data.add(new MockSpecialDate(dateStr, hoursStr, txtEvent.getText()));
                
                txtOpen.clear(); txtClose.clear(); txtEvent.clear(); datePicker.setValue(null);
            }
        });

        // 3. Delete Button
        Button btnRemove = new Button("Remove Selected");
        btnRemove.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        btnRemove.setOnAction(e -> {
            MockSpecialDate selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainUI.showAlert("Error", "Please select a row to remove.");
            } else {
                data.remove(selected);
            }
        });

        javafx.scene.layout.HBox inputRow = new javafx.scene.layout.HBox(10, datePicker, txtOpen, new Label("-"), txtClose, txtEvent, btnAdd);
        inputRow.setAlignment(Pos.CENTER);
        
        javafx.scene.layout.HBox actionRow = new javafx.scene.layout.HBox(10, btnRemove);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15, lblInfo, table, actionRow, new Separator(), new Label("Add New Exception:"), inputRow);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(15));
        return layout;
    }


    // =================================================================================
    // 3. WAITING LIST UI (MOCK)
    // =================================================================================
    public void displayWaitingList() {
        mainLayout.getChildren().clear();

        Label header = new Label("Current Waiting List");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        TableView<MockWait> table = new TableView<>();
        
        TableColumn<MockWait, String> nameCol = new TableColumn<>("Customer");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("identifier"));
        
        TableColumn<MockWait, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
        
        TableColumn<MockWait, Integer> sizeCol = new TableColumn<>("Party Size");
        sizeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("size"));
        
        table.getColumns().addAll(nameCol, typeCol, sizeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.collections.ObservableList<MockWait> data = javafx.collections.FXCollections.observableArrayList(
            new MockWait("050-1234567", "Casual", 3),
            new MockWait("Sub #992", "Subscriber", 5),
            new MockWait("052-9876543", "Casual", 2)
        );
        table.setItems(data);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, table, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
    }

    // =================================================================================
    // 4. CURRENT DINERS UI (MOCK)
    // =================================================================================
    public void displayCurrentDiners() {
        mainLayout.getChildren().clear();

        Label header = new Label("Active Diners (Seated)");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        TableView<MockDiner> table = new TableView<>();
        
        TableColumn<MockDiner, String> tblCol = new TableColumn<>("Table #");
        tblCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tableId"));
        
        TableColumn<MockDiner, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customer"));
        
        TableColumn<MockDiner, String> timeCol = new TableColumn<>("Status");
        timeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        table.getColumns().addAll(tblCol, custCol, timeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.collections.ObservableList<MockDiner> data = javafx.collections.FXCollections.observableArrayList(
            new MockDiner("2", "Cohen (Sub)", "Eating"),
            new MockDiner("5", "050-111222", "Seated (Ordering)"),
            new MockDiner("8", "Levi (Sub)", "Check Requested")
        );
        table.setItems(data);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, table, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
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
    public void displaySubscribers() {
        mainLayout.getChildren().clear();

        Label header = new Label("All Subscribers");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold;");

        TableView<MockSubscriber> table = new TableView<>();
        
        TableColumn<MockSubscriber, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        
        TableColumn<MockSubscriber, String> fNameCol = new TableColumn<>("First Name");
        fNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("firstName"));
        
        TableColumn<MockSubscriber, String> lNameCol = new TableColumn<>("Last Name");
        lNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("lastName"));
        
        TableColumn<MockSubscriber, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("phone"));

        TableColumn<MockSubscriber, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));

        table.getColumns().addAll(idCol, fNameCol, lNameCol, phoneCol, emailCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // MOCK DATA
        javafx.collections.ObservableList<MockSubscriber> data = javafx.collections.FXCollections.observableArrayList(
            new MockSubscriber("101", "Yossi", "Cohen", "050-1234567", "yossi@gmail.com"),
            new MockSubscriber("102", "Dana", "Levi", "052-9876543", "dana@walla.co.il"),
            new MockSubscriber("103", "Ron", "Bar", "054-5556667", "ronb@post.com")
        );
        table.setItems(data);

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> showDashboardScreen(currentUsername));

        VBox content = new VBox(15, header, table, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600); // Slightly wider for this table
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
    }
    public static class MockSpecialDate {
        private String date; 
        private String hours; 
        private String event; // Renamed from 'reason'

        public MockSpecialDate(String date, String hours, String event) {
            this.date = date;
            this.hours = hours;
            this.event = event;
        }

        public String getDate() { return date; }
        public String getHours() { return hours; }
        public String getEvent() { return event; } // JavaFX looks for this
    }
    
 // --- UPDATE THIS CLASS AT THE BOTTOM OF THE FILE ---
    public static class MockTable {
        private String id; 
        private int seats; 
        private String status;
        
        public MockTable(String id, int s, String st) { 
            this.id = id; 
            this.seats = s; 
            this.status = st; 
        }
        
        // Getters
        public String getId() { return id; }
        public int getSeats() { return seats; }
        public String getStatus() { return status; }
        
        // Setters (NEW: Required for editing)
        public void setId(String id) { this.id = id; }
        public void setSeats(int seats) { this.seats = seats; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class MockWait {
        private String identifier; private String type; private int size;
        public MockWait(String i, String t, int s) { this.identifier=i; this.type=t; this.size=s; }
        public String getIdentifier() { return identifier; }
        public String getType() { return type; }
        public int getSize() { return size; }
    }

    public static class MockDiner {
        private String tableId; private String customer; private String status;
        public MockDiner(String t, String c, String s) { this.tableId=t; this.customer=c; this.status=s; }
        public String getTableId() { return tableId; }
        public String getCustomer() { return customer; }
        public String getStatus() { return status; }
    }
    public static class MockSubscriber {
        private String id; private String firstName; private String lastName; private String phone; private String email;
        
        public MockSubscriber(String id, String f, String l, String p, String e) {
            this.id = id; this.firstName = f; this.lastName = l; this.phone = p; this.email = e;
        }
        
        public String getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }
}