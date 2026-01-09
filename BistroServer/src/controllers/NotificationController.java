package controllers;

/**
 * Handles simulated notifications (Email/SMS).
 * In the prototype, these simply print to the server console.
 */
public class NotificationController {

    public NotificationController() {
        // No DB connection needed for simulation
    }

    public void sendReservationConfirmation(int userId, String date, String time) {
        // In real app: Fetch user email from DB using userId
        System.out.println(">>> [EMAIL SENT] To User " + userId + ": Reservation confirmed for " + date + " at " + time);
    }

    public void sendWaitingListAlert(int userId) {
        System.out.println(">>> [SMS SENT] To User " + userId + ": A table is now available! You have 1 hour to confirm.");
    }
    
    public void sendReminder(int userId) {
         System.out.println(">>> [SMS SENT] To User " + userId + ": Reminder - Your reservation is tomorrow.");
         
    }
    
    public void sendTwoHourReminder(String contactInfo, String time) {
        System.out.println(">>> [SIMULATION - SMS/EMAIL SENT] To: " + contactInfo);
        System.out.println(">>> Message: Reminder! Your reservation at Bistro is in 2 hours (" + time + "). We look forward to seeing you!");
    }
}