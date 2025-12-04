package common;

import java.sql.Date;

/**
 * Entity class representing an Order.
 * Note: With Kryo, implementing Serializable is NOT required.
 * However, a no-arg constructor is recommended for Kryo.
 */
public class Order {
    
    private int orderNumber;
    private Date orderDate;
    private int numberOfGuests;
    private int confirmationCode;
    private int subscriberId;
    private Date dateOfPlacingOrder;

    // Must have a no-arg constructor for Kryo (can be empty)
    public Order() {}

    public Order(int orderNumber, Date orderDate, int numberOfGuests, int confirmationCode, int subscriberId, Date dateOfPlacingOrder) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.numberOfGuests = numberOfGuests;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    // Getters and Setters (Important: Kryo accesses fields directly or via setters)
    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }
    
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    
    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    

    @Override
    public String toString() {
        return String.format("Order #%d | Date: %s | Guests: %d | Conf: %d | SubID: %d | Placed: %s",
                orderNumber, orderDate, numberOfGuests, confirmationCode, subscriberId, dateOfPlacingOrder);
    }
    
}