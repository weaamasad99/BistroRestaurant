package client;

import common.Message;
import common.TaskType;

public class ManagerController extends RepresentativeController {

    public ManagerController(ClientController networkController) {
        super(networkController);
    }

    public void getMonthlyReport(String month, String year) {
        // Construct a request for reports (Assuming TaskType.GET_REPORT exists)
        // String payload = month + "-" + year;
        // networkController.accept(new Message(TaskType.GET_REPORT, payload));
        
        System.out.println("Log: Manager requested report for " + month + "/" + year);
    }
    
    public void exportSystemData() {
        // networkController.accept(new Message(TaskType.EXPORT_DATA, null));
        System.out.println("Log: Manager requested system data export...");
    }
}