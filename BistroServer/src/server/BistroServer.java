package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.BistroSchedule;
import common.KryoUtil;
import common.Message;
import common.Order;
import common.Table;
import common.TaskType;
import common.User;
import common.WaitingList;

// Import the specific Controllers
import controllers.UserController;
import controllers.ReservationController;
import controllers.SubscriberController;
import controllers.WaitingListController;
import JDBC.DatabaseConnection;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class BistroServer extends AbstractServer {

    // Callback to update the Server GUI: (Client, IsConnected)
    private BiConsumer<ConnectionToClient, Boolean> connectionListener;

    // Controllers to handle specific business logic
    private UserController userController;
    private ReservationController reservationController;
    private SubscriberController subscriberController;
    private WaitingListController waitingListController;

    public BistroServer(int port, BiConsumer<ConnectionToClient, Boolean> connectionListener) {
        super(port);
        this.connectionListener = connectionListener;
        
        // Initialize Controllers
        // Note: They rely on DatabaseConnection which is initialized in serverStarted()
        this.userController = new UserController();
        this.reservationController = new ReservationController();
        this.subscriberController = new SubscriberController();
        this.waitingListController = new WaitingListController();
    }

    /**
     * This method handles any messages received from the client.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        // Step 1: Check if the message is a byte array (Kryo payload)
        if (msg instanceof byte[]) {
            // System.out.println("Log: Received binary data (Kryo). Deserializing...");
            
            // Step 2: Deserialize using Kryo
            Object deserializedMsg = KryoUtil.deserialize((byte[]) msg);
            
            if (deserializedMsg instanceof Message) {
                Message message = (Message) deserializedMsg;
                try {
                    processMessage(message, client);
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Log: Received unknown format: " + msg.getClass());
        }
    }

    /**
     * Routes the incoming message to the appropriate Controller based on TaskType.
     */
    private void processMessage(Message message, ConnectionToClient client) {
        Message response = null;
        boolean success;

        switch (message.getTask()) {

            // ===============================================================
            // USER & LOGIN
            // ===============================================================
            case LOGIN_REQUEST:
                System.out.println("Log: Processing Login Request...");
                User loginReq = (User) message.getObject();
                // Delegate to UserController
                User loggedInUser = userController.loginUser(loginReq.getUsername(), loginReq.getPassword());
                
                // Return the full User object (or null if failed)
                response = new Message(TaskType.LOGIN_RESPONSE, loggedInUser);
                sendKryoToClient(response, client);
                break;

            case CREATE_CASUAL:
                System.out.println("Log: Creating Casual User...");
                String phone = (String) message.getObject();
                success = userController.createCasualRecord(phone);
                
                String resultMsg = success ? "Casual registered successfully" : "Failed to register casual";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                sendKryoToClient(response, client);
                break;

            case REGISTER_USER:
                // Logic for full registration would go here via UserController
                break;

            // ===============================================================
            // ORDERS & RESERVATIONS
            // ===============================================================
            case GET_ORDERS:
                System.out.println("Log: Fetching all orders...");
                ArrayList<Order> orders = reservationController.getAllOrders();
                response = new Message(TaskType.GET_ORDERS, orders);
                sendKryoToClient(response, client);
                break;

            case UPDATE_ORDER:
                System.out.println("Log: Updating order...");
                Order orderToUpdate = (Order) message.getObject();
                success = reservationController.updateOrder(orderToUpdate);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // TABLE MANAGEMENT
            // ===============================================================
            case GET_TABLES:
                System.out.println("Log: Fetching all tables...");
                ArrayList<Table> tables = reservationController.getAllTables();
                response = new Message(TaskType.GET_TABLES, tables);
                sendKryoToClient(response, client);
                break;

            case UPDATE_TABLE:
                System.out.println("Log: Updating table...");
                Table tableToUpdate = (Table) message.getObject();
                success = reservationController.updateTable(tableToUpdate);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;
                
            case ADD_TABLE:
                System.out.println("Log: Adding new table...");
                Table newTable = (Table) message.getObject();
                success = reservationController.addTable(newTable);
                // Send refresh or success
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            case REMOVE_TABLE:
                System.out.println("Log: Removing table...");
                int tableId = (int) message.getObject();
                success = reservationController.removeTable(tableId);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            // SUBSCRIBERS
            case GET_ALL_SUBSCRIBERS:
                System.out.println("Log: Fetching subscribers...");
                ArrayList<User> subscribers = subscriberController.getAllSubscribers();
                response = new Message(TaskType.GET_ALL_SUBSCRIBERS, subscribers);
                sendKryoToClient(response, client);
                break;

            // WAITING LIST
            case GET_WAITING_LIST:
                System.out.println("Log: Fetching waiting list...");
                ArrayList<WaitingList> waitList = waitingListController.getAllWaitingList();
                response = new Message(TaskType.GET_WAITING_LIST, waitList);
                sendKryoToClient(response, client);
                break;
            
             // ===============================================================
                // OPENING HOURS / SCHEDULE
                // ===============================================================
                case GET_SCHEDULE:
                    System.out.println("Log: Fetching schedule...");
                    ArrayList<BistroSchedule> schedule = reservationController.getSchedule();
                    response = new Message(TaskType.GET_SCHEDULE, schedule);
                    sendKryoToClient(response, client);
                    break;

                case SAVE_SCHEDULE_ITEM:
                    System.out.println("Log: Saving full schedule...");
                    
                    // 1. Cast the object to ArrayList
                    ArrayList<BistroSchedule> list = (ArrayList<BistroSchedule>) message.getObject();
                    
                    // 2. Loop and save each item silently
                    boolean allSaved = true;
                    for (BistroSchedule item : list) {
                        if (!reservationController.saveScheduleItem(item)) {
                            allSaved = false;
                        }
                    }
                    
                    // 3. Send  response back to the client
                    if (allSaved) {
                        response = new Message(TaskType.UPDATE_SUCCESS, null);
                    } else {
                        response = new Message(TaskType.UPDATE_FAILED, "Some items failed to save.");
                    }
                    sendKryoToClient(response, client);
                    break;

                case DELETE_SCHEDULE_ITEM:
                    System.out.println("Log: Deleting schedule item...");
                    String id = (String) message.getObject();
                    success = reservationController.deleteScheduleItem(id);
                    response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                    sendKryoToClient(response, client);
                    break;

            // DEFAULT
            default:
                System.out.println("Log: Unknown TaskType received: " + message.getTask());
                break;
        }
    }

    /**
     * Helper method to serialize an object and send it to the client.
     */
    private void sendKryoToClient(Object msg, ConnectionToClient client) {
        try {
            byte[] bytes = KryoUtil.serialize(msg);
            client.sendToClient(bytes);
        } catch (Exception e) {
            System.err.println("Error sending message to client.");
            e.printStackTrace();
        }
    }

    /**
     * Called when the server starts listening for connections.
     * Initializes the DB connection.
     */
    @Override
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
        // Ensure DB connection is established immediately upon server start
        DatabaseConnection.getInstance(); 
    }

    /**
     * Called when the server stops listening for connections.
     */
    @Override
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    /**
     * Hook method called each time a new client connection is accepted.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        // Update Server GUI
        if (connectionListener != null) {
            connectionListener.accept(client, true);
        }
    }
    
    /**
     * Hook method called each time a client disconnects.
     */
    @Override
    synchronized protected void clientDisconnected(ConnectionToClient client) {
        // Update Server GUI
        if (connectionListener != null) {
            connectionListener.accept(client, false);
        }
    }
    
    /**
     * Called when the client crashes or is closed abruptly.
     */
    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
        clientDisconnected(client);
    }
}