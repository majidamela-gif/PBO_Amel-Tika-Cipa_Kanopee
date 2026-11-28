package latihan.model;

import java.sql.Timestamp;

/** Baris pesanan untuk admin (hasil JOIN orders + users + dining_tables). */
public class OrderModel {
    private int id;
    private Timestamp orderDate;
    private String username;
    private String tableNumber;
    private String status;
    private double total;

    public OrderModel() {
    }

    public OrderModel(int id, Timestamp orderDate, String username,
                      String tableNumber, String status, double total) {
        this.id = id;
        this.orderDate = orderDate;
        this.username = username;
        this.tableNumber = tableNumber;
        this.status = status;
        this.total = total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
