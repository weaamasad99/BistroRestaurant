package common;

/**
 * Enumeration defining all possible network tasks and commands used in the system.
 * This is used to route messages between the Client and Server.
 * * @author Group 6
 * @version 1.0
 */
public enum TaskType {
	// --- General Status ---
	/** Indicates a successful operation. */
	SUCCESS,
	/** Indicates a failed operation. */
	FAIL,
	
	// --- User Management ---
	/** Set the current user context. */
	SET_USER,
	/** Retrieve casual user data. */
	GET_CASUAL,
	/** Create a new casual user profile. */
	CREATE_CASUAL,
	
    // --- Order Management ---
    /** Retrieve a list of orders. */
    GET_ORDERS,
    /** Retrieve only active orders. */
    GET_ACTIVE_ORDERS,
    /** Update an existing order's status or details. */
    UPDATE_ORDER,
    /** Indicates orders were successfully imported. */
    ORDERS_IMPORTED,
    /** General update success signal. */
    UPDATE_SUCCESS,
    /** General update failure signal. */
    UPDATE_FAILED,
    /** Cancel a specific order. */
    CANCEL_ORDER,
    /** General error signal. */
    ERROR,

    // --- Authentication ---
    /** Request to log in. */
    LOGIN_REQUEST,
    /** Response to a login request. */
    LOGIN_RESPONSE,
    /** Request to log out. */
    LOGOUT,

    // --- Reservation Process ---
    /** Submit a new reservation. */
    REQUEST_RESERVATION,
    /** Confirm a reservation. */
    RESERVATION_CONFIRMED,
    /** Reject a reservation. */
    RESERVATION_REJECTED,

    // --- Waiting List ---
    /** Add user to waiting list. */
    ENTER_WAITING_LIST,
    /** Confirm user added to waiting list. */
    WAITING_LIST_ADDED,
    /** Remove user from waiting list. */
    EXIT_WAITING_LIST,

    // --- Identification / Check-In ---
    /** Validate customer arrival. */
    CHECK_IN_CUSTOMER,
    /** Check-in successfully approved. */
    CHECK_IN_APPROVED,
    /** Check-in denied. */
    CHECK_IN_DENIED,
    
    // --- Smart Check-In ---
    /** Request daily orders (Client -> Server). */
    GET_DAILY_ORDERS,      
    /** Return daily orders list (Server -> Client). */
    DAILY_ORDERS_RESULT,   

    // --- Billing ---
    /** Request bill details. */
    GET_BILL,
    /** Process payment for a bill. */
    PAY_BILL,

    // --- History & Data ---
    /** Retrieve user history. */
    GET_USER_HISTORY,
    /** History data imported successfully. */
    HISTORY_IMPORTED,
    
    // --- Table Management ---
    /** Get all tables. */
    GET_TABLES,
    /** Update table details. */
    UPDATE_TABLE,
    /** Update just the status of a table. */
    UPDATE_TABLE_STATUS,
    /** Add a new table to the layout. */
    ADD_TABLE,
    /** Remove a table from the layout. */
    REMOVE_TABLE,
    
    // --- Admin / Lists ---
    /** Retrieve the current waiting list. */
    GET_WAITING_LIST,
    /** Add entry to waiting list (Admin). */
    ADD_TO_WAITING_LIST,
    /** Remove entry from waiting list (Admin). */
    REMOVE_FROM_WAITING_LIST,
    
    /** Get all registered subscribers. */
    GET_ALL_SUBSCRIBERS,
    /** Register a new subscriber. */
    REGISTER_USER,
    /** Check if a user already exists. */
    CHECK_USER_EXISTS,
    
    // --- Schedule ---
    /** Get the operating schedule. */
    GET_SCHEDULE,
    /** Save a schedule item. */
    SAVE_SCHEDULE_ITEM,
    /** Delete a schedule item. */
    DELETE_SCHEDULE_ITEM,
    
    // --- Reports ---
    /** Request a monthly report. */
    GET_MONTHLY_REPORT,
    /** Report generation complete. */
    REPORT_GENERATED,
    
    // --- Registration/Misc ---
    /** Registration successful signal. */
    REGISTRATION_SUCCESS,
    /** User found in database. */
    USER_FOUND,
    /** User not found in database. */
    USER_NOT_FOUND,
    /** Resend a verification code. */
    RESEND_CODE,
    /** Update subscriber details. */
    UPDATE_SUBSCRIBER
}