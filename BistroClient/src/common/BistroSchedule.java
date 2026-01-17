package common;

import java.io.Serializable;

/**
 * Represents a schedule entry for the restaurant (e.g., opening hours, holidays).
 * Implements Serializable for network transmission and object persistence.
 * @author Group 6
 * @version 1.0
 */
public class BistroSchedule implements Serializable {
    /**
     * Recommended for serialization to ensure compatibility.
     */
    private static final long serialVersionUID = 1L; 

    /** Unique identifier for the schedule item (e.g., "Sunday", "2026-01-01"). */
    private String identifier; 
    
    /** The opening time of the restaurant (format: HH:mm). */
    private String openTime;
    
    /** The closing time of the restaurant (format: HH:mm). */
    private String closeTime;
    
    /** Indicates if the restaurant is closed on this day. */
    private boolean isClosed;
    
    /** The type of schedule (e.g., "WEEKDAY", "HOLIDAY"). */
    private String type;       
    
    /** The name of the event if applicable (e.g., "Christmas"). */
    private String eventName;  

    /**
     * Default constructor required by some serializers (like Kryo).
     */
    public BistroSchedule() {} 

    /**
     * Constructs a new BistroSchedule with the specified details.
     * @param identifier Unique ID for the day or date.
     * @param openTime Opening time.
     * @param closeTime Closing time.
     * @param isClosed True if closed, false otherwise.
     * @param type The type of schedule entry.
     * @param eventName Name of the specific event.
     */
    public BistroSchedule(String identifier, String openTime, String closeTime, boolean isClosed, String type, String eventName) {
        this.identifier = identifier;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.type = type;
        this.eventName = eventName;
    }

    /**
     * Gets the identifier.
     * @return The schedule identifier.
     */
    public String getIdentifier() { return identifier; }
    
    /**
     * Gets the opening time.
     * @return The opening time string.
     */
    public String getOpenTime() { return openTime; }
    
    /**
     * Gets the closing time.
     * @return The closing time string.
     */
    public String getCloseTime() { return closeTime; }
    
    /**
     * Checks if the restaurant is closed.
     * @return True if closed, false otherwise.
     */
    public boolean isClosed() { return isClosed; }
    
    /**
     * Gets the schedule type.
     * @return The type string.
     */
    public String getType() { return type; }
    
    /**
     * Gets the event name.
     * @return The event name.
     */
    public String getEventName() { return eventName; }
    
    /**
     * Sets the opening time.
     * @param t The new opening time.
     */
    public void setOpenTime(String t) { this.openTime = t; }
    
    /**
     * Sets the closing time.
     * @param t The new closing time.
     */
    public void setCloseTime(String t) { this.closeTime = t; }
    
    /**
     * Sets the closed status.
     * @param c True if closed.
     */
    public void setClosed(boolean c) { this.isClosed = c; }
    
    /**
     * Returns a string representation of the operating hours.
     * @return "Closed" or a range like "08:00 - 22:00".
     */
    public String getHoursString() {
        return isClosed ? "Closed" : openTime + " - " + closeTime;
    }
}