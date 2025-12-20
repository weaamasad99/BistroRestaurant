package client;

import javafx.geometry.Insets;
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

/**
 * Boundary class for displaying Monthly Reports & Analytics.
 * Handles the visualization of system performance and subscriber data.
 */
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

    public MonthlyReportUI(VBox mainLayout, ClientUI mainUI, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
    }

    public void start() {
        showReportScreen();
    }

    private void showReportScreen() {
        mainLayout.getChildren().clear();

        // 1. Header
        Label header = new Label("Manager Reports & Analytics");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0;");

        // 2. Date Selection Controls
        Label lblDate = new Label("Select Period:");
        lblDate.setStyle("-fx-font-weight: bold;");

        cmbMonth = new ComboBox<>();
        cmbMonth.getItems().addAll(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        );
        cmbMonth.getSelectionModel().select(0); // Default to January
        cmbMonth.setPrefWidth(120);

        cmbYear = new ComboBox<>();
        int currentYear = Year.now().getValue();
        for (int i = currentYear; i >= currentYear - 5; i--) {
            cmbYear.getItems().add(i);
        }
        cmbYear.getSelectionModel().select(0); // Default to current year
        cmbYear.setPrefWidth(80);

        HBox controlsBox = new HBox(10, lblDate, cmbMonth, cmbYear);
        controlsBox.setAlignment(Pos.CENTER);

        // 3. Chart Area
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        reportChart = new BarChart<>(xAxis, yAxis);
        reportChart.setAnimated(true);
        reportChart.setTitle("Select a report type below");
        reportChart.setLegendVisible(true);
        
        // Ensure chart grows
        VBox.setVgrow(reportChart, Priority.ALWAYS);

        // 4. Action Buttons (Report Types)
        Button btnTimeReport = createReportButton("Time & Performance Report", "⏱");
        Button btnSubReport = createReportButton("Orders & Waiting List Report", "📊");

        // --- Logic: Time Report ---
        // "Arrival and departure times, including lateness and delays"
        btnTimeReport.setOnAction(e -> renderTimeReport());

        // --- Logic: Subscriber Report ---
        // "Order data and waiting lists throughout the month"
        btnSubReport.setOnAction(e -> renderOrdersReport());

        HBox actionBox = new HBox(15, btnTimeReport, btnSubReport);
        actionBox.setAlignment(Pos.CENTER);

        // 5. Back Button
        Button btnBack = new Button("Back to Dashboard");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        // Assembly
        VBox container = new VBox(15, header, controlsBox, reportChart, actionBox, btnBack);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // Ensure container fills the layout nicely
        container.setMaxWidth(700);
        container.setPrefHeight(550);

        mainLayout.getChildren().add(container);
    }

    /**
     * Report 1: Time Report
     * Visualizes arrival/departure times, lateness, and delays.
     */
    private void renderTimeReport() {
        reportChart.getData().clear();
        String period = cmbMonth.getValue() + " " + cmbYear.getValue();
        reportChart.setTitle("Performance Analysis - " + period);
        xAxis.setLabel("Metric");
        yAxis.setLabel("Total Customers");

        XYChart.Series<String, Number> seriesOnTime = new XYChart.Series<>();
        seriesOnTime.setName("On Time");
        seriesOnTime.getData().add(new XYChart.Data<>("Arrivals", 120));
        seriesOnTime.getData().add(new XYChart.Data<>("Departures", 110));

        XYChart.Series<String, Number> seriesLate = new XYChart.Series<>();
        seriesLate.setName("Late / Delayed");
        seriesLate.getData().add(new XYChart.Data<>("Arrivals", 30)); // Late arrivals
        seriesLate.getData().add(new XYChart.Data<>("Departures", 15)); // Overstayed

        XYChart.Series<String, Number> seriesNoShow = new XYChart.Series<>();
        seriesNoShow.setName("No Show / Cancelled");
        seriesNoShow.getData().add(new XYChart.Data<>("Arrivals", 10));
        seriesNoShow.getData().add(new XYChart.Data<>("Departures", 0));

        reportChart.getData().addAll(seriesOnTime, seriesLate, seriesNoShow);
    }

    /**
     * Report 2: Subscriber/Orders Report
     * Visualizes Order data vs Waiting List entries throughout the month.
     */
    private void renderOrdersReport() {
        reportChart.getData().clear();
        String period = cmbMonth.getValue() + " " + cmbYear.getValue();
        reportChart.setTitle("Orders & Waiting List Activity - " + period);
        xAxis.setLabel("Week of Month");
        yAxis.setLabel("Count");

        // Data for Reservations (Orders)
        XYChart.Series<String, Number> seriesOrders = new XYChart.Series<>();
        seriesOrders.setName("Total Orders");
        seriesOrders.getData().add(new XYChart.Data<>("Week 1", 45));
        seriesOrders.getData().add(new XYChart.Data<>("Week 2", 58));
        seriesOrders.getData().add(new XYChart.Data<>("Week 3", 52));
        seriesOrders.getData().add(new XYChart.Data<>("Week 4", 60));

        // Data for Waiting List Entries
        XYChart.Series<String, Number> seriesWaiting = new XYChart.Series<>();
        seriesWaiting.setName("Waiting List Entries");
        seriesWaiting.getData().add(new XYChart.Data<>("Week 1", 12));
        seriesWaiting.getData().add(new XYChart.Data<>("Week 2", 20));
        seriesWaiting.getData().add(new XYChart.Data<>("Week 3", 15));
        seriesWaiting.getData().add(new XYChart.Data<>("Week 4", 25));

        reportChart.getData().addAll(seriesOrders, seriesWaiting);
    }

    private Button createReportButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(220);
        btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        return btn;
    }
}