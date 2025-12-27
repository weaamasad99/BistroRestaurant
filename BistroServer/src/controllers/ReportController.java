package controllers;

import JDBC.DatabaseConnection;
import common.Order;
import java.sql.*;
import java.util.ArrayList;

public class ReportController {

    private Connection conn;

    public ReportController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Retrieves all orders for a specific month and year.
     * Used by ManagerUI to generate histograms.
     */
    public ArrayList<Order> getMonthlyOrders(int month, int year) {
        ArrayList<Order> reportData = new ArrayList<>();
        if (conn == null) return reportData;

        // Uses SQL functions MONTH() and YEAR() to filter dates
        String query = "SELECT * FROM orders WHERE MONTH(order_date) = ? AND YEAR(order_date) = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, month);
            ps.setInt(2, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_number"),
                        rs.getInt("user_id"),
                        rs.getDate("order_date"),
                        rs.getTime("order_time"),
                        rs.getInt("num_of_diners"),
                        rs.getString("status"),
                        rs.getString("confirmation_code"),
                        rs.getTime("actual_arrival_time")
                    );
                    reportData.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating monthly report: " + e.getMessage());
        }
        return reportData;
    }
}