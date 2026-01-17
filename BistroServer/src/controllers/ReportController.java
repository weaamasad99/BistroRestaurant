package controllers;

import JDBC.DatabaseConnection;
import common.MonthlyReportData;
import common.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the generation of statistical reports for the Bistro.
 * Aggregates data from orders and waiting lists for performance analysis.
 * * @author Group 6
 * @version 1.0
 */
public class ReportController {

    private Connection conn;

    /**
     * Initializes the controller with a database connection.
     */
    public ReportController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Generates a comprehensive monthly report containing performance stats and activity logs.
     * * @param month The month to generate for.
     * @param year The year to generate for.
     * @return MonthlyReportData object with full stats.
     */
    public MonthlyReportData generateMonthlyReport(int month, int year) {
        MonthlyReportData data = new MonthlyReportData(month, year);
        
        if (conn == null) {
            System.out.println("Error: No DB Connection in ReportController");
            return data;
        }

        // ---------------------------------------------------------
        // 1. FETCH RAW DATA (Performance & Activity combined)
        // ---------------------------------------------------------
        String query = "SELECT * FROM orders WHERE MONTH(order_date) = ? AND YEAR(order_date) = ?";
        
        int onTime = 0, late = 0, noShow = 0;
        long totalDurationMinutes = 0;
        int durationCount = 0;
        
        ArrayList<Order> fullList = new ArrayList<>();
        ArrayList<Order> exceptionList = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Create Order Object
                    Order order = new Order(
                        rs.getInt("order_number"),
                        rs.getInt("user_id"),
                        rs.getDate("order_date"),
                        rs.getTime("order_time"),
                        rs.getInt("num_of_diners"),
                        rs.getString("status"),
                        rs.getString("confirmation_code"),
                        rs.getTime("actual_arrival_time"),
                        rs.getTime("leaving_time") // Make sure DB has this column
                    );
                    
                    fullList.add(order); // Store for Activity Report Table

                    // --- ANALYZE PERFORMANCE ---
                    String status = order.getStatus();
                    Time ordered = order.getOrderTime();
                    Time arrived = order.getActualArrivalTime();
                    Time left = order.getLeavingTime();

                    if ("CANCELLED".equalsIgnoreCase(status)) {
                        noShow++;
                        exceptionList.add(order); // Add to "Bad" list
                    } 
                    else if (ordered != null && arrived != null) {
                        // Calc Delay
                        long diffMs = arrived.getTime() - ordered.getTime();
                        long diffMinutes = diffMs / (60 * 1000);
                        
                        // Rule: Late if > 15 Minutes
                        if (diffMinutes > 15) {
                            late++;
                            exceptionList.add(order); // Add to "Bad" list
                        } else {
                            onTime++;
                        }

                        // Calc Dining Duration (if they have left)
                        if (left != null) {
                            long diningMs = left.getTime() - arrived.getTime();
                            if (diningMs > 0) {
                                totalDurationMinutes += (diningMs / (60 * 1000));
                                durationCount++;
                            }
                        }
                    } else {
                        // Fallback for active/finished without timestamps
                        if (!"CANCELLED".equalsIgnoreCase(status)) onTime++;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Set Counters
        data.setTotalOnTime(onTime);
        data.setTotalLate(late);
        data.setTotalNoShow(noShow);
        
        // Set Lists
        data.setAllMonthOrders(fullList);
        data.setExceptionOrders(exceptionList);

        // Calc Average
        if (durationCount > 0) {
            long avg = totalDurationMinutes / durationCount;
            data.setAverageDiningTime(avg + " mins");
        } else {
            data.setAverageDiningTime("N/A");
        }

        // ---------------------------------------------------------
        // 2. AGGREGATE DATA FOR CHARTS (Day of Week)
        // ---------------------------------------------------------
        
        // A. Total Guests (Sum from the full list we just fetched to save SQL calls)
        int guests = fullList.stream().mapToInt(Order::getNumberOfDiners).sum();
        data.setTotalGuests(guests);

        // B. Orders by Day (using SQL for ease of grouping)
        String dailyOrderQuery = 
            "SELECT DAYNAME(order_date) as day_name, COUNT(*) " +
            "FROM orders " +
            "WHERE MONTH(order_date) = ? AND YEAR(order_date) = ? " +
            "GROUP BY day_name " +
            "ORDER BY FIELD(day_name, 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')";
        data.setOrdersByDayOfWeek(fetchMap(dailyOrderQuery, month, year));

        // C. Waiting List by Day
        String dailyWaitQuery = 
            "SELECT DAYNAME(date_requested) as day_name, COUNT(*) " +
            "FROM waiting_list " +
            "WHERE MONTH(date_requested) = ? AND YEAR(date_requested) = ? " +
            "GROUP BY day_name " +
            "ORDER BY FIELD(day_name, 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')";
        data.setWaitingListByDayOfWeek(fetchMap(dailyWaitQuery, month, year));

        return data;
    }

    /**
     * Helper to fetch data map for charts.
     */
    private Map<String, Integer> fetchMap(String query, int month, int year) {
        Map<String, Integer> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt(2));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}