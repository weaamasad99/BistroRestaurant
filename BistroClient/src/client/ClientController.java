package client;

import common.Message;
import common.Order;
import common.TaskType;
import common.Table;
import common.User;
import common.WaitingList;

import javafx.application.Platform;
import java.util.ArrayList;

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
            
                // --- LOGIN PROCESS ---
                case LOGIN_RESPONSE:
                    User user = (User) msg.getObject();
                    if (user != null) {
                        ui.showAlert("Login Success", "Welcome back, " + user.getUsername());
                        // Note: You can add logic here to auto-navigate based on user role if needed
                    } else {
                        ui.showAlert("Login Failed", "Invalid credentials.");
                    }
                    break;

                // --- RESERVATION PROCESS ---
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

                case CHECK_IN_DENIED:
                    String reason = (String) msg.getObject();
                    ui.showAlert("Check-in Error", reason);
                    break;

                // --- WAITING LIST UPDATES ---
                case WAITING_LIST_ADDED:
                    ui.showAlert("Success", "You have been added to the Waiting List.");
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
                    ui.showAlert("Order History", sb.toString());
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

                default:
                    System.out.println("Log: Unknown TaskType received: " + msg.getTask());
                    break;
            }
        });
    }
}