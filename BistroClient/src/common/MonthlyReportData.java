package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MonthlyReportData implements Serializable {
    private int month;
    private int year;
    
    // --- 1. Performance Data ---
    private int totalOnTime;
    private int totalLate;
    private int totalNoShow;
    
    // Detailed list for the "Time Report" Table (Exceptions like Late/NoShow)
    private ArrayList<Order> exceptionOrders; 
    
    // Statistics calculated on server
    private String averageDiningTime; // e.g. "1h 15m"

    // --- 2. Activity Data ---
    private int totalGuests; 
    
    // Charts Data
    private Map<String, Integer> ordersByDayOfWeek;
    private Map<String, Integer> waitingListByDayOfWeek;
    
    // Detailed list for "Activity Report" Table (All orders history)
    private ArrayList<Order> allMonthOrders;

    public MonthlyReportData() {
        this.ordersByDayOfWeek = new HashMap<>();
        this.waitingListByDayOfWeek = new HashMap<>();
        this.exceptionOrders = new ArrayList<>();
        this.allMonthOrders = new ArrayList<>();
    }
    
    public MonthlyReportData(int month, int year) {
        this.month = month;
        this.year = year;
        this.ordersByDayOfWeek = new HashMap<>();
        this.waitingListByDayOfWeek = new HashMap<>();
        this.exceptionOrders = new ArrayList<>();
        this.allMonthOrders = new ArrayList<>();
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

    public String getAverageDiningTime() { return averageDiningTime; }
    public void setAverageDiningTime(String avg) { this.averageDiningTime = avg; }

    public Map<String, Integer> getOrdersByDayOfWeek() { return ordersByDayOfWeek; }
    public void setOrdersByDayOfWeek(Map<String, Integer> map) { this.ordersByDayOfWeek = map; }

    public Map<String, Integer> getWaitingListByDayOfWeek() { return waitingListByDayOfWeek; }
    public void setWaitingListByDayOfWeek(Map<String, Integer> map) { this.waitingListByDayOfWeek = map; }
    
    public ArrayList<Order> getExceptionOrders() { return exceptionOrders; }
    public void setExceptionOrders(ArrayList<Order> list) { this.exceptionOrders = list; }

    public ArrayList<Order> getAllMonthOrders() { return allMonthOrders; }
    public void setAllMonthOrders(ArrayList<Order> list) { this.allMonthOrders = list; }

    public boolean isEmpty() {
        return totalOnTime == 0 && totalLate == 0 && totalNoShow == 0 &&
               (allMonthOrders == null || allMonthOrders.isEmpty());
    }
}