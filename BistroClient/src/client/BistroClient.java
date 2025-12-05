package client;


import ocsf.client.AbstractClient;
import common.KryoUtil; // Import the utility
import common.Message;
import common.Order;
import common.TaskType;
import java.util.ArrayList;

public class BistroClient extends AbstractClient {

	private ClientController controller;
    /*
      Constructs an instance of the chat client.
    */
    public BistroClient(String host, int port, ClientController controller) {
        super(host, port);
        this.controller = controller;
    }

    /*
      This method handles all data that comes in from the server.
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

    /*
      Sends a request to the server using Kryo serialization.
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

    /*
      Hook method called after the connection has been established.
    */
    @Override
    protected void connectionEstablished() {
        System.out.println("Log: Connection to server established successfully.");
    }
    

    /*
      Hook method called after the connection has been closed.
    */
    @Override
    protected void connectionClosed() {
        System.out.println("Server connection closed.");
        if (controller != null) {
            controller.serverWentDown();	
        }
    }

    /*
      Hook method called each time an exception is thrown by the client's
      thread that is waiting for messages from the server.
    */
    @Override
    protected void connectionException(Exception exception) {
        System.out.println("Server connection exception (Crash).");
        if (controller != null) {
            controller.serverWentDown();
        }
    }

    /*
      Closes the connection and terminates the client.
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