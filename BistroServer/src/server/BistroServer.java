package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

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
        System.out.println("Message received: " + msg + " from " + client);

        // TODO: Add database logic here (SELECT/UPDATE) based on 'msg' content.
        
        // Example: Sending a response back to the client
        try {
            client.sendToClient("Server response: Received " + msg);
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