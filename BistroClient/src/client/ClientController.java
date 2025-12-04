package client;

import common.Message;
import common.Order;
import common.TaskType;
import java.sql.Date;

/**
 * Controller to manage logic between UI and Client networking.
 */
public class ClientController {
    
    public static BistroClient client;

    /**
     * Connects to the server.
     */
    public void connect(String ip, int port) {
        try {
            client = new BistroClient(ip, port);
            client.openConnection();
            System.out.println("Log: Connected to server.");
        } catch (Exception e) {
            System.out.println("Log: Connection failed.");
        }
    }

    /**
     * Asks the server to retrieve all orders.
     */
    public void getAllOrders() {
        Message msg = new Message(TaskType.GET_ORDERS, null);
        client.sendKryoRequest(msg);
    }

    /**
     * Asks the server to update a specific order.
     */
    public void updateOrderSample() {
        // Sample data update for prototype
        Date newDate = Date.valueOf("2026-01-01");
        // Order #1, update date and guests to 10.
        Order updatedOrder = new Order(1, newDate, 10, 0, 0, null); 
        
        Message msg = new Message(TaskType.UPDATE_ORDER, updatedOrder);
        client.sendKryoRequest(msg);
    }
}