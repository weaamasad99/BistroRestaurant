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
import controllers.PaymentController;
import controllers.ReservationController;
import controllers.SubscriberController;
import controllers.WaitingListController;
import JDBC.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiConsumer;

public class BistroServer extends AbstractServer {

    // Callback to update the Server GUI: (Client, IsConnected)
    private BiConsumer<ConnectionToClient, Boolean> connectionListener;

    // Controllers to handle specific business logic
    private UserController userController;
    private ReservationController reservationController;
    private SubscriberController subscriberController;
    private WaitingListController waitingListController;
    private PaymentController paymentController;

    public BistroServer(int port, BiConsumer<ConnectionToClient, Boolean> connectionListener) {
        super(port);
        this.connectionListener = connectionListener;
        
        // Initialize Controllers
        // Note: They rely on DatabaseConnection which is initialized in serverStarted()
        this.userController = new UserController();
        this.reservationController = new ReservationController();
        this.subscriberController = new SubscriberController();
        this.waitingListController = new WaitingListController();
        this.paymentController = new PaymentController();
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
        String resultMsg,code;
        boolean success;
        int tableId;
        
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
            case SET_USER:
        		System.out.println("Log: Retrieving user...");
        		String phone = (String) message.getObject();
                User user = userController.getUserByPhone(phone);
                
                response = new Message(TaskType.SET_USER, user);
                sendKryoToClient(response, client);
                break;    

            case CREATE_CASUAL:
                System.out.println("Log: Creating Casual User...");
                String phoneNumber = (String) message.getObject();
                success = userController.createCasualRecord(phoneNumber);
                
                resultMsg = success ? "Casual registered successfully" : "Failed to register casual";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                sendKryoToClient(response, client);
                break;


            case REGISTER_USER:
                System.out.println("Log: Processing Subscriber Registration...");
                User subToRegister = (User) message.getObject();

                // 1. Register in DB (This generates the ID)
                User registeredSub = userController.registerNewSubscriber(subToRegister);

                if (registeredSub != null) {
                    // 2. CRITICAL: Send specific success message with the User object
                    response = new Message(TaskType.REGISTRATION_SUCCESS, registeredSub);
                } else {
                    response = new Message(TaskType.FAIL, "Registration failed. User may already exist.");
                }
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // ORDERS & RESERVATIONS
            // ===============================================================
            case REQUEST_RESERVATION:
            	System.out.println("Log: Creating Reservation...");
            	Order order = (Order) message.getObject();
            	success = reservationController.createReservation(order);
            	
            	resultMsg = success ? "Registered reservation successfully\nConfirmation code: " + order.getConfirmationCode() : "reservation is booked";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                sendKryoToClient(response, client);
                break;
            	
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
                
            case ENTER_WAITING_LIST:
                System.out.println("Log: Adding to Waiting List...");
                WaitingList wlData = (WaitingList) message.getObject();

                success = waitingListController.addToWaitingList(wlData);
                resultMsg = success ? "Successfully joined waiting list!\n Confirmation code: " + userController.generateConfirmationCode() : "Failed to join waiting list";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                
                sendKryoToClient(response, client);
                break;    
            case CHECK_IN_CUSTOMER:
            	System.out.println("Log: Check-in request received...");
                code = (String) message.getObject();
                
                tableId = reservationController.checkIn(code);

                if (tableId > 0) {
                    String msg = "Check-in Successful! Please proceed to Table " + tableId;
                    response = new Message(TaskType.SUCCESS, msg); 
                } 
                else if (tableId == -2) {
                    response = new Message(TaskType.FAIL, "Invalid confirmation code.");
                } 
                else if (tableId == -3) {
                    response = new Message(TaskType.FAIL, "No tables available for your group size, please wait.");
                } 
                else {
                    response = new Message(TaskType.FAIL, "Database error occurred.");
                }
                
                sendKryoToClient(response, client);
                break;
            case GET_BILL:
            	System.out.println("Log: Getting Bill...");
                code = (String) message.getObject();
                
                success = paymentController.getBill(code);
                resultMsg = success ? code : "Invalid confirmation code.";
                response = new Message(success ? TaskType.GET_BILL : TaskType.FAIL, resultMsg);
                              
                sendKryoToClient(response, client);
                break;
            case PAY_BILL:
            	System.out.println("Log: Paying Bill...");
                code = (String) message.getObject();
                
                success = paymentController.payBill(code);
                resultMsg = success ? "Thank you! Enjoy your evening" : "Your payment method failed";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                              
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
                tableId = (int) message.getObject();
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
                
             // Add this case inside the switch(message.getTask()) block
            case CHECK_USER_EXISTS:
                System.out.println("Log: Verifying user existence...");
                String inputId = (String) message.getObject();
                User foundUser = null;

                // 1. Try to find by Subscriber ID first
                // (Assuming userController has getSubscriber or you add it)
                foundUser = userController.getSubscriber(inputId); 

                // 2. If not found, try to find by Phone Number (for Casuals)
                if (foundUser == null) {
                    foundUser = userController.getUserByPhone(inputId);
                }

                // 3. Send result back
                if (foundUser != null) {
                    response = new Message(TaskType.USER_FOUND, foundUser);
                } else {
                    response = new Message(TaskType.USER_NOT_FOUND, null);
                }
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