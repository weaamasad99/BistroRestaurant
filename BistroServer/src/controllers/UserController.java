package controllers;

import JDBC.DatabaseConnection;
import common.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages user-related database operations including login (DB & Hardcoded) and registration.
 * Handles authentication for Subscribers, Staff, and Casual users.
 * @author Group 6
 * @version 1.0
 */
public class UserController {

    private Connection conn;

    /**
     * Initializes the controller with a database connection.
     */
    public UserController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Retrieves Email by Phone OR Subscriber ID (Identifier).
     * Used for recovering accounts or notifications.
     * @param identifier Phone or Subscriber ID.
     * @return Email address string.
     */
    public String getEmailByIdentifier(String identifier) {
        if (conn == null) return null;
        
        int subId = -1;
        try {
            subId = Integer.parseInt(identifier);
        } catch (NumberFormatException e) {
            subId = -1;
        }

        String query = "SELECT email FROM users WHERE phone_number = ? OR email = ? OR subscriber_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setInt(3, subId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    if (email != null && email.contains("@") && !email.equals("no-email")) {
                        return email;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Fetches a full User object by their internal ID.
     * @param userId Unique database ID.
     * @return User object.
     */
    public User getUserById(int userId) {
        if (conn == null) return null;
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Assuming you have a helper or constructor to map ResultSet to User
                    // For now, mapping manually based on your previous code structure:
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
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Authenticates a user.
     * Logic:
     * 1. CHECKS HARDCODED ACCOUNTS (Admin/Rep).
     * 2. IF SUBSCRIBER: Checks if 'username' exists AND 'subscriber_number' matches the 'password' input (parsed as int).
     * 3. IF OTHER DB USERS: Standard password check (future proofing).
     * @param username The username input.
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
        // Rep Login We have same password 
        if (credentials.equals("1234")) { 
            switch (username) {
                case "rep":
                    System.out.println("Log: Rep logged in.");
                    return new User(2, "N/A", "rep@bistro.com", "Staff", "Member", "REPRESENTATIVE", null, "rep", "1234");
                
                case "rep1":
                    System.out.println("Log: Rep1 logged in.");
                    return new User(3, "N/A", "rep1@bistro.com", "Staff", "One", "REPRESENTATIVE", null, "rep1", "1234");
                
                case "rep2":
                    System.out.println("Log: Rep2 logged in.");
                    return new User(4, "N/A", "rep2@bistro.com", "Staff", "Two", "REPRESENTATIVE", null, "rep2", "1234");
                
                case "rep3":
                    System.out.println("Log: Rep3 logged in.");
                    return new User(5, "N/A", "rep3@bistro.com", "Staff", "Three", "REPRESENTATIVE", null, "rep3", "1234");
                
                case "rep4":
                    System.out.println("Log: Rep4 logged in.");
                    return new User(6, "N/A", "rep4@bistro.com", "Staff", "Four", "REPRESENTATIVE", null, "rep4", "1234");
                    
                default:
                    // Password was 1234, but username wasn't one of the reps
                    return null;
            }
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
     * Helper: Get the email address for a user based on phone OR email input.
     * Used for notifications when the user identifies by phone.
     * @param contactInfo The phone number or email to search for.
     * @return The email string if found, otherwise null.
     */
    public String getEmailByContact(String contactInfo) {
        if (conn == null) return null;
        
        String query = "SELECT email FROM users WHERE phone_number = ? OR email = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, contactInfo);
            ps.setString(2, contactInfo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    // Ensure we don't return "no-email" (dummy value for casuals)
                    if (email != null && email.contains("@")) {
                        return email;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching email: " + e.getMessage());
        }
        return null; // No valid email found
    }
    
    /**
     * Checks if a subscriber exists by their Subscriber Number.
     * Used for the dashboard validation check.
     * @param subscriberIdString The ID entered by the Representative (as a String).
     * @return The User object if found, null otherwise.
     */
    public User getSubscriber(String subscriberIdString) {
        if (conn == null) return null;
        User user = null;

        try {
            // 1. Parse string to int (since database column is likely INT)
            int subId = Integer.parseInt(subscriberIdString);

            // 2. Query the users table
            String query = "SELECT * FROM users WHERE subscriber_number = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, subId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // 3. Use your existing helper method to map the result
                        user = mapResultSetToUser(rs);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // This handles cases where the Rep types "abc" instead of numbers
            System.out.println("Log: Validation failed - ID is not a number: " + subscriberIdString);
        } catch (SQLException e) {
            System.err.println("SQL Error in getSubscriber: " + e.getMessage());
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Retrieves a user by phone number.
     * @param phone Phone number string.
     * @return User object.
     */
    public User getUserByPhone(String phone) {
        User user = null;
        
        try {
            String searchSQL = "SELECT * FROM users WHERE phone_number = ?";
            PreparedStatement searchStmt = conn.prepareStatement(searchSQL);
            searchStmt.setString(1, phone);
            ResultSet rs = searchStmt.executeQuery();

            // FIX: Change 'rs.next();' to 'if (rs.next())'
            if (rs.next()) { 
                user = new User(
                    rs.getInt("user_id"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("user_type"),
                    rs.getInt("subscriber_number"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getUserByPhone: " + e.getMessage());
            e.printStackTrace();
        }
        
        return user;
    }
    
    
    
    /**
     * Registers a temporary/casual user. 
     * Handles the case where the user ALREADY exists (treats it as a login).
     * @param phone The phone number of the customer.
     * @param email The email of the customer.
     * @return true if successful (either registered OR already exists).
     */
    public boolean createCasualRecord(String phone, String email) {
        if (conn == null) return false;

        // --- STEP 1: Check if this phone number already exists ---
        String checkQuery = "SELECT * FROM users WHERE phone_number = ?";
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, phone);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // User found! This is a successful "Login" for a casual diner.
                    System.out.println("Log: Casual user " + phone + " already exists. proceeding.");
                    return true; 
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking for existing user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // --- STEP 2: If not found, INSERT a new record ---
        // Note: We set 'username' to the phone number to ensure it satisfies any NOT NULL constraints on username.
        String insertQuery = "INSERT INTO users (phone_number, username, user_type, password, first_name, last_name, email) " +
                             "VALUES (?, ?, 'CASUAL', 'casual', 'Guest', 'Diner', ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, phone); // phone_number
            stmt.setString(2, phone); // username (using phone as username for casuals)
            stmt.setString(3, email);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error creating casual user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generates a unique 4-digit confirmation code.
     * Ensures code uniqueness by checking database.
     * @return Unique 4-digit code.
     */
    public String generateConfirmationCode() {
    	String code;
        boolean isUnique = false;
        
        // Loop until we find a code that doesn't exist in the DB
        do {
            code = String.format("%04d", new java.util.Random().nextInt(10000));
            
            // Check database for existence
            // Adjust "orders" to your actual table name if different (e.g., "reservations")
            String query = "SELECT confirmation_code FROM orders WHERE confirmation_code = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    // If rs.next() is false, the code is not found -> it is unique
                    if (!rs.next()) {
                        isUnique = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // In case of error, break to avoid infinite loop (or handle appropriately)
                break; 
            }
        } while (!isUnique);
        
        return code;
    }
    /**
     * Helper method to map a SQL ResultSet row to a User object.
     * @param rs The ResultSet from the query.
     * @return A User object populated with data.
     * @throws SQLException If a database access error occurs.
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
    
 
    /**
     * Registers a new subscriber in the DB.
     * Generates a unique 6-digit Subscriber Number.
     * @param user User object containing registration details.
     * @return The registered User object with generated ID.
     */
    public User registerNewSubscriber(User user) {
        if (conn == null) return null;

        // 1. Generate a random 6-digit subscriber number (100000 - 999999)
        int subNum = 100000 + new java.util.Random().nextInt(900000);

        // 2. Insert into Database
        String query = "INSERT INTO users (first_name, last_name, phone_number, email, username, password, user_type, subscriber_number) " +
                       "VALUES (?, ?, ?, ?, ?, ?, 'SUBSCRIBER', ?)";

        try (PreparedStatement ps = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getPhoneNumber());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getUsername());
            ps.setString(6, user.getUsername()); // Default password = username (Requirement usually simpler for prototype)
            ps.setInt(7, subNum);

            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                // 3. Update the User object with the new IDs
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1)); // The Auto-Increment DB ID
                    }
                }
                user.setSubscriberNumber(subNum);
                user.setUserType("SUBSCRIBER");
                
                System.out.println("Log: Registered new subscriber: " + user.getUsername() + " ID: " + subNum);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Registration Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
}