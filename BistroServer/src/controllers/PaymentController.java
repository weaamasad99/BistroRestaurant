package controllers;

import JDBC.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        // 1. Find the User ID associated with this code
        // We only look for 'ACTIVE' status (meaning they are currently eating)
        String findUser = "SELECT user_id FROM orders WHERE confirmation_code = ? AND status = 'ACTIVE'";

        try (PreparedStatement ps = conn.prepareStatement(findUser)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    System.out.println("Checkout failed: No active order found for code " + code);
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // 2. Free the Table (using the retrieved user_id)
        if (userId != -1) {
            String freeTable = "UPDATE restaurant_tables SET status = 'AVAILABLE', user_id = NULL WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(freeTable)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
                // We proceed even if no table was updated (in case of data sync issues), 
                // because we still want to close the order.
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        // 3. Close the Order
        String closeOrder = "UPDATE orders SET status = 'FINISHED' WHERE confirmation_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(closeOrder)) {
            ps.setString(1, code);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}