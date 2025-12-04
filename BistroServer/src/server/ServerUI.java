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
        primaryStage.setTitle("Bistro Server");

        // --- UI Header ---
        Label lblHeader = new Label("Connected Clients");
        lblHeader.setFont(new Font("Arial", 20));

        // --- Table for Clients ---
        TableView<ClientConnectionData> table = new TableView<>();
        connectedClients = FXCollections.observableArrayList();
        table.setItems(connectedClients);

        TableColumn<ClientConnectionData, String> colIp = new TableColumn<>("IP Address");
        colIp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIp()));
        colIp.setPrefWidth(150);

        TableColumn<ClientConnectionData, String> colHost = new TableColumn<>("Host Name");
        colHost.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHost()));
        colHost.setPrefWidth(150);

        TableColumn<ClientConnectionData, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setPrefWidth(100);

        table.getColumns().addAll(colIp, colHost, colStatus);
        table.setPlaceholder(new Label("No clients connected"));

        // --- Exit Button ---
        Button btnExit = new Button("Close Server");
        btnExit.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        btnExit.setOnAction(e -> {
            try {
                if (server != null) server.close();
            } catch (Exception ex) {}
            System.exit(0);
        });

        // --- Layout ---
        VBox root = new VBox(10, lblHeader, table, btnExit);
        root.setPadding(new Insets(20));
        root.setPrefSize(450, 300);

        // --- Start Server ---
        startServer();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    private void startServer() {
        // Define what happens when a client connects/disconnects
        server = new BistroServer(5555, (client, isConnected) -> {
            Platform.runLater(() -> {
                updateClientList(client, isConnected);
            });
        });

        try {
            server.listen();
        } catch (IOException e) {
            System.out.println("Error listening on port 5555");
        }
    }

    private void updateClientList(ConnectionToClient client, boolean isConnected) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();

        if (isConnected) {
            // Add to table
            connectedClients.add(new ClientConnectionData(ip, host, "Connected"));
        } else {
            // Remove from table (find by IP/Host match)
            connectedClients.removeIf(c -> c.getIp().equals(ip) && c.getHost().equals(host));
        }
    }

    // --- Inner Class for Table Data ---
    public static class ClientConnectionData {
        private final String ip;
        private final String host;
        private final String status;

        public ClientConnectionData(String ip, String host, String status) {
            this.ip = ip;
            this.host = host;
            this.status = status;
        }

        public String getIp() { return ip; }
        public String getHost() { return host; }
        public String getStatus() { return status; }
    }
}