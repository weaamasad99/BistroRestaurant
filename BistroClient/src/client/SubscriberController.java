package client;

import common.Message;
import common.TaskType;
import common.User;

public class SubscriberController extends CasualController {

    public SubscriberController(ClientController networkController) {
        super(networkController);
    }

    /**
     * Sends login request using Username and INT Subscriber ID.
     */
    public void login(String username, int subscriberId) {
        // Assuming your User class has a constructor: User(String username, int id)
        // If your User class only stores strings, use: new User(username, String.valueOf(subscriberId));
    	User loginUser = new User(username, String.valueOf(subscriberId));
    	
        networkController.accept(new Message(TaskType.LOGIN_REQUEST, loginUser));
    }

    public void getHistory(int subscriberId) {
        // We pass the ID as an object (Integer)
        networkController.accept(new Message(TaskType.GET_USER_HISTORY, subscriberId));
    }
}