package client;

import controllers.ManagerController;
import common.MonthlyReportData;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MonthlyReportUI {

	
    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;
    private ManagerController managerController;

    // --- UI Components ---
    private ComboBox<String> cmbMonth;
    private ComboBox<Integer> cmbYear;
    private VBox chartContainer; 
    private MonthlyReportData currentReportData; 

    public MonthlyReportUI(VBox mainLayout, ClientUI mainUI, ManagerController managerController, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.managerController = managerController;
        this.onBack = onBack;
    }

    public void start() {
        mainUI.setMonthlyReportUI(this); 
        showReportScreen();
    }

    private void showReportScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Manager Analytics Dashboard");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0;");

        // --- Date Selection ---
        Label lblDate = new Label("Select Past Period:");
        
        cmbMonth = new ComboBox<>();
        cmbMonth.getItems().addAll("January", "February", "March", "April", "May", "June", 
                                   "July", "August", "September", "October", "November", "December");
        // Default to previous month
        int prevMonthIdx = LocalDate.now().minusMonths(1).getMonthValue() - 1;
        cmbMonth.getSelectionModel().select(prevMonthIdx); 
        
        cmbYear = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= currentYear - 2; i--) cmbYear.getItems().add(i);
        cmbYear.getSelectionModel().select(0); 

        Button btnFetch = new Button("Load Data");
        btnFetch.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold;");
        btnFetch.setOnAction(e -> fetchReportData());

        HBox controlsBox = new HBox(10, lblDate, cmbMonth, cmbYear, btnFetch);
        controlsBox.setAlignment(Pos.CENTER);

        // --- Report Buttons ---
        Button btnTimeReport = createReportButton("Performance (On-Time/Late)", "⏱");
        Button btnActivityReport = createReportButton("Weekly Load & Diners", "📊");

        btnTimeReport.setOnAction(e -> {
            if (validateDataLoaded()) renderTimeReport();
        });

        btnActivityReport.setOnAction(e -> {
            if (validateDataLoaded()) renderActivityReport();
        });

        HBox actionBox = new HBox(15, btnTimeReport, btnActivityReport);
        actionBox.setAlignment(Pos.CENTER);

        // --- Chart Container ---
        chartContainer = new VBox();
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPadding(new Insets(15));
        chartContainer.setMinHeight(400);
        chartContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
        chartContainer.getChildren().add(new Label("Select a past month and click 'Load Data' to view analytics."));

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> onBack.run());

        VBox container = new VBox(20, header, controlsBox, actionBox, chartContainer, btnBack);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setMaxWidth(900);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(container);
    }

    private void fetchReportData() {
        int selectedMonth = cmbMonth.getSelectionModel().getSelectedIndex() + 1;
        int selectedYear = cmbYear.getValue();
        LocalDate now = LocalDate.now();
        
        // --- 1. STRICT VALIDATION: Only Past Months Allowed ---
        if (selectedYear > now.getYear() || 
           (selectedYear == now.getYear() && selectedMonth >= now.getMonthValue())) {
            
            mainUI.showAlert("Restricted Access", 
                "You can only view reports for past months.\nData for the current month is incomplete.");
            return;
        }

        if (this.managerController != null) {
            chartContainer.getChildren().clear();
            chartContainer.getChildren().add(new Label("Loading..."));
            this.managerController.requestMonthlyReport(selectedMonth, selectedYear);
        }
    }

    public void updateReportData(MonthlyReportData data) {
        this.currentReportData = data;
        Platform.runLater(() -> {
            // --- 2. NO DATA HANDLING ---
            if (data == null || data.isEmpty()) {
                showNoDataMessage();
            } else {
                renderActivityReport(); // Show the main report by default
                // mainUI.showAlert("Success", "Report loaded successfully."); // Optional: Don't annoy user if not needed
            }
        });
    }
    
    private void showNoDataMessage() {
        chartContainer.getChildren().clear();
        Label lbl = new Label("No data found for the selected period.");
        lbl.setFont(new Font("Arial", 18));
        lbl.setTextFill(javafx.scene.paint.Color.RED);
        chartContainer.getChildren().add(lbl);
    }

    private boolean validateDataLoaded() {
        if (currentReportData == null || currentReportData.isEmpty()) {
            mainUI.showAlert("No Data", "Please load valid data first.");
            return false;
        }
        return true;
    }

    // --- REPORT 1: PERFORMANCE ---
    private void renderTimeReport() {
        chartContainer.getChildren().clear();

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Arrival Performance");

        PieChart.Data slice1 = new PieChart.Data("On Time", currentReportData.getTotalOnTime());
        PieChart.Data slice2 = new PieChart.Data("Late (>20m)", currentReportData.getTotalLate());
        PieChart.Data slice3 = new PieChart.Data("No Show", currentReportData.getTotalNoShow());

        pieChart.getData().addAll(slice1, slice2, slice3);
        
        chartContainer.getChildren().add(pieChart);
    }

    // --- REPORT 2: LOAD ANALYSIS (Orders vs Waiting List by Day) ---
    private void renderActivityReport() {
        chartContainer.getChildren().clear();
        
        Label lblTitle = new Label("Weekly Load Analysis: Orders vs Waiting List");
        lblTitle.setFont(new Font("Arial", 18));
        lblTitle.setStyle("-fx-font-weight: bold;");

        // 1. Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day of Week");
        yAxis.setLabel("Count");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        barChart.setTitle("Peak Days Identification");

        // Series 1: Actual Orders
        XYChart.Series<String, Number> seriesOrders = new XYChart.Series<>();
        seriesOrders.setName("Completed Orders");
        fillDaySeries(seriesOrders, currentReportData.getOrdersByDayOfWeek());

        // Series 2: Waiting List Demand
        XYChart.Series<String, Number> seriesWait = new XYChart.Series<>();
        seriesWait.setName("Waiting List Entries");
        fillDaySeries(seriesWait, currentReportData.getWaitingListByDayOfWeek());

        barChart.getData().addAll(seriesOrders, seriesWait);

        // 2. Summary Stats
        int totalGuests = currentReportData.getTotalGuests();
        int totalOrders = currentReportData.getOrdersByDayOfWeek().values().stream().mapToInt(Integer::intValue).sum();
        
        Label lblStats = new Label(
            String.format("Summary: Served %d Total Guests across %d Orders this month.", totalGuests, totalOrders)
        );
        lblStats.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10; -fx-background-radius: 5;");

        chartContainer.getChildren().addAll(lblTitle, barChart, lblStats);
    }

    // Helper to fill data in correct Sunday-Saturday order
    private void fillDaySeries(XYChart.Series<String, Number> series, Map<String, Integer> map) {
        List<String> days = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        for (String day : days) {
            series.getData().add(new XYChart.Data<>(day, map.getOrDefault(day, 0)));
        }
    }

    private Button createReportButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(40);
        return btn;
    }
}