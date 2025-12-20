package common;

import java.io.Serializable;

public class Table implements Serializable {
    private int tableId; // Changed to int to match DB
    private int seats;
    private String status; // 'AVAILABLE', 'OCCUPIED', 'RESERVED'

    public Table() {} // Required for Kryo

    public Table(int tableId, int seats, String status) {
        this.tableId = tableId;
        this.seats = seats;
        this.status = status;
    }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Table " + tableId + " (" + seats + " seats) - " + status;
    }
}