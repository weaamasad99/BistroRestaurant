package controllers;

import common.Message;
import common.TaskType;
import common.User;

/**
 * The SubscriberController class handles client-side logic specific to registered subscribers.
 * <p>
 * It extends the {@link CasualController}, inheriting basic capabilities (like making reservations),
 * while adding features exclusive to subscribers, such as logging in, viewing order history,
 * and updating personal profile details.
 */
public class SubscriberController extends CasualController {

    /**
     * Constructs a SubscriberController instance.
     *
     * @param networkController The main client controller used for server communication.
     */
    public SubscriberController(ClientController networkController) {
        super(networkController);
    }

    /**
     * Sends a login request to the server using the subscriber's username and ID.
     * <p>
     * The ID is converted to a string to match the User entity constructor expected by the server.
     *
     * @param username     The subscriber's username.
     * @param subscriberId The subscriber's numeric ID.
     */
    public void login(String username, int subscriberId) {
        // Create a temporary User object to hold credentials
    	User loginUser = new User(username, String.valueOf(subscriberId));
    	
        networkController.accept(new Message(TaskType.LOGIN_REQUEST, loginUser));
    }

    /**
     * Requests the full order history for a specific subscriber.
     *
     * @param subscriberId The internal database ID of the user (not the subscriber number).
     */
    public void getHistory(int subscriberId) {
        // We pass the ID as an object (Integer) so the server knows whose history to fetch
        networkController.accept(new Message(TaskType.GET_USER_HISTORY, subscriberId));
    }
    
    /**
     * Sends a request to update the subscriber's personal details (e.g., phone, email).
     *
     * @param user The User object containing the updated information.
     */
    public void updateSubscriberDetails(User user) {
        networkController.accept(new Message(TaskType.UPDATE_SUBSCRIBER, user));
    }
}