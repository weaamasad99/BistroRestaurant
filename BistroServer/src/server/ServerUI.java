package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

import java.io.IOException;

public class ServerUI extends Application {

    private BistroServer server;
    private ObservableList<ClientConnectionData> connectedClients;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bistro Server Manager");

        // --- UI Header ---
        Label lblHeader = new Label("Connected Clients Monitor");
        lblHeader.setFont(new Font("Arial", 18));
        lblHeader.setStyle("-fx-font-weight: bold;");

        // --- Table Setup ---
        TableView<ClientConnectionData> table = new TableView<>();
        connectedClients = FXCollections.observableArrayList();
        table.setItems(connectedClients);

        // Column 1: Client IP (More reliable than Host Name)
        TableColumn<ClientConnectionData, String> colIp = new TableColumn<>("IP Address");
        colIp.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        colIp.setPrefWidth(120);

        // Column 2: Host Name (DNS Name)
        TableColumn<ClientConnectionData, String> colHost = new TableColumn<>("Host Name");
        colHost.setCellValueFactory(cellData -> cellData.getValue().hostProperty());
        colHost.setPrefWidth(150);

        // Column 3: Unique Port/ID (To distinguish multiple local clients)
        TableColumn<ClientConnectionData, String> colId = new TableColumn<>("Client Port");
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colId.setPrefWidth(100);

        // Column 4: Status
        TableColumn<ClientConnectionData, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colStatus.setPrefWidth(120);

        // Add custom color to status column (Optional styling)
        colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<ClientConnectionData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Connected")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(colIp, colHost, colId, colStatus);
        table.setPlaceholder(new Label("Waiting for connections..."));

        // --- Exit Button ---
        Button btnExit = new Button("Shutdown Server");
        btnExit.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExit.setOnAction(e -> {
            try {
                if (server != null) server.close();
            } catch (Exception ex) {}
            System.exit(0);
        });

        // --- Layout ---
        VBox root = new VBox(15, lblHeader, table, btnExit);
        root.setPadding(new Insets(20));
        root.setPrefSize(600, 400);

        // --- Start Server Logic ---
        startServer();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    private void startServer() {
        // Initialize Server with a callback for connections
        server = new BistroServer(5555, (client, isConnected) -> {
            // Must update GUI on JavaFX Thread
            Platform.runLater(() -> updateClientList(client, isConnected));
        });

        try {
            server.listen();
        } catch (IOException e) {
            System.out.println("Error: Could not listen on port 5555");
        }
    }

    private void updateClientList(ConnectionToClient client, boolean isConnected) {
        // We use the unique 'client' object hash or ID to find the row
        String clientUniqueId = String.valueOf(client.getId()); // OCSF assigns an ID
        
        // If getId is null/0 depending on version, use port:
        if (client.getInetAddress() != null) {
             // In local env, the remote port is the best differentiator
             // Note: OCSF might not expose remote port easily in all versions, 
             // but let's try to use the object reference logic below.
        }

        if (isConnected) {
            // New Connection: Add to table
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();
            // Using hashCode as a simple unique visual ID if standard ID isn't clear
            String visualId = String.valueOf(client.hashCode()); 
            
            ClientConnectionData data = new ClientConnectionData(client, ip, host, visualId, "Connected");
            connectedClients.add(data);
            
        } else {
            // Disconnection: Find the row and update status
            for (ClientConnectionData data : connectedClients) {
                // Check if this row belongs to the specific client object
                if (data.getClientReference() == client) {
                    data.setStatus("Disconnected");
                    break; 
                }
            }
        }
    }

    
    public static class ClientConnectionData {
        
        private final ConnectionToClient clientReference;
        
        private final StringProperty ip;
        private final StringProperty host;
        private final StringProperty id;
        private final StringProperty status;

        public ClientConnectionData(ConnectionToClient client, String ip, String host, String id, String status) {
            this.clientReference = client;
            this.ip = new SimpleStringProperty(ip);
            this.host = new SimpleStringProperty(host);
            this.id = new SimpleStringProperty(id);
            this.status = new SimpleStringProperty(status);
        }

        public ConnectionToClient getClientReference() {
            return clientReference;
        }

        
        public StringProperty ipProperty() { return ip; }
        public StringProperty hostProperty() { return host; }
        public StringProperty idProperty() { return id; }
        public StringProperty statusProperty() { return status; }

        // Setter for Status update
        public void setStatus(String newStatus) {
            this.status.set(newStatus);
        }
    }
}