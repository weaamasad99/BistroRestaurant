package server;

import java.io.IOException;


public class ServerUI {
    
    public static void main(String[] args) {
        int port = 5555; // Default port
        
        BistroServer server = new BistroServer(port);
        
        try {
            server.listen(); // Start listening for connections
        } catch (IOException e) {
            System.out.println("ERROR - Could not listen for clients!");
            e.printStackTrace();
        }
    }
}