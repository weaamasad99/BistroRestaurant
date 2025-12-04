package client;


import ocsf.client.AbstractClient;
import common.KryoUtil; // Import the utility
import common.Message;
import common.Order;
import common.TaskType;
import java.util.ArrayList;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class BistroClient extends AbstractClient {

    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     */
    public BistroClient(String host, int port) {
        super(host, port);
    }

    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
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
            }
        }
    }

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
     * Sends a request to the server using Kryo serialization.
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
     */
    @Override
    protected void connectionEstablished() {
        System.out.println("Log: Connection to server established successfully.");
    }
    

    
    /**
     * Closes the connection and terminates the client.
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