package client;

import common.Message;
import common.TaskType;
import common.Table;
import common.User;

public class RepresentativeController {
    
    protected ClientController networkController;

    public RepresentativeController(ClientController networkController) {
        this.networkController = networkController;
    }
    
    public void staffLogin(String username, String password) {
        User user = new User(username, password);
        networkController.accept(new Message(TaskType.LOGIN_REQUEST, user));
    }

    public void getAllTables() {
        networkController.accept(new Message(TaskType.GET_TABLES, null));
    }

    public void updateTable(Table table) {
        networkController.accept(new Message(TaskType.UPDATE_TABLE, table));
    }
    
    public void addTable(Table table) {
        networkController.accept(new Message(TaskType.ADD_TABLE, table));
    }

    public void removeTable(int tableId) {
        networkController.accept(new Message(TaskType.REMOVE_TABLE, tableId));
    }

    public void getAllSubscribers() {
        networkController.accept(new Message(TaskType.GET_ALL_SUBSCRIBERS, null));
    }
    
    public void getActiveOrders() {
        networkController.accept(new Message(TaskType.GET_ORDERS, null));
    }
    
    public void getWaitingList() {
        networkController.accept(new Message(TaskType.GET_WAITING_LIST, null));
    }
    
    public void registerNewSubscriber(User newUser) {
        networkController.accept(new Message(TaskType.REGISTER_USER, newUser));
    }
}