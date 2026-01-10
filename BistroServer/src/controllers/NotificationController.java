package controllers;

import java.util.ArrayList;

import common.Order;
import common.User;
import server.EmailService;
import server.ServerEventListener;

public class NotificationController {

    private UserController userController;
    private ServerEventListener serverLogger;

 //  Constructor
    public NotificationController(ServerEventListener serverLogger) {
        this.userController = new UserController();
        this.serverLogger = serverLogger;
    }
    
    // Default constructor for cases where logging isn't strictly required
    public NotificationController() {
        this(null);
    }

    private void log(String message) {
        System.out.println(message); // Console
        if (serverLogger != null) {
            serverLogger.onLog("[Notification] " + message); // UI
        }
    }
    // --- 1. RESERVATION CONFIRMATION ---
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
    public void sendWaitingListAlert(int userId) {
    	log("Sending Table Availability Alert to User " + userId);
        System.out.println(">>> [SMS MOCK] To User " + userId + ": A table is available! Confirm within 1 hour.");

        User user = userController.getUserById(userId);
        if (shouldSendEmail(user)) {
            String subject = "Table Available!";
            String body = "<p>Good news! A table matching your request is available.</p>" +
                          "<p>Please confirm within <b>1 hour</b>.</p>";
            
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }

    // --- 3. REGISTRATION WELCOME ---
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
    // Helper: Check if we should send email
    private boolean shouldSendEmail(User user) {
        if (user == null || user.getEmail() == null || !user.getEmail().contains("@")) return false;
        // Casual users usually don't have an email or are marked explicitly
        return !"CASUAL".equalsIgnoreCase(user.getUserType());
    }


}