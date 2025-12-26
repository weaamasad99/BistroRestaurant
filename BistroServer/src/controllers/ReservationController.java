package controllers;

import JDBC.DatabaseConnection;
import common.Order;
import common.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manages Orders (Reservations) and Physical Table configurations.
 */
public class ReservationController {

    private Connection conn;

    public ReservationController() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ========================
    // ORDER / RESERVATION LOGIC
    // ========================

    public ArrayList<Order> getAllOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        if (conn == null) return orders;

        String query = "SELECT * FROM orders";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_number"),
                    rs.getInt("user_id"),
                    rs.getDate("order_date"),
                    rs.getTime("order_time"),
                    rs.getInt("num_of_diners"),
                    rs.getString("status"),
                    rs.getInt("confirmation_code"),
                    rs.getTime("actual_arrival_time")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean updateOrder(Order order) {
        if (conn == null) return false;
        String query = "UPDATE orders SET order_date = ?, order_time = ?, num_of_diners = ?, status = ? WHERE order_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, order.getOrderDate());
            ps.setTime(2, order.getOrderTime());
            ps.setInt(3, order.getNumberOfDiners());
            ps.setString(4, order.getStatus());
            ps.setInt(5, order.getOrderNumber());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========================
    // TABLE MANAGEMENT LOGIC
    // ========================

    public ArrayList<Table> getAllTables() {
        ArrayList<Table> tables = new ArrayList<>();
        if (conn == null) return tables;

        String query = "SELECT * FROM restaurant_tables";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                tables.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("seats"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public boolean updateTable(Table table) {
        if (conn == null) return false;
        String query = "UPDATE restaurant_tables SET seats = ?, status = ? WHERE table_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, table.getSeats());
            ps.setString(2, table.getStatus());
            ps.setInt(3, table.getTableId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addTable(Table table) {
        if (conn == null) return false;
        String query = "INSERT INTO restaurant_tables (table_id, seats, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, table.getTableId());
            ps.setInt(2, table.getSeats());
            ps.setString(3, table.getStatus());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeTable(int tableId) {
        if (conn == null) return false;
        String query = "DELETE FROM restaurant_tables WHERE table_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tableId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}