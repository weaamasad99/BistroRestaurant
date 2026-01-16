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
import controllers.NotificationController;
import controllers.PaymentController;
import controllers.ReportController;
import controllers.ReservationController;
import controllers.SubscriberController;
import controllers.WaitingListController;
import JDBC.DatabaseConnection;

import java.io.IOException;
import java.util.ArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BistroServer extends AbstractServer {

    // Interface to communicate with ServerUI
    private ServerEventListener uiListener;
    
    private ScheduledExecutorService scheduler;

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
                Object[] obj = (Object[]) message.getObject();
                String phoneNumber = (String) obj[0];
                String email = (String) obj[1];

                success = userController.createCasualRecord(phoneNumber,email);
                
                if (success) {
                    // NOTIFY UI: Add casual user to the list
                    if (uiListener != null) {
                        uiListener.onUserLoggedIn(client, phoneNumber, "Casual Diner");
                    }
                }
                
                resultMsg = success ? "Welcome " + phoneNumber : "Error registering casual user";
                response = new Message(success ? TaskType.SUCCESS : TaskType.FAIL, resultMsg);
                if (!success)
                	sendKryoToClient(response, client);
                break;

            case REGISTER_USER:
                log("Processing Subscriber Registration...");
                User subToRegister = (User) message.getObject();
                User registeredSub = userController.registerNewSubscriber(subToRegister);

                if (registeredSub != null) {
                    new controllers.NotificationController().sendRegistrationWelcome(registeredSub);
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
	            if (result.startsWith("OK")) {
	                new controllers.NotificationController(this.uiListener).sendReservationConfirmation(
	                        order.getUserId(), 
	                        order.getOrderDate().toString(), 
	                        order.getOrderTime().toString(), 
	                        order.getConfirmationCode(), 
	                        order.getNumberOfDiners()
	                    );
                }
                sendKryoToClient(response, client);
                break;

                
            case RESEND_CODE:

                String inputIdentifier = (String) message.getObject();
                log("Processing Lost Code Request for: " + inputIdentifier);
                String contactInput = (String) message.getObject();
                log("Client requested lost code recovery for: " + contactInput);

                // 1. Fetch List of Orders (Active/Pending/Approved) using Subscriber ID or Phone
                ArrayList<Order> ordersList = reservationController.getActiveOrdersForContact(inputIdentifier);
                // 1. Find ALL active/approved orders for this contact
                ArrayList<Order> ActiveOrders = reservationController.getActiveOrdersForContact(contactInput);
                

                if (!ordersList.isEmpty()) {
                    // 2. Resolve Email for Notification (Using the new robust method)
                    String emailTarget = userController.getEmailByIdentifier(inputIdentifier);
                    
                    // Fallback to input if email not found (e.g., if it's a phone number, NotificationController handles SMS simulation)
                    if (emailTarget == null) emailTarget = inputIdentifier;
                if (!ActiveOrders.isEmpty()) {
                    log("Found " + ActiveOrders.size() + " active orders. Resolving email...");



                    log("Found " + ordersList.size() + " active/pending orders. Sending details to: " + emailTarget);

                    // 2. Resolve the REAL email address
                    String realEmail = userController.getEmailByContact(contactInput);
                    String targetContact = (realEmail != null) ? realEmail : contactInput;
                    
                    // 3. Send Notification (Pass 'this.uiListener' for UI logging)
                    new controllers.NotificationController(this.uiListener)
                        .sendLostCodes(targetContact, ActiveOrders);

                    

                    // 3. Send List Notification
                    // Ensure you pass 'this.uiListener' if your NotificationController supports logging to UI
                    new controllers.NotificationController(this.uiListener).sendLostCodes(emailTarget, ordersList);
                    

                    response = new Message(TaskType.SUCCESS, "Reservation details sent to your registered contact.");
                } else {
                    log("No active bookings found for identifier: " + inputIdentifier);
                    response = new Message(TaskType.FAIL, "No active or pending bookings found.");

                    log("No active bookings found for: " + contactInput);
                    response = new Message(TaskType.FAIL, "No active bookings found for this contact.");

                }
                sendKryoToClient(response, client);
                break;}
            case GET_ORDERS:
                // log("Fetching all orders..."); 
                ArrayList<Order> orders = reservationController.getAllOrders();
                response = new Message(TaskType.GET_ORDERS, orders);
                sendKryoToClient(response, client);
                break;
                
            case GET_ACTIVE_ORDERS:
                // log("Fetching all orders..."); 
                ArrayList<Order> activeOrders = reservationController.getActiveOrders();
                response = new Message(TaskType.GET_ORDERS, activeOrders);
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
                
                if (success) {
                    // Passing 'this.uiListener' ensures the server log updates in the UI
                    new controllers.NotificationController(this.uiListener)
                        .sendCancellationNotification(cUserId, cCode);
                }
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
                    response = new Message(TaskType.FAIL, "Your order is not approved.");
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
                
            case EXIT_WAITING_LIST:
                log("Exiting Waiting List...");
                int userId = (int) message.getObject();
                success = waitingListController.exitWaitingList(userId);
                resultMsg = success ? "You have successfully left the waiting list" : "You are not currently in the waiting list";                
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
                
                Object[] billData = paymentController.getBillData(code);
                
                success = (billData != null);
                
                response = new Message(success ? TaskType.GET_BILL : TaskType.FAIL, 
                                       success ? billData : "Invalid Code or Order not Active");
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

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            // log("Running background maintenance..."); // Optional: Comment out to reduce noise
            
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn == null) return;

            NotificationController nc = new NotificationController(this.uiListener);

            // =================================================================================
            // 1. IDENTIFY LATE ORDERS (Select them BEFORE updating)
            // =================================================================================
            String findLateOrders = "SELECT order_number, user_id, confirmation_code " +
                                    "FROM orders " +
                                    "WHERE status = 'APPROVED' " +
                                    "AND (" +
                                    "  order_date < CURDATE() " +
                                    "  OR " +
                                    "  (order_date = CURDATE() AND order_time < SUBTIME(CURTIME(), '00:15:00'))" +
                                    ")";

            ArrayList<Integer> ordersToCancel = new ArrayList<>();

            try (PreparedStatement psFind = conn.prepareStatement(findLateOrders)) {
                ResultSet rs = psFind.executeQuery();
                while (rs.next()) {
                    int oId = rs.getInt("order_number");
                    int uId = rs.getInt("user_id");
                    String code = rs.getString("confirmation_code");
                    
                    ordersToCancel.add(oId);

                    // SEND NOTIFICATION (The missing piece)
                    log("Auto-cancelling late order: " + code);
                    nc.sendCancellationNotification(uId, code); 
                }
            } catch (SQLException e) {
                log("Error finding late orders: " + e.getMessage());
            }

            // =================================================================================
            // 2. CANCEL LATE ORDERS 
            // =================================================================================
            if (!ordersToCancel.isEmpty()) {
                // Build a dynamic query like: UPDATE ... WHERE order_number IN (101, 102, 105)
                StringBuilder sb = new StringBuilder("UPDATE orders SET status = 'CANCELLED' WHERE order_number IN (");
                for (int i = 0; i < ordersToCancel.size(); i++) {
                    sb.append(ordersToCancel.get(i));
                    if (i < ordersToCancel.size() - 1) sb.append(",");
                }
                sb.append(")");

                try (PreparedStatement psUpdate = conn.prepareStatement(sb.toString())) {
                    psUpdate.executeUpdate();
                    log("Cleanup: Cancelled " + ordersToCancel.size() + " late orders.");
                } catch (SQLException e) {
                    log("Error updating late orders: " + e.getMessage());
                }
            }

            // =================================================================================
            // 3. CLEANUP WAITING LIST
            // =================================================================================
            String cancelWaiting = "UPDATE waiting_list SET status = 'CANCELLED' " +
                                   "WHERE status = 'NOTIFIED' " +
                                   "AND (" +
                                   "  date_requested < CURDATE() " +
                                   "  OR " +
                                   "  (date_requested = CURDATE() AND time_requested < SUBTIME(NOW(), '00:15:00'))" +
                                   ")";
            try (PreparedStatement psWait = conn.prepareStatement(cancelWaiting)) {
                psWait.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }

            // =================================================================================
            // 4. SEND 2-HOUR REMINDERS
            // =================================================================================
            String reminderQuery = "SELECT u.email, u.phone_number, o.order_time " +
                                   "FROM orders o " +
                                   "JOIN users u ON o.user_id = u.user_id " +
                                   "WHERE o.status = 'APPROVED' " +
                                   "AND o.order_date = CURDATE() " +
                                   "AND o.order_time BETWEEN ADDTIME(CURTIME(), '01:59:00') AND ADDTIME(CURTIME(), '02:01:00')";

            try (PreparedStatement psRemind = conn.prepareStatement(reminderQuery)) {
                ResultSet rs = psRemind.executeQuery();
                while (rs.next()) {
                    String contact = rs.getString("email");
                    // Fallback for contact...
                    if (contact == null || !contact.contains("@")) contact = rs.getString("phone_number");
                    nc.sendTwoHourReminder(contact, rs.getString("order_time"));
                }
            } catch (SQLException e) {
                log("Reminder Error: " + e.getMessage());
            }

        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void serverStopped() {
    	log("Server stopped.");
        if (scheduler != null) {
            scheduler.shutdownNow();
            log("Background scheduler stopped.");
        }
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