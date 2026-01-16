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
 * The ClientController acts as the central "Network Manager".
 * It handles the physical connection to the server and routes incoming 
 * responses to the main ClientUI.
 */
public class ClientController {

    // Reference to the network client
    private BistroClient client;

    // Reference to the UI layer
    private ClientUI ui;

    // Constructor receives UI so controller can update it
    public ClientController(ClientUI ui) {
        this.ui = ui;
    }

    // =======================================================
    // CONNECTION MANAGEMENT
    // =======================================================

    /**
     * Attempts to connect to the server.
     * @param ip The IP address of the server.
     * @param port The port number.
     * @return true if connection successful, false otherwise.
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
     * Disconnects gracefully when exiting.
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
     * Called when the server disconnects unexpectedly.
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
     * The main method used by all specific controllers (Casual, Subscriber, Rep)
     * to send messages to the server.
     * @param msg The Message object containing TaskType and data.
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
     * Called when the client receives a message from the server.
     * Routes the data to the correct UI method.
     */
    public void handleMessageFromClient(Message msg) {

        // Ensure UI updates run on the JavaFX Application Thread
        Platform.runLater(() -> {
        	
        	User user;
        	
            switch (msg.getTask()) {
		
		        // --- NEW CASES FOR GENERIC SUCCESS/FAIL ---
		        case SUCCESS:
		            String successText = (String) msg.getObject();
		            ui.showAlert("Success", successText);
		            break;
		
		        case FAIL:
		            String failText = (String) msg.getObject();
		            ui.showAlert("Operation Failed", failText);
		            break;
		        case SET_USER:
		        	user = (User) msg.getObject();
		        	this.ui.currentUser = user;    
            
                // --- LOGIN PROCESS ---
		        	// Inside ClientController.java -> handleMessageFromClient

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
		                // --- ADD THIS BLOCK ---
		                else if ("REPRESENTATIVE".equalsIgnoreCase(type) || "MANAGER".equalsIgnoreCase(type)) {
		                    ui.openRepresentativeDashboard(user);
		                } 
		                // ----------------------
		                else {
		                    // Only defaults to casual if it's not the others
		                    ui.openCasualDashboard();
		                }

		            } else {
		                ui.showAlert("Login Failed", "Invalid credentials.");
		            }
		            break;

                // --- RESERVATION PROCESS ---
		        case REQUEST_RESERVATION:
	                String resResult = (String) msg.getObject();
	                
	                Platform.runLater(() -> {
	                    if (resResult.startsWith("OK:")) {
	                        // Success
	                        String code = resResult.split(":", 2)[1];
	                        ui.showAlert("Success", "Reservation Confirmed!\nYour Code: " + code);
	                    } 
	                    else if (resResult.startsWith("SUGGEST:")) {
	                        // FIX IS HERE: split(":", 2) ensures we don't break the time string (15:00)
	                        String timesStr = resResult.split(":", 2)[1]; 
	                        String[] options = timesStr.split(",");
	                        
	                        ui.showAlternativeTimesDialog(options);
	                    } 
	                    else {
	                        // Regular Error
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

                // --- CHECK-IN / IDENTIFICATION ---
                case CHECK_IN_APPROVED:
                    ui.showAlert("Welcome", "Check-in Approved! Your table is ready.");
                    break;
                case REPORT_GENERATED:
                    common.MonthlyReportData reportData = (common.MonthlyReportData) msg.getObject();
                    // Assuming you added the method to ClientUI:
                    ui.getMonthlyReportUI().updateReportData(reportData); 
                    break;

                case CHECK_IN_DENIED:
                    String reason = (String) msg.getObject();
                    ui.showAlert("Check-in Error", reason);
                    break;
                case GET_BILL:
                    // Server now sends: Object[] { code, price, userType }
                    Object[] billData = (Object[]) msg.getObject();
                    String bCode = (String) billData[0];
                    Double bPrice = (Double) billData[1];
                    String bType = (String) billData[2];
                    
                    boolean isSub = "SUBSCRIBER".equalsIgnoreCase(bType);
                    
                    // We need to pass these real values to the UI
                    // Assuming ClientUI has a method to access checkoutUI or we call it directly if available
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
                        String code = parts[2]; // Now extracting the code

                        // Display the code clearly to the user
                        ui.showAlert("Good News!", 
                                     "A table is available right now!\n" +
                                     "Please proceed to Table #" + tableId + ".\n\n" +
                                     "Your Confirmation Code: " + code); // Added code here
                                     
                        // Optional: Refresh table view if necessary
                        // accept(new Message(TaskType.GET_TABLES, null));

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

                // --- ORDER MANAGEMENT ---
                case UPDATE_SUCCESS:
                    ui.showAlert("Success", "Operation successful!");
                    // If we just updated an order or table, we often want to refresh the view
                    break;

                case UPDATE_FAILED:
                    ui.showAlert("Error", "Operation failed on server side.");
                    break;

                case GET_ORDERS:
                case ORDERS_IMPORTED:
                    // System.out.println("Log: Received orders from server.");
                    ArrayList<Order> orders = (ArrayList<Order>) msg.getObject();
                    ui.refreshOrderData(orders); 
                    break;
                
                case HISTORY_IMPORTED:
                    ArrayList<Order> history = (ArrayList<Order>) msg.getObject();
                    // Display history in a popup or alert for now
                    StringBuilder sb = new StringBuilder("Your History:\n");
                    for(Order o : history) {
                        sb.append(o.getOrderDate()).append(" - ").append(o.getOrderTime()).append("\n");
                    }
                    ui.openOrderHistory(history);
                    break;

                // --- STAFF / DATA MANAGEMENT ---
                case GET_TABLES:
                    ArrayList<Table> tables = (ArrayList<Table>) msg.getObject();
                    ui.refreshTableData(tables); 
                    break;

                case GET_ALL_SUBSCRIBERS:
                    ArrayList<User> subs = (ArrayList<User>) msg.getObject();
                    ui.refreshSubscriberData(subs);
                    break;
                
                case REGISTER_USER:
                    // Usually returns a boolean or the new User
                    ui.showAlert("Registration", "User registered successfully.");
                    break;

                // --- ERROR HANDLING ---
                case ERROR:
                    String errorMsg = (String) msg.getObject();
                    ui.showAlert("Server Error", errorMsg);
                    break;
                case GET_SCHEDULE:
                    ArrayList<BistroSchedule> schedule = (ArrayList<BistroSchedule>) msg.getObject();
                    // Pass data to the UI manager
                    ui.refreshScheduleData(schedule);
                    break;
                    

                case REGISTRATION_SUCCESS:
                    User newSub = (User) msg.getObject();
                    // 1. Show simple success alert
                    ui.showAlert("Success", "Subscriber Registered Successfully!");
                    
                    // 2. Launch the Digital Card Popup
                    Platform.runLater(() -> ui.showDigitalCard(newSub));
                    break;
                    

                case USER_FOUND:
                    User validUser = (User) msg.getObject();
                    this.ui.currentUser = validUser; 
                    
                    // ui.showAlert("Success", "User found! Redirecting..."); // Optional
                    
                    // Check type to know which Java Class to load
                    if ("SUBSCRIBER".equalsIgnoreCase(validUser.getUserType())) {
                        ui.openSubscriberDashboard(); // Call the new method
                    } else {
                        ui.openCasualDashboard();     // Call the new method
                    }
                    break;

                case USER_NOT_FOUND:
                    ui.showAlert("Login Error", "No user found with that ID or Phone Number.");
                    break;    

                default:
                    System.out.println("Log: Unknown TaskType received: " + msg.getTask());
                    break;
            }
        });
    }
}