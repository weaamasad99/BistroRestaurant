package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import common.Order;
import common.Table;
import common.User;
import common.WaitingList;

/*
  Singleton class handling the JDBC connection to the MySQL database.
*/
public class DatabaseConnection {
    
    // Hold the single instance of the class
    private static DatabaseConnection instance = null;
    
    private static Connection conn = null;

    private static final String URL = "jdbc:mysql://localhost:3306/bistro_db?serverTimezone=UTC";
    private static final String USER = "root";        
    private static final String PASSWORD = "Aa123456";  

    /*
      Private Constructor to prevent instantiation from outside
    */
    private DatabaseConnection() {
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

    /*
      Public static method to get the single instance.
      Implements Lazy Initialization.
    */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /*
      Fetches all records from the 'orders' table.
    */
 // =========================================================================
    // 1. ORDERS
    // =========================================================================
    public ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        if (conn == null) return orders;

        String query = "SELECT * FROM orders";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Constructor matches common.Order:
                // (orderNumber, userId, orderDate, orderTime, numOfDiners, status, confirmationCode, actualArrivalTime)
                Order order = new Order(
                    rs.getInt("order_number"),
                    rs.getInt("user_id"),
                    rs.getDate("order_date"),
                    rs.getTime("order_time"),
                    rs.getInt("num_of_diners"),
                    rs.getString("status"),
                    rs.getInt("confirmation_code"),
                    rs.getTime("actual_arrival_time")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean updateOrder(Order order) {
        if (conn == null) return false;
        // Matches DB columns: num_of_diners
        String query = "UPDATE orders SET order_date = ?, order_time = ?, num_of_diners = ?, status = ? WHERE order_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, order.getOrderDate());
            ps.setTime(2, order.getOrderTime());
            ps.setInt(3, order.getNumberOfDiners());
            ps.setString(4, order.getStatus());
            ps.setInt(5, order.getOrderNumber());
            
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // 2. TABLES (restaurant_tables)
    // =========================================================================
    public ArrayList<Table> getAllTables() {
        ArrayList<Table> tables = new ArrayList<>();
        if (conn == null) return tables;

        String query = "SELECT * FROM restaurant_tables";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Table table = new Table(
                    rs.getInt("table_id"),
                    rs.getInt("seats"),
                    rs.getString("status") // AVAILABLE, OCCUPIED, RESERVED
                );
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public boolean updateTable(Table table) {
        if (conn == null) return false;
        // Allows updating seats and status
        String query = "UPDATE restaurant_tables SET seats = ?, status = ? WHERE table_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, table.getSeats());
            ps.setString(2, table.getStatus());
            ps.setInt(3, table.getTableId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addTable(Table table) {
        if (conn == null) return false;
        String query = "INSERT INTO restaurant_tables (table_id, seats, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, table.getTableId());
            ps.setInt(2, table.getSeats());
            ps.setString(3, table.getStatus());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeTable(int tableId) {
        if (conn == null) return false;
        String query = "DELETE FROM restaurant_tables WHERE table_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tableId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // 3. USERS & SUBSCRIBERS
    // =========================================================================
    public User loginUser(String username, String password) {
        if (conn == null) return null;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<User> getAllSubscribers() {
        ArrayList<User> subscribers = new ArrayList<>();
        if (conn == null) return subscribers;

        // Fetch only Subscribers
        String query = "SELECT * FROM users WHERE user_type = 'SUBSCRIBER'";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                subscribers.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subscribers;
    }
    
    // Helper to map User row
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        // Handle nullable integers (subscriber_number)
        int subNum = rs.getInt("subscriber_number");
        Integer subNumObj = rs.wasNull() ? null : subNum;

        return new User(
            rs.getInt("user_id"),
            rs.getString("phone_number"),
            rs.getString("email"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("user_type"),
            subNumObj,
            rs.getString("username"),
            rs.getString("password")
        );
    }

    // =========================================================================
    // 4. WAITING LIST
    // =========================================================================
    public ArrayList<WaitingList> getAllWaitingList() {
        ArrayList<WaitingList> list = new ArrayList<>();
        if (conn == null) return list;

        String query = "SELECT * FROM waiting_list";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                WaitingList item = new WaitingList(
                    rs.getInt("waiting_id"),
                    rs.getInt("user_id"),
                    rs.getDate("date_requested"),
                    rs.getTime("time_requested"),
                    rs.getInt("num_of_diners"),
                    rs.getString("status")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}