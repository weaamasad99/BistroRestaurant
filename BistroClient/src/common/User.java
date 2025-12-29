package common;

import java.io.Serializable;

public class User implements Serializable {
    private int userId;
    private String phoneNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String userType; // 'CASUAL' or 'SUBSCRIBER'
    private String username;
    private String password;
    
    // --- MISSING FIELD ADDED HERE ---
    private Integer subscriberNumber; 

    public User() {}

    public User(String username, int userId) { // Login Constructor
        this.username = username;
        this.userId = userId;
    }

    public User(String username, String password) { // Staff Login Constructor
        this.username = username;
        this.password = password;
    }

    // Full Constructor
    public User(int userId, String phoneNumber, String email, String firstName, String lastName, 
                String userType, Integer subscriberNumber, String username, String password) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.subscriberNumber = subscriberNumber; // --- ASSIGNMENT ADDED ---
        this.username = username;
        this.password = password;
    }

    // --- GETTER & SETTER ADDED ---
    public Integer getSubscriberNumber() { return subscriberNumber; }
    public void setSubscriberNumber(Integer subscriberNumber) { this.subscriberNumber = subscriberNumber; }
    // ------
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}