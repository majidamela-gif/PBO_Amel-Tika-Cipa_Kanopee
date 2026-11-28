package latihan.entity;

import java.sql.Timestamp;

public class Order {
    public static final String MENUNGGU = "MENUNGGU";
    public static final String DIPROSES = "DIPROSES";
    public static final String SIAP_DIANTAR = "SIAP DIANTAR";
    public static final String SELESAI = "SELESAI";
    public static final String DIBATALKAN = "DIBATALKAN";

    private int id;
    private int userId;
    private int tableId;
    private Timestamp orderDate;
    private String status = MENUNGGU;
    private double total;
    private double paid;
    private double changeAmount;

    public Order() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getPaid() { return paid; }
    public void setPaid(double paid) { this.paid = paid; }

    public double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }

    /** Alur status pesanan. Mengembalikan null jika sudah tidak bisa dilanjutkan. */
    public static String nextStatus(String current) {
        if (current == null) {
            return null;
        }
        return switch (current) {
            case MENUNGGU -> DIPROSES;
            case DIPROSES -> SIAP_DIANTAR;
            case SIAP_DIANTAR -> SELESAI;
            default -> null;
        };
    }

    /** Label status untuk ditampilkan di tabel. */
    public static String label(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case MENUNGGU -> "Menunggu";
            case DIPROSES -> "Diproses";
            case SIAP_DIANTAR -> "Siap Diantar";
            case SELESAI -> "Selesai";
            case DIBATALKAN -> "Dibatalkan";
            default -> status;
        };
    }
}
