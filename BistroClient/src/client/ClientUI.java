package client;

import common.BistroSchedule;
import common.Order;
import common.Table;
import common.User;
import common.WaitingList;
import controllers.ClientController;
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

/**
 * The ClientUI class is the main entry point for the Bistro Management System client application.
 * <p>
 * It extends {@link Application} and manages the primary stage, scene transitions between 
 * different user roles (Casual, Subscriber, Representative, Manager), and holds global state 
 * such as the network controller and current user session.
 */
public class ClientUI extends Application {

	private Stage primaryStage;
    // Reference to the active Representative/Manager screen
    // This allows us to pass data (tables, orders) from the server to the screen.
    public RepresentativeUI repUI;
    public User currentUser;
    public CheckoutUI checkoutUI;
    
    public IdentificationUI currentIdentificationUI;
    public ClientController controller;
    private VBox mainLayout; 

    // Connection Fields
    private TextField txtIp;
    private TextField txtPort;
    private Label lblStatus;
    private Button btnConnect; 
    
    private MonthlyReportUI monthlyReportUI;
    public boolean isRemote;
 
    public ArrayList<BistroSchedule> masterSchedule = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the JavaFX application, sets up the main layout, 
     * and displays the initial connection screen.
     *
     * @param primaryStage The primary stage for this application.
     */
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

    /**
     * SCREEN 1: Connection UI
     * <p>
     * Displays the form to input the server IP and Port.
     * Initiates the connection via the ClientController.
     */
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
                        
                        // NEW: Pre-fetch schedule immediately!
                        controller.accept(new common.Message(common.TaskType.GET_SCHEDULE, null));
                        
                        showLocationModeScreen();
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
    
    /**
     * SCREEN 2: Location Selection
     * <p>
     * Prompts the user to select their physical location (In Restaurant vs Remote).
     * This setting affects the available permissions and UI options.
     */
    private void showLocationModeScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Select Connection Mode");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label instruction = new Label("Where are you currently located?");
        instruction.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        // Option 1: In Restaurant
        Button btnOnSite = new Button("In Restaurant");
        btnOnSite.setPrefWidth(250);
        btnOnSite.setPrefHeight(50);
        btnOnSite.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
        
        btnOnSite.setOnAction(e -> {
            this.isRemote = false; // PARAMETER SET: ON-SITE
            System.out.println("Mode: In Restaurant (Full Access)");
            showRoleSelectionScreen(); // Proceed
        });

        // Option 2: From Afar
        Button btnRemote = new Button("From Afar (Remote)");
        btnRemote.setPrefWidth(250);
        btnRemote.setPrefHeight(50);
        btnRemote.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
        
        btnRemote.setOnAction(e -> {
            this.isRemote = true; // PARAMETER SET: REMOTE
            System.out.println("Mode: Remote (Restricted Access)");
            showRoleSelectionScreen(); // Proceed
        });

        VBox content = new VBox(20, header, instruction, btnOnSite, btnRemote);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }
    
    /**
     * SCREEN 3: Role Selection
     * <p>
     * Displays the main menu where the user selects their user type:
     * Casual Diner, Subscriber, Representative, or Manager.
     * Resets any previous user session data.
     */
    public void showRoleSelectionScreen(){
    	
        this.repUI = null;          // Forget the previous Representative/Manager
        this.currentUser = null;    // Forget the previous User
        this.checkoutUI = null;     // Clear any active checkout reference
       
    	
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
        
        Button btnBack = new Button("Back to Location select");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        
        // Navigation: Back to Location Options
        btnBack.setOnAction(e -> showLocationModeScreen());

        VBox menuBox = new VBox(20, header, btnCasual, btnSubscriber, btnRep, btnManager, btnBack);
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
    
    /**
     * Opens the dashboard for an authenticated Subscriber.
     * Sets up the navigation callback to return to the appropriate previous screen.
     */
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
    
    /**
     * Opens the dashboard for a Casual user.
     * Sets up navigation callbacks.
     */
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
    
    /**
     * Displays a dialog offering alternative reservation times when the requested time is full.
     *
     * @param options An array of available time strings (e.g., "18:00", "19:00").
     */
    public void showAlternativeTimesDialog(String[] options) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restaurant Full");
        alert.setHeaderText("The requested time is fully booked.");
        alert.setContentText("We found available spots at nearby times.\nSelect a time to book it:");

        // Create a button for each available time
        ArrayList<ButtonType> buttons = new ArrayList<>();
        for (String time : options) {
            buttons.add(new ButtonType(time));
        }
        buttons.add(new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));

        alert.getButtonTypes().setAll(buttons);

        // Show and wait for user action
        java.util.Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get().getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE) {
            // User picked a time
            String newTimeStr = result.get().getText();
            System.out.println("User switched to: " + newTimeStr);
            
            // We need to resend the request. 
            // NOTE: We need the original Order object details (Date/Diners).
            // Since we don't have the object here easily, the cleanest way 
            // is to tell the user to re-enter, or simpler: 
            // Just update the UI fields if you are on the reservation screen.
            
            // For now, let's just show an info message telling them what to do:
            showAlert("Time Selected", "Please change the time to " + newTimeStr + " and click Submit again.");
            
            // OPTIONAL: If you want it fully automatic, you would need to store the 
            // 'pendingOrder' in ClientController and re-trigger 'createReservation' here.
        }
    }
    
    /**
     * Opens the dashboard for a Representative or Manager.
     *
     * @param user The authenticated user object.
     */
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

    /**
     * Updates the Representative UI with fresh table data from the server.
     * @param tables The list of tables.
     */
    public void refreshTableData(ArrayList<Table> tables) {
        if (repUI != null) {
            repUI.updateTableData(tables);
        }
    }

    /**
     * Updates the Representative UI with fresh subscriber data from the server.
     * @param subscribers The list of subscribers.
     */
    public void refreshSubscriberData(ArrayList<User> subscribers) {
        if (repUI != null) {
            repUI.updateSubscriberData(subscribers);
        }
    }

    /**
     * Updates the Representative UI with fresh order data from the server.
     * @param orders The list of orders.
     */
    public void refreshOrderData(ArrayList<Order> orders) {
        if (repUI != null) {
            // Ensure RepresentativeUI has this method!
            repUI.updateOrdersData(orders);
        }
    }

    /**
     * Updates the Representative UI with fresh waiting list data.
     * @param list The list of waiting entries.
     */
    public void refreshWaitingListData(ArrayList<WaitingList> list) {
        if (repUI != null) {
            // Ensure RepresentativeUI has this method!
            repUI.updateWaitingListData(list);
        }
    }
    
    /**
     * Updates the local master schedule and refreshes the Representative UI if active.
     * @param schedule The list of schedule objects.
     */
    public void refreshScheduleData(ArrayList<BistroSchedule> schedule) {
        // 1. ALWAYS save it to the client memory
        this.masterSchedule = schedule;
        System.out.println("Client: Schedule synced. Total items: " + schedule.size());

        // 2. If the Rep screen is open, update it too
        if (repUI != null) {
            repUI.updateScheduleData(schedule);
        }
    }
    
    
    public void setCheckoutUI(CheckoutUI checkoutUI) {
    	this.checkoutUI = checkoutUI;
    }
    
    
 // Setter
    public void setMonthlyReportUI(MonthlyReportUI ui) {
        this.monthlyReportUI = ui;
    }
    
   public  MonthlyReportUI  getMonthlyReportUI() {
        return this.monthlyReportUI;
    }
    
    /**
     * Displays a professional "Digital Card" window with a live QR Code.
     * The QR code encodes the user's subscriber number.
     *
     * @param user The user for whom the card is generated.
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
    
    /**
     * Opens the Order History screen for the current user.
     *
     * @param history The list of past orders to display.
     */
    public void openOrderHistory(ArrayList<Order> history) {
        Platform.runLater(() -> {
            
            // 1. Define where "Back" goes
            Runnable onBack;
            
            // If we have a logged-in subscriber, go back to their dashboard
            if (currentUser != null && currentUser.getSubscriberNumber() != null) {
                onBack = () -> openSubscriberDashboard(); // <--- FIXED THIS LINE
            } else {
                // Otherwise go to main menu
                onBack = () -> showRoleSelectionScreen();
            }

            String subId = (currentUser != null && currentUser.getSubscriberNumber() != null) 
                           ? String.valueOf(currentUser.getSubscriberNumber()) : "N/A";

            // 2. Pass the real history list to the UI
            SubscriberHistoryUI historyScreen = new SubscriberHistoryUI(mainLayout, this, onBack, subId, history);
            historyScreen.start();
        });
    }

    // =========================================================
    // UTILS
    // =========================================================

    /**
     * Displays an informational alert dialog to the user.
     *
     * @param title   The title of the alert window.
     * @param content The message body of the alert.
     */
    public void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Handles the event where the server connection is lost.
     * Notifies the user and resets the UI to the connection screen.
     */
    public void handleServerDisconnect() {
        showAlert("Connection Lost", "The server has stopped or crashed.\nRestart client.");
        showConnectionScreen();
    }

    /**
     * Stops the application and ensures the client disconnects gracefully.
     *
     * @throws Exception if an error occurs during shutdown.
     */
    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.disconnect(); 
        }
        super.stop();
        System.exit(0); 
    }
    
    
    
    /**
     * OFFLINE CHECK: Finds opening hours for a date from the local list.
     * <p>
     * Checks if a specific date override exists first. If not, falls back to the
     * general weekday schedule.
     *
     * @param date The date to check.
     * @return A string representing hours (e.g., "08:00-14:00") or "CLOSED".
     */
    public String getOfflineHours(java.time.LocalDate date) {
        if (masterSchedule == null || masterSchedule.isEmpty()) return "00:00-23:59"; // Default fallback

        String dateId = date.toString(); // "2026-01-02"
        String dayName = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH); // "Friday"

        BistroSchedule found = null;

        // 1. Search for SPECIAL DATE first
        for (BistroSchedule item : masterSchedule) {
            if (item.getIdentifier().equalsIgnoreCase(dateId)) {
                found = item;
                break;
            }
        }

        // 2. If not found, search for WEEKDAY
        if (found == null) {
            for (BistroSchedule item : masterSchedule) {
                if (item.getIdentifier().equalsIgnoreCase(dayName)) {
                    found = item;
                    break;
                }
            }
        }

        // 3. Process Result
        if (found == null || found.isClosed()) {
            return "CLOSED";
        }

        // Return format: "08:00-14:00"
        return found.getOpenTime() + "-" + found.getCloseTime();
    }
}