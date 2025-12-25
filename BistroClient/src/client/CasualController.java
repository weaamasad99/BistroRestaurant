package client;

import common.Message;
import common.Order;
import common.TaskType;
import common.WaitingList;

public class CasualController {
    
    protected ClientController networkController;

    public CasualController(ClientController networkController) {
        this.networkController = networkController;
    }

    public void requestReservation(Order order) {
        // Sends order object (Date, Time, Guests, Phone/ID)
        networkController.accept(new Message(TaskType.REQUEST_RESERVATION, order));
    }

    public void enterWaitingList(WaitingList entry) {
        networkController.accept(new Message(TaskType.ENTER_WAITING_LIST, entry));
    }

    public void identifyAndCheckIn(String code) {
        networkController.accept(new Message(TaskType.CHECK_IN_CUSTOMER, code));
    }
    
    public void requestCheckout(String orderCode) {
        // Placeholder for checkout logic
        System.out.println("Sending checkout request for: " + orderCode);
    }
}