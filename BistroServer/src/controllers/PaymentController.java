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
        boolean isCasual = false;

        // ==========================================================
        // STEP 1: Find the User ID associated with this Order Code
        // ==========================================================
        String findUserSql = "SELECT user_id FROM orders WHERE confirmation_code = ? AND status = 'ACTIVE'";

        try (PreparedStatement ps = conn.prepareStatement(findUserSql)) {
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

        // ==========================================================
        // STEP 2: Find the Role of this User
        // ==========================================================
        if (userId != -1) {
            String findRoleSql = "SELECT user_type FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(findRoleSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("user_type");
                        if ("CASUAL".equalsIgnoreCase(role)) {
                            isCasual = true;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        // ==========================================================
        // STEP 3: Free the Table
        // ==========================================================
        if (userId != -1) {
            String freeTable = "UPDATE restaurant_tables SET status = 'AVAILABLE', user_id = NULL WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(freeTable)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            // ==========================================================
            // STEP 4: Close Order & Unlink User (Set user_id = NULL)
            // ==========================================================
            String closeOrder = "UPDATE orders SET status = 'FINISHED', user_id = NULL WHERE confirmation_code = ?";
            try (PreparedStatement ps = conn.prepareStatement(closeOrder)) {
                ps.setString(1, code);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            // ==========================================================
            // STEP 5: Delete Casual User (If applicable)
            // ==========================================================
            if (isCasual) {
                String deleteUser = "DELETE FROM users WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                    ps.setInt(1, userId);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Casual user #" + userId + " deleted from database.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Proceed even if deletion fails (payment succeeded)
                }
            }

            return true;
        }

        return false;
    }
}