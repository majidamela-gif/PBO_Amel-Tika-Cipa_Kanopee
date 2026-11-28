package latihan.entity;

public class DiningTable {
    public static final String STATUS_TERSEDIA = "TERSEDIA";
    public static final String STATUS_TERISI = "TERISI";

    private int id;
    private String tableNumber;
    private int capacity;
    private String status = STATUS_TERSEDIA;

    public DiningTable() {
    }

    public DiningTable(int id, String tableNumber, int capacity, String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAvailable() {
        return STATUS_TERSEDIA.equalsIgnoreCase(status);
    }

    /** @return pesan error, atau null jika data valid. */
    public String validate() {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            return "Nomor meja wajib diisi.";
        }
        if (capacity <= 0) {
            return "Kapasitas harus lebih dari 0.";
        }
        return null;
    }
}
