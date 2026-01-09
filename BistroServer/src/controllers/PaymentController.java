package controllers;

import JDBC.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

public class PaymentController {

    private Connection conn;

    public PaymentController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Simulation: Processes payment by marking an order as 'FINISHED'.
     * In a real system, this would integrate with a Payment Gateway API.
     */
    public boolean getBill(String code) {
        if (conn == null) return false;

        String updateQuery = "SELECT order_number FROM orders WHERE confirmation_code = ? AND status = 'ACTIVE'";

        try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setString(1, code);
            
            ResultSet rs = ps.executeQuery();
            
            // If rs.next() is true, it means a row was found -> Reservation exists
            return rs.next(); 
        } catch (SQLException e) {
            System.err.println("Error getting bill: " + e.getMessage());
        }
        return false;
    }
    
    public boolean payBill(String code) {
        if (conn == null) return false;

        int userId = -1;
        String userType = "";
        int seatsFreed = 0;

        // ==========================================================
        // STEP 1: Find User, Role, and Table Size (Combined for efficiency)
        // ==========================================================
        String findDataSql = "SELECT o.user_id, u.user_type, t.seats " +
                             "FROM orders o " +
                             "JOIN users u ON o.user_id = u.user_id " +
                             "JOIN restaurant_tables t ON o.user_id = t.user_id " +
                             "WHERE o.confirmation_code = ? AND o.status = 'ACTIVE'";

        try (PreparedStatement ps = conn.prepareStatement(findDataSql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    userType = rs.getString("user_type");
                    seatsFreed = rs.getInt("seats");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // ==========================================================
        // STEP 2: Calculate Price & Apply Subscriber Discount
        // ==========================================================
        double basePrice = 250.0; // Simulated price [cite: 48]
        if ("SUBSCRIBER".equalsIgnoreCase(userType)) {
            double finalPrice = basePrice * 0.9; // 10% Discount 
            System.out.println("DEBUG: Subscriber discount applied. Total: " + finalPrice);
        } else {
            System.out.println("DEBUG: Casual bill total: " + basePrice);
        }

        // ==========================================================
        // STEP 3: Free the Table & Trigger Waiting List
        // ==========================================================
        String freeTable = "UPDATE restaurant_tables SET status = 'AVAILABLE', user_id = NULL WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(freeTable)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            
            // AUTOMATION: Notify the next group that fits this specific table size
            WaitingListController wlController = new WaitingListController();
            wlController.notifyNextInLine(seatsFreed); 
        } catch (SQLException e) { e.printStackTrace(); }

        // ==========================================================
        // STEP 4: Close Order & Record Leaving Time
        // ==========================================================
        String closeOrder = "UPDATE orders SET status = 'FINISHED', user_id = NULL, actual_arrival_time = actual_arrival_time " + 
                            "WHERE confirmation_code = ?";
        // Note: Your DB schema uses 'actual_arrival_time' but the requirements ask for arrival/departure.
        // Ensure your table has a column for 'leaving_time' or similar for the visual reports.
        try (PreparedStatement ps = conn.prepareStatement(closeOrder)) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        // ==========================================================
        // STEP 5: Cleanup Casual User
        // ==========================================================
        if ("CASUAL".equalsIgnoreCase(userType)) {
            String deleteUser = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        return true;
    }
    
    
}