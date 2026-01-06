package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// Implements the Listener interface to receive updates from BistroServer
public class ServerUI extends Application implements ServerEventListener {

	
    private BistroServer server;
    private ObservableList<ClientConnectionData> connectedClients;
    private TableView<ClientConnectionData> table;
    private TextArea consoleLog; // The visual log area

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bistro System - Server Console");

        // --- 1. Header ---
        Label lblHeader = new Label("Server Dashboard");
        lblHeader.setFont(new Font("Arial", 22));
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label lblStatus = new Label("Status: Running on Port 5555");
        lblStatus.setStyle("-fx-text-fill: green;");

        // --- 2. Client Table (Improved) ---
        table = new TableView<>();
        connectedClients = FXCollections.observableArrayList();
        table.setItems(connectedClients);

        TableColumn<ClientConnectionData, String> colIp = new TableColumn<>("IP Address");
        colIp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIp()));

        TableColumn<ClientConnectionData, String> colHost = new TableColumn<>("Host Name");
        colHost.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHost()));

        // New Column: Identity (Username)
        TableColumn<ClientConnectionData, String> colIdentity = new TableColumn<>("User / Role");
        colIdentity.setCellValueFactory(cellData -> cellData.getValue().identityProperty());
        colIdentity.setPrefWidth(150);

        TableColumn<ClientConnectionData, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty("Connected"));
        
        table.getColumns().addAll(colIp, colHost, colIdentity, colStatus);
        table.setPlaceholder(new Label("Waiting for connections..."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- 3. Console Log Area ---
        consoleLog = new TextArea();
        consoleLog.setEditable(false);
        consoleLog.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #0f0; -fx-font-family: 'Consolas';");
        consoleLog.setPrefHeight(200);
        consoleLog.setText(">>> Server initializing...\n");

        VBox logBox = new VBox(5, new Label("Live System Logs:"), consoleLog);

        // --- 4. Controls ---
        Button btnExit = new Button("Stop & Exit");
        btnExit.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExit.setOnAction(e -> {
            try { if (server != null) server.close(); } catch (Exception ex) {}
            System.exit(0);
        });

        // Layout Assembly
        VBox topContainer = new VBox(10, lblHeader, lblStatus, table);
        VBox mainContainer = new VBox(15, topContainer, new Separator(), logBox, btnExit);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setPrefSize(700, 600);

        // Start Server Logic
        startServer();

        Scene scene = new Scene(mainContainer);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    private void startServer() {
        // Pass 'this' as the listener because ServerUI implements ServerEventListener
        server = new BistroServer(5555, this);
        try {
            server.listen();
        } catch (IOException e) {
            appendLog("Error listening on port 5555: " + e.getMessage());
        }
    }

    // =========================================================
    // IMPLEMENTING ServerEventListener (Callbacks from BistroServer)
    // =========================================================

    @Override
    public void onClientConnected(ConnectionToClient client) {
        Platform.runLater(() -> {
            long threadId = client.getId();
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();

            // Add as "Guest" initially
            connectedClients.add(new ClientConnectionData(threadId, ip, host, "Guest"));
            appendLog("Client Connected: " + ip);
        });
    }

    @Override
    public void onClientDisconnected(ConnectionToClient client) {
        Platform.runLater(() -> {
            long threadId = client.getId();
            // Remove the client from the list
            connectedClients.removeIf(c -> c.getOriginalId() == threadId);
            table.refresh();
            appendLog("Client Disconnected.");
        });
    }

    @Override
    public void onUserLoggedIn(long threadId, String username, String role) {
        Platform.runLater(() -> {
            // Find the row and update the name
            for (ClientConnectionData data : connectedClients) {
                if (data.getOriginalId() == threadId) {
                    data.setIdentity(username + " (" + role + ")");
                    table.refresh();
                    appendLog("User Authenticated: " + username);
                    break;
                }
            }
        });
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> appendLog(message));
    }

    // Helper to print to TextArea with timestamp
    private void appendLog(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        consoleLog.appendText("[" + time + "] " + msg + "\n");
    }

    // =========================================================
    // Data Model for Table
    // =========================================================
    public static class ClientConnectionData {
        private final long originalId;
        private final String ip;
        private final String host;
        private final SimpleStringProperty identity; // Observable so Table updates automatically

        public ClientConnectionData(long originalId, String ip, String host, String identity) {
            this.originalId = originalId;
            this.ip = ip;
            this.host = host;
            this.identity = new SimpleStringProperty(identity);
        }

        public long getOriginalId() { return originalId; }
        public String getIp() { return ip; }
        public String getHost() { return host; }
        
        public SimpleStringProperty identityProperty() { return identity; }
        public void setIdentity(String s) { this.identity.set(s); }
    }
}