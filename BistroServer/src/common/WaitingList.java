package common;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Represents an entry in the restaurant's waiting list.
 * Stores details about customers waiting for a table.
 * @author Group 6
 * @version 1.0
 */
public class WaitingList implements Serializable {
    /** Unique ID for the waiting list entry. */
    private int waitingId;
    
    /** The ID of the user waiting. */
    private int userId;
    
    /** The date the user joined the list. */
    private Date dateRequested;
    
    /** The time the user joined the list. */
    private Time timeRequested;
    
    /** The number of diners in the party. */
    private int numOfDiners;
    
    /** Current status: 'WAITING', 'NOTIFIED', 'FULFILLED', 'CANCELLED'. */
    private String status; 
    
    /** Confirmation code for identifying the request. */
    private String code;

    /**
     * Default constructor.
     */
    public WaitingList() {}

    /**
     * Constructs a WaitingList entry.
     * @param waitingId Unique entry ID.
     * @param userId User ID.
     * @param dateRequested Request date.
     * @param timeRequested Request time.
     * @param numOfDiners Number of guests.
     * @param status Current status.
     * @param code Confirmation code.
     */
    public WaitingList(int waitingId, int userId, Date dateRequested, Time timeRequested, int numOfDiners, String status, String code) {
        this.waitingId = waitingId;
        this.userId = userId;
        this.dateRequested = dateRequested;
        this.timeRequested = timeRequested;
        this.numOfDiners = numOfDiners;
        this.status = status;
        this.code = code;
    }

    // --- Getters and Setters ---

    /** @return The waiting list ID. */
    public int getWaitingId() { return waitingId; }
    /** @param waitingId The ID to set. */
    public void setWaitingId(int waitingId) { this.waitingId = waitingId; }

    /** @return The user ID. */
    public int getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return The date requested. */
    public Date getDateRequested() { return dateRequested; }
    /** @param dateRequested The date to set. */
    public void setDateRequested(Date dateRequested) { this.dateRequested = dateRequested; }

    /** @return The time requested. */
    public Time getTimeRequested() { return timeRequested; }
    /** @param timeRequested The time to set. */
    public void setTimeRequested(Time timeRequested) { this.timeRequested = timeRequested; }

    /** @return The number of diners. */
    public int getNumOfDiners() { return numOfDiners; }
    /** @param numOfDiners The number to set. */
    public void setNumOfDiners(int numOfDiners) { this.numOfDiners = numOfDiners; }

    /** @return The status. */
    public String getStatus() { return status; }
    /** @param status The status to set. */
    public void setStatus(String status) { this.status = status; }
    
    /** @return The confirmation code. */
    public String getCode() { return code; }
    /** @param code The code to set. */
    public void setCode(String code) { this.code = code; }
}