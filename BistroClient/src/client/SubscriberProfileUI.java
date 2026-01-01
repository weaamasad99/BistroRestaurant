package client;

import common.User;
import controllers.SubscriberController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SubscriberProfileUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;
    private User currentUser;
    private SubscriberController controller;

    public SubscriberProfileUI(VBox mainLayout, ClientUI mainUI, Runnable onBack, User user) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.currentUser = user;
        this.controller = new SubscriberController(mainUI.controller);
    }

    public void start() {
        showProfileForm();
    }

    private void showProfileForm() {
        mainLayout.getChildren().clear();

        Label header = new Label("My Profile");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // --- Fields ---
        // ID (Read Only)
        TextField txtId = new TextField(String.valueOf(currentUser.getSubscriberNumber()));
        txtId.setEditable(false);
        txtId.setStyle("-fx-background-color: #e0e0e0;");

        // Name (Read Only)
        TextField txtName = new TextField(currentUser.getFirstName() + " " + currentUser.getLastName());
        txtName.setEditable(false);
        txtName.setStyle("-fx-background-color: #e0e0e0;");

        // Phone (Editable)
        TextField txtPhone = new TextField(currentUser.getPhoneNumber());
        txtPhone.setPromptText("05X-XXXXXXX");

        // Email (Editable)
        TextField txtEmail = new TextField(currentUser.getEmail());
        txtEmail.setPromptText("email@example.com");

        // Add to Grid
        grid.add(new Label("Subscriber ID:"), 0, 0); grid.add(txtId, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);     grid.add(txtName, 1, 1);
        grid.add(new Label("Phone Number:"), 0, 2);  grid.add(txtPhone, 1, 2);
        grid.add(new Label("Email:"), 0, 3);         grid.add(txtEmail, 1, 3);

        // --- Buttons ---
        Button btnSave = new Button("Save Changes");
        btnSave.setPrefWidth(200);
        btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        // --- Save Logic ---
        btnSave.setOnAction(e -> {
            String newPhone = txtPhone.getText().trim();
            String newEmail = txtEmail.getText().trim();

            if (newPhone.isEmpty() || newEmail.isEmpty()) {
                mainUI.showAlert("Validation Error", "Phone and Email cannot be empty.");
                return;
            }

            // Update the local User object
            currentUser.setPhoneNumber(newPhone);
            currentUser.setEmail(newEmail);

            // Send update to server
            controller.updateSubscriberDetails(currentUser);
            
            // Note: ClientController will handle the SUCCESS/FAIL message
        });

        VBox content = new VBox(20, header, grid, btnSave, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }
}