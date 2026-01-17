package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class responsible strictly for establishing and providing 
 * the JDBC connection to the MySQL database.
 * Ensures only one connection instance exists throughout the application lifecycle.
 * * @author Group 6
 * @version 1.0
 */
public class DatabaseConnection {
    
    /** The single instance of this class. */
    private static DatabaseConnection instance = null;
    
    /** The active SQL connection object. */
    private static Connection conn = null;

    /** Database URL string including timezone configuration. */
    private static final String URL = "jdbc:mysql://localhost:3306/bistro_db?serverTimezone=UTC";
    
    /** Database username. */
    private static final String USER = "root";        
    
    /** Database password. */
    private static final String PASSWORD = "Aa123456";

    /**
     * Private Constructor to prevent instantiation.
     * Loads the MySQL JDBC driver and establishes the connection.
     */
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(">>> Connected Successfully to MySQL");
        } catch (SQLException e) {
            System.err.println("SQL Connection Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver Not Found: " + e.getMessage());
        }
    }

    /**
     * Retrieves the singleton instance of the DatabaseConnection.
     * Creates the instance if it does not already exist.
     * * @return The singleton DatabaseConnection instance.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Provides the active connection to other controllers.
     * * @return The SQL Connection object.
     */
    public Connection getConnection() {
        return conn;
    }
}