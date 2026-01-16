package controllers;

import JDBC.DatabaseConnection;
import common.WaitingList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manages the Waiting List database operations.
 */
public class WaitingListController {

    private Connection conn;

    public WaitingListController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Retrieves the entire waiting list from the database.
     * @return ArrayList of WaitingList objects.
     */
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
                    rs.getString("status"),
                    rs.getString("confirmation_code")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching waiting list: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    
    /**
     * Tries to add a user to the waiting list, BUT first checks if a table is available immediately.
     * Returns a status string: 
     * - "IMMEDIATE:<TableID>:<Code>" (Table found, assigned immediately)
     * - "WAITING" (Added to waiting list)
     * - "DUPLICATE" (Already in list)
     * - "ERROR" (Database error)
     */
    public String addToWaitingList(WaitingList wlData) {
        if (conn == null) return "ERROR";

        // --- STEP 1: Check if User is already waiting ---
        String checkQuery = "SELECT waiting_id FROM waiting_list WHERE user_id = ? AND status = 'WAITING'";
        try (PreparedStatement psCheck = conn.prepareStatement(checkQuery)) {
            psCheck.setInt(1, wlData.getUserId());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) return "DUPLICATE"; 
            }
        } catch (SQLException e) { return "ERROR"; }

        // --- STEP 2: CHECK FOR AVAILABLE TABLE (Instant Seating Rule) ---
        // Look for a table that fits the group and is currently FREE
        String findTable = "SELECT table_id FROM restaurant_tables " +
                           "WHERE status = 'AVAILABLE' AND seats >= ? " +
                           "ORDER BY seats ASC LIMIT 1"; // 'ASC' to get the smallest fitting table (efficiency)

        try (PreparedStatement psFind = conn.prepareStatement(findTable)) {
            psFind.setInt(1, wlData.getNumOfDiners());
            ResultSet rsTable = psFind.executeQuery();

            if (rsTable.next()) {
                // FOUND A TABLE! Assign it immediately.
                int tableId = rsTable.getInt("table_id");
                UserController userController = new UserController();
                String code = userController.generateConfirmationCode();

                // A. Mark Table as OCCUPIED
                String updateTable = "UPDATE restaurant_tables SET status = 'OCCUPIED', user_id = ? WHERE table_id = ?";
                try (PreparedStatement psUpd = conn.prepareStatement(updateTable)) {
                    psUpd.setInt(1, wlData.getUserId());
                    psUpd.setInt(2, tableId);
                    psUpd.executeUpdate();
                }

                // B. Create an ACTIVE ORDER (So they can pay later)
                String createOrder = "INSERT INTO orders (user_id, order_date, order_time, num_of_diners, status, confirmation_code) " +
                        "VALUES (?, CURDATE(), CURTIME(), ?, 'ACTIVE', ?)";
                try (PreparedStatement psOrd = conn.prepareStatement(createOrder)) {
                    psOrd.setInt(1, wlData.getUserId());
                    psOrd.setInt(2, wlData.getNumOfDiners());
                    psOrd.setString(3, code);
                    psOrd.executeUpdate();
                }
                
                System.out.println("Log: User " + wlData.getUserId() + " skipped waiting list -> Assigned Table " + tableId + ". Code: " + code);
                return "IMMEDIATE:" + tableId + ":" + code;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }
        

        // --- STEP 3: NO TABLE FOUND -> ADD TO WAITING LIST ---
        String insertWait = "INSERT INTO waiting_list (user_id, date_requested, time_requested, num_of_diners, status, confirmation_code) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
        
        UserController userController = new UserController();
        String code = userController.generateConfirmationCode();
        wlData.setCode(code); // Update object with code
        
        try (PreparedStatement ps = conn.prepareStatement(insertWait)) {
            ps.setInt(1, wlData.getUserId());
            ps.setDate(2, wlData.getDateRequested());
            ps.setTime(3, wlData.getTimeRequested());
            ps.setInt(4, wlData.getNumOfDiners());
            ps.setString(5, wlData.getStatus());
            ps.setString(6, code);
            
            ps.executeUpdate();
            return "WAITING"; // Successfully added to list
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
    
    public boolean exitWaitingList(int userId) {
    	String sql = "UPDATE waiting_list SET status = 'CANCELLED' WHERE user_id = ? AND status = 'WAITING'";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            int rowsAffected = ps.executeUpdate();
            
            // returns TRUE if they were in the list (and are now out)
            // returns FALSE if they weren't in the list at all
            return rowsAffected > 0; 
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void notifyNextInLine(int vacatedTableSeats) {
        // 1. Find the best candidate from the waiting list
        String findNext = "SELECT * FROM waiting_list " +
                          "WHERE status = 'WAITING' " +
                          "AND num_of_diners <= ? " + 
                          "ORDER BY num_of_diners DESC, waiting_id ASC LIMIT 1";
                          
        try (PreparedStatement ps = conn.prepareStatement(findNext)) {
            ps.setInt(1, vacatedTableSeats);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int waitingId = rs.getInt("waiting_id");
                int userId = rs.getInt("user_id");
                int diners = rs.getInt("num_of_diners");
                
                // Get the Confirmation Code from DB ---
                String code = rs.getString("confirmation_code"); 

                // A. Find a suitable 'AVAILABLE' table
                int tableToLock = -1;
                String findTableSQL = "SELECT table_id FROM restaurant_tables " +
                                      "WHERE status = 'AVAILABLE' AND seats >= ? " +
                                      "ORDER BY seats ASC LIMIT 1";
                
                try (PreparedStatement psTable = conn.prepareStatement(findTableSQL)) {
                    psTable.setInt(1, diners);
                    ResultSet rsTable = psTable.executeQuery();
                    if (rsTable.next()) {
                        tableToLock = rsTable.getInt("table_id");
                    }
                }
                
                if (tableToLock != -1) {
                    // B. LOCK THE TABLE (Status='RESERVED', User=The Waiting Person)
                    String lockTableSQL = "UPDATE restaurant_tables SET status = 'RESERVED', user_id = ? WHERE table_id = ?";
                    try (PreparedStatement psLock = conn.prepareStatement(lockTableSQL)) {
                        psLock.setInt(1, userId);
                        psLock.setInt(2, tableToLock);
                        psLock.executeUpdate();
                    }

                    // C. Update Waiting List Status
                    String updateStatus = "UPDATE waiting_list SET status = 'NOTIFIED' WHERE waiting_id = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateStatus)) {
                        psUpdate.setInt(1, waitingId);
                        psUpdate.executeUpdate();
                    }

                    // D. Send Notification
                    controllers.NotificationController nc = new controllers.NotificationController();
                    // Now 'code' is defined, so this line works
                    nc.sendWaitingListAlert(userId, code);
                    
                    System.out.println("Log: Table " + tableToLock + " is now RESERVED for User " + userId + " (15 min hold).");
                }
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}