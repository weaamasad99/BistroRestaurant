package controllers;

import common.Message;
import common.TaskType;

/**
 * The ManagerController class handles client-side logic specific to the Restaurant Manager role.
 * <p>
 * It extends the functionality of the {@link RepresentativeController}, inheriting standard staff 
 * operations (like table and schedule management), while adding exclusive administrative capabilities 
 * such as generating system performance reports and exporting data.
 */
public class ManagerController extends RepresentativeController {

    /**
     * Constructs a ManagerController instance.
     *
     * @param networkController The main client controller used for server communication.
     */
    public ManagerController(ClientController networkController) {
        super(networkController);
    }

    /**
     * Sends a request to the server to generate a monthly performance report.
     * <p>
     * The request payload is formatted as a string "MM-YYYY" (e.g., "5-2025").
     * The server is expected to respond with a {@link common.MonthlyReportData} object via the 
     * {@code REPORT_GENERATED} task type.
     *
     * @param month The month for the report (1-12).
     * @param year  The year for the report.
     */
    public void requestMonthlyReport(int month, int year) {
        String payload = month + "-" + year;
        Message msg = new Message(TaskType.GET_MONTHLY_REPORT, payload);
        networkController.accept(msg);
        System.out.println("Log: Requested report for " + payload);
    }

    /**
     * Initiates the export of system data.
     * <p>
     * Currently serves as a placeholder for future implementation of data export features 
     * (e.g., exporting logs or statistics to CSV/PDF).
     */
    public void exportSystemData() {
        System.out.println("Log: Manager requested system data export...");
        // Implement export logic if needed
    }
}