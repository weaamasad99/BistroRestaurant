package client;

import controllers.ManagerController;
import common.MonthlyReportData;
import common.Order;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The MonthlyReportUI class manages the analytics dashboard for the Restaurant Manager.
 * <p>
 * It provides a visual interface to select a specific month and year, fetches statistical data
 * from the server via the {@link ManagerController}, and renders two primary types of reports:
 * <ul>
 * <li><b>Time &amp; Performance:</b> Visualizes arrival/departure punctuality using Pie Charts and tables.</li>
 * <li><b>Activity &amp; Subscribers:</b> Visualizes order volume and waiting list activity by day of the week using Bar Charts.</li>
 * </ul>
 */
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

    /**
     * Constructs the MonthlyReportUI instance.
     *
     * @param mainLayout        The main layout container where the dashboard will be rendered.
     * @param mainUI            The main application instance.
     * @param managerController The controller responsible for fetching report data.
     * @param onBack            A callback to execute when the user navigates back to the main menu.
     */
    public MonthlyReportUI(VBox mainLayout, ClientUI mainUI, ManagerController managerController, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.managerController = managerController;
        this.onBack = onBack;
    }

    /**
     * Starts the report UI by registering this instance with the main UI
     * and displaying the initial report selection screen.
     */
    public void start() {
        mainUI.setMonthlyReportUI(this); 
        showReportScreen();
    }

    /**
     * Initializes and displays the main dashboard structure.
     * <p>
     * Sets up the date selection controls (Month/Year), action buttons for generating reports,
     * and the container area where charts will be dynamically rendered.
     */
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
        int prevMonthIdx = LocalDate.now().minusMonths(1).getMonthValue() - 1;
        cmbMonth.getSelectionModel().select(Math.max(0, prevMonthIdx)); 
        
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
        Button btnTimeReport = createReportButton("1. Time & Performance", "");
        Button btnActivityReport = createReportButton("2. Activity & Subscribers", "");

        btnTimeReport.setOnAction(e -> {
            if (validateDataLoaded()) renderTimeReport();
        });

        btnActivityReport.setOnAction(e -> {
            if (validateDataLoaded()) renderActivityReport();
        });

        HBox actionBox = new HBox(15, btnTimeReport, btnActivityReport);
        actionBox.setAlignment(Pos.CENTER);

        // --- Content Container ---
        chartContainer = new VBox();
        chartContainer.setAlignment(Pos.TOP_CENTER);
        chartContainer.setPadding(new Insets(15));
        chartContainer.setMinHeight(450);
        chartContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
        chartContainer.getChildren().add(new Label("Select a past month and click 'Load Data' to view analytics."));

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setOnAction(e -> onBack.run());

        VBox container = new VBox(20, header, controlsBox, actionBox, chartContainer, btnBack);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setMaxWidth(1000);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        mainLayout.getChildren().add(scroll);
    }

    /**
     * Triggered when the "Load Data" button is clicked.
     * <p>
     * Validates that the selected date is in the past (to ensure complete data).
     * If valid, sends a request to the server to fetch the report for the specified month/year.
     */
    private void fetchReportData() {
        int selectedMonth = cmbMonth.getSelectionModel().getSelectedIndex() + 1;
        int selectedYear = cmbYear.getValue();
        LocalDate now = LocalDate.now();
        
        // Strict Validation: Ensure user selects a past period
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

    /**
     * Callback method used by the controller to update the dashboard with fresh data.
     * Automatically renders the "Time Report" view upon successful data load.
     *
     * @param data The MonthlyReportData object containing statistical information.
     */
    public void updateReportData(MonthlyReportData data) {
        this.currentReportData = data;
        Platform.runLater(() -> {
            if (data == null || data.isEmpty()) {
                showNoDataMessage();
            } else {
                renderTimeReport(); // Show first report by default
            }
        });
    }
    
    /**
     * Displays a message indicating no data was available for the selected period.
     */
    private void showNoDataMessage() {
        chartContainer.getChildren().clear();
        Label lbl = new Label("No data found for the selected period.");
        lbl.setFont(new Font("Arial", 18));
        lbl.setTextFill(Color.RED);
        chartContainer.getChildren().add(lbl);
    }

    /**
     * Checks if report data has been loaded before attempting to render charts.
     * @return true if data exists, false otherwise (showing an alert).
     */
    private boolean validateDataLoaded() {
        if (currentReportData == null || currentReportData.isEmpty()) {
            mainUI.showAlert("No Data", "Please load valid data first.");
            return false;
        }
        return true;
    }

    // =====================================================================
    // REPORT 1: TIME & PERFORMANCE (Pie + Table of Delays)
    // =====================================================================
    
    /**
     * Renders the Time & Performance Report.
     * <p>
     * Displays:
     * 1. A summary of average dining time.
     * 2. A Pie Chart showing the distribution of On-Time, Late, and No-Show orders.
     * 3. A detailed table listing specific orders that were late or no-shows.
     */
    private void renderTimeReport() {
        chartContainer.getChildren().clear();
        
        Label lblTitle = new Label("Arrivals, Departures & Delays Analysis");
        lblTitle.setFont(new Font("Arial", 18));
        lblTitle.setStyle("-fx-font-weight: bold;");

        // 1. Summary Box
        String avgTime = currentReportData.getAverageDiningTime() != null ? currentReportData.getAverageDiningTime() : "N/A";
        Label lblStats = new Label(
            String.format("Month Summary:\nOn Time: %d | Late (>15m): %d | No Shows: %d\nAverage Dining Duration: %s", 
            currentReportData.getTotalOnTime(), currentReportData.getTotalLate(), currentReportData.getTotalNoShow(), avgTime)
        );
        lblStats.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 10; -fx-border-color: #FF9800; -fx-border-radius: 5;");

        // 2. Pie Chart
        PieChart pieChart = new PieChart();
        pieChart.getData().add(new PieChart.Data("On Time", currentReportData.getTotalOnTime()));
        pieChart.getData().add(new PieChart.Data("Late (>15m)", currentReportData.getTotalLate()));
        pieChart.getData().add(new PieChart.Data("No Show", currentReportData.getTotalNoShow()));
        pieChart.setPrefHeight(300);

        // 3. Detailed Table (Late/No Show)
        Label lblTable = new Label("Exception Details (Late / No-Show):");
        lblTable.setFont(new Font("Arial", 14));
        lblTable.setStyle("-fx-underline: true;");

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);

        TableColumn<Order, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        
        TableColumn<Order, String> colScheduled = new TableColumn<>("Scheduled");
        colScheduled.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        
        TableColumn<Order, String> colArrived = new TableColumn<>("Arrived");
        colArrived.setCellValueFactory(new PropertyValueFactory<>("actualArrivalTime"));
        
        TableColumn<Order, String> colLeft = new TableColumn<>("Left");
        colLeft.setCellValueFactory(new PropertyValueFactory<>("leavingTime"));
        
        TableColumn<Order, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colDate, colScheduled, colArrived, colLeft, colStatus);
        
        if (currentReportData.getExceptionOrders() != null) {
            table.setItems(FXCollections.observableArrayList(currentReportData.getExceptionOrders()));
        }

        chartContainer.getChildren().addAll(lblTitle, lblStats, pieChart, new Separator(), lblTable, table);
    }

    // =====================================================================
    // REPORT 2: ACTIVITY (Bar Chart + Full Order List)
    // =====================================================================
    
    /**
     * Renders the Activity & Subscribers Report.
     * <p>
     * Displays:
     * 1. A Bar Chart comparing Orders vs. Waiting List entries by day of the week.
     * 2. A summary of total guests served.
     * 3. A complete log of all orders for the selected month.
     */
    private void renderActivityReport() {
        chartContainer.getChildren().clear();
        
        Label lblTitle = new Label("Order & Waiting List Distribution");
        lblTitle.setFont(new Font("Arial", 18));
        lblTitle.setStyle("-fx-font-weight: bold;");

        // 1. Bar Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Day of Week");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");
        yAxis.setTickUnit(1); // Force Integer Ticks
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        barChart.setTitle("Peak Days");

        XYChart.Series<String, Number> seriesOrders = new XYChart.Series<>();
        seriesOrders.setName("Orders");
        fillDaySeries(seriesOrders, currentReportData.getOrdersByDayOfWeek());

        XYChart.Series<String, Number> seriesWait = new XYChart.Series<>();
        seriesWait.setName("Waiting List");
        fillDaySeries(seriesWait, currentReportData.getWaitingListByDayOfWeek());

        barChart.getData().addAll(seriesOrders, seriesWait);
        barChart.setPrefHeight(300);

        // 2. Summary Stats
        Label lblStats = new Label(
            "Total Guests Served: " + currentReportData.getTotalGuests()
        );
        lblStats.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 10; -fx-border-color: #2196F3; -fx-border-radius: 5;");

        // 3. Detailed Order Table
        Label lblTable = new Label("Detailed Order Log:");
        lblTable.setFont(new Font("Arial", 14));
        lblTable.setStyle("-fx-underline: true;");

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(250);

        TableColumn<Order, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        
        TableColumn<Order, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        
        TableColumn<Order, Integer> colGuests = new TableColumn<>("Guests");
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfDiners"));
        
        TableColumn<Order, Integer> colOrderNum = new TableColumn<>("Order ID");
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

        table.getColumns().addAll(colDate, colTime, colGuests, colOrderNum);
        
        if (currentReportData.getAllMonthOrders() != null) {
            table.setItems(FXCollections.observableArrayList(currentReportData.getAllMonthOrders()));
        }

        chartContainer.getChildren().addAll(lblTitle, lblStats, barChart, new Separator(), lblTable, table);
    }

    /**
     * Helper method to populate a chart series with data for each day of the week.
     * Ensures all 7 days are represented, using 0 for days with no data.
     *
     * @param series The XYChart Series to populate.
     * @param map    The map containing counts per day (e.g., "Sunday" -> 5).
     */
    private void fillDaySeries(XYChart.Series<String, Number> series, Map<String, Integer> map) {
        List<String> days = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        for (String day : days) {
            series.getData().add(new XYChart.Data<>(day, map.getOrDefault(day, 0)));
        }
    }

    /**
     * Creates a styled button for switching between report views.
     *
     * @param text The text to display on the button.
     * @param icon The icon (emoji or character) to display.
     * @return A styled Button instance.
     */
    private Button createReportButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(40);
        return btn;
    }
}