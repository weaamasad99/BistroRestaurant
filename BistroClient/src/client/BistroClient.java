package client;

import ocsf.client.AbstractClient;

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
        // TODO: Update Client GUI with the data received from Server/DB
        System.out.println("Message received from server: " + msg.toString());
    }

    /**
     * Hook method called after the connection has been established.
     */
    @Override
    protected void connectionEstablished() {
        System.out.println("Log: Connection to server established successfully.");
    }
    
    /**
     * Handles the request sent from the UI to the server.
     * * @param message The object/command to send to the server.
     */
    public void sendRequestToServer(Object message) {
        try {
            sendToServer(message);
        } catch (Exception e) {
            System.out.println("Error: Could not send message to server. Terminating client.");
            quit();
        }
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