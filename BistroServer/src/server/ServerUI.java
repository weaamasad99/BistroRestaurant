package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerUI extends Application {

    private BistroServer server;
    private ObservableList<ClientConnectionData> connectedClients;
    private TableView<ClientConnectionData> table;
    
    // Map to assign simple IDs (1, 2, 3...) to clients instead of long Thread IDs
    private Map<Long, Integer> clientSimpleIdMap = new HashMap<>();
    private int idCounter = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bistro Server Manager");

        Label lblHeader = new Label("Connected Clients Monitor");
        lblHeader.setFont(new Font("Arial", 18));
        lblHeader.setStyle("-fx-font-weight: bold;");

        // --- Table Setup ---
        table = new TableView<>();
        connectedClients = FXCollections.observableArrayList();
        table.setItems(connectedClients);

        TableColumn<ClientConnectionData, String> colIp = new TableColumn<>("IP Address");
        colIp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIp()));
        colIp.setPrefWidth(130);

        TableColumn<ClientConnectionData, String> colHost = new TableColumn<>("Host Name");
        colHost.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHost()));
        colHost.setPrefWidth(180);

        // Shows simple numbers like "1", "2" instead of huge random numbers
        TableColumn<ClientConnectionData, String> colId = new TableColumn<>("Client ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSimpleId())));
        colId.setPrefWidth(80);

        TableColumn<ClientConnectionData, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colStatus.setPrefWidth(120);

        colStatus.setCellFactory(column -> new TableCell<ClientConnectionData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Connected".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(colIp, colHost, colId, colStatus);
        table.setPlaceholder(new Label("Waiting for connections..."));

        Button btnExit = new Button("Shutdown Server");
        btnExit.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExit.setOnAction(e -> {
            try { if (server != null) server.close(); } catch (Exception ex) {}
            System.exit(0);
        });

        VBox root = new VBox(15, lblHeader, table, btnExit);
        root.setPadding(new Insets(20));
        root.setPrefSize(600, 400);

        startServer();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    private void startServer() {
        server = new BistroServer(5555, (client, isConnected) -> {
            Platform.runLater(() -> updateClientList(client, isConnected));
        });

        try {
            server.listen();
        } catch (IOException e) {
            System.out.println("Error listening on port 5555");
        }
    }

    private void updateClientList(ConnectionToClient client, boolean isConnected) {
        long threadId = client.getId(); // The original long ID from OCSF

        if (isConnected) {
            // Assign a simple ID (1, 2, 3) for display purposes
            if (!clientSimpleIdMap.containsKey(threadId)) {
                clientSimpleIdMap.put(threadId, idCounter++);
            }
            int simpleId = clientSimpleIdMap.get(threadId);

            // Check duplicates
            for (ClientConnectionData data : connectedClients) {
                if (data.getOriginalId() == threadId) {
                    data.setStatus("Connected");
                    table.refresh();
                    return;
                }
            }

            // Add new
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();
            connectedClients.add(new ClientConnectionData(threadId, simpleId, ip, host, "Connected"));

        } else {
            // Disconnect logic
            for (ClientConnectionData data : connectedClients) {
                if (data.getOriginalId() == threadId) {
                    data.setStatus("Disconnected");
                    break;
                }
            }
            table.refresh();
        }
    }

    // --- Data Class ---
    public static class ClientConnectionData {
        private final long originalId; // Hidden internal ID
        private final int simpleId;    // Visible ID (1, 2, 3...)
        private final String ip;
        private final String host;
        private final SimpleStringProperty status;

        public ClientConnectionData(long originalId, int simpleId, String ip, String host, String status) {
            this.originalId = originalId;
            this.simpleId = simpleId;
            this.ip = ip;
            this.host = host;
            this.status = new SimpleStringProperty(status);
        }

        public long getOriginalId() { return originalId; }
        public int getSimpleId() { return simpleId; }
        public String getIp() { return ip; }
        public String getHost() { return host; }
        
        public SimpleStringProperty statusProperty() { return status; }
        public void setStatus(String s) { this.status.set(s); }
    }
}