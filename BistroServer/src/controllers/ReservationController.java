package controllers;

import JDBC.DatabaseConnection;
import common.BistroSchedule;
import common.Order;
import common.Table;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Manages Orders (Reservations) and Physical Table configurations.
 */
public class ReservationController {

    private Connection conn;

    public ReservationController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ========================
    // ORDER / RESERVATION LOGIC
    // ========================

    public ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        if (conn == null) return orders;

        String query = "SELECT * FROM orders";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
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
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    
    
    
    /**
     * Fetches all orders belonging to a specific user.
     */
    public ArrayList<Order> getOrdersByUserId(int userId) {
        ArrayList<Order> history = new ArrayList<>();
        if (conn == null) return history;

        String query = "SELECT * FROM orders WHERE user_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            
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
                    history.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user history: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }

    public boolean updateOrder(Order order) {
        if (conn == null) return false;
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
    
    
    /**
     * Checks if the restaurant has enough open seats for a specific time slot,
     * accounting for the 2-hour dining duration.
     */
    private boolean checkRestaurantCapacity(Date date, Time time, int requestedSeats) {
        if (conn == null) return false;

        // 1. Get Total Restaurant Capacity
        int totalCapacity = 0;
        String capQuery = "SELECT SUM(seats) as total_seats FROM restaurant_tables";
        try (PreparedStatement ps = conn.prepareStatement(capQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalCapacity = rs.getInt("total_seats");
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }

        // If no tables exist, capacity is 0 -> Block reservation
        if (totalCapacity == 0) {
            System.out.println("Capacity Check Failed: No tables defined in database.");
            return false; 
        }

        // 2. Count Reserved Seats (Logic: 2-Hour Dining Window)
        // We sum diners from any order that started up to 2 hours before the requested time.
        // E.g. If requesting 13:00, we count active diners from 11:00, 11:30, 12:00, 12:30, and 13:00.
        int reservedSeats = 0;
        
        // MySQL Syntax: Check orders where time is between (RequestTime - 2 hours) and (RequestTime + 1 min)
        // We look for 'APPROVED' (Future booking) or 'ACTIVE' (Currently eating)
        String reserveQuery = "SELECT SUM(num_of_diners) as booked FROM orders " +
                              "WHERE order_date = ? " +
                              "AND order_time > SUBTIME(?, '02:00:00') " + 
                              "AND order_time <= ? " +
                              "AND status IN ('APPROVED', 'ACTIVE')";
        
        try (PreparedStatement ps = conn.prepareStatement(reserveQuery)) {
            ps.setDate(1, date);
            ps.setTime(2, time); // Used for SUBTIME calculation
            ps.setTime(3, time); // Used for upper bound
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reservedSeats = rs.getInt("booked");
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }

        System.out.println("Capacity Check: Total=" + totalCapacity + ", Reserved=" + reservedSeats + ", Requesting=" + requestedSeats);

        // 3. Check availability
        return (reservedSeats + requestedSeats) <= totalCapacity;
    }
    
    public boolean createReservation(Order order) {
    	UserController userController = new UserController();
    	
        try {
        	int userID = order.getUserId();
            Date sqlDate = Date.valueOf(order.getOrderDate().toString()); // String "YYYY-MM-DD" -> sql.Date
            Time sqlTime = order.getOrderTime(); // String "HH:mm" -> sql.Time
            
            
         // 1. Check Capacity (The Missing Requirement)
            if (!checkRestaurantCapacity(sqlDate, sqlTime, order.getNumberOfDiners())) {
                System.out.println("Reservation failed: Restaurant is fully booked at this time.");
                return false; // Tells Client "Fully Booked"
            }

            // 3. CHECK DUPLICATE: Does this user already have a booking at this exact time?
            if (checkIfReservationExists(userID, sqlDate, sqlTime)) {
            	System.out.println("Reservation failed: User already has a booking at this time.");
                return false; 
            }

            // 4. INSERT the new reservation
            String insertSQL = "INSERT INTO orders (user_id, order_date, order_time, num_of_diners, status, confirmation_code) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL)	;

            String code = userController.generateConfirmationCode();
            
            ps.setInt(1, userID);
            ps.setDate(2, sqlDate);
            ps.setTime(3, sqlTime);
            ps.setInt(4, order.getNumberOfDiners());
            ps.setString(5, "APPROVED");
            ps.setString(6, code);
            ps.executeUpdate();
            
            order.setConfirmationCode(code);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Helper Method: Checks DB for existing reservation for a specific user.
     * @return true if reservation exists, false if clear.
     */
    private boolean checkIfReservationExists(int userId, Date date, Time time) {
        // Added 'user_id = ?' to ensure we only block if THIS specific user already booked this time
        String query = "SELECT order_number FROM orders WHERE user_id = ? AND order_date = ? AND order_time = ? AND status != 'CANCELLED'";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setDate(2, date);
            ps.setTime(3, time);
            ResultSet rs = ps.executeQuery();
            return rs.next(); 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int checkIn(String code) {
    	if (conn == null) return -1;

        int diners = 0;
        int orderId = 0;
        int userId = 0;

        // --- PHASE 1: Check the Reservation Code ---
        String orderQuery = "SELECT order_number, user_id, num_of_diners FROM orders WHERE confirmation_code = ? AND status = 'PENDING'";
        
        try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orderId = rs.getInt("order_number");
                    userId = rs.getInt("user_id");
                    diners = rs.getInt("num_of_diners");
                } else {
                    return -2; // Error Code -2: Invalid Reservation
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // DB Error
        }

        // --- PHASE 2: Find a Suitable Table ---
        // We look for an AVAILABLE table that has enough seats (seats >= diners)
        // We order by seats ASC to find the "best fit" (smallest suitable table)
        int assignedTableId = -1;
        String tableQuery = "SELECT table_id FROM restaurant_tables WHERE status = 'AVAILABLE' AND seats >= ? ORDER BY seats ASC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(tableQuery)) {
            ps.setInt(1, diners);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    assignedTableId = rs.getInt("table_id");
                } else {
                    System.out.println("Check-in failed: No suitable table available.");
                    return -3; // Error Code -3: No Table Available
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        // --- PHASE 3: Update Statuses (Commit the Check-in) ---
        if (assignedTableId != -1) {
            try {
                // 1. Mark table as OCCUPIED
                String updateTable = "UPDATE restaurant_tables SET status = 'OCCUPIED', user_id = ? WHERE table_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTable)) {
                	ps.setInt(1, userId);          
                    ps.setInt(2, assignedTableId);
                    ps.executeUpdate();
                }

                // 2. Mark reservation as ACTIVE
                String updateOrder = "UPDATE orders SET status = 'ACTIVE', actual_arrival_time = ? WHERE order_number = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
                	ps.setTime(1, new Time(System.currentTimeMillis()));
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }
                                               
                return assignedTableId; // SUCCESS: Return the table number
                
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }

        return -1;
    }
    

    // ========================
    // TABLE MANAGEMENT LOGIC
    // ========================

    public ArrayList<Table> getAllTables() {
        ArrayList<Table> tables = new ArrayList<>();
        if (conn == null) return tables;

        String query = "SELECT * FROM restaurant_tables";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                tables.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("seats"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public boolean updateTable(Table table) {
        if (conn == null) return false;
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
 // ========================
    // SCHEDULE / OPENING HOURS LOGIC
    // ========================

    // 1. Get All Schedule Items
    public ArrayList<BistroSchedule> getSchedule() {
        ArrayList<BistroSchedule> list = new ArrayList<>();
        if (conn == null) return list;

        try {
            
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM schedule");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new BistroSchedule(
                    rs.getString("identifier"),
                    rs.getString("open_time"),
                    rs.getString("close_time"),
                    rs.getBoolean("is_closed"),
                    rs.getString("schedule_type"),
                    rs.getString("event_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Save Item (Update if exists, Insert if new)

    public boolean saveScheduleItem(BistroSchedule item) {
        if (conn == null) return false;

        try {
            // "REPLACE INTO" is required to overwrite the existing Monday/Tuesday/etc rows
            String query = "REPLACE INTO schedule (identifier, open_time, close_time, is_closed, schedule_type, event_name) VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, item.getIdentifier());
            ps.setString(2, item.getOpenTime());
            ps.setString(3, item.getCloseTime());
            ps.setBoolean(4, item.isClosed());
            ps.setString(5, item.getType());
            ps.setString(6, item.getEventName());
            
            int rows = ps.executeUpdate();
            System.out.println("Server Log: Saved schedule for " + item.getIdentifier() + ". Rows affected: " + rows);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Delete Item (For removing special dates)
    public boolean deleteScheduleItem(String identifier) {
        if (conn == null) return false;

        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM schedule WHERE identifier = ?");
            ps.setString(1, identifier);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    
}