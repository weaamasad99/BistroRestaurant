package common;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public class WaitingList implements Serializable {
    private int waitingId;
    private int userId;
    private Date dateRequested;
    private Time timeRequested;
    private int numOfDiners;
    private String status; // 'WAITING','NOTIFIED','FULFILLED','CANCELLED'

    public WaitingList() {}

    public WaitingList(int waitingId, int userId, Date dateRequested, Time timeRequested, int numOfDiners, String status) {
        this.waitingId = waitingId;
        this.userId = userId;
        this.dateRequested = dateRequested;
        this.timeRequested = timeRequested;
        this.numOfDiners = numOfDiners;
        this.status = status;
    }

    // Getters and Setters
    public int getWaitingId() { return waitingId; }
    public void setWaitingId(int waitingId) { this.waitingId = waitingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getDateRequested() { return dateRequested; }
    public void setDateRequested(Date dateRequested) { this.dateRequested = dateRequested; }

    public Time getTimeRequested() { return timeRequested; }
    public void setTimeRequested(Time timeRequested) { this.timeRequested = timeRequested; }

    public int getNumOfDiners() { return numOfDiners; }
    public void setNumOfDiners(int numOfDiners) { this.numOfDiners = numOfDiners; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}