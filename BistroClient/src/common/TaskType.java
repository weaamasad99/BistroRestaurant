package common;

/**
 * Enum defining the types of tasks/commands sent between Client and Server.
 */
public enum TaskType {
    // --- Orders---
    GET_ORDERS,
    UPDATE_ORDER,
    ORDERS_IMPORTED,
    UPDATE_SUCCESS,
    UPDATE_FAILED,
    ERROR,

    LOGIN_REQUEST,      // Client sends User object (username/userid)
    LOGIN_RESPONSE,     // Server sends User object (with ID/Type) or null
    LOGOUT,


    // --- Reservation Process ---
    REQUEST_RESERVATION,    // Client sends Order object (Date, Time, Guests)
    RESERVATION_CONFIRMED,  // Server returns Order with Confirmation Code
    RESERVATION_REJECTED,   // Server indicates no availability

    // --- Waiting List Process ---
    ENTER_WAITING_LIST,     // Client sends WaitingListEntry
    WAITING_LIST_ADDED,     // Server confirms addition to list
    EXIT_WAITING_LIST,      // Client requests removal

    // --- Identification / Check-In ---
    CHECK_IN_CUSTOMER,      // Client sends Confirmation Code
    CHECK_IN_APPROVED,      // Server assigns table
    CHECK_IN_DENIED,        // Reservation not found or too early

    // --- History & Data ---
    GET_USER_HISTORY,       // Client requests history for a User ID
    HISTORY_IMPORTED,        // Server returns ArrayList<Order>
    
    GET_TABLES,         // Request all tables
    UPDATE_TABLE,       // Update seats/status
    UPDATE_TABLE_STATUS,// Specific status update
    ADD_TABLE,          
    REMOVE_TABLE,  
    
    GET_WAITING_LIST,
    ADD_TO_WAITING_LIST,
    REMOVE_FROM_WAITING_LIST,
    
    GET_ALL_SUBSCRIBERS, // Request list of all subscribers
    REGISTER_USER,       // Register a new Subscriber/Casual
    CHECK_USER_EXISTS, 
}