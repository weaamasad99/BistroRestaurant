package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    private static String username;
    private static String password;

    // Load credentials once when class is loaded
    static {
        loadEnv();
    }

    private static void loadEnv() {
        try {
            // Check where we are looking for the file
            System.out.println("EmailService: Looking for .env in: " + System.getProperty("user.dir"));
            
            if (Files.exists(Paths.get(".env"))) {
                for (String line : Files.readAllLines(Paths.get(".env"))) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        if (parts[0].trim().equals("MAIL_USERNAME")) username = parts[1].trim();
                        if (parts[0].trim().equals("MAIL_PASSWORD")) password = parts[1].trim();
                    }
                }
                if (username != null) System.out.println("EmailService: Credentials loaded for " + username);
            } else {
                System.out.println("EmailService: .env file NOT found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendEmail(String recipient, String subject, String body) {
        if (username == null || password == null) {
            System.out.println("Log: Credentials missing. Cannot send email.");
            return;
        }

        System.out.println("EmailService: Preparing to send email to " + recipient + "...");

        try {
            // STEP 1: Setup Properties (Using setProperty ensures Strings)
            Properties prop = new Properties();
            prop.setProperty("mail.smtp.host", "smtp.gmail.com");
            prop.setProperty("mail.smtp.port", "587");
            prop.setProperty("mail.smtp.auth", "true");
            prop.setProperty("mail.smtp.starttls.enable", "true");
            prop.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // STEP 2: Create Session WITHOUT Authenticator (Prevents ClassCastException here)
            // We will authenticate later in the Transport.connect() method.
            Session session = Session.getInstance(prop);
            System.out.println("Debug: Session created successfully.");

            // STEP 3: Create the Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // STEP 4: Explicit Connection & Send
            System.out.println("Debug: Connecting to Gmail...");
            
            Transport transport = session.getTransport("smtp");
            
            // This acts as the login
            transport.connect("smtp.gmail.com", username, password);
            
            // Send the message
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            
            System.out.println("EmailService: SUCCESS! Email sent to " + recipient);

        } catch (AuthenticationFailedException e) {
            System.err.println("EmailService: Login Failed! Check your App Password in .env");
        } catch (Exception e) {
            System.err.println("EmailService: Error sending email (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
}