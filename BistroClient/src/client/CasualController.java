package client;

import common.Message;
import common.Order;
import common.TaskType;
import common.User;
import common.WaitingList;

public class CasualController {
    
    protected ClientController networkController;

    public CasualController(ClientController networkController) {
        this.networkController = networkController;
    }
    
    /**
     * Fix: Sends the phone number as a String, because the Server
     * casts message.getObject() to (String).
     */
    public void createCasualUser(String phone) {
        networkController.accept(new Message(TaskType.CREATE_CASUAL, phone));
    }

    public void requestReservation(Order order) {
        networkController.accept(new Message(TaskType.REQUEST_RESERVATION, order));
    }

    public void enterWaitingList(WaitingList entry) {
        networkController.accept(new Message(TaskType.ENTER_WAITING_LIST, entry));
    }

    public void identifyAndCheckIn(String code) {
        networkController.accept(new Message(TaskType.CHECK_IN_CUSTOMER, code));
    }
    
    public void requestCheckout(String orderCode) {
        System.out.println("Sending checkout request for: " + orderCode);
    }
}