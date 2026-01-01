package controllers;

import JDBC.DatabaseConnection;
import common.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manages operations specific to Subscribers.
 */
public class SubscriberController {

    private Connection conn;

    public SubscriberController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Fetches all users who have the role 'SUBSCRIBER'.
     * @return ArrayList of User objects.
     */
    public ArrayList<User> getAllSubscribers() {
        ArrayList<User> subscribers = new ArrayList<>();
        if (conn == null) return subscribers;

        String query = "SELECT * FROM users WHERE user_type = 'SUBSCRIBER'";
        
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Mapping logic duplicated here or could be a static utility in a helper class
                int subNum = rs.getInt("subscriber_number");
                Integer subNumObj = rs.wasNull() ? null : subNum;

                User user = new User(
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
                subscribers.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscribers: " + e.getMessage());
            e.printStackTrace();
        }
        return subscribers;
    }
    
    
    public boolean updateSubscriberDetails(User user) {
        if (conn == null) return false;

        String query = "UPDATE users SET phone_number = ?, email = ? WHERE user_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, user.getPhoneNumber());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getUserId()); // Use the internal DB ID for safety
            
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subscriber: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}