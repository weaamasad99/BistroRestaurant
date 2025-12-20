package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.util.Random;

public class SubscriberRegistrationUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack; 

    public SubscriberRegistrationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
    }

    public void start() {
        showRegistrationForm();
    }

    private void showRegistrationForm() {
        mainLayout.getChildren().clear();

        Label header = new Label("Register New Subscriber");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // שדות הקלט (לפי הדיאגרמה והסיפור)
        TextField txtFirstName = new TextField(); txtFirstName.setPromptText("First Name");
        TextField txtLastName = new TextField(); txtLastName.setPromptText("Last Name");
        TextField txtPhone = new TextField(); txtPhone.setPromptText("05X-XXXXXXX");
        TextField txtEmail = new TextField(); txtEmail.setPromptText("email@example.com");
        TextField txtUsername = new TextField(); txtUsername.setPromptText("Choose Username");
        
        // הערה: אין שדה סיסמה!

        grid.add(new Label("First Name:"), 0, 0); grid.add(txtFirstName, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1); grid.add(txtLastName, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(txtPhone, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(txtEmail, 1, 3);
        grid.add(new Label("Username:"), 0, 4); grid.add(txtUsername, 1, 4);

        Button btnRegister = new Button("Register & Generate ID");
        btnRegister.setPrefWidth(200);
        btnRegister.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true;");
        btnBack.setOnAction(e -> onBack.run());

        // logic
        btnRegister.setOnAction(e -> {
            String fName = txtFirstName.getText();
            String lName = txtLastName.getText();
            String phone = txtPhone.getText();
            String email = txtEmail.getText();
            String user = txtUsername.getText();

            if (validateInput(user, phone, email, fName)) {
                createSubscriber(user, phone, email, fName, lName);
            }
        });

        VBox content = new VBox(20, header, grid, btnRegister, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    // --- Methods from Class Diagram ---

    private boolean validateInput(String userName, String phoneNumber, String email, String fName) {
        if (userName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || fName.isEmpty()) {
            mainUI.showAlert("Validation Error", "All fields are required.");
            return false;
        }
        if (!phoneNumber.matches("\\d{10}")) { // Simple validation
            mainUI.showAlert("Validation Error", "Phone must be 10 digits.");
            return false;
        }
        return true;
    }

    private void createSubscriber(String userName, String phone, String email, String fName, String lName) {
        // 1. Generate ID
        String newSubscriberID = String.valueOf(1000 + new Random().nextInt(9000)); // Mock ID generation
        
        // 2. Generate Digital Card (Simulation)
        generateDigitalCard(newSubscriberID);

        // 3. Show Success & ID to Representative
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText("New Subscriber Created!");
        alert.setContentText("Subscriber: " + fName + " " + lName + "\n" +
                             "Username: " + userName + "\n" +
                             "Generated ID: " + newSubscriberID + "\n\n" + 
                             "Please give this ID to the customer.");
        alert.showAndWait();
        
        // 4. Return
        onBack.run();
    }

    private void generateDigitalCard(String subscriberID) {
        System.out.println("Simulating QR Code generation for ID: " + subscriberID);
    }
}