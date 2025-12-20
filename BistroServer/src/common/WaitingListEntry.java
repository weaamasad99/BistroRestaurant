package common;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public class WaitingListEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private int waitingId;
    private int userId;
    private Date dateRequested;
    private Time timeRequested;
    private int numberOfDiners;
    private String status;

    public WaitingListEntry(int userId, Date dateRequested, Time timeRequested, int numberOfDiners) {
        this.userId = userId;
        this.dateRequested = dateRequested;
        this.timeRequested = timeRequested;
        this.numberOfDiners = numberOfDiners;
        this.status = "WAITING";
    }

    // Getters...
    public int getUserId() { return userId; }
    public int getNumberOfDiners() { return numberOfDiners; }
    public Date getDateRequested() { return dateRequested; }
    public Time getTimeRequested() { return timeRequested; }
}