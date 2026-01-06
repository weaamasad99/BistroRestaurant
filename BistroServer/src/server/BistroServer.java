package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.KryoUtil;
import common.Message;
import common.MonthlyReportData;
import common.Order;
import common.TaskType;
import common.User;
import common.WaitingList;

// Import Controllers
import controllers.UserController;
import controllers.PaymentController;
import controllers.ReportController;
import controllers.ReservationController;
import controllers.SubscriberController;
import controllers.WaitingListController;
import JDBC.DatabaseConnection;

import java.io.IOException;
import java.util.ArrayList;

public class BistroServer extends AbstractServer {

    // Replacement for the simple BiConsumer
    private ServerEventListener uiListener;

    // Controllers
    private UserController userController;
    private ReservationController reservationController;
    private SubscriberController subscriberController;
    private WaitingListController waitingListController;
    private PaymentController paymentController;
    private ReportController reportController;

    // Constructor accepts the Listener (The UI)
    public BistroServer(int port, ServerEventListener uiListener) {
        super(port);
        this.uiListener = uiListener;
        
        // Initialize Controllers
        this.userController = new UserController();
        this.reservationController = new ReservationController();
        this.subscriberController = new SubscriberController();
        this.waitingListController = new WaitingListController();
        this.paymentController = new PaymentController();
        this.reportController = new ReportController();
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof byte[]) {
            Object deserializedMsg = KryoUtil.deserialize((byte[]) msg);
            if (deserializedMsg instanceof Message) {
                try {
                    processMessage((Message) deserializedMsg, client);
                } catch (Exception e) {
                    log("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void processMessage(Message message, ConnectionToClient client) {
        Message response = null;

        switch (message.getTask()) {

            // --- LOGIN HANDLING (To update UI Table) ---
            case LOGIN_REQUEST:
                log("Processing Login Request from " + client.getInetAddress().getHostAddress());
                User loginReq = (User) message.getObject();
                User loggedInUser = userController.loginUser(loginReq.getUsername(), loginReq.getPassword());
                
                if (loggedInUser != null) {
                    // NOTIFY UI: Update the table row with the real username and role
                    uiListener.onUserLoggedIn(client.getId(), loggedInUser.getUsername(), loggedInUser.getUserType());
                }

                response = new Message(TaskType.LOGIN_RESPONSE, loggedInUser);
                sendKryoToClient(response, client);
                break;

            // --- GENERAL LOGGING EXAMPLES ---
            case REQUEST_RESERVATION:
                log("New Reservation Request received.");
                Order order = (Order) message.getObject();
                String result = reservationController.createReservation(order);
                response = new Message(TaskType.REQUEST_RESERVATION, result);
                sendKryoToClient(response, client);
                break;

            case GET_ORDERS:
                // Only log if necessary to avoid spamming the console
                // log("Fetching all orders..."); 
                ArrayList<Order> orders = reservationController.getAllOrders();
                response = new Message(TaskType.GET_ORDERS, orders);
                sendKryoToClient(response, client);
                break;
                
            case CHECK_IN_CUSTOMER:
                String code = (String) message.getObject();
                log("Check-In attempt for code: " + code);
                int tableId = reservationController.checkIn(code);
                
                if (tableId > 0) {
                    log("Check-In Approved. Assigned Table " + tableId);
                    response = new Message(TaskType.SUCCESS, "Check-in Successful! Table " + tableId); 
                } else {
                    log("Check-In Denied.");
                    response = new Message(TaskType.FAIL, "Check-in Failed.");
                }
                sendKryoToClient(response, client);
                break;

            // ... (Keep all your other cases here, just replace System.out.println with log() method) ...

            default:
                // Fallback for other existing logic in your code
                log("Received Task: " + message.getTask());
                // You can paste the rest of your original switch case here
                break;
        }
    }

    // Helper to send logs to UI
    private void log(String msg) {
        if (uiListener != null) {
            uiListener.onLog(msg);
        } else {
            System.out.println(msg); // Fallback
        }
    }

    private void sendKryoToClient(Object msg, ConnectionToClient client) {
        try {
            client.sendToClient(KryoUtil.serialize(msg));
        } catch (IOException e) {
            log("Error sending to client: " + e.getMessage());
        }
    }

    @Override
    protected void serverStarted() {
        log("Server listening on port " + getPort());
        DatabaseConnection.getInstance(); 
    }

    @Override
    protected void serverStopped() {
        log("Server stopped.");
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        if (uiListener != null) uiListener.onClientConnected(client);
    }

    
    @Override
    synchronized protected void clientDisconnected(ConnectionToClient client) {
        if (uiListener != null) uiListener.onClientDisconnected(client);
    }

    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
        if (uiListener != null) uiListener.onClientDisconnected(client);
    }
}