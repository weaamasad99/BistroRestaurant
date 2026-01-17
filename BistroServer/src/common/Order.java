package common;

import java.io.Serializable;
import java.sql.Date; 
import java.sql.Time;

/**
 * Represents a Reservation/Order in the system.
 * Matches the 'orders' table in the database.
 * @author Group 6
 * @version 1.0
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique Order ID (Primary Key). */
    private int orderNumber;        
    
    /** Foreign Key to User table. */
    private int userId;             
    
    /** Date of the reservation. */
    private Date orderDate;         
    
    /** Time of the reservation. */
    private Time orderTime;         
    
    /** Number of guests for the booking. */
    private int numberOfDiners;     
    
    /** Status of the order: PENDING, APPROVED, ACTIVE, FINISHED, CANCELLED. */
    private String status;          
    
    /** Unique confirmation code sent to the customer. */
    private String confirmationCode;   
    
    /** Timestamp when the customer actually arrived. */
    private Time actualArrivalTime; 
    
    /** Timestamp when the customer leaves (pays the bill). */
    private Time leavingTime; 

    /**
     * Empty constructor required for serialization.
     */
    public Order() {
    }

    /**
     * Constructor for creating a new Reservation Request (Client -> Server).
     * Used in ReservationUI.
     * @param userId The ID of the user making the booking.
     * @param orderDate The requested date.
     * @param orderTime The requested time.
     * @param numberOfDiners Number of guests.
     */
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
     * @param orderNumber Unique Order ID.
     * @param userId User ID.
     * @param orderDate Reservation Date.
     * @param orderTime Reservation Time.
     * @param numberOfDiners Guest count.
     * @param status Current status.
     * @param confirmationCode Code for check-in.
     * @param actualArrivalTime Time of arrival.
     * @param leavingTime Time of departure.
     */
    public Order(int orderNumber, int userId, Date orderDate, Time orderTime, int numberOfDiners, String status, String confirmationCode, Time actualArrivalTime, Time leavingTime) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.numberOfDiners = numberOfDiners;
        this.status = status;
        this.confirmationCode = confirmationCode;
        this.actualArrivalTime = actualArrivalTime;
        this.leavingTime = leavingTime;
    }

    // --- Getters and Setters ---

    /** @return The unique order number. */
    public int getOrderNumber() { return orderNumber; }
    /** @param orderNumber The order number to set. */
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    /** @return The user ID associated with the order. */
    public int getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return The date of the reservation. */
    public Date getOrderDate() { return orderDate; }
    /** @return The time of the reservation. */
    public Time getOrderTime() { return orderTime; }
    
    /** @return The number of diners. */
    public int getNumberOfDiners() { return numberOfDiners; }
    /** @param numberOfDiners The number of diners to set. */
    public void setNumberOfDiners(int numberOfDiners) { this.numberOfDiners = numberOfDiners; }

    /** @return The current status of the order. */
    public String getStatus() { return status; }
    /** @param status The status to set. */
    public void setStatus(String status) { this.status = status; }

    /** @return The confirmation code. */
    public String getConfirmationCode() { return confirmationCode; }
    /** @param confirmationCode The code to set. */
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    /** @return The actual arrival time. */
    public Time getActualArrivalTime() { return actualArrivalTime; }
    /** @param actualArrivalTime The arrival time to set. */
    public void setActualArrivalTime(Time actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }
    
    /** @return The time the customer left. */
    public Time getLeavingTime() { return leavingTime; }
    /** @param leavingTime The leaving time to set. */
    public void setLeavingTime(Time leavingTime) { this.leavingTime = leavingTime; }

    @Override
    public String toString() {
        return "Order #" + orderNumber + " | " + orderDate + " @ " + orderTime + " | Guests: " + numberOfDiners + " | Status: " + status;
    }
}