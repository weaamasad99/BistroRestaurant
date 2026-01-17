package common;

import java.io.Serializable;

/**
 * Represents a user in the system (Subscriber, Casual, Manager, or Representative).
 * Matches the 'users' table in the database.
 * @author Group 6
 * @version 1.0
 */
public class User implements Serializable {
    /** Unique user identifier (PK). */
    private int userId;
    
    /** User's phone number. */
    private String phoneNumber;
    
    /** User's email address. */
    private String email;
    
    /** User's first name. */
    private String firstName;
    
    /** User's last name. */
    private String lastName;
    
    /** Type of user: 'CASUAL', 'SUBSCRIBER', 'MANAGER', etc. */
    private String userType; 
    
    /** Username for login (Nullable). */
    private String username;          
    
    /** Password for login (Nullable). */
    private String password;          

    /** Subscriber ID for loyalty members. */
    private Integer subscriberNumber;
    
    /**
     * Default constructor.
     */
    public User() {}

    /**
     * Constructor for basic login requests.
     * @param username The username.
     * @param userId The ID (if known).
     */
    public User(String username, int userId) {
    	this.username = username;
    	this.userId = userId;
    }

    /**
     * Constructor for login verification (Manager/Rep).
     * @param username The username input.
     * @param password The password input.
     */
    public User(String username, String password) {
    	this.username = username;
    	this.password = password;
    }

    /**
     * Full Constructor for User object creation.
     * @param userId Unique ID.
     * @param phoneNumber Phone number.
     * @param email Email address.
     * @param firstName First name.
     * @param lastName Last name.
     * @param userType Role type.
     * @param subscriberNumber Subscriber ID (can be null).
     * @param username Login username.
     * @param password Login password.
     */
    public User(int userId, String phoneNumber, String email, String firstName, String lastName, 
                String userType, Integer subscriberNumber, String username, String password) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.username = username;
        this.password = password;
        this.subscriberNumber = subscriberNumber;
    }

    // --- Getters and Setters ---

    /** @return The user ID. */
    public int getUserId() { return userId; }
    /** @param userId The user ID to set. */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return The phone number. */
    public String getPhoneNumber() { return phoneNumber; }
    /** @param phoneNumber The phone number to set. */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /** @return The email address. */
    public String getEmail() { return email; }
    /** @param email The email to set. */
    public void setEmail(String email) { this.email = email; }

    /** @return The first name. */
    public String getFirstName() { return firstName; }
    /** @param firstName The first name to set. */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return The last name. */
    public String getLastName() { return lastName; }
    /** @param lastName The last name to set. */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return The user role type. */
    public String getUserType() { return userType; }
    /** @param userType The user type to set. */
    public void setUserType(String userType) { this.userType = userType; }
   
    /** @return The username. */
    public String getUsername() { return username; }
    /** @param username The username to set. */
    public void setUsername(String username) { this.username = username; }

    /** @return The password. */
    public String getPassword() { return password; }
    /** @param password The password to set. */
    public void setPassword(String password) { this.password = password; }
    
    /** @return The subscriber number. */
    public Integer getSubscriberNumber() { return subscriberNumber; }
    /** @param subscriberNumber The subscriber number to set. */
    public void setSubscriberNumber(Integer subscriberNumber) { this.subscriberNumber = subscriberNumber; }
}