package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object (DTO) for generating monthly performance and activity reports.
 * Holds aggregated statistics and detailed lists required for charts and tables.
 * @author Group 6
 * @version 1.0
 */
public class MonthlyReportData implements Serializable {
    /** The month of the report. */
    private int month;
    
    /** The year of the report. */
    private int year;
    
    // --- 1. Performance Data ---
    
    /** Count of orders arrived on time. */
    private int totalOnTime;
    
    /** Count of orders that arrived late. */
    private int totalLate;
    
    /** Count of orders where customers did not show up. */
    private int totalNoShow;
    
    /** Detailed list for the "Time Report" Table (Exceptions like Late/NoShow). */
    private ArrayList<Order> exceptionOrders; 
    
    /** Statistics calculated on server representing average dining duration. */
    private String averageDiningTime; // e.g. "1h 15m"

    // --- 2. Activity Data ---
    
    /** Total number of guests served in the month. */
    private int totalGuests; 
    
    /** Map containing order counts grouped by day of the week. */
    private Map<String, Integer> ordersByDayOfWeek;
    
    /** Map containing waiting list entry counts grouped by day of the week. */
    private Map<String, Integer> waitingListByDayOfWeek;
    
    /** Detailed list for "Activity Report" Table (All orders history). */
    private ArrayList<Order> allMonthOrders;

    /**
     * Default constructor initializing collections.
     */
    public MonthlyReportData() {
        this.ordersByDayOfWeek = new HashMap<>();
        this.waitingListByDayOfWeek = new HashMap<>();
        this.exceptionOrders = new ArrayList<>();
        this.allMonthOrders = new ArrayList<>();
    }
    
    /**
     * Constructor specifying the month and year.
     * @param month The month integer (1-12).
     * @param year The year integer.
     */
    public MonthlyReportData(int month, int year) {
        this.month = month;
        this.year = year;
        this.ordersByDayOfWeek = new HashMap<>();
        this.waitingListByDayOfWeek = new HashMap<>();
        this.exceptionOrders = new ArrayList<>();
        this.allMonthOrders = new ArrayList<>();
    }

    // Getters and Setters
    
    /** @return The report month. */
    public int getMonth() { return month; }
    /** @param month The month to set. */
    public void setMonth(int month) { this.month = month; }
    
    /** @return The report year. */
    public int getYear() { return year; }
    /** @param year The year to set. */
    public void setYear(int year) { this.year = year; }

    /** @return Total on-time orders. */
    public int getTotalOnTime() { return totalOnTime; }
    /** @param totalOnTime Value to set. */
    public void setTotalOnTime(int totalOnTime) { this.totalOnTime = totalOnTime; }

    /** @return Total late orders. */
    public int getTotalLate() { return totalLate; }
    /** @param totalLate Value to set. */
    public void setTotalLate(int totalLate) { this.totalLate = totalLate; }
    
    /** @return Total no-show orders. */
    public int getTotalNoShow() { return totalNoShow; }
    /** @param n Value to set. */
    public void setTotalNoShow(int n) { this.totalNoShow = n; }
    
    /** @return Total guests served. */
    public int getTotalGuests() { return totalGuests; }
    /** @param totalGuests Value to set. */
    public void setTotalGuests(int totalGuests) { this.totalGuests = totalGuests; }

    /** @return formatted string of average dining time. */
    public String getAverageDiningTime() { return averageDiningTime; }
    /** @param avg Average string to set. */
    public void setAverageDiningTime(String avg) { this.averageDiningTime = avg; }

    /** @return Map of orders by day of week. */
    public Map<String, Integer> getOrdersByDayOfWeek() { return ordersByDayOfWeek; }
    /** @param map Map to set. */
    public void setOrdersByDayOfWeek(Map<String, Integer> map) { this.ordersByDayOfWeek = map; }

    /** @return Map of waiting list entries by day of week. */
    public Map<String, Integer> getWaitingListByDayOfWeek() { return waitingListByDayOfWeek; }
    /** @param map Map to set. */
    public void setWaitingListByDayOfWeek(Map<String, Integer> map) { this.waitingListByDayOfWeek = map; }
    
    /** @return List of orders flagged as exceptions. */
    public ArrayList<Order> getExceptionOrders() { return exceptionOrders; }
    /** @param list List to set. */
    public void setExceptionOrders(ArrayList<Order> list) { this.exceptionOrders = list; }

    /** @return List of all orders for the month. */
    public ArrayList<Order> getAllMonthOrders() { return allMonthOrders; }
    /** @param list List to set. */
    public void setAllMonthOrders(ArrayList<Order> list) { this.allMonthOrders = list; }

    /**
     * Checks if the report contains any data.
     * @return true if there are no stats or orders recorded.
     */
    public boolean isEmpty() {
        return totalOnTime == 0 && totalLate == 0 && totalNoShow == 0 &&
               (allMonthOrders == null || allMonthOrders.isEmpty());
    }
}