package controllers;

import JDBC.DatabaseConnection;
import common.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages user-related database operations including login (DB & Hardcoded) and registration.
 */
public class UserController {

    private Connection conn;

    public UserController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Authenticates a user.
     * * Logic:
     * 1. CHECKS HARDCODED ACCOUNTS (Admin/Rep).
     * 2. IF SUBSCRIBER: Checks if 'username' exists AND 'subscriber_number' matches the 'password' input (parsed as int).
     * 3. IF OTHER DB USERS: Standard password check (future proofing).
     * * @param username The username input.
     * @param credentials The second input field (Password OR Subscriber ID).
     * @return A User object if successful, null otherwise.
     */
    public User loginUser(String username, String credentials) {
        
        // --- 1. HARDCODED SYSTEM ACCOUNTS (Not in DB) ---
        
        // Manager / Admin (Login: admin / admin)
        if (username.equals("admin") && credentials.equals("admin")) {
            System.out.println("Log: Admin logged in via hardcoded credentials.");
            return new User(1, "N/A", "admin@bistro.com", "System", "Admin", "MANAGER", null, "admin", "admin");
        }

        // Restaurant Representative (Login: rep / 1234)
        if (username.equals("rep") && credentials.equals("1234")) {
            System.out.println("Log: Representative logged in via hardcoded credentials.");
            return new User(2, "N/A", "rep@bistro.com", "Staff", "Member", "REPRESENTATIVE", null, "rep", "1234");
        }

        // --- 2. DATABASE AUTHENTICATION (Subscribers) ---
        
        if (conn == null) {
            System.err.println("Error: No DB Connection.");
            return null;
        }
        
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbUserType = rs.getString("user_type");
                    int dbSubscriberId = rs.getInt("subscriber_number");
                    
                    // --- CASE A: SUBSCRIBER LOGIN ---
                    // For subscribers, the "credentials" string is actually their ID (int)
                    if ("SUBSCRIBER".equalsIgnoreCase(dbUserType)) {
                        try {
                            int inputId = Integer.parseInt(credentials); // Parse input to int
                            
                            if (inputId == dbSubscriberId) {
                                return mapResultSetToUser(rs);
                            } else {
                                System.out.println("Login Failed: Subscriber ID does not match.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Login Failed: Subscriber ID input must be a number.");
                        }
                    } 
                    
                    // --- CASE B: FUTURE DB USERS (With Passwords) ---
                    // If we eventually move Admin/Rep to the DB, they will use passwords
                    else {
                        String dbPassword = rs.getString("password");
                        if (credentials.equals(dbPassword)) {
                            return mapResultSetToUser(rs);
                        }
                    }
                } else {
                    System.out.println("Login Failed: Username not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during login query: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a temporary/casual user (e.g., someone walking into the restaurant).
     * @param phone The phone number of the customer.
     * @return true if successful.
     */
    public boolean createCasualRecord(String phone) {
        if (conn == null) return false;
        String query = "INSERT INTO users (phone_number, user_type) VALUES (?, 'CASUAL')";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phone);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error creating casual user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to map a SQL ResultSet row to a User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
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
}