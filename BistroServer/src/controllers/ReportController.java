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

    /**
     * Generates the full report data for a specific month/year.
     */
    public MonthlyReportData generateMonthlyReport(int month, int year) {
        MonthlyReportData data = new MonthlyReportData(month, year);
        
        if (conn == null) {
            System.out.println("Error: No DB Connection in ReportController");
            return data;
        }

        // 1. PERFORMANCE REPORT (SQL Logic)
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
                        // Check logic: Late if arrival > order + 20 mins
                        long diff = arrival.getTime() - ordered.getTime();
                        long minutes = diff / (60 * 1000);
                        if (minutes > 20) late++;
                        else onTime++;
                    } else {
                        // If Finished/Active but no arrival data, assume on time
                        if (!"CANCELLED".equalsIgnoreCase(status)) onTime++;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        data.setTotalOnTime(onTime);
        data.setTotalLate(late);
        data.setTotalNoShow(noShow);

        // 2. ORDERS PER WEEK
        String ordersQuery = "SELECT (DAY(order_date) - 1) / 7 + 1 AS week, COUNT(*) FROM orders " +
                             "WHERE MONTH(order_date) = ? AND YEAR(order_date) = ? GROUP BY week";
        data.setWeeklyOrderCounts(fetchMap(ordersQuery, month, year));

        // 3. WAITING LIST PER WEEK
        String waitQuery = "SELECT (DAY(date_requested) - 1) / 7 + 1 AS week, COUNT(*) FROM waiting_list " +
                           "WHERE MONTH(date_requested) = ? AND YEAR(date_requested) = ? GROUP BY week";
        data.setWeeklyWaitingListCounts(fetchMap(waitQuery, month, year));

        return data;
    }

    // Helper for map queries
    private Map<String, Integer> fetchMap(String query, int month, int year) {
        Map<String, Integer> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put("Week " + rs.getInt(1), rs.getInt(2));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}