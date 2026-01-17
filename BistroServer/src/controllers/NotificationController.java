package controllers;

import java.util.ArrayList;

import common.Order;
import common.User;
import server.EmailService;
import server.ServerEventListener;

/**
 * Manages outgoing notifications to users via simulated SMS and real Email.
 * Handles templates for confirmations, alerts, reminders, and cancellations.
 * @author Group 6
 * @version 1.0
 */
public class NotificationController {

    private UserController userController;
    private ServerEventListener serverLogger;

    /**
     * Constructor with logger.
     * @param serverLogger Listener for logging events to the server UI.
     */
    public NotificationController(ServerEventListener serverLogger) {
        this.userController = new UserController();
        this.serverLogger = serverLogger;
    }
    
    /**
     * Default constructor for cases where logging isn't strictly required.
     */
    public NotificationController() {
        this(null);
    }

    /**
     * Logs messages to console and server log if available.
     */
    private void log(String message) {
        System.out.println(message); // Console
        if (serverLogger != null) {
            serverLogger.onLog("[Notification] " + message); // UI
        }
    }
    // --- 1. RESERVATION CONFIRMATION ---
    
    /**
     * Sends a reservation confirmation message.
     * @param userId User ID.
     * @param date Reservation date.
     * @param time Reservation time.
     * @param code Confirmation code.
     * @param guests Number of guests.
     */
    public void sendReservationConfirmation(int userId, String date, String time, String code, int guests) {
        // 1. Mock SMS (Everyone gets this)
        System.out.println(">>> [SMS MOCK] To User " + userId + ": Reservation confirmed. Code: " + code);
        log("Sending Confirmation to User " + userId + ". Code: " + code);
        // 2. Real Email (Only if User has email & is not Casual)
        User user = userController.getUserById(userId); 
        if (shouldSendEmail(user)) {
            String subject = "Reservation Confirmation - Bistro";
            String body = "<h3>Your Reservation is Confirmed!</h3>" +
                          "<p><b>Date:</b> " + date + "</p>" +
                          "<p><b>Time:</b> " + time + "</p>" +
                          "<p><b>Guests:</b> " + guests + "</p>" +
                          "<p><b>Code:</b> " + code + "</p>";
            
            // Run in background thread to avoid freezing the server
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }

 // --- 2. WAITING LIST ALERT ---
    /**
     * Alerts a user that a table is available.
     * @param userId User ID.
     * @param confirmationCode The code required to claim the table.
     */
    public void sendWaitingListAlert(int userId, String confirmationCode) {
        
        // Console/SMS Mock Message
        String msg = "Good news! A table is available and reserved for you for 15 minutes. " +
                     "Please check in using code: " + confirmationCode;
                     
        System.out.println(">>> [SMS MOCK] To User " + userId + ": " + msg);

        User user = userController.getUserById(userId);
        if (shouldSendEmail(user)) {
            String subject = "Table Available!";
            
            // Updated Email Body:
            // 1. Shows the specific Code
            // 2. Corrects '1 hour' to '15 minutes'
            String body = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd;'>" +
                          "<h2 style='color: #2E8B57;'>Good News!</h2>" +
                          "<p>A table matching your request is now available.</p>" +
                          "<p>We have reserved this table for you for <b>15 minutes</b>.</p>" +
                          "<div style='background-color: #f9f9f9; padding: 15px; margin: 20px 0; border-left: 5px solid #2E8B57;'>" +
                          "<h3>Your Check-In Code: <span style='color: #d9534f;'>" + confirmationCode + "</span></h3>" +
                          "</div>" +
                          "<p>Please proceed to the terminal and enter this code to claim your table immediately.</p>" +
                          "</div>";
            
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }
    

    // --- 3. REGISTRATION WELCOME ---
    /**
     * Sends a welcome email to a new subscriber.
     * @param user The new user object.
     */
    public void sendRegistrationWelcome(User user) {
    	
        if (shouldSendEmail(user)) {
        	log("Sending Welcome Email to new Subscriber: " + user.getUsername());
            String subject = "Welcome to Bistro!";
            String body = "<h2>Welcome " + user.getFirstName() + "!</h2>" +
                          "<p>You are now a registered Subscriber.</p>" +
                          "<p><b>Subscriber ID:</b> " + user.getSubscriberNumber() + "</p>";
            
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }
    
    // --- 4. LOST CODE ---
    /**
     * Sends recovered reservation codes to a user.
     * @param contact The email address.
     * @param orders List of active orders found.
     */
    public void sendLostCodes(String contact, ArrayList<Order> orders) {
        log("Sending list of " + orders.size() + " recovered codes to: " + contact);

        if (contact != null && contact.contains("@")) {
            String subject = "Your Reservation Details - Bistro";
            
            StringBuilder body = new StringBuilder();
            body.append("<h3>Your Active Reservations</h3>");
            body.append("<p>You requested to recover your reservation codes. Here they are:</p>");
            body.append("<hr>");
            
            for (Order order : orders) {
                body.append("<p><b>Date:</b> ").append(order.getOrderDate()).append("<br>");
                body.append("<b>Time:</b> ").append(order.getOrderTime()).append("<br>");
                body.append("<b>Guests:</b> ").append(order.getNumberOfDiners()).append("<br>");
                body.append("<b>Confirmation Code:</b> <span style='color:blue; font-size:16px;'>")
                    .append(order.getConfirmationCode()).append("</span></p>");
                body.append("<hr>");
            }
            body.append("<p>We look forward to seeing you!</p>");

            new Thread(() -> EmailService.sendEmail(contact, subject, body.toString())).start();
        } else {
            log("Warning: No valid email found for recovery. SMS simulation only.");
        }
    }
    /**
     * Sends a reminder 2 hours before the reservation.
     * Logic:
     * 1. Always prints console log (Simulating SMS).
     * 2. If contactInfo is an email, sends a real email.
     * @param contactInfo Phone or Email.
     * @param time Reservation time string.
     */
    public void sendTwoHourReminder(String contactInfo, String time) {
    	log("Sending 2-Hour Reminder to: " + contactInfo);
        // 1. Simulation / SMS Log
        System.out.println(">>> [SIMULATION - SMS SENT] To: " + contactInfo);
        System.out.println(">>> Message: Reminder! Your reservation is in 2 hours (" + time + ").");

        // 2. Real Email Logic
        if (contactInfo != null && contactInfo.contains("@")) {
            String subject = "Reservation Reminder - 2 Hours Left";
            String body = "<h3>Upcoming Reservation Reminder</h3>" +
                          "<p>Hello,</p>" +
                          "<p>We are looking forward to seeing you!</p>" +
                          "<p><b>This is a reminder that your table is reserved for today at " + time + ".</b></p>" +
                          "<p>Please arrive on time to ensure your seating.</p>" +
                          "<br><p>Best regards,<br>Bistro Team</p>";

            // Send in a background thread to prevent blocking the Server Scheduler
            new Thread(() -> {
                EmailService.sendEmail(contactInfo, subject, body);
            }).start();
        }
    }
    

 // --- 5. CANCELLATION NOTICE ---
    /**
     * Notifies a user that their reservation was cancelled.
     * @param userId User ID.
     * @param code The reservation code.
     */
    public void sendCancellationNotification(int userId, String code) {
        log("Sending Cancellation Notification to User " + userId);
        
        // 1. Mock SMS
        System.out.println(">>> [SMS MOCK] To User " + userId + ": Reservation " + code + " cancelled.");

        // 2. Real Email
        User user = userController.getUserById(userId);
        if (shouldSendEmail(user)) {
            String subject = "Reservation Cancelled - Bistro";
            String body = "<h3>Reservation Cancellation</h3>" +
                          "<p>Your reservation with confirmation code <b>" + code + "</b> has been successfully cancelled.</p>" +
                          "<p>We hope to see you again soon!</p>";
            
            // Run in background thread
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }
    
    
    // --- 6. BILL / TIME LIMIT ALERT ---
    /**
     * Notifies a user when their dining time limit is reached.
     * @param userId User ID.
     * @param code Order code.
     */
    public void sendBillNotification(int userId, String code) {
        log("Sending 2-Hour Bill Notification to User " + userId);
        
        // 1. Mock SMS
        System.out.println(">>> [SMS MOCK] To User " + userId + ": Your 2 hours are up. Please checkout. Code: " + code);

        // 2. Real Email
        User user = userController.getUserById(userId);
        if (shouldSendEmail(user)) {
            String subject = "Your Bill is Ready - Bistro";
            String body = "<h3>Time to Checkout</h3>" +
                          "<p>We hope you enjoyed your meal!</p>" +
                          "<p>It has been 2 hours since you were seated.</p>" +
                          "<p><b>Please proceed to checkout using your code: <span style='color:blue;'>" + code + "</span></b></p>" +
                          "<p>You can pay at the terminal or via the app.</p>";
            
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }
    
    
    // Helper: Check if we should send email
    private boolean shouldSendEmail(User user) {
        if (user == null || user.getEmail() == null || !user.getEmail().contains("@"))
        	return false;
        return true;
    }
    /**
     * Notifies users of changes to the restaurant schedule that affect their booking.
     * @param userId User ID.
     * @param date Date of change.
     * @param openTime New opening time.
     * @param closeTime New closing time.
     * @param isClosed True if closed completely.
     */
    public void sendScheduleUpdateNotification(int userId, String date, String openTime, String closeTime, boolean isClosed) {
        log("Sending Schedule Update Notification to User " + userId);

        // 1. Mock SMS
        String msg = isClosed ? "Restaurant closed on " + date : "Restaurant hours changed on " + date;
        System.out.println(">>> [SMS MOCK] To User " + userId + ": " + msg);

        // 2. Real Email
        User user = userController.getUserById(userId);
        if (shouldSendEmail(user)) {
            String subject = "Important: Restaurant Schedule Change";
            String body = "<h3>Important Update Regarding Your Reservation</h3>" +
                          "<p>Dear " + user.getFirstName() + ",</p>" +
                          "<p>We are writing to inform you of a change in our operating hours for <b>" + date + "</b>.</p>";
            
            if (isClosed) {
                body += "<p style='color:red;'><b>Please note that the restaurant will be CLOSED on this date.</b></p>" +
                        "<p>Unfortunately, we must cancel your reservation. We apologize for the inconvenience.</p>";
            } else {
                body += "<p>Our new hours are: <b>" + openTime + " - " + closeTime + "</b>.</p>" +
                        "<p>Please check if your reservation time is still within our opening hours.</p>";
            }
            
            body += "<br><p>Best regards,<br>Bistro Team</p>";
            String finalBody = body;

            // Run in background thread
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, finalBody)).start();        }
    }

    
    // --- 7. ADMIN CANCELLATION ALERT ---
    /**
     * Sends an urgent cancellation notification to a user when their reservation is forced 
     * to cancel by the system (e.g., due to table removal or capacity reduction).
     * <p>
     * This alert utilizes both a simulated SMS log and a real HTML email to ensure the 
     * user is informed of the unexpected change.
     *
     * @param userId The ID of the user to notify.
     * @param date   The date of the cancelled reservation.
     * @param time   The time of the cancelled reservation.
     */
    public void sendSystemCancellation(int userId, String date, String time) {
        log("Sending System Cancellation to User " + userId);

        // 1. Mock SMS
        System.out.println(">>> [SMS MOCK] To User " + userId + ": Urgent. Your reservation on " + date + " was cancelled due to restaurant changes.");

        // 2. Real Email
        User user = userController.getUserById(userId);
        if (shouldSendEmail(user)) {
            String subject = "Urgent: Reservation Cancellation";
            String body = "<h3>Reservation Cancelled</h3>" +
                          "<p>Dear " + user.getFirstName() + ",</p>" +
                          "<p>We regret to inform you that due to unexpected changes in our seating arrangements, we can no longer accommodate your reservation on:</p>" +
                          "<p><b>Date:</b> " + date + "<br><b>Time:</b> " + time + "</p>" +
                          "<p>Your order has been cancelled from our system.</p>" +
                          "<p>We sincerely apologize for the inconvenience.</p>" +
                          "<br><p>Bistro Management</p>";
            
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }

}