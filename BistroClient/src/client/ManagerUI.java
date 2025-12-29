package client;

import common.Message;
import common.TaskType;
import common.User;
import controllers.ManagerController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ManagerUI extends RepresentativeUI {

    private ManagerController managerController;

    public ManagerUI(VBox mainLayout, ClientUI mainUI) {
        super(mainLayout, mainUI);
        // Initialize specific Manager Controller
        this.managerController = new ManagerController(mainUI.controller);
        // Link the inherited controller to this specific one so Rep functions work too
        this.controller = this.managerController; 
    }

    /**
     * Override the login screen to enforce Manager-specific credentials (admin/admin).
     */
    @Override
    protected void showLoginScreen() {
        mainLayout.getChildren().clear();

        Label header = new Label("Manager Login");
        header.setFont(new Font("Arial", 22));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #9C27B0;"); // Purple for Manager

        TextField txtUsername = new TextField(); 
        txtUsername.setPromptText("Manager Username"); 
        txtUsername.setMaxWidth(300);

        PasswordField txtPassword = new PasswordField(); 
        txtPassword.setPromptText("Password"); 
        txtPassword.setMaxWidth(300);

        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLogin.setPrefWidth(150);

        Button btnBack = new Button("Back");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true;");
        btnBack.setOnAction(e -> mainUI.showRoleSelectionScreen());

        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText();
            String pass = txtPassword.getText();
            
            // 1. Send Login Request to Server (Using the Manager Controller)
            // Even though we validate locally for the prototype, we still send the request 
            // so the server knows a user is active.
            managerController.staffLogin(user, pass);
            
            // 2. Strict Validation for Manager
            if (user.equals("admin") && pass.equals("admin")) {
                showDashboardScreen(user);
            } else {
                mainUI.showAlert("Access Denied", "Invalid Manager Credentials.\nTry: admin / admin");
            }
        });

        VBox content = new VBox(15, header, txtUsername, txtPassword, btnLogin, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        mainLayout.getChildren().add(content);
    }

    @Override
    protected void addManagerContent(VBox container) {
        Button viewReports = createWideButton("View Monthly Reports", "📊");
        viewReports.setStyle("-fx-background-color: #f3e5f5; -fx-border-color: #ba68c8; -fx-text-fill: #4a148c;");
        
        viewReports.setOnAction(e -> {
            //Initialize and start the Report UI
            MonthlyReportUI reportUI = new MonthlyReportUI(mainLayout, mainUI, () -> showDashboardScreen("admin"));
            reportUI.start();
        });

        container.getChildren().addAll(new Separator(), viewReports);
    }
}