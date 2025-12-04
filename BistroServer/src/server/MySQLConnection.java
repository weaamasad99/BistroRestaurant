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
    
    // 1. Hold the single instance of the class
    private static MySQLConnection instance = null;
    
    // Connection object (now an instance variable, not static)
    private static Connection conn = null;

    private static final String URL = "jdbc:mysql://localhost:3306/bistro_db?serverTimezone=UTC";
    private static final String USER = "root";        
    private static final String PASSWORD = "Aa123456";  

    /**
     * 2. Private Constructor to prevent instantiation from outside
     */
    private MySQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(">>> Connected Successfully to MySQL");
        } catch (SQLException e) {
            System.err.println("SQL Connection Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver Not Found: " + e.getMessage());
        }
    }

    /**
     * 3. Public static method to get the single instance.
     * Implements Lazy Initialization.
     */
    public static synchronized MySQLConnection getInstance() {
        if (instance == null) {
            instance = new MySQLConnection();
        }
        return instance;
    }

    /**
     * Fetches all records from the 'orders' table.
     * Note: Removed 'static' keyword.
     */
    public ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        if (conn == null) return orders; // Safety check

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
     * Note: Removed 'static' keyword.
     */
    public  boolean updateOrder(Order orderToUpdate) {
        if (conn == null) return false; // Safety check

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