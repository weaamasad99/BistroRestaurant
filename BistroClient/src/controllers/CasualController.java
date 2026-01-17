package controllers;

import common.Message;
import common.Order;
import common.TaskType;
import common.User;
import common.WaitingList;

/**
 * The CasualController class handles client-side logic for casual diners (walk-ins) 
 * and general customer operations.
 * <p>
 * It acts as an intermediary between the Casual/Check-In UIs and the {@link ClientController},
 * sending specific requests (like creating reservations, joining waiting lists, or paying bills)
 * to the server via the network layer.
 */
public class CasualController {
    
    protected ClientController networkController;

    /**
     * Constructs a CasualController instance.
     *
     * @param networkController The main client controller used for server communication.
     */
    public CasualController(ClientController networkController) {
        this.networkController = networkController;
    }
    
    /**
     * Sends a request to create a new casual user profile.
     *
     * @param phone The user's phone number.
     * @param email The user's email address.
     */
    public void createCasualUser(String phone, String email) {
    	Object[] obj = {phone, email};
        networkController.accept(new Message(TaskType.CREATE_CASUAL, obj));
    }
    
    /**
     * Sends a request to resend the order confirmation code to the user's contact info.
     *
     * @param contact The phone number or subscriber ID to send the code to.
     */
    public void recoverLostCode(String contact) {
        // Send the TaskType.RESEND_CODE message to the server
        networkController.accept(new Message(TaskType.RESEND_CODE, contact));
    }

    /**
     * Sets the current active user in the main client controller based on the phone number.
     * This is typically used after a casual user identifies themselves.
     *
     * @param phone The phone number of the user.
     */
    public void setCurrentUserByPhone(String phone) {
        networkController.accept(new Message(TaskType.SET_USER, phone));
    }

    /**
     * Requests the list of orders scheduled for today associated with a specific user.
     * Used by the Check-In UI to populate the selection list.
     *
     * @param identifier The user's phone number or subscriber ID.
     */
    public void getDailyOrders(String identifier) {
        networkController.accept(new Message(TaskType.GET_DAILY_ORDERS, identifier));
    }

    /**
     * Sends a new reservation request to the server.
     *
     * @param order The Order object containing date, time, and number of diners.
     */
    public void requestReservation(Order order) {
        networkController.accept(new Message(TaskType.REQUEST_RESERVATION, order));
    }
    
    /**
     * Sends a request to cancel an existing reservation.
     *
     * @param code   The order confirmation code.
     * @param userId The ID of the user requesting the cancellation.
     */
    public void cancelReservation(String code, int userId) {
    	Object[] obj = new Object[] {code, userId};
        networkController.accept(new Message(TaskType.CANCEL_ORDER, obj));	
        
	}

    /**
     * Sends a request to add a user to the waiting list.
     *
     * @param entry The WaitingList object containing user details and preferences.
     */
    public void enterWaitingList(WaitingList entry) {
        networkController.accept(new Message(TaskType.ENTER_WAITING_LIST, entry));
    }
    
    /**
     * Sends a request to remove a user from the waiting list.
     *
     * @param userId The ID of the user to remove.
     */
    public void exitWaitingList(int userId) {
        networkController.accept(new Message(TaskType.EXIT_WAITING_LIST, userId));
    }

    /**
     * Sends a check-in request for a customer who has arrived at the restaurant.
     *
     * @param code The order confirmation code provided by the customer.
     */
    public void checkIn(String code) {
        networkController.accept(new Message(TaskType.CHECK_IN_CUSTOMER, code));
    }
    
    /**
     * Requests the bill details for a specific order.
     *
     * @param code The order confirmation code.
     */
    public void getBill(String code) {
        networkController.accept(new Message(TaskType.GET_BILL, code));
    }
    
    /**
     * Sends a payment notification for a specific order.
     *
     * @param code The order confirmation code associated with the payment.
     */
    public void payBill(String code) {
        networkController.accept(new Message(TaskType.PAY_BILL, code));
    }
}