package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class responsible strictly for establishing and providing 
 * the JDBC connection to the MySQL database.
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance = null;
    private static Connection conn = null;

    private static final String URL = "jdbc:mysql://localhost:3306/bistro_db?serverTimezone=UTC";
    private static final String USER = "root";        
    private static final String PASSWORD = "Aa123456";

    // Private Constructor
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

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Provides the active connection to other controllers.
     * @return The SQL Connection object.
     */
    public Connection getConnection() {
        return conn;
    }
}