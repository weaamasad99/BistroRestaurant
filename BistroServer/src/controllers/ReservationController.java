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
import java.time.LocalDate;
import java.time.LocalTime;
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
     * FIXED LOGIC: Checks for future overlaps to prevent overbooking.
     */
    private boolean checkRestaurantCapacity(Date date, Time reqTime, int requestedSeats) {
        if (conn == null) return false;

        // --- Helper Class for this calculation ---
        class BookingInfo {
            Time time;
            int diners;
            public BookingInfo(Time t, int d) { this.time = t; this.diners = d; }
        }

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

        if (totalCapacity == 0) return false;

        // 2. Fetch ALL overlapping reservations
        // We look for any order that overlaps with our requested 2-hour window.
        ArrayList<BookingInfo> overlaps = new ArrayList<>();
        
        String overlapQuery = "SELECT order_time, num_of_diners FROM orders " +
                              "WHERE order_date = ? " +
                              "AND status IN ('APPROVED', 'ACTIVE') " + 
                              // Logic: Order Start < Our End (req+2h) AND Order End (start+2h) > Our Start
                              "AND order_time < ADDTIME(?, '02:00:00') " +
                              "AND order_time > SUBTIME(?, '02:00:00')";

        try (PreparedStatement ps = conn.prepareStatement(overlapQuery)) {
            ps.setDate(1, date);
            ps.setTime(2, reqTime); 
            ps.setTime(3, reqTime); 
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    overlaps.add(new BookingInfo(
                        rs.getTime("order_time"), 
                        rs.getInt("num_of_diners")
                    ));
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }

        // 3. Calculate Peak Load
        // We must ensure that at no point during the meal does the total diners exceed capacity.
        
        // Convert to minutes for easier comparison
        long reqStartMin = (reqTime.getTime() / 60000) % (24 * 60); 
        long reqEndMin = reqStartMin + 120; // Dining duration is 120 mins

        // Identify critical check points: Our start time + the start time of any overlapping order
        ArrayList<Long> checkPoints = new ArrayList<>();
        checkPoints.add(reqStartMin);

        for (BookingInfo b : overlaps) {
            long oStart = (b.time.getTime() / 60000) % (24 * 60);
            // Only care about start times that happen during our meal
            if (oStart > reqStartMin && oStart < reqEndMin) {
                checkPoints.add(oStart);
            }
        }

        // Check the total load at each critical time point
        for (Long point : checkPoints) {
            int currentLoad = 0;
            for (BookingInfo b : overlaps) {
                long oStart = (b.time.getTime() / 60000) % (24 * 60);
                long oEnd = oStart + 120;
                
                // If this other order is active at this specific minute, count them
                if (point >= oStart && point < oEnd) {
                    currentLoad += b.diners;
                }
            }

            // If adding us pushes the load over capacity at this specific minute -> Fail
            if (currentLoad + requestedSeats > totalCapacity) {
                System.out.println("Capacity Check Failed. Peak Load: " + (currentLoad + requestedSeats) + "/" + totalCapacity);
                return false; 
            }
        }

        return true; 
    }
    
    public String createReservation(Order order) {
        UserController userController = new UserController();
        try {
            int userID = order.getUserId();
            Date sqlDate = Date.valueOf(order.getOrderDate().toString());
            Time sqlTime = order.getOrderTime();

            // --- 1. CAPACITY CHECK ---
            if (!checkRestaurantCapacity(sqlDate, sqlTime, order.getNumberOfDiners())) {
                // IT IS FULL. Find alternatives.
                String alternatives = getAlternativeTimes(sqlDate, sqlTime, order.getNumberOfDiners());
                
                if (alternatives.isEmpty()) {
                    return "Full: No tables available around this time.";
                } else {
                    // Return special protocol: "SUGGEST:18:30,19:30"
                    return "SUGGEST:" + alternatives;
                }
            }

            // --- 2. DUPLICATE CHECK ---
            if (checkIfReservationExists(userID, sqlDate, sqlTime)) {
                return "Duplicate: You already have a reservation at this time.";
            }

            // --- 3. PROCEED TO BOOK ---
            String insertSQL = "INSERT INTO orders (user_id, order_date, order_time, num_of_diners, status, confirmation_code) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            String code = userController.generateConfirmationCode();
            
            ps.setInt(1, userID);
            ps.setDate(2, sqlDate);
            ps.setTime(3, sqlTime);
            ps.setInt(4, order.getNumberOfDiners());
            ps.setString(5, "APPROVED");
            ps.setString(6, code);
            ps.executeUpdate();
            
            order.setConfirmationCode(code);
            return "OK:" + code;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database error.";
        }
    }
    
    public boolean cancelOrder(String code,int userId) {
    	String sql = "UPDATE orders SET status = 'CANCELLED' WHERE confirmation_code = ? AND user_id = ? AND status != 'CANCELLED'";
    	
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        boolean ifWaitingList = true;
        Date date;
        String status = null;

        // --- PHASE 1: Check the Reservation Code ---
        String orderQuery = "SELECT order_number, user_id, num_of_diners FROM orders WHERE confirmation_code = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orderId = rs.getInt("order_number");
                    userId = rs.getInt("user_id");
                    diners = rs.getInt("num_of_diners");
                    ifWaitingList = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // DB Error
        }
        
        if (ifWaitingList) {
	        orderQuery = "SELECT waiting_id, user_id, num_of_diners FROM waiting_list WHERE confirmation_code = ? AND status = 'WAITING'";
	        
	        try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
	            ps.setString(1, code);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    orderId = rs.getInt("waiting_id");
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
        }
        
        // --- PHASE 2: Check if customer arrived at the right date ---
        
        orderQuery = "SELECT order_date FROM orders WHERE order_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();                  
                date = rs.getDate("order_date");
                
                if (!Date.valueOf(LocalDate.now()).equals(date)) {
                    return -3; // Error Code -3: Not the day of the reservation
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // DB Error
        }
        
        // --- PHASE 2: Check if customer reservation is canceled ---
        
        orderQuery = "SELECT status FROM orders WHERE order_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())                  
                status = rs.getString("status");
                
                if (status.equals("CANCELLED")) {
                    return -4; // Error Code -4: Customer reservation is canceled
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // DB Error
        }

        // --- PHASE 3: Find a Suitable Table ---
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
                    return -5; // Error Code -5: No Table Available
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        // --- PHASE 4: Update Statuses (Commit the Check-in) ---
        if (assignedTableId != -1) {
            try {
                // 1. Mark table as OCCUPIED
                String updateTable = "UPDATE restaurant_tables SET status = 'OCCUPIED', user_id = ? WHERE table_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTable)) {
                	ps.setInt(1, userId);          
                    ps.setInt(2, assignedTableId);
                    ps.executeUpdate();
                }
                
                String update;
                
                if (ifWaitingList) {
                	update = "UPDATE waiting_list SET user_id = NULL, status = 'FULFILLED' WHERE waiting_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(update)) {
                        ps.setInt(1, orderId);
                        ps.executeUpdate();
                    }
                    
                    String insertSQL = "INSERT INTO orders (user_id, order_date, order_time, num_of_diners, status, confirmation_code, actual_arrival_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
                    try(PreparedStatement ps = conn.prepareStatement(insertSQL)) {                    
	                    ps.setInt(1, userId);
	                    ps.setDate(2, Date.valueOf(LocalDate.now()));
	                    ps.setTime(3, Time.valueOf(LocalTime.now()));
	                    ps.setInt(4, diners);
	                    ps.setString(5, "ACTIVE");
	                    ps.setString(6, code);
	                    ps.setTime(7, new Time(System.currentTimeMillis()));
	                    ps.executeUpdate();
                    }
                    
                }
                else {
                	update = "UPDATE orders SET status = 'ACTIVE', actual_arrival_time = ? WHERE order_number = ?";
                    try (PreparedStatement ps = conn.prepareStatement(update)) {
                    	ps.setTime(1, new Time(System.currentTimeMillis()));
                        ps.setInt(2, orderId);
                        ps.executeUpdate();
                    }
                }
                
                                               
                return assignedTableId; // SUCCESS: Return the table number
                
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }

        return -1;
    }
    
    
    
 

    private String getAlternativeTimes(Date date, Time targetTime, int diners) {
        StringBuilder suggestions = new StringBuilder();
        int[] offsets = {-30, 30, -60, 60}; 

        // Default: Open 24/7 if no schedule found (failsafe)
        Time openTime = Time.valueOf("00:00:00");
        Time closeTime = Time.valueOf("23:59:59");
        boolean isClosed = false;
        boolean scheduleFound = false;

        // --- STEP A: Check for SPECIAL DATE first (e.g., "2026-01-02") ---
        String dateId = date.toString(); 
        String query = "SELECT open_time, close_time, is_closed FROM schedule WHERE identifier = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, dateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isClosed = rs.getBoolean("is_closed");
                    openTime = parseTimeSafe(rs.getString("open_time"));   // <--- FIXED
                    closeTime = parseTimeSafe(rs.getString("close_time")); // <--- FIXED
                    scheduleFound = true;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // --- STEP B: If no special date, check WEEKDAY (e.g., "Friday") ---
        if (!scheduleFound) {
            int dayInt = getDayOfWeek(date); 
            String dayName = getDayName(dayInt); 
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, dayName); 
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        isClosed = rs.getBoolean("is_closed");
                        openTime = parseTimeSafe(rs.getString("open_time"));   // <--- FIXED
                        closeTime = parseTimeSafe(rs.getString("close_time")); // <--- FIXED
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        if (isClosed) return ""; // Closed for the day

        // --- STEP C: Calculate Suggestions ---
        for (int offset : offsets) {
            Time altTime = addMinutesToTime(targetTime, offset);

            // 1. Check Opening Hours
            if (altTime.before(openTime) || altTime.after(closeTime)) {
                continue; 
            }

            // 2. Check Capacity
            if (checkRestaurantCapacity(date, altTime, diners)) {
                if (suggestions.length() > 0) suggestions.append(",");
                suggestions.append(altTime.toString().substring(0, 5));
            }
        }
        return suggestions.toString();
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
    
    
    // --- HELPER METHODS ---
    private Time parseTimeSafe(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return Time.valueOf("00:00:00");
        
        // If DB has "08:00", append ":00" to make it "08:00:00" for Java
        if (timeStr.length() == 5) {
            timeStr += ":00";
        }
        try {
            return Time.valueOf(timeStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing time: " + timeStr + ". Defaulting to 00:00:00");
            return Time.valueOf("00:00:00");
        }
    }
    
    
    // 1. Add this helper to convert numbers to Names (matches your DB)
    private String getDayName(int dayId) {
        String[] days = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        if (dayId >= 1 && dayId <= 7) {
            return days[dayId];
        }
        return "";
    }
    
    // Helper to get day of week (1=Sunday, ..., 7=Saturday)
    private int getDayOfWeek(Date date) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        return c.get(java.util.Calendar.DAY_OF_WEEK);
    }
    
    
    // Helper method
    private Time addMinutesToTime(Time time, int minutesToAdd) {
        long millis = time.getTime();
        long extraMillis = minutesToAdd * 60 * 1000;
        return new Time(millis + extraMillis);
    }

	public String cancelReservation(String code) {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}