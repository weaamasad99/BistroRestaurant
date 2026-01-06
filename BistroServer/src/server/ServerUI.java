package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServerUI extends Application implements ServerEventListener {

    private BistroServer server;
    private ObservableList<ClientConnectionData> connectedClients;
    private TableView<ClientConnectionData> table;
    private TextArea consoleLog;

    // Map to assign simple IDs (1, 2, 3...) to clients
    private Map<Long, Integer> clientSimpleIdMap = new HashMap<>();
    private int idCounter = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bistro System - Server Console");

        Label lblHeader = new Label("Server Dashboard");
        lblHeader.setFont(new Font("Arial", 22));
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label lblStatus = new Label("Status: Running on Port 5555");
        lblStatus.setStyle("-fx-text-fill: green;");

        // --- Table Setup ---
        table = new TableView<>();
        connectedClients = FXCollections.observableArrayList();
        table.setItems(connectedClients);

        TableColumn<ClientConnectionData, String> colIp = new TableColumn<>("IP Address");
        colIp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIp()));

        TableColumn<ClientConnectionData, String> colHost = new TableColumn<>("Host Name");
        colHost.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHost()));

        TableColumn<ClientConnectionData, String> colIdentity = new TableColumn<>("User / Role");
        colIdentity.setCellValueFactory(cellData -> cellData.getValue().identityProperty());
        colIdentity.setPrefWidth(200);

        TableColumn<ClientConnectionData, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty("Active"));
        
        table.getColumns().addAll(colIp, colHost, colIdentity, colStatus);
        table.setPlaceholder(new Label("Waiting for users to log in..."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- Log Area ---
        consoleLog = new TextArea();
        consoleLog.setEditable(false);
        consoleLog.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #0f0; -fx-font-family: 'Consolas';");
        consoleLog.setPrefHeight(200);
        consoleLog.setText(">>> Server initializing...\n");

        VBox logBox = new VBox(5, new Label("Live Logs:"), consoleLog);

        Button btnExit = new Button("Stop & Exit");
        btnExit.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExit.setOnAction(e -> {
            try { if (server != null) server.close(); } catch (Exception ex) {}
            System.exit(0);
        });

        VBox mainContainer = new VBox(15, lblHeader, lblStatus, table, new Separator(), logBox, btnExit);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setPrefSize(750, 650);

        startServer();

        Scene scene = new Scene(mainContainer);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    private void startServer() {
        server = new BistroServer(5555, this);
        try {
            server.listen();
        } catch (IOException e) {
            appendLog("Error listening on port 5555: " + e.getMessage());
        }
    }

    // =========================================================
    // SERVER EVENT LISTENER IMPLEMENTATION
    // =========================================================

    @Override
    public void onClientConnected(ConnectionToClient client) {
        // CHANGED: We do NOT add to the table here anymore.
        // We only log the TCP connection.
        Platform.runLater(() -> {
            String ip = client.getInetAddress().getHostAddress();
            appendLog("New TCP Connection from: " + ip + " (Waiting for Login...)");
        });
    }

    @Override
    public void onUserLoggedIn(ConnectionToClient client, String username, String role) {
        Platform.runLater(() -> {
            long threadId = client.getId();
            
            // 1. Check if user is already in the table (maybe updating role?)
            boolean found = false;
            for (ClientConnectionData data : connectedClients) {
                if (data.getOriginalId() == threadId) {
                    data.setIdentity(username + " (" + role + ")");
                    table.refresh();
                    found = true;
                    break;
                }
            }

            // 2. If not found, ADD them now (This creates the "Show on Login" effect)
            if (!found) {
                String ip = client.getInetAddress().getHostAddress();
                String host = client.getInetAddress().getHostName();
                
                // Generate a simple ID for display
                if (!clientSimpleIdMap.containsKey(threadId)) {
                    clientSimpleIdMap.put(threadId, idCounter++);
                }
                int simpleId = clientSimpleIdMap.get(threadId);

                connectedClients.add(new ClientConnectionData(threadId, simpleId, ip, host, username + " (" + role + ")"));
            }
            
            appendLog("User Logged In: " + username + " as " + role);
        });
    }

    @Override
    public void onClientDisconnected(ConnectionToClient client) {
        Platform.runLater(() -> {
            long threadId = client.getId();
            // Remove from list if they were logged in
            boolean removed = connectedClients.removeIf(c -> c.getOriginalId() == threadId);
            if (removed) {
                table.refresh();
                appendLog("User Disconnected and removed from list.");
            } else {
                appendLog("Guest Disconnected (Was not logged in).");
            }
        });
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> appendLog(message));
    }

    private void appendLog(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        consoleLog.appendText("[" + time + "] " + msg + "\n");
    }

    // --- Data Model ---
    public static class ClientConnectionData {
        private final long originalId;
        private final int simpleId;
        private final String ip;
        private final String host;
        private final SimpleStringProperty identity;

        public ClientConnectionData(long originalId, int simpleId, String ip, String host, String identity) {
            this.originalId = originalId;
            this.simpleId = simpleId;
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