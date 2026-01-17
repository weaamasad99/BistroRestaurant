package client;

import controllers.CasualController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * The CheckoutUI class handles the user interface for the billing and checkout process.
 * It guides the user through entering an order code, reviewing the bill (with applicable discounts),
 * and processing the final payment.
 */
public class CheckoutUI {

    private VBox mainLayout;
    private ClientUI mainUI;
    private Runnable onBack;
    private CasualController casualController;

    /**
     * Constructs a new CheckoutUI instance.
     *
     * @param mainLayout The main layout container where the UI will be rendered.
     * @param mainUI     The main application instance.
     * @param onBack     A Runnable callback to execute when the user navigates back.
     */
    public CheckoutUI(VBox mainLayout, ClientUI mainUI, Runnable onBack) {
        this.mainLayout = mainLayout;
        this.mainUI = mainUI;
        this.onBack = onBack;
        this.casualController = new CasualController(mainUI.controller);
    }

    /**
     * Starts the checkout process by registering this UI instance with the main application
     * and displaying the code input form.
     */
    public void start() {
    	this.mainUI.checkoutUI = this;
        showCodeInputForm();
    }

    /**
     * Displays the initial form where the user enters their Order Confirmation Code.
     * Corresponds to Step 1 of the checkout process.
     */
    private void showCodeInputForm() {
        mainLayout.getChildren().clear();

        // Header
        Label header = new Label("Bill Checkout");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label instruction = new Label("Please enter your Order Confirmation Code:");

        // Input Field
        TextField txtCode = new TextField();
        txtCode.setPromptText("e.g. 1001");
        txtCode.setMaxWidth(200);

        // Buttons
        Button btnNext = new Button("View Bill");
        btnNext.setPrefWidth(200);
        btnNext.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnBack = new Button("Back to Menu");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-underline: true; -fx-cursor: hand;");
        btnBack.setOnAction(e -> onBack.run());

        // Logic
        btnNext.setOnAction(e -> {
            String code = txtCode.getText().trim();
            if (code.isEmpty()) {
                mainUI.showAlert("Error", "Please enter a code.");
            } else {
                casualController.getBill(code);
            }
        });

        VBox content = new VBox(20, header, instruction, txtCode, btnNext, btnBack);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

    /**
     * Displays the bill details, including the subtotal, discounts, and final total.
     * Corresponds to Step 2 of the checkout process (Review & Pay).
     *
     * @param code           The order code associated with the bill.
     * @param originalAmount The subtotal amount before any discounts.
     * @param isSubscriber   Indicates whether the user is a subscriber (eligible for a discount).
     */
    public void showBillDetails(String code, double originalAmount, boolean isSubscriber) {
        mainLayout.getChildren().clear();

        Label header = new Label("Payment Details");
        header.setFont(new Font("Arial", 24));
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // 1. Order Code
        grid.add(new Label("Order Code:"), 0, 0);
        Label lblCode = new Label(code);
        lblCode.setStyle("-fx-font-weight: bold;");
        grid.add(lblCode, 1, 0);

        // 2. Original Amount
        grid.add(new Label("Subtotal:"), 0, 1);
        Label lblAmount = new Label("$" + String.format("%.2f", originalAmount));
        grid.add(lblAmount, 1, 1);

        // 3. Discount (If Subscriber)
        double discount = 0;
        if (isSubscriber) {
            discount = originalAmount * 0.10; // 10% Discount
            Label lblDiscTitle = new Label("Subscriber Discount (10%):");
            lblDiscTitle.setStyle("-fx-text-fill: green;");
            
            Label lblDiscValue = new Label("- $" + String.format("%.2f", discount));
            lblDiscValue.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            
            grid.add(lblDiscTitle, 0, 2);
            grid.add(lblDiscValue, 1, 2);
        } else {
            grid.add(new Label("Discount:"), 0, 2);
            grid.add(new Label("- $0.00"), 1, 2);
        }

        // 4. Final Total
        double finalTotal = originalAmount - discount;
        Label lblTotalTitle = new Label("TOTAL TO PAY:");
        lblTotalTitle.setFont(new Font("Arial", 18));
        lblTotalTitle.setStyle("-fx-font-weight: bold;");
        
        Label lblTotalValue = new Label("$" + String.format("%.2f", finalTotal));
        lblTotalValue.setFont(new Font("Arial", 18));
        lblTotalValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // Separator Line
        Separator sep = new Separator();
        
        // Pay Button
        Button btnPay = new Button("Pay Now");
        btnPay.setPrefWidth(200);
        btnPay.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #d32f2f; -fx-underline: true; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> onBack.run());

        // Payment Logic
        btnPay.setOnAction(e -> {
            casualController.payBill(code);
           
            // Future: Send payment to server
            /*mainUI.showAlert("Payment Successful", 
                "Transaction Approved!\n" +
                "Amount Paid: $" + String.format("%.2f", finalTotal) + "\n\n" +
                "The table is now free.");*/
            if (mainUI.currentUser.getUserType().equals("CASUAL"))
            	mainUI.showRoleSelectionScreen();
            else
            	onBack.run();
        });

        VBox content = new VBox(20, header, grid, sep, new VBox(5, lblTotalTitle, lblTotalValue), btnPay, btnCancel);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        mainLayout.getChildren().add(content);
    }

}