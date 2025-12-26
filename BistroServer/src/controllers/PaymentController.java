package controllers;

import JDBC.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
    public boolean processPayment(int orderNumber) {
        if (conn == null) return false;

        String updateQuery = "UPDATE orders SET status = 'FINISHED' WHERE order_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setInt(1, orderNumber);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                System.out.println("Log: Payment processed successfully for Order #" + orderNumber);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error processing payment: " + e.getMessage());
        }
        return false;
    }
}