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
    
    public boolean addToWaitingList(WaitingList wlData) {
        if (conn == null) return false;

        String query = "INSERT INTO waiting_list (user_id, date_requested, time_requested, num_of_diners, status, confirmation_code) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        UserController userController = new UserController();
        String code = userController.generateConfirmationCode();
        wlData.setCode(code);
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, wlData.getUserId());
            ps.setDate(2, wlData.getDateRequested());
            ps.setTime(3, wlData.getTimeRequested());
            ps.setInt(4, wlData.getNumOfDiners());
            ps.setString(5, wlData.getStatus());
            ps.setString(6, wlData.getCode());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Error adding to waiting list: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    
    public void notifyNextInLine(int vacatedTableSeats) {
        // 1. Find the person who fits AND has the largest group (High utilization logic)
        // If two groups have the same size, the one who waited longer (smaller ID) goes first.
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
                String code = rs.getString("confirmation_code");

                // 2. Update status to 'NOTIFIED'
                String updateStatus = "UPDATE waiting_list SET status = 'NOTIFIED' WHERE waiting_id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateStatus)) {
                    psUpdate.setInt(1, waitingId);
                    psUpdate.executeUpdate();
                }
                
                // 3. Simulation: Send Notification 
                // We print to console to simulate the SMS/Email requirement
                System.out.println("SIMULATION: Notification sent to User #" + userId + 
                                   ". Table (Size " + vacatedTableSeats + ") is ready for your party of " + 
                                   rs.getInt("num_of_diners") + "!");
                System.out.println("DEBUG: Use Confirmation Code: " + code);
            } else {
                System.out.println("DEBUG: Table vacated, but no one in waiting list fits seats: " + vacatedTableSeats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}