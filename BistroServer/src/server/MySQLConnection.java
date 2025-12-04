package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import common.Order;

/**
 * Singleton class handling the JDBC connection to the MySQL database.
 */
public class MySQLConnection {
    
    private static Connection conn;

    /**
     * Establishes a connection to the DB. 
     * IMPORTANT: Update 'password' to your local MySQL password.
     */
    public static void connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Log: Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Log: Driver definition failed");
        }

        try {
            
            conn = DriverManager.getConnection("jdbc:mysql://localhost/bistro_db?serverTimezone=IST", "root", "Aa123456");
            System.out.println("Log: SQL connection succeed");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
    }

    /**
     * Fetches all records from the 'orders' table.
     * @return ArrayList of Order objects.
     */
    public static ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_number"),
                    rs.getDate("order_date"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getDate("date_of_placing_order")
                );
                orders.add(order);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Updates the date and number of guests for a specific orders.
     * @param orderToUpdate The order object containing updated info.
     * @return true if update succeeded, false otherwise.
     */
    public static boolean updateOrder(Order orderToUpdate) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE orders SET order_date = ?, number_of_guests = ? WHERE order_number = ?");
            ps.setDate(1, orderToUpdate.getOrderDate());
            ps.setInt(2, orderToUpdate.getNumberOfGuests());
            ps.setInt(3, orderToUpdate.getOrderNumber());
            
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}