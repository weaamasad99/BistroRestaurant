package client;

import common.Message;
import common.TaskType;

public class ManagerController extends RepresentativeController {

    public ManagerController(ClientController networkController) {
        super(networkController);
    }

    public void getMonthlyReport(String month, String year) {
        // Placeholder for report request
        System.out.println("Requesting report for " + month + "/" + year);
    }
    
    public void exportSystemData() {
        // Placeholder for export
        System.out.println("Requesting system data export...");
    }
}