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

    // Load credentials from .env file upon class loading
    static {
        try {
            loadEnv();
        } catch (Exception e) {
            System.err.println("Error loading .env file. Email service will not work.");
            e.printStackTrace();
        }
    }

    private static void loadEnv() throws IOException {
        // Simple manual parsing of .env to avoid external dependencies like Dotenv
        // Assumes .env is in the project root
        for (String line : Files.readAllLines(Paths.get(".env"))) {
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (key.equals("MAIL_USERNAME")) username = value;
                if (key.equals("MAIL_PASSWORD")) password = value;
            }
        }
    }

    public static void sendEmail(String recipient, String subject, String body) {
        if (username == null || password == null) {
            System.out.println("Log: Email credentials missing. Skipping email to " + recipient);
            return;
        }

        // SMTP Server Properties
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            
            // Allow HTML content for better formatting
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println(">>> [REAL EMAIL SENT] To: " + recipient + " | Subject: " + subject);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}