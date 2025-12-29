package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.util.Random;

import common.User;
import controllers.RepresentativeController;

public class SubscriberRegistrationUI {

	private RepresentativeController repController;
	
    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;

    // Fields from Class Diagram
    private TextField txtUserName;
    private TextField txtPhoneNumber;
    private TextField txtEmail;
    
    // Additional fields
    private TextField txtFirstName;
    private TextField txtLastName;

    public SubscriberRegistrationUI(VBox mainLayout, ClientUI mainUI, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.repController = new RepresentativeController(mainUI.controller);
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

        // Fields
        txtFirstName = new TextField(); txtFirstName.setPromptText("First Name");
        txtLastName = new TextField(); txtLastName.setPromptText("Last Name");
        txtPhoneNumber = new TextField(); txtPhoneNumber.setPromptText("05X-XXXXXXX");
        txtEmail = new TextField(); txtEmail.setPromptText("email@example.com");
        txtUserName = new TextField(); txtUserName.setPromptText("Choose Username");

        grid.add(new Label("First Name:"), 0, 0); grid.add(txtFirstName, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1); grid.add(txtLastName, 1, 1);
        grid.add(new Label("Phone Number:"), 0, 2); grid.add(txtPhoneNumber, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(txtEmail, 1, 3);
        grid.add(new Label("Username:"), 0, 4); grid.add(txtUserName, 1, 4);

        Button btnRegister = new Button("Register & Generate ID");
        btnRegister.setPrefWidth(200);
        btnRegister.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        
        // --- NAVIGATION BACK ---
        btnBack.setOnAction(e -> onBack.run());

        // Logic
        btnRegister.setOnAction(e -> {
            String uName = txtUserName.getText();
            String phone = txtPhoneNumber.getText();
            String mail = txtEmail.getText();
            
            if (validateInput(uName, phone, mail)) {
                createSubscriber(uName, phone, mail);
            }
        });

        VBox content = new VBox(20, header, grid, btnRegister, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    // Methods from Diagram
    public boolean validateInput(String userName, String phoneNumber, String Email) {
        if (userName.isEmpty() || phoneNumber.isEmpty() || Email.isEmpty()) {
            mainUI.showAlert("Validation Error", "All fields are required.");
            return false;
        }
        return true;
    }

    public void createSubscriber(String userName, String phoneNumber, String Email) {
        String fName = txtFirstName.getText();
        String lName = txtLastName.getText();

        // Create the User object
        // Note: ID is 0 and SubscriberNum is null because the Server generates them
        User newUser = new common.User(0, phoneNumber, Email, fName, lName, "SUBSCRIBER", null, userName, null);

        // Send to Server via the controller
        repController.registerSubscriber(newUser);
        
        // Navigate back immediately (The ClientController will show the success popup when the server responds)
        onBack.run();
    }
        

    public void generateDigitalCard(String subscriberID) {
        System.out.println("LOG: Generating QR Digital Card for Subscriber ID: " + subscriberID);
    }
}