package common;

import java.io.Serializable;

// 1. MUST implement Serializable
public class BistroSchedule implements Serializable {
    private static final long serialVersionUID = 1L; // 2. Recommended for serialization

    private String identifier; 
    private String openTime;
    private String closeTime;
    private boolean isClosed;
    private String type;       
    private String eventName;  

    // Empty constructor is required by some serializers (like Kryo)
    public BistroSchedule() {} 

    public BistroSchedule(String identifier, String openTime, String closeTime, boolean isClosed, String type, String eventName) {
        this.identifier = identifier;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.type = type;
        this.eventName = eventName;
    }

    public String getIdentifier() { return identifier; }
    public String getOpenTime() { return openTime; }
    public String getCloseTime() { return closeTime; }
    public boolean isClosed() { return isClosed; }
    public String getType() { return type; }
    public String getEventName() { return eventName; }
    
    public void setOpenTime(String t) { this.openTime = t; }
    public void setCloseTime(String t) { this.closeTime = t; }
    public void setClosed(boolean c) { this.isClosed = c; }
    
    public String getHoursString() {
        return isClosed ? "Closed" : openTime + " - " + closeTime;
    }
}