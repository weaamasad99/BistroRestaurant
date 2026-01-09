package controllers;

import common.User;
import server.EmailService;

public class NotificationController {

    private UserController userController;

    public NotificationController() {
        this.userController = new UserController();
    }

    // --- 1. RESERVATION CONFIRMATION ---
    public void sendReservationConfirmation(int userId, String date, String time, String code, int guests) {
        // 1. Mock SMS (Everyone gets this)
        System.out.println(">>> [SMS MOCK] To User " + userId + ": Reservation confirmed. Code: " + code);

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
            String subject = "Welcome to Bistro!";
            String body = "<h2>Welcome " + user.getFirstName() + "!</h2>" +
                          "<p>You are now a registered Subscriber.</p>" +
                          "<p><b>Subscriber ID:</b> " + user.getSubscriberNumber() + "</p>";
            
            new Thread(() -> EmailService.sendEmail(user.getEmail(), subject, body)).start();
        }
    }
    
    // --- 4. LOST CODE ---
    public void sendLostCode(String contact, String code) {
        System.out.println(">>> [SMS MOCK] Recovery code " + code + " sent to " + contact);
        
        // If the contact input looks like an email, send email
        if (contact.contains("@")) {
             String subject = "Your Lost Code";
             String body = "<p>Your confirmation code is: <b>" + code + "</b></p>";
             new Thread(() -> EmailService.sendEmail(contact, subject, body)).start();
        }
    }
    /**
     * Sends a reminder 2 hours before the reservation.
     * Logic:
     * 1. Always prints console log (Simulating SMS).
     * 2. If contactInfo is an email, sends a real email.
     */
    public void sendTwoHourReminder(String contactInfo, String time) {
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