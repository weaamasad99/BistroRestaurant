package client;

import common.Message;
import common.Order;
import common.TaskType;
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

            switch (msg.getTask()) {

                // Server sent list of orders
                case ORDERS_IMPORTED:
                    ArrayList<Order> orders = (ArrayList<Order>) msg.getObject();
                    ui.updateOrderTable(orders);
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
