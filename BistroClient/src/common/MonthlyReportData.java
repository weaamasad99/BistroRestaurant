package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO (Data Transfer Object) to hold the calculated report statistics.
 * This is sent from Server -> Client.
 */
public class MonthlyReportData implements Serializable {
    private int month;
    private int year;
    
    // -- Performance Report Data --
    private int totalOnTime;
    private int totalLate;
    private int totalNoShow;
    
    // -- Orders & Waiting List Data (Weekly Breakdown) --
    // Key: Week Number (1-4), Value: Count
    private Map<String, Integer> weeklyOrderCounts; 
    private Map<String, Integer> weeklyWaitingListCounts;

    public MonthlyReportData(int month, int year) {
        this.month = month;
        this.year = year;
        this.weeklyOrderCounts = new HashMap<>();
        this.weeklyWaitingListCounts = new HashMap<>();
    }

    // Getters and Setters
    public int getTotalOnTime() { return totalOnTime; }
    public void setTotalOnTime(int totalOnTime) { this.totalOnTime = totalOnTime; }

    public int getTotalLate() { return totalLate; }
    public void setTotalLate(int totalLate) { this.totalLate = totalLate; }
    
    public int getTotalNoShow() { return totalNoShow; }
    public void setTotalNoShow(int n) { this.totalNoShow = n; }

    public Map<String, Integer> getWeeklyOrderCounts() { return weeklyOrderCounts; }
    public void setWeeklyOrderCounts(Map<String, Integer> map) { this.weeklyOrderCounts = map; }

    public Map<String, Integer> getWeeklyWaitingListCounts() { return weeklyWaitingListCounts; }
    public void setWeeklyWaitingListCounts(Map<String, Integer> map) { this.weeklyWaitingListCounts = map; }
}