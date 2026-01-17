package common;

public enum TaskType {
	// Success or Fail
	SUCCESS,
	FAIL,
	
	// Set User
	SET_USER,
	GET_CASUAL,
	CREATE_CASUAL,
	
    // --- Orders---
    GET_ORDERS,
    GET_ACTIVE_ORDERS,
    UPDATE_ORDER,
    ORDERS_IMPORTED,
    UPDATE_SUCCESS,
    UPDATE_FAILED,
    CANCEL_ORDER,
    ERROR,

    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    LOGOUT,

    // --- Reservation Process ---
    REQUEST_RESERVATION,
    RESERVATION_CONFIRMED,
    RESERVATION_REJECTED,

    // --- Waiting List ---
    ENTER_WAITING_LIST,
    WAITING_LIST_ADDED,
    EXIT_WAITING_LIST,

    // --- Identification / Check-In ---
    CHECK_IN_CUSTOMER,
    CHECK_IN_APPROVED,
    CHECK_IN_DENIED,
    
    // --- NEW: Smart Check-In (Get list for dropdown) ---
    GET_DAILY_ORDERS,      // Client -> Server (String ID)
    DAILY_ORDERS_RESULT,   // Server -> Client (ArrayList<Order>)

    GET_BILL,
    PAY_BILL,

    // --- History & Data ---
    GET_USER_HISTORY,
    HISTORY_IMPORTED,
    
    GET_TABLES,
    UPDATE_TABLE,
    UPDATE_TABLE_STATUS,
    ADD_TABLE,
    REMOVE_TABLE,
    
    GET_WAITING_LIST,
    ADD_TO_WAITING_LIST,
    REMOVE_FROM_WAITING_LIST,
    
    GET_ALL_SUBSCRIBERS,
    REGISTER_USER,
    CHECK_USER_EXISTS,
    GET_SCHEDULE,
    SAVE_SCHEDULE_ITEM,
    DELETE_SCHEDULE_ITEM,
    
    GET_MONTHLY_REPORT,
    REPORT_GENERATED,
    REGISTRATION_SUCCESS,
    
    USER_FOUND,
    USER_NOT_FOUND,
    RESEND_CODE,
    UPDATE_SUBSCRIBER
}