package latihan.model;

import java.sql.Timestamp;

/** Baris riwayat transaksi milik satu user. */
public class OrderHistoryModel {
    private int id;
    private Timestamp orderDate;
    private String tableNumber;
    private String status;
    private double total;
    private double paid;
    private double changeAmount;

    public OrderHistoryModel() {
    }

    public OrderHistoryModel(int id, Timestamp orderDate, String tableNumber,
                             String status, double total, double paid,
                             double changeAmount) {
        this.id = id;
        this.orderDate = orderDate;
        this.tableNumber = tableNumber;
        this.status = status;
        this.total = total;
        this.paid = paid;
        this.changeAmount = changeAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getPaid() { return paid; }
    public void setPaid(double paid) { this.paid = paid; }

    public double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }
}
