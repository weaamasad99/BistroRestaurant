package controllers;

import JDBC.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

/**
 * Manages payment processing, bill calculation, and closing of orders.
 * * @author Group 6
 * @version 1.0
 */
public class PaymentController {

    private Connection conn;

    /**
     * Initializes the controller with a database connection.
     */
    public PaymentController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Retrieves bill details including price and user type.
     * Simulation: Processes payment by marking an order as 'FINISHED'.
     * In a real system, this would integrate with a Payment Gateway API.
     * * @param code The confirmation code of the active order.
     * @return Object array [Code, Price, UserType] or null if not found.
     */
    public Object[] getBillData(String code) { // Renamed to getBillData
        if (conn == null) return null;

        String query = "SELECT u.user_type FROM orders o " +
                       "JOIN users u ON o.user_id = u.user_id " +
                       "WHERE o.confirmation_code = ? AND o.status = 'ACTIVE'";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String userType = rs.getString("user_type");
                double basePrice = 250.0; // Standard fixed price
                
                // Return data package: [Code, Price, UserType]
                return new Object[]{code, basePrice, userType};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Processes the bill payment, frees the table, and closes the order.
     * Also triggers the waiting list mechanism to notify the next customer.
     * * @param code The confirmation code.
     * @return true if payment successful, false otherwise.
     */
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
        double basePrice = 250.0; // Simulated price
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
            try {
                // 1. Unlink ALL orders associated with this user (Active, Cancelled, Finished)
                // We set user_id to NULL so the history remains, but the link to the user is cut.
                String unlinkOrders = "UPDATE orders SET user_id = NULL WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(unlinkOrders)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // 2. Unlink any Waiting List entries (just in case)
                String unlinkWaiting = "UPDATE waiting_list SET user_id = NULL WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(unlinkWaiting)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // 3. NOW it is safe to delete the user
                String deleteUser = "DELETE FROM users WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                
                System.out.println("DEBUG: Casual user " + userId + " cleaned up successfully.");
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
    
    
}