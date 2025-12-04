package client;

import common.Message;
import common.Order;
import common.TaskType;
import javafx.application.Platform;

import java.util.ArrayList;

public class ClientController {
    
    private BistroClient client;
    private ClientUI ui;

    public ClientController(ClientUI ui) {
        this.ui = ui;
    }

    public boolean connect(String ip, int port) {
        try {
            client = new BistroClient(ip, port, this);
            client.openConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void getAllOrders() {
        if (client == null) return;
        Message msg = new Message(TaskType.GET_ORDERS, null);
        client.sendKryoRequest(msg);
    }

    public void updateOrder(Order order) {
        if (client == null) return;
        Message msg = new Message(TaskType.UPDATE_ORDER, order);
        client.sendKryoRequest(msg);
    }

    // Callback from BistroClient
    public void handleMessageFromClient(Message msg) {
        // Run on JavaFX Thread
        Platform.runLater(() -> {
            switch (msg.getTask()) {
                case ORDERS_IMPORTED:
                    ArrayList<Order> orders = (ArrayList<Order>) msg.getObject();
                    ui.updateOrderTable(orders);
                    break;
                case UPDATE_SUCCESS:
                    ui.showAlert("Success", "Order updated successfully!");
                    getAllOrders(); // Refresh table
                    break;
                case UPDATE_FAILED:
                    ui.showAlert("Error", "Update failed.");
                    break;
            }
        });
    }
    
    public void serverWentDown() {
        Platform.runLater(() -> {
            ui.handleServerDisconnect(); 
        });}
    public void disconnect() {
        if (client != null) {
            try {
                client.quit(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}