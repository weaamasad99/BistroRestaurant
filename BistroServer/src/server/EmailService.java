package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Handles outgoing email notifications using Gmail SMTP.
 * Configuration is loaded from a local .env file.
 * @author Group 6
 * @version 1.0
 */
public class EmailService {

    /** SMTP Username (Email). */
    private static String username;
    
    /** SMTP Password (App Password). */
    private static String password;
    
    /** Flag to indicate if configuration was successful. */
    private static boolean isConfigured = false;

    // Static block to load credentials once when the server starts
    static {
        loadEnv();
    }

    /**
     * Loads credentials from the .env file in the root directory.
     */
    private static void loadEnv() {
        try {
            Path path = Paths.get(".env");
            System.out.println("[EmailService] Loading configuration from: " + path.toAbsolutePath());
            
            if (Files.exists(path)) {
                for (String line : Files.readAllLines(path)) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length >= 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            if (key.equals("MAIL_USERNAME")) username = value;
                            if (key.equals("MAIL_PASSWORD")) password = value;
                        }
                    }
                }
                
                if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                    isConfigured = true;
                    System.out.println("[EmailService] Service configured successfully for: " + username);
                } else {
                    System.err.println("[EmailService] Warning: Credentials found but incomplete.");
                }
            } else {
                System.err.println("[EmailService] Error: .env file NOT FOUND. Email features disabled.");
            }
        } catch (IOException e) {
            System.err.println("[EmailService] Config Error: " + e.getMessage());
        }
    }

    /**
     * Sends an email in a background thread to avoid blocking the UI/Server.
     * @param recipient The email address of the receiver.
     * @param subject The subject line.
     * @param body The email content (HTML allowed).
     */
    public static void sendEmail(String recipient, String subject, String body) {
        if (!isConfigured) {
            System.err.println("[EmailService] Failed to send: Service not configured.");
            return;
        }

        System.out.println("[EmailService] Sending email to: " + recipient);

        // 1. SMTP Properties
        Properties prop = new Properties();
        prop.setProperty("mail.smtp.host", "smtp.gmail.com");
        prop.setProperty("mail.smtp.port", "587");
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.smtp.starttls.enable", "true");
        prop.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        // 2. Session with Authenticator (The clean method)
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 3. Build Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // 4. Send
            Transport.send(message);
            
            System.out.println("[EmailService] Success: Email delivered to " + recipient);

        } catch (MessagingException e) {
            System.err.println("[EmailService] Delivery Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}