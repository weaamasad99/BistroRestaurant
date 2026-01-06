package server;

import ocsf.server.ConnectionToClient;

public interface ServerEventListener {
    void onLog(String message);
    void onClientConnected(ConnectionToClient client);
    void onClientDisconnected(ConnectionToClient client);
    
    // Updated: Now accepts the 'client' object to get IP/Host info when adding to the table
    void onUserLoggedIn(ConnectionToClient client, String username, String role);
}