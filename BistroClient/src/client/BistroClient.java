package client;

import ocsf.client.AbstractClient;
import common.KryoUtil;
import common.Message;
import common.Order;
import common.TaskType;
import controllers.ClientController;

import java.util.ArrayList;

/**
 * The BistroClient class handles the client-side connection and communication logic
 * for the bistro application, extending the OCSF AbstractClient.
 */
public class BistroClient extends AbstractClient {

	private ClientController controller;

    /**
     * Constructs an instance of the BistroClient.
     *
     * @param host       The server's host address.
     * @param port       The port number to connect to.
     * @param controller The controller instance used to communicate with the client GUI.
     */
    public BistroClient(String host, int port, ClientController controller) {
        super(host, port);
        this.controller = controller;
    }

    /**
     * Handles any message received from the server.
     * Expects the message to be a byte array, which is deserialized into a Message object
     * before being processed and sent to the controller.
     *
     * @param msg The message received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        // Step 1: Check for byte array
        if (msg instanceof byte[]) {
            // Step 2: Deserialize
            Object deserializedObj = KryoUtil.deserialize((byte[]) msg);
            
            if (deserializedObj instanceof Message) {
            	
                Message message = (Message) deserializedObj;
                processServerResponse(message);
                if (controller != null) {
                    controller.handleMessageFromClient(message);
                } else {
                    System.out.println("Error: Controller is null, cannot update GUI.");
                }
            }
        }
    }

    /**
     * Processes the deserialized message based on its TaskType.
     * Handles specific logic for order imports and update statuses.
     *
     * @param message The deserialized message object containing the task and data.
     */
    private void processServerResponse(Message message) {
        switch (message.getTask()) {
            case ORDERS_IMPORTED:
                ArrayList<Order> orders = (ArrayList<Order>) message.getObject();
                System.out.println("--- Kryo Data from DB ---");
                for (Order o : orders) {
                    System.out.println(o);
                }
                break;
            case UPDATE_SUCCESS:
                System.out.println("--- Update Successful (Kryo) ---");
                break;
            case UPDATE_FAILED:
                System.out.println("--- Update Failed ---");
                break;
        }
    }

    /**
     * Serializes a Message object using Kryo and sends it to the server.
     *
     * @param msg The message object to be sent.
     */
    public void sendKryoRequest(Message msg) {
        try {
            byte[] data = KryoUtil.serialize(msg);
            sendToServer(data);
        } catch (Exception e) {
            System.out.println("Could not send Kryo message to server.");
            e.printStackTrace();
        }
    }

    /**
     * Hook method called after the connection has been established.
     * Logs the successful connection.
     */
    @Override
    protected void connectionEstablished() {
        System.out.println("Log: Connection to server established successfully.");
    }
    

    /**
     * Hook method called after the connection has been closed.
     * Notifies the controller that the server connection has gone down.
     */
    @Override
    protected void connectionClosed() {
        System.out.println("Server connection closed.");
        if (controller != null) {
            controller.serverWentDown();	
        }
    }

    /**
     * Hook method called each time an exception is thrown by the client's
     * thread that is waiting for messages from the server.
     *
     * @param exception The exception raised.
     */
    @Override
    protected void connectionException(Exception exception) {
        System.out.println("Server connection exception (Crash).");
        if (controller != null) {
            controller.serverWentDown();
        }
    }

    /**
     * Closes the connection to the server and terminates the client application.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}