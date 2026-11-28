package latihan.model;

import java.util.ArrayList;
import java.util.List;

/** Hasil laporan penjualan: daftar pesanan + ringkasan. */
public class SalesReportModel {
    private final List<OrderModel> orders = new ArrayList<>();
    private double totalRevenue;
    private int totalTransactions;

    public void addOrder(OrderModel order) {
        orders.add(order);
        totalRevenue += order.getTotal();
        totalTransactions++;
    }

    public List<OrderModel> getOrders() { return orders; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getTotalTransactions() { return totalTransactions; }
}
