package common;

import java.io.Serializable;

/**
 * Represents a restaurant table entity.
 * Contains ID, seating capacity, and current status.
 * * @author Group 6
 * @version 1.0
 */
public class Table implements Serializable {
    
    /** The unique ID of the table in the database. */
    private int tableId; 
    
    /** The number of seats at the table. */
    private int seats;
    
    /** The status of the table (e.g., 'AVAILABLE', 'OCCUPIED', 'RESERVED'). */
    private String status; 

    /**
     * No-arg constructor required for serialization frameworks like Kryo.
     */
    public Table() {} 

    /**
     * Constructs a Table with specified details.
     * * @param tableId The unique table ID.
     * @param seats Capacity of the table.
     * @param status Current status.
     */
    public Table(int tableId, int seats, String status) {
        this.tableId = tableId;
        this.seats = seats;
        this.status = status;
    }

    /** @return The table ID. */
    public int getTableId() { return tableId; }
    /** @param tableId The table ID to set. */
    public void setTableId(int tableId) { this.tableId = tableId; }

    /** @return The seating capacity. */
    public int getSeats() { return seats; }
    /** @param seats The seating capacity to set. */
    public void setSeats(int seats) { this.seats = seats; }

    /** @return The current status. */
    public String getStatus() { return status; }
    /** @param status The status to set. */
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Table " + tableId + " (" + seats + " seats) - " + status;
    }
}