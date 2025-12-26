package client;

import java.util.ArrayList;

import common.*;

public class RepresentativeController {

    private ClientController networkController;

    public RepresentativeController(ClientController networkController) {
        this.networkController = networkController;
    }

    // --- Login ---
    public void staffLogin(String username, String password) {
        User user = new User(username, password);
        networkController.accept(new Message(TaskType.LOGIN_REQUEST, user));
    }

    // --- Table Management ---
    public void getAllTables() {
        networkController.accept(new Message(TaskType.GET_TABLES, null));
    }

    public void addTable(Table table) {
        networkController.accept(new Message(TaskType.ADD_TABLE, table));
    }

    public void updateTable(Table table) {
        networkController.accept(new Message(TaskType.UPDATE_TABLE, table));
    }

    public void removeTable(int tableId) {
        networkController.accept(new Message(TaskType.REMOVE_TABLE, tableId));
    }

    // --- Data Retrieval ---
    public void getAllSubscribers() {
        networkController.accept(new Message(TaskType.GET_ALL_SUBSCRIBERS, null));
    }

    public void getActiveOrders() {
        networkController.accept(new Message(TaskType.GET_ORDERS, null));
    }

    public void getWaitingList() {
        networkController.accept(new Message(TaskType.GET_WAITING_LIST, null));
    }
    

    public void getSchedule() {
        networkController.accept(new Message(TaskType.GET_SCHEDULE, null));
    }


    public void saveSchedule(ArrayList<BistroSchedule> scheduleList) {
        networkController.accept(new Message(TaskType.SAVE_SCHEDULE_ITEM, scheduleList));
    }
    public void saveScheduleItem(BistroSchedule item) {
        networkController.accept(new Message(TaskType.SAVE_SCHEDULE_ITEM, item));
    }

    public void deleteScheduleItem(String identifier) {
        networkController.accept(new Message(TaskType.DELETE_SCHEDULE_ITEM, identifier));
    }
}