package controllers;

import JDBC.DatabaseConnection;
import common.MonthlyReportData;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReportController {

    private Connection conn;

    public ReportController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public MonthlyReportData generateMonthlyReport(int month, int year) {
        MonthlyReportData data = new MonthlyReportData(month, year);
        
        if (conn == null) {
            System.out.println("Error: No DB Connection in ReportController");
            return data;
        }

        // --- 1. PERFORMANCE REPORT ---
        String perfQuery = "SELECT status, order_time, actual_arrival_time FROM orders " +
                           "WHERE MONTH(order_date) = ? AND YEAR(order_date) = ?";

        int onTime = 0, late = 0, noShow = 0;

        try (PreparedStatement ps = conn.prepareStatement(perfQuery)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    Time ordered = rs.getTime("order_time");
                    Time arrival = rs.getTime("actual_arrival_time");

                    if ("CANCELLED".equalsIgnoreCase(status)) {
                        noShow++;
                    } else if (ordered != null && arrival != null) {
                        long diff = arrival.getTime() - ordered.getTime();
                        long minutes = diff / (60 * 1000);
                        if (minutes > 20) late++;
                        else onTime++;
                    } else {
                        if (!"CANCELLED".equalsIgnoreCase(status)) onTime++;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        data.setTotalOnTime(onTime);
        data.setTotalLate(late);
        data.setTotalNoShow(noShow);

        // --- 2. DETAILED ACTIVITY REPORT ---

        // A. Total Guests Served
        String guestQuery = "SELECT SUM(num_of_diners) FROM orders WHERE MONTH(order_date) = ? AND YEAR(order_date) = ?";
        try (PreparedStatement ps = conn.prepareStatement(guestQuery)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) data.setTotalGuests(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // B. Orders by Day of Week (e.g. 'Monday', 'Tuesday')
        // Uses DAYNAME() function in MySQL
        String dailyOrderQuery = 
            "SELECT DAYNAME(order_date) as day_name, COUNT(*) " +
            "FROM orders " +
            "WHERE MONTH(order_date) = ? AND YEAR(order_date) = ? " +
            "GROUP BY day_name " +
            "ORDER BY FIELD(day_name, 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')";
        data.setOrdersByDayOfWeek(fetchMap(dailyOrderQuery, month, year));

        // C. Waiting List by Day of Week
        String dailyWaitQuery = 
            "SELECT DAYNAME(date_requested) as day_name, COUNT(*) " +
            "FROM waiting_list " +
            "WHERE MONTH(date_requested) = ? AND YEAR(date_requested) = ? " +
            "GROUP BY day_name " +
            "ORDER BY FIELD(day_name, 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')";
        data.setWaitingListByDayOfWeek(fetchMap(dailyWaitQuery, month, year));

        return data;
    }

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