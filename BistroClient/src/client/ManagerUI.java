package client;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Boundary class for Manager Dashboard.
 * Inherits operational functions from RepresentativeUI and adds Reporting capabilities.
 */
public class ManagerUI extends RepresentativeUI {

    // --- Fields from Class Diagram ---
    private Button viewReportsButton;
    private Button exportDataButton;

    public ManagerUI(VBox mainLayout, ClientUI mainUI) {
        super(mainLayout, mainUI);
    }

    @Override
    public void start() {
        // Skip login (handled in ClientUI) and show dashboard directly
        showDashboardScreen("Admin");
    }

    /**
     * Override the hook method to inject Manager-specific buttons
     * into the Representative dashboard layout.
     */
    @Override
    protected void addManagerContent(VBox container) {
        
        // 1. Initialize Buttons
        viewReportsButton = createWideButton("View Monthly Reports", "📊");
        viewReportsButton.setOnAction(e -> openReportUI());

        exportDataButton = createWideButton("Export System Data", "💾");
        exportDataButton.setOnAction(e -> mainUI.showAlert("Export", "Data exported successfully to CSV."));

        // 2. Layout for Manager Section
        Label lblReports = new Label("Manager Reports & Analytics");
        lblReports.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0; -fx-underline: true;");

        VBox reportsBox = new VBox(8, lblReports, viewReportsButton, exportDataButton);
        reportsBox.setAlignment(Pos.CENTER);

        // 3. Add to the main container
        container.getChildren().addAll(new Separator(), reportsBox);
    }

    /**
     * Opens the MonthlyReportUI screen.
     */
    public void openReportUI() {
        // Create and start the Report UI, passing a callback to return to this dashboard
        MonthlyReportUI reportUI = new MonthlyReportUI(mainLayout, mainUI, () -> showDashboardScreen("Admin"));
        reportUI.start();
    }
}