package client;

import common.Message;
import common.TaskType;
import common.User;

public class SubscriberController extends CasualController {

    public SubscriberController(ClientController networkController) {
        super(networkController);
    }

    /**
     * Sends login request using Username and Subscriber ID.
     * We map Subscriber ID to the 'password' field of the User object for transport.
     */
    public void login(String username, int subscriberId) {
        User loginUser = new User(username, subscriberId);
        networkController.accept(new Message(TaskType.LOGIN_REQUEST, loginUser));
    }

    public void getHistory(String subscriberId) {
        networkController.accept(new Message(TaskType.GET_USER_HISTORY, subscriberId));
    }
}