package client;

import common.MonthlyReportData;
import javafx.application.Platform;
import javafx.geometry.Insets;
import controllers.ManagerController;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.Year;
import java.util.Map;

public class MonthlyReportUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;

    // --- UI Components ---
    private ComboBox<String> cmbMonth;
    private ComboBox<Integer> cmbYear;
    private BarChart<String, Number> reportChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    
    // Store data to toggle views without re-fetching
    private MonthlyReportData currentReportData; 
    private ManagerController managerController;

    public MonthlyReportUI(VBox mainLayout, ClientUI mainUI,ManagerController managerController, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.managerController = managerController;
    }

    public void start() {
        // Register this UI with ClientUI so it can receive data
        mainUI.setMonthlyReportUI(this); 
        showReportScreen();
    }

    private void showReportScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Manager Reports & Analytics");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0;");

        // --- Date Selection ---
        Label lblDate = new Label("Select Period:");
        
        cmbMonth = new ComboBox<>();
        cmbMonth.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        cmbMonth.getSelectionModel().select(4); // Default: May (for data example)
        
        cmbYear = new ComboBox<>();
        int currentYear = Year.now().getValue();
        for (int i = currentYear; i >= currentYear - 2; i--) cmbYear.getItems().add(i);
        cmbYear.getSelectionModel().select(0);

        Button btnFetch = new Button("Load Data");
        btnFetch.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        btnFetch.setOnAction(e -> fetchReportData());

        HBox controlsBox = new HBox(10, lblDate, cmbMonth, cmbYear, btnFetch);
        controlsBox.setAlignment(Pos.CENTER);

        // --- Chart Area ---
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        reportChart = new BarChart<>(xAxis, yAxis);
        reportChart.setTitle("Please Load Data");
        reportChart.setAnimated(false); // Disable animation to prevent artifacts on refresh
        VBox.setVgrow(reportChart, Priority.ALWAYS);

        // --- Report Type Buttons ---
        Button btnTimeReport = createReportButton("Time & Performance", "⏱");
        Button btnSubReport = createReportButton("Orders & Waiting List", "📊");

        btnTimeReport.setOnAction(e -> {
            if (currentReportData != null) renderTimeReport();
            else mainUI.showAlert("Info", "Please click 'Load Data' first.");
        });

        btnSubReport.setOnAction(e -> {
            if (currentReportData != null) renderOrdersReport();
            else mainUI.showAlert("Info", "Please click 'Load Data' first.");
        });

        HBox actionBox = new HBox(15, btnTimeReport, btnSubReport);
        actionBox.setAlignment(Pos.CENTER);

        // Back Button
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> onBack.run());

        VBox container = new VBox(15, header, controlsBox, reportChart, actionBox, btnBack);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setMaxWidth(800);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        mainLayout.getChildren().add(container);
    }

    private void fetchReportData() {
        int monthIndex = cmbMonth.getSelectionModel().getSelectedIndex() + 1;
        int year = cmbYear.getValue();
        
        // Use the ClientUI's controller (casted to ManagerController if needed, 
        // or just use generic controller accepting the message)
        if (this.managerController != null) {
            this.managerController.requestMonthlyReport(monthIndex, year);
        } else {
            mainUI.showAlert("Error", "Controller not initialized properly.");
        }
    }

    /**
     * Called by ClientUI when server sends REPORT_GENERATED.
     */
    public void updateReportData(MonthlyReportData data) {
        this.currentReportData = data;
        Platform.runLater(() -> {
            renderTimeReport(); // Default view
            mainUI.showAlert("Report Loaded", "Data loaded for " + cmbMonth.getValue());
        });
    }

    private void renderTimeReport() {
        reportChart.getData().clear();
        reportChart.setTitle("Performance: On-Time vs Late vs No-Show");
        xAxis.setLabel("Status");
        yAxis.setLabel("Count");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Customers");
        
        series.getData().add(new XYChart.Data<>("On Time", currentReportData.getTotalOnTime()));
        series.getData().add(new XYChart.Data<>("Late (>20m)", currentReportData.getTotalLate()));
        series.getData().add(new XYChart.Data<>("No Show", currentReportData.getTotalNoShow()));

        reportChart.getData().add(series);
    }

    private void renderOrdersReport() {
        reportChart.getData().clear();
        reportChart.setTitle("Activity: Orders & Waiting List by Week");
        xAxis.setLabel("Week");
        yAxis.setLabel("Total");

        XYChart.Series<String, Number> seriesOrders = new XYChart.Series<>();
        seriesOrders.setName("Orders");
        Map<String, Integer> ordersMap = currentReportData.getWeeklyOrderCounts();
        
        // Ensure we show weeks 1-4 even if empty
        for(int i=1; i<=5; i++) {
            String key = "Week " + i;
            if(ordersMap.containsKey(key))
                seriesOrders.getData().add(new XYChart.Data<>(key, ordersMap.get(key)));
        }

        XYChart.Series<String, Number> seriesWait = new XYChart.Series<>();
        seriesWait.setName("Waiting List");
        Map<String, Integer> waitMap = currentReportData.getWeeklyWaitingListCounts();
        
        for(int i=1; i<=5; i++) {
             String key = "Week " + i;
             if(waitMap.containsKey(key))
                 seriesWait.getData().add(new XYChart.Data<>(key, waitMap.get(key)));
        }

        reportChart.getData().addAll(seriesOrders, seriesWait);
    }

    private Button createReportButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(220);
        return btn;
    }
}