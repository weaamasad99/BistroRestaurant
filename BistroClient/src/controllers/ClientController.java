package controllers;

import common.BistroSchedule;
import common.Message;
import common.Order;
import common.TaskType;
import common.Table;
import common.User;
import common.WaitingList;

import javafx.application.Platform;
import java.util.ArrayList;

import client.BistroClient;
import client.ClientUI;

/**
 * The ClientController serves as the central "Network Manager" for the client-side application.
 * <p>
 * It acts as the bridge between the User Interface (JavaFX controllers) and the Network Layer (OCSF).
 * Its primary responsibilities include:
 * <ul>
 * <li>Establishing and maintaining the connection to the server.</li>
 * <li>Routing outgoing requests from specific UI controllers (e.g., CasualController, SubscriberController) to the server.</li>
 * <li>Receiving incoming responses from the server and updating the appropriate UI screens on the JavaFX Application Thread.</li>
 * </ul>
 */
public class ClientController {

    // Reference to the network client
    private BistroClient client;

    // Reference to the UI layer
    private ClientUI ui;

    /**
     * Constructs the ClientController.
     *
     * @param ui The main UI instance, allowing this controller to trigger screen updates.
     */
    public ClientController(ClientUI ui) {
        this.ui = ui;
    }

    // =======================================================
    // CONNECTION MANAGEMENT
    // =======================================================

    /**
     * Attempts to establish a connection to the server.
     *
     * @param ip   The IP address of the server.
     * @param port The port number to connect to.
     * @return {@code true} if the connection was successfully established; {@code false} otherwise.
     */
    public boolean connect(String ip, int port) {
        try {
            // Create client and open connection
            client = new BistroClient(ip, port, this);
            client.openConnection();
            return true;
        } catch (Exception e) {
            // Connection failed
            System.out.println("Error: Failed to connect to server. " + e.getMessage());
            return false;
        }
    }

    /**
     * Disconnects the client from the server gracefully.
     * This is typically called when the application is closing.
     */
    public void disconnect() {
        if (client != null) {
            try {
                client.quit();  // Close connection
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the event where the server connection is lost unexpectedly (crash).
     * Triggers a UI alert to notify the user.
     */
    public void serverWentDown() {
        Platform.runLater(() -> {
            ui.handleServerDisconnect();
        });
    }

    // =======================================================
    // SENDING REQUESTS (Client -> Server)
    // =======================================================

    /**
     * Sends a message to the server.
     * <p>
     * This method is the single entry point for all specific controllers (Casual, Subscriber, Rep)
     * to transmit data. It uses Kryo serialization via the {@link BistroClient}.
     *
     * @param msg The {@link Message} object containing the TaskType and associated data.
     */
    public void accept(Object msg) {
        if (client != null) {
            client.sendKryoRequest((Message) msg);
        }
    }

    // =======================================================
    // HANDLING RESPONSES (Server -> Client)
    // =======================================================
    
    /**
     * Processes incoming messages received from the server.
     * <p>
     * This method acts as a router, inspecting the {@link TaskType} of the message
     * and delegating the data to the appropriate method in the {@link ClientUI}.
     * All UI updates are wrapped in {@code Platform.runLater} to ensure thread safety.
     *
     * @param msg The message received from the server.
     */
    @SuppressWarnings("unchecked")
    public void handleMessageFromClient(Message msg) {

        // Ensure UI updates run on the JavaFX Application Thread
        Platform.runLater(() -> {
        	
        	User user;
        	
            switch (msg.getTask()) {
		
		        // --- GENERIC STATUS NOTIFICATIONS ---
		        case SUCCESS:
		            String successText = (String) msg.getObject();
		            ui.showAlert("Success", successText);
		            break;
		
		        case FAIL:
		            String failText = (String) msg.getObject();
		            ui.showAlert("Operation Failed", failText);
		            break;
                    
                // --- AUTHENTICATION & SESSION ---
		        case SET_USER:
		        	user = (User) msg.getObject();
		        	this.ui.currentUser = user; 
                    // Fallthrough intentional if SET_USER implies login logic in some flows, 
                    // usually SET_USER is just state update.
                    break;
            
		        case LOGIN_RESPONSE:
		            user = (User) msg.getObject();
		            
		            if (user != null) {
		                ui.currentUser = user;
		                
		                if (user.getUserType().equals("CASUAL"))
			                ui.showAlert("Login Success", "Welcome ");
		                else
		                	ui.showAlert("Login Success", "Welcome, " + user.getUsername());

		                String type = user.getUserType();

		                if ("SUBSCRIBER".equalsIgnoreCase(type)) {
		                    ui.openSubscriberDashboard();
		                } 
		                else if ("REPRESENTATIVE".equalsIgnoreCase(type) || "MANAGER".equalsIgnoreCase(type)) {
		                    ui.openRepresentativeDashboard(user);
		                } 
		                else {
		                    // Default to casual dashboard
		                    ui.openCasualDashboard();
		                }

		            } else {
		                ui.showAlert("Login Failed", "Invalid credentials.");
		            }
		            break;

                // --- IDENTIFICATION UI ---
		        case DAILY_ORDERS_RESULT:
                    ArrayList<Order> todayOrders = (ArrayList<Order>) msg.getObject();
                    if (ui.currentIdentificationUI != null) {
                        ui.currentIdentificationUI.updateOrderList(todayOrders);
                    }
                    break;

                // --- RESERVATION FLOW ---
		        case REQUEST_RESERVATION:
	                String resResult = (String) msg.getObject();
	                
	                Platform.runLater(() -> {
	                    if (resResult.startsWith("OK:")) {
	                        // Success format: "OK:Code"
	                        String code = resResult.split(":", 2)[1];
	                        ui.showAlert("Success", "Reservation Confirmed!\nYour Code: " + code);
	                    } 
	                    else if (resResult.startsWith("SUGGEST:")) {
	                        // Suggestion format: "SUGGEST:Time1,Time2,Time3"
	                        String timesStr = resResult.split(":", 2)[1]; 
	                        String[] options = timesStr.split(",");
	                        ui.showAlternativeTimesDialog(options);
	                    } 
	                    else {
	                        // Regular Error Message
	                        ui.showAlert("Reservation Failed", resResult);
	                    }
	                });
	                break;
		            
                case RESERVATION_CONFIRMED:
                    Order confirmedOrder = (Order) msg.getObject();
                    ui.showAlert("Success", "Reservation Confirmed!\nYour Code: " + confirmedOrder.getConfirmationCode());
                    break;

                case RESERVATION_REJECTED:
                    ui.showAlert("Fully Booked", "No tables available. Please join the waiting list.");
                    break;

                // --- CHECK-IN & BILLING ---
                case CHECK_IN_APPROVED:
                    ui.showAlert("Welcome", "Check-in Approved! Your table is ready.");
                    break;

                case CHECK_IN_DENIED:
                    String reason = (String) msg.getObject();
                    ui.showAlert("Check-in Error", reason);
                    break;

                case GET_BILL:
                    // Server sends: Object[] { code, price, userType }
                    Object[] billData = (Object[]) msg.getObject();
                    String bCode = (String) billData[0];
                    Double bPrice = (Double) billData[1];
                    String bType = (String) billData[2];
                    
                    boolean isSub = "SUBSCRIBER".equalsIgnoreCase(bType);
                    
                    if (ui.checkoutUI != null) {
                        Platform.runLater(() -> ui.checkoutUI.showBillDetails(bCode, bPrice, isSub));
                    }
                    break;
                    
                 // --- WAITING LIST UPDATES ---
                case WAITING_LIST_ADDED:
                    String responseStr = (String) msg.getObject();

                    if (responseStr.startsWith("IMMEDIATE:")) {
                        // Format: IMMEDIATE:TableID:Code
                        String[] parts = responseStr.split(":");
                        String tableId = parts[1];
                        String code = parts[2]; 

                        ui.showAlert("Good News!", 
                                     "A table is available right now!\n" +
                                     "Please proceed to Table #" + tableId + ".\n\n" +
                                     "Your Confirmation Code: " + code);
                    } else if ("WAITING".equals(responseStr)) {
                        ui.showAlert("Success", "No tables available right now.\nYou have been added to the Waiting List.");
                        
                    } else if ("DUPLICATE".equals(responseStr)) {
                        ui.showAlert("Info", "You are already on the waiting list.");
                        
                    } else {
                        ui.showAlert("Error", "Could not complete request.");
                    }
                    break;

                case GET_WAITING_LIST:
                    ArrayList<WaitingList> waitList = (ArrayList<WaitingList>) msg.getObject();
                    ui.refreshWaitingListData(waitList);
                    break;

                // --- DATA REFRESH (STAFF VIEWS) ---
                case UPDATE_SUCCESS:
                    ui.showAlert("Success", "Operation successful!");
                    break;

                case UPDATE_FAILED:
                    ui.showAlert("Error", "Operation failed on server side.");
                    break;

                case GET_ORDERS:
                case ORDERS_IMPORTED:
                    ArrayList<Order> orders = (ArrayList<Order>) msg.getObject();
                    ui.refreshOrderData(orders); 
                    break;
                
                case HISTORY_IMPORTED:
                    ArrayList<Order> history = (ArrayList<Order>) msg.getObject();
                    ui.openOrderHistory(history);
                    break;

                case GET_TABLES:
                    ArrayList<Table> tables = (ArrayList<Table>) msg.getObject();
                    ui.refreshTableData(tables); 
                    break;

                case GET_ALL_SUBSCRIBERS:
                    ArrayList<User> subs = (ArrayList<User>) msg.getObject();
                    ui.refreshSubscriberData(subs);
                    break;

                case GET_SCHEDULE:
                    ArrayList<BistroSchedule> schedule = (ArrayList<BistroSchedule>) msg.getObject();
                    ui.refreshScheduleData(schedule);
                    break;
                    
                case REPORT_GENERATED:
                    common.MonthlyReportData reportData = (common.MonthlyReportData) msg.getObject();
                    if (ui.getMonthlyReportUI() != null) {
                         ui.getMonthlyReportUI().updateReportData(reportData); 
                    }
                    break;

                // --- USER REGISTRATION & SEARCH ---
                case REGISTER_USER:
                    ui.showAlert("Registration", "User registered successfully.");
                    break;

                case REGISTRATION_SUCCESS:
                    User newSub = (User) msg.getObject();
                    ui.showAlert("Success", "Subscriber Registered Successfully!");
                    Platform.runLater(() -> ui.showDigitalCard(newSub));
                    break;

                case USER_FOUND:
                    User validUser = (User) msg.getObject();
                    this.ui.currentUser = validUser; 
                    
                    if ("SUBSCRIBER".equalsIgnoreCase(validUser.getUserType())) {
                        ui.openSubscriberDashboard(); 
                    } else {
                        ui.openCasualDashboard();     
                    }
                    break;

                case USER_NOT_FOUND:
                    ui.showAlert("Login Error", "No user found with that ID or Phone Number.");
                    break;    
                    
                case ERROR:
                    String errorMsg = (String) msg.getObject();
                    ui.showAlert("Server Error", errorMsg);
                    break;

                default:
                    System.out.println("Log: Unknown TaskType received: " + msg.getTask());
                    break;
            }
        });
    }
}