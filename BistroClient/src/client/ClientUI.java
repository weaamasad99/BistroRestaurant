package client;

import java.io.IOException;

/**
 * Main entry point for the Client side.
 */
public class ClientUI {
    
    public static void main(String[] args) {
        String host = "localhost"; // Use "localhost" for testing on same machine, or IP for remote.
        int port = 5555;
        
        BistroClient client = new BistroClient(host, port);
        
        try {
            client.openConnection(); // Attempt to connect to the server
            
            // Sending a test message (Simulating a request to the DB)
            client.sendRequestToServer("Prototype Test: Hello Server!"); 
            
        } catch (IOException e) {
            System.out.println("Error: Failed to open connection to server.");
            System.out.println("Ensure the Server is running first.");
            e.printStackTrace();
        }
    }
}