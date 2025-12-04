package client;

public class ClientUI {
    public static void main(String[] args) {
        ClientController controller = new ClientController();
        
        // 1. Connect
        controller.connect("localhost", 5555);
        
        // 2. Get Data
        System.out.println("Command: Get All Orders");
        controller.getAllOrders();
        
        // Sleep mainly for prototype visualization in console
        try { Thread.sleep(2000); } catch (Exception e) {} 

        // 3. Update Data
        System.out.println("\nCommand: Update Order #1");
        controller.updateOrderSample();
        
        // Sleep to ensure update finishes before reading again
        try { Thread.sleep(2000); } catch (Exception e) {} 

        // 4. Verify Update
        System.out.println("\nCommand: Verify Update (Get All Orders)");
        controller.getAllOrders();
    }
}