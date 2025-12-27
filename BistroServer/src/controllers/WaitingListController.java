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
                    rs.getString("status")
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

        String query = "INSERT INTO waiting_list (user_id, date_requested, time_requested, num_of_diners, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, wlData.getUserId()); // The phone number acts as username for casuals
            ps.setDate(2, wlData.getDateRequested());
            ps.setTime(3, wlData.getTimeRequested());
            ps.setInt(4, wlData.getNumOfDiners());
            ps.setString(5, wlData.getStatus());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Error adding to waiting list: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Future methods: addToWaitingList, removeFromWaitingList can be added here.
}