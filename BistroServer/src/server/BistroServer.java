package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import ocsf.server.ConnectionToClient;
import common.KryoUtil; // Import the utility 
import common.Message;
import common.Order;
import common.TaskType;
import java.util.ArrayList;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 */
public class BistroServer extends AbstractServer {

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public BistroServer(int port) {
        super(port);
    }

    /**
     * This method handles any messages received from the client.
     *
     * @param msg    The message received from the client.
     * @param client The connection from which the message originated.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        // Step 1: Check if the message is a byte array (Kryo payload)
        if (msg instanceof byte[]) {
            System.out.println("Log: Received binary data (Kryo). Deserializing...");
            
            // Step 2: Deserialize using Kryo
            Object deserializedMsg = KryoUtil.deserialize((byte[]) msg);
            
            if (deserializedMsg instanceof Message) {
                Message message = (Message) deserializedMsg;
                processMessage(message, client);
            }
        } else {
            System.out.println("Log: Received unknown format: " + msg.getClass());
        }
    }

    private void processMessage(Message message, ConnectionToClient client) {
        switch (message.getTask()) {
            case GET_ORDERS:
                System.out.println("Log: Fetching orders...");
                ArrayList<Order> orders = MySQLConnection.getInstance().getAllOrders();
                
                // Step 3: Serialize response using Kryo before sending
                Message response = new Message(TaskType.ORDERS_IMPORTED, orders);
                sendKryoToClient(response, client);
                break;

            case UPDATE_ORDER:
                System.out.println("Log: Updating order...");
                Order orderToUpdate = (Order) message.getObject();
                boolean success = MySQLConnection.getInstance().updateOrder(orderToUpdate);
                
                Message updateResponse = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(updateResponse, client);
                break;
        }
    }

    // Helper method to send Kryo bytes
    private void sendKryoToClient(Object msg, ConnectionToClient client) {
        try {
            byte[] bytes = KryoUtil.serialize(msg);
            client.sendToClient(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    @Override
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
        MySQLConnection.getInstance();
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    @Override
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    /**
     * Hook method called each time a new client connection is accepted.
     * Required for the prototype to display connection details.
     * * @param client the connection connected to the client.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        super.clientConnected(client);
        System.out.println("--- Client Connected ---");
        System.out.println("Client IP: " + client.getInetAddress().getHostAddress());
        System.out.println("Client Host: " + client.getInetAddress().getHostName());
        System.out.println("Connection Status: Active");
    }

    /**
     * Hook method called each time a client disconnects.
     * * @param client the connection with the client.
     */
    @Override
    synchronized protected void clientDisconnected(ConnectionToClient client) {
        System.out.println("--- Client Disconnected ---");
    }
}