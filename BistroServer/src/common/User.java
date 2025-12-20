package common;

import java.io.Serializable;

/**
 * Represents a User in the system (either a Subscriber or a Casual customer).
 * Matches the 'users' table in the database.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Database Fields
    private int userId;             // Internal DB ID (PK)
    private String username;        // For login (Subscribers)
    private int subscriberNumber;   // The visible Subscriber ID (e.g., 1001)
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String userType;        // "SUBSCRIBER" or "CASUAL"

    /**
     * Constructor for Login Requests (Client -> Server).
     */
    public User(String username, int subscriberNumber) {
        this.username = username;
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Full Constructor (Server -> Client).
     * Used when fetching user details from the DB.
     */
    public User(int userId, String username, int subscriberNumber, String firstName, String lastName, String phoneNumber, String email, String userType) {
        this.userId = userId;
        this.username = username;
        this.subscriberNumber = subscriberNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.userType = userType;
    }

    // --- Getters and Setters ---

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getSubscriberNumber() { return subscriberNumber; }
    public void setSubscriberNumber(int subscriberNumber) { this.subscriberNumber = subscriberNumber; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    @Override
    public String toString() {
        return "User: " + firstName + " " + lastName + " [" + userType + "]";
    }
}