package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.BistroSchedule;
import common.KryoUtil;
import common.Message;
import common.MonthlyReportData;
import common.Order;
import common.Table;
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

    // Interface to communicate with ServerUI
    private ServerEventListener uiListener;

    // Controllers
    private UserController userController;
    private ReservationController reservationController;
    private SubscriberController subscriberController;
    private WaitingListController waitingListController;
    private PaymentController paymentController;
    private ReportController reportController;

    /**
     * Constructor.
     * @param port The port to listen on.
     * @param uiListener The listener to update the ServerUI.
     */
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
        // Step 1: Check if message is Kryo byte array
        if (msg instanceof byte[]) {
            // Step 2: Deserialize
            Object deserializedMsg = KryoUtil.deserialize((byte[]) msg);
            
            if (deserializedMsg instanceof Message) {
                try {
                    processMessage((Message) deserializedMsg, client);
                } catch (Exception e) {
                    log("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            log("Received unknown format from client.");
        }
    }

    /**
     * Main Business Logic Switch
     */
    private void processMessage(Message message, ConnectionToClient client) {
        Message response = null;
        String resultMsg, code, result;
        boolean success;
        int tableId;

        switch (message.getTask()) {

            // ===============================================================
            // USER & LOGIN (Updates UI List)
            // ===============================================================
            case LOGIN_REQUEST:
                log("Processing Login Request from " + client.getInetAddress().getHostAddress());
                User loginReq = (User) message.getObject();
                User loggedInUser = userController.loginUser(loginReq.getUsername(), loginReq.getPassword());
                
                if (loggedInUser != null) {
                    // NOTIFY UI: Update the client list with real name and role
                    if (uiListener != null) {
                        uiListener.onUserLoggedIn(client, loggedInUser.getUsername(), loggedInUser.getUserType());
                    }
                }

                response = new Message(TaskType.LOGIN_RESPONSE, loggedInUser);
                sendKryoToClient(response, client);
                break;

            case SET_USER:
                log("Retrieving user by phone...");
                String phone = (String) message.getObject();
                User user = userController.getUserByPhone(phone);
                response = new Message(TaskType.SET_USER, user);
                sendKryoToClient(response, client);
                break;

            case CREATE_CASUAL:
                log("Registering/Identifying Casual User...");
                String phoneNumber = (String) message.getObject();
                success = userController.createCasualRecord(phoneNumber);
                
                if (success) {
                    // NOTIFY UI: Add casual user to the list
                    if (uiListener != null) {
                        uiListener.onUserLoggedIn(client, phoneNumber, "Casual Diner");
                    }
                }
                
                resultMsg = success ? "Welcome " + phoneNumber : "Error registering casual user";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                sendKryoToClient(response, client);
                break;

            case REGISTER_USER:
                log("Processing Subscriber Registration...");
                User subToRegister = (User) message.getObject();
                User registeredSub = userController.registerNewSubscriber(subToRegister);

                if (registeredSub != null) {
                    response = new Message(TaskType.REGISTRATION_SUCCESS, registeredSub);
                } else {
                    response = new Message(TaskType.FAIL, "Registration failed. User may already exist.");
                }
                sendKryoToClient(response, client);
                break;
                
            case CHECK_USER_EXISTS:
                log("Verifying user existence...");
                String inputId = (String) message.getObject();
                User foundUser = userController.getSubscriber(inputId); 
                if (foundUser == null) {
                    foundUser = userController.getUserByPhone(inputId);
                }
                if (foundUser != null) {
                    response = new Message(TaskType.USER_FOUND, foundUser);
                } else {
                    response = new Message(TaskType.USER_NOT_FOUND, null);
                }
                sendKryoToClient(response, client);
                break;
                
            case UPDATE_SUBSCRIBER:
                log("Updating subscriber details...");
                User userToUpdate = (User) message.getObject();
                success = subscriberController.updateSubscriberDetails(userToUpdate);
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, 
                        success ? "Profile updated!" : "Update failed.");
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // ORDERS & RESERVATIONS
            // ===============================================================
            case REQUEST_RESERVATION:
                log("Creating New Reservation...");
                Order order = (Order) message.getObject();
                result = reservationController.createReservation(order);
                response = new Message(TaskType.REQUEST_RESERVATION, result);
                sendKryoToClient(response, client);
                break;

            case GET_ORDERS:
                // log("Fetching all orders..."); 
                ArrayList<Order> orders = reservationController.getAllOrders();
                response = new Message(TaskType.GET_ORDERS, orders);
                sendKryoToClient(response, client);
                break;

            case UPDATE_ORDER:
                log("Updating Order...");
                Order orderUpdate = (Order) message.getObject();
                success = reservationController.updateOrder(orderUpdate);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            case CANCEL_ORDER:
                log("Cancelling Order...");
                Object[] cancelData = (Object[]) message.getObject();
                String cCode = (String) cancelData[0];
                int cUserId = (int) cancelData[1];
                
                success = reservationController.cancelOrder(cCode, cUserId);
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, 
                                       success ? "Order Canceled" : "Failed to Cancel");
                sendKryoToClient(response, client);
                break;
                
            case GET_USER_HISTORY:
                log("Fetching history for user...");
                int historyUserId = (int) message.getObject();
                ArrayList<Order> history = reservationController.getOrdersByUserId(historyUserId);
                response = new Message(TaskType.HISTORY_IMPORTED, history);
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // CHECK-IN & WAITING LIST
            // ===============================================================
            case CHECK_IN_CUSTOMER:
                code = (String) message.getObject();
                log("Check-In attempt: " + code);
                
                tableId = reservationController.checkIn(code);

                if (tableId > 0) {
                    log("Check-In Approved. Table " + tableId);
                    response = new Message(TaskType.SUCCESS, "Check-in Successful! Table " + tableId); 
                } 
                else if (tableId == -2) {
                    response = new Message(TaskType.FAIL, "Invalid confirmation code.");
                } 
                else if (tableId == -3) {
                    response = new Message(TaskType.FAIL, "Your reservation is for a different day.");
                } 
                else if (tableId == -4) {
                    response = new Message(TaskType.FAIL, "Your order was cancelled.");
                } 
                else if (tableId == -5) {
                    response = new Message(TaskType.FAIL, "No tables available, please wait.");
                } 
                else {
                    response = new Message(TaskType.FAIL, "Database error.");
                }
                sendKryoToClient(response, client);
                break;

            case ENTER_WAITING_LIST:
                log("Adding to Waiting List...");
                WaitingList wlData = (WaitingList) message.getObject();
                success = waitingListController.addToWaitingList(wlData);
                resultMsg = success ? "Added to Waiting List!\nCode: " + wlData.getCode() : "Failed to add";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                sendKryoToClient(response, client);
                break;

            case GET_WAITING_LIST:
                ArrayList<WaitingList> waitList = waitingListController.getAllWaitingList();
                response = new Message(TaskType.GET_WAITING_LIST, waitList);
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // PAYMENT
            // ===============================================================
            case GET_BILL:
                log("Retrieving Bill...");
                code = (String) message.getObject();
                success = paymentController.getBill(code);
                response = new Message(success ? TaskType.GET_BILL : TaskType.FAIL, 
                                       success ? code : "Invalid Code");
                sendKryoToClient(response, client);
                break;

            case PAY_BILL:
                log("Processing Payment...");
                code = (String) message.getObject();
                success = paymentController.payBill(code);
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, 
                                       success ? "Payment Successful" : "Payment Failed");
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // TABLE MANAGEMENT
            // ===============================================================
            case GET_TABLES:
                ArrayList<Table> tables = reservationController.getAllTables();
                response = new Message(TaskType.GET_TABLES, tables);
                sendKryoToClient(response, client);
                break;

            case UPDATE_TABLE:
                log("Updating Table...");
                Table tableToUpdate = (Table) message.getObject();
                success = reservationController.updateTable(tableToUpdate);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            case ADD_TABLE:
                log("Adding Table...");
                Table newTable = (Table) message.getObject();
                success = reservationController.addTable(newTable);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            case REMOVE_TABLE:
                log("Removing Table...");
                tableId = (int) message.getObject();
                success = reservationController.removeTable(tableId);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // SCHEDULE / OPENING HOURS
            // ===============================================================
            case GET_SCHEDULE:
                ArrayList<BistroSchedule> schedule = reservationController.getSchedule();
                response = new Message(TaskType.GET_SCHEDULE, schedule);
                sendKryoToClient(response, client);
                break;

            case SAVE_SCHEDULE_ITEM:
                log("Saving Schedule...");
                ArrayList<BistroSchedule> list = (ArrayList<BistroSchedule>) message.getObject();
                boolean allSaved = true;
                for (BistroSchedule item : list) {
                    if (!reservationController.saveScheduleItem(item)) allSaved = false;
                }
                response = new Message(allSaved ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            case DELETE_SCHEDULE_ITEM:
                log("Deleting Schedule Item...");
                String id = (String) message.getObject();
                success = reservationController.deleteScheduleItem(id);
                response = new Message(success ? TaskType.UPDATE_SUCCESS : TaskType.UPDATE_FAILED, null);
                sendKryoToClient(response, client);
                break;

            // ===============================================================
            // REPORTS
            // ===============================================================
            case GET_MONTHLY_REPORT:
                log("Generating Monthly Report...");
                try {
                    String payload = (String) message.getObject();
                    String[] parts = payload.split("-");
                    MonthlyReportData reportData = reportController.generateMonthlyReport(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    response = new Message(TaskType.REPORT_GENERATED, reportData);
                } catch (Exception e) {
                    log("Error generating report: " + e.getMessage());
                    response = new Message(TaskType.ERROR, "Report Generation Failed");
                }
                sendKryoToClient(response, client);
                break;
                
            case GET_ALL_SUBSCRIBERS:
                ArrayList<User> subs = subscriberController.getAllSubscribers();
                response = new Message(TaskType.GET_ALL_SUBSCRIBERS, subs);
                sendKryoToClient(response, client);
                break;

            // DEFAULT
            default:
                log("Received Unknown Task: " + message.getTask());
                break;
        }
    }

    private void log(String msg) {
        if (uiListener != null) uiListener.onLog(msg);
        else System.out.println(msg);
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