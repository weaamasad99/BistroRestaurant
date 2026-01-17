package controllers;

import java.util.ArrayList;

import common.*;

/**
 * The RepresentativeController class handles the client-side logic for restaurant staff (Representatives).
 * <p>
 * It provides methods for administrative tasks such as managing tables, checking the waiting list,
 * viewing orders, modifying the schedule, and registering new subscribers.
 * This class sends requests to the server via the {@link ClientController}.
 */
public class RepresentativeController {

    protected ClientController networkController;

    /**
     * Constructs a RepresentativeController instance.
     *
     * @param networkController The main client controller used for server communication.
     */
    public RepresentativeController(ClientController networkController) {
        this.networkController = networkController;
    }

    // --- Login ---

    /**
     * Sends a login request for a staff member.
     *
     * @param username The staff username.
     * @param password The staff password.
     */
    public void staffLogin(String username, String password) {
        User user = new User(username, password);
        networkController.accept(new Message(TaskType.LOGIN_REQUEST, user));
    }

    // --- Table Management ---

    /**
     * Requests the list of all tables in the restaurant from the server.
     */
    public void getAllTables() {
        networkController.accept(new Message(TaskType.GET_TABLES, null));
    }

    /**
     * Sends a request to add a new table to the restaurant layout.
     *
     * @param table The Table object to be added.
     */
    public void addTable(Table table) {
        networkController.accept(new Message(TaskType.ADD_TABLE, table));
    }

    /**
     * Sends a request to update an existing table's details (e.g., seat count or status).
     *
     * @param table The Table object containing updated information.
     */
    public void updateTable(Table table) {
        networkController.accept(new Message(TaskType.UPDATE_TABLE, table));
    }

    /**
     * Sends a request to remove a table from the restaurant.
     *
     * @param tableId The ID of the table to remove.
     */
    public void removeTable(int tableId) {
        networkController.accept(new Message(TaskType.REMOVE_TABLE, tableId));
    }

    // --- Data Retrieval ---

    /**
     * Requests the list of all registered subscribers.
     */
    public void getAllSubscribers() {
        networkController.accept(new Message(TaskType.GET_ALL_SUBSCRIBERS, null));
    }

    /**
     * Requests the list of currently active orders (diners currently in the restaurant).
     */
    public void getActiveOrders() {
        networkController.accept(new Message(TaskType.GET_ACTIVE_ORDERS, null));
    }
    
    /**
     * Requests the full history of all orders.
     */
    public void getAllOrders() {
        networkController.accept(new Message(TaskType.GET_ORDERS, null));
    }

    /**
     * Requests the current waiting list.
     */
    public void getWaitingList() {
        networkController.accept(new Message(TaskType.GET_WAITING_LIST, null));
    }
    
    /**
     * Requests the restaurant's operating schedule (opening hours and special events).
     */
    public void getSchedule() {
        networkController.accept(new Message(TaskType.GET_SCHEDULE, null));
    }

    /**
     * Sends a bulk update for the restaurant schedule.
     * Typically used for saving standard weekly opening hours.
     *
     * @param scheduleList A list of BistroSchedule objects to save.
     */
    public void saveSchedule(ArrayList<BistroSchedule> scheduleList) {
        networkController.accept(new Message(TaskType.SAVE_SCHEDULE_ITEM, scheduleList));
    }

    /**
     * Sends a request to save or update a single schedule item (e.g., a specific holiday).
     *
     * @param item The BistroSchedule object to save.
     */
    public void saveScheduleItem(BistroSchedule item) {
        networkController.accept(new Message(TaskType.SAVE_SCHEDULE_ITEM, item));
    }

    /**
     * Sends a request to delete a specific schedule entry.
     *
     * @param identifier The unique identifier (e.g., date string) of the schedule item to delete.
     */
    public void deleteScheduleItem(String identifier) {
        networkController.accept(new Message(TaskType.DELETE_SCHEDULE_ITEM, identifier));
    }

    /**
     * Sends a request to register a new subscriber.
     *
     * @param newSubscriber The User object containing the new subscriber's details.
     */
    public void registerSubscriber(User newSubscriber) {
        networkController.accept(new Message(TaskType.REGISTER_USER, newSubscriber));
    }
    
    /**
     * Checks if a user exists in the system based on an identifier.
     * Used for validating access permissions or looking up customers.
     *
     * @param identifier The Subscriber ID or Phone Number to check.
     */
    public void checkUserExists(String identifier) {
        // identifier can be Subscriber ID or Phone Number
        networkController.accept(new Message(TaskType.CHECK_USER_EXISTS, identifier));
    }
}