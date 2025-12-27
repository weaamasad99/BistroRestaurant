package common;

import java.io.Serializable;
import java.sql.Date; 
import java.sql.Time;

/**
 * Represents a Reservation/Order in the system.
 * Matches the 'orders' table in the database.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private int orderNumber;        // Unique Order ID (PK)
    private int userId;             // Foreign Key to User
    private Date orderDate;         // Date of reservation
    private Time orderTime;         // Time of reservation
    private int numberOfDiners;     // Amount of guests
    private String status;          // PENDING, APPROVED, ACTIVE, FINISHED, CANCELLED
    private String confirmationCode;   // Code sent to the customer
    private Time actualArrivalTime; // When the customer actually arrived

    /**
     * Constructor for creating a new Reservation Request (Client -> Server).
     * Used in ReservationUI.
     * * @param userId The ID of the user making the booking.
     * @param orderDate The requested date.
     * @param orderTime The requested time.
     * @param numberOfDiners Number of guests.
     */
    public Order() {
    }
    public Order(int userId, Date orderDate, Time orderTime, int numberOfDiners) {
        this.userId = userId;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.numberOfDiners = numberOfDiners;
        this.status = "PENDING"; // Default status
    }

    /**
     * Full Constructor (Server -> Client).
     * Used when loading history or details from DB.
     */
    public Order(int orderNumber, int userId, Date orderDate, Time orderTime, int numberOfDiners, String status, String confirmationCode, Time actualArrivalTime) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.numberOfDiners = numberOfDiners;
        this.status = status;
        this.confirmationCode = confirmationCode;
        this.actualArrivalTime = actualArrivalTime;
    }

    // --- Getters and Setters ---

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getOrderDate() { return orderDate; }
    public Time getOrderTime() { return orderTime; }
    
    public int getNumberOfDiners() { return numberOfDiners; }
    public void setNumberOfDiners(int numberOfDiners) { this.numberOfDiners = numberOfDiners; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    public Time getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(Time actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }

    @Override
    public String toString() {
        return "Order #" + orderNumber + " | " + orderDate + " @ " + orderTime + " | Guests: " + numberOfDiners + " | Status: " + status;
    }
}