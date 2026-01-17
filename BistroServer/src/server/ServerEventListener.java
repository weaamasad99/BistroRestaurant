package server;

import ocsf.server.ConnectionToClient;

/**
 * Interface for communication between the BistroServer and the ServerUI.
 * Allows the server logic to push updates (logs, client connections) to the GUI.
 * @author Group 6
 * @version 1.0
 */
public interface ServerEventListener {
    /**
     * Outputs a log message to the server console.
     * @param message The text to log.
     */
    void onLog(String message);
    
    /**
     * Triggered when a new client connects to the server (TCP level).
     * @param client The client connection object.
     */
    void onClientConnected(ConnectionToClient client);
    
    /**
     * Triggered when a client disconnects from the server.
     * @param client The client connection object.
     */
    void onClientDisconnected(ConnectionToClient client);
    
    /**
     * Triggered when a client successfully logs in or identifies.
     * Updates the connected clients table with user details.
     * @param client The client connection.
     * @param username The name/ID of the user.
     * @param role The role of the user.
     */
    void onUserLoggedIn(ConnectionToClient client, String username, String role);
}