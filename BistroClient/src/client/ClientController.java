package client;

import common.Message;
import common.Order;
import common.TaskType;
import common.Table;
import common.User;
import common.WaitingList;

import javafx.application.Platform;

import java.util.ArrayList;

public class ClientController {

    // Reference to the network client
    private BistroClient client;

    // Reference to the UI layer
    private ClientUI ui;

    // Constructor receives UI so controller can update it
    public ClientController(ClientUI ui) {
        this.ui = ui;
    }

    // Attempt to connect to the server
    public boolean connect(String ip, int port) {
        try {
            // Create client and open connection
            client = new BistroClient(ip, port, this);
            client.openConnection();
            return true;
        } catch (Exception e) {
            // Connection failed
            return false;
        }
    }
    public void accept(Object msg) {
        if (client != null) {
            client.sendKryoRequest((Message) msg);
        }
    }

    // Request all orders from the server
    public void getAllOrders() {
        if (client == null) return;
        Message msg = new Message(TaskType.GET_ORDERS, null);
        client.sendKryoRequest(msg);
    }

    // Send update request for a specific order
    public void updateOrder(Order order) {
        if (client == null) return;
        Message msg = new Message(TaskType.UPDATE_ORDER, order);
        client.sendKryoRequest(msg);
    }

    // Called when the client receives a message from the server
    public void handleMessageFromClient(Message msg) {

        // Ensure UI updates run on the JavaFX Application Thread
        Platform.runLater(() -> {
        		
        	String response = msg.getObject().toString();
            switch (msg.getTask()) {

            	case SUCCESS:
            		ui.showAlert("Success", response);
                    getAllOrders(); // Refresh UI
                    break;
            	case FAIL:
            		ui.showAlert("Error", response);
                    getAllOrders(); // Refresh UI
                    break;    
                // Order updated successfully
                case UPDATE_SUCCESS:
                    ui.showAlert("Success", "Order updated successfully!");
                    getAllOrders(); // Refresh UI
                    break;

                // Update failed on server side
                case UPDATE_FAILED:
                    ui.showAlert("Error", "Update failed.");
                    break;
                case GET_TABLES:
                    ArrayList<Table> tables = (ArrayList<Table>) msg.getObject();
                    ui.refreshTableData(tables); 
                    break;
                // --------------------------
                 //  SUBSCRIBERS
                case GET_ALL_SUBSCRIBERS:
                    ArrayList<User> subs = (ArrayList<User>) msg.getObject();
                    ui.refreshSubscriberData(subs);
                    break;

                //  ACTIVE ORDERS / DINERS
                case GET_ORDERS:
                case ORDERS_IMPORTED: // Check if your Server uses this name instead!
                    System.out.println("Log: Received orders from server.");
                    ArrayList<Order> orders = (ArrayList<Order>) msg.getObject();
                    ui.refreshOrderData(orders); // Calls the bridge in ClientUI
                    break;

                //  WAITING LIST
                case GET_WAITING_LIST:
                    ArrayList<WaitingList> waitList = (ArrayList<WaitingList>) msg.getObject();
                    ui.refreshWaitingListData(waitList);
                    break;
            }


            
        });
    }

    // Called when server disconnects unexpectedly
    public void serverWentDown() {
        Platform.runLater(() -> {
            ui.handleServerDisconnect();
        });
    }

    // Disconnect gracefully when exiting
    public void disconnect() {
        if (client != null) {
            try {
                client.quit();  // Close connection
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
