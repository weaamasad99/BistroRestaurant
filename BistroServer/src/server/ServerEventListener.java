package server;

import ocsf.server.ConnectionToClient;

/**
 * Interface to handle communication between the BistroServer logic and the ServerUI.
 */
public interface ServerEventListener {
    // Called when a log message needs to be displayed
    void onLog(String message);

    // Called when a new physical client connects
    void onClientConnected(ConnectionToClient client);

    // Called when a client disconnects
    void onClientDisconnected(ConnectionToClient client);

    // Called when a user successfully logs in (updates the table row)
    void onUserLoggedIn(long threadId, String username, String role);
}