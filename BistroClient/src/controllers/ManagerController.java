package controllers;

import common.Message;
import common.TaskType;

public class ManagerController extends RepresentativeController {

    public ManagerController(ClientController networkController) {
        super(networkController);
    }

    /**
     * Sends a request to generate reports for a specific period.
     * Payload format: "MM-YYYY" (e.g., "5-2025")
     */
    public void requestMonthlyReport(int month, int year) {
        String payload = month + "-" + year;
        Message msg = new Message(TaskType.GET_MONTHLY_REPORT, payload);
        networkController.accept(msg);
        System.out.println("Log: Requested report for " + payload);
    }

    public void exportSystemData() {
        System.out.println("Log: Manager requested system data export...");
        // Implement export logic if needed
    }
}