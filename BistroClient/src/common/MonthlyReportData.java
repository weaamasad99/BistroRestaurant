package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MonthlyReportData implements Serializable {
    private int month;
    private int year;
    
    // -- 1. Performance Data --
    private int totalOnTime;
    private int totalLate;
    private int totalNoShow;
    
    // -- 2. Detailed Activity Data --
    // Total number of actual diners served
    private int totalGuests; 
    
    // Analysis by Day of Week (e.g., "Monday" -> 15 orders)
    // This helps the manager identify the busiest days.
    private Map<String, Integer> ordersByDayOfWeek;
    private Map<String, Integer> waitingListByDayOfWeek;

    public MonthlyReportData() {
        this.ordersByDayOfWeek = new HashMap<>();
        this.waitingListByDayOfWeek = new HashMap<>();
    }
    
    public MonthlyReportData(int month, int year) {
        this.month = month;
        this.year = year;
        this.ordersByDayOfWeek = new HashMap<>();
        this.waitingListByDayOfWeek = new HashMap<>();
    }

    // Getters and Setters
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTotalOnTime() { return totalOnTime; }
    public void setTotalOnTime(int totalOnTime) { this.totalOnTime = totalOnTime; }

    public int getTotalLate() { return totalLate; }
    public void setTotalLate(int totalLate) { this.totalLate = totalLate; }
    
    public int getTotalNoShow() { return totalNoShow; }
    public void setTotalNoShow(int n) { this.totalNoShow = n; }
    
    public int getTotalGuests() { return totalGuests; }
    public void setTotalGuests(int totalGuests) { this.totalGuests = totalGuests; }

    public Map<String, Integer> getOrdersByDayOfWeek() { return ordersByDayOfWeek; }
    public void setOrdersByDayOfWeek(Map<String, Integer> map) { this.ordersByDayOfWeek = map; }

    public Map<String, Integer> getWaitingListByDayOfWeek() { return waitingListByDayOfWeek; }
    public void setWaitingListByDayOfWeek(Map<String, Integer> map) { this.waitingListByDayOfWeek = map; }
    
    public boolean isEmpty() {
        return totalOnTime == 0 && totalLate == 0 && totalNoShow == 0 &&
               (ordersByDayOfWeek == null || ordersByDayOfWeek.isEmpty());
    }
}