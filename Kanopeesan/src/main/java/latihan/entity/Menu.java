package latihan.entity;

public class Menu {
    public static final String STATUS_TERSEDIA = "TERSEDIA";
    public static final String STATUS_HABIS = "HABIS";

    private int id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private String description;
    private String status = STATUS_TERSEDIA;

    public Menu() {
    }

    public Menu(int id, String name, String category, double price,
                int stock, String description, String status) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.status = status;
        applyStockRule();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }

    /** Kalau status HABIS, stok tetap dipaksa 0 apa pun nilai yang dikirim. */
    public void setStock(int stock) {
        this.stock = stock;
        applyStockRule();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }

    /** Mengubah status ke HABIS otomatis membuat stok menjadi 0. */
    public void setStatus(String status) {
        this.status = status;
        applyStockRule();
    }

    public boolean isHabis() {
        return STATUS_HABIS.equalsIgnoreCase(status);
    }

    /** Menu bisa dipesan jika status TERSEDIA dan stok masih ada. */
    public boolean isAvailable() {
        return !isHabis() && stock > 0;
    }

    // Aturan bisnis: status HABIS => stok selalu 0.
    private void applyStockRule() {
        if (isHabis()) {
            this.stock = 0;
        }
    }

    /** @return pesan error, atau null jika data valid. */
    public String validate() {
        if (name == null || name.trim().isEmpty()) {
            return "Nama menu wajib diisi.";
        }
        if (price < 0) {
            return "Harga tidak boleh negatif.";
        }
        if (stock < 0) {
            return "Stok tidak boleh negatif.";
        }
        if (!isHabis() && stock == 0) {
            return "Menu berstatus Tersedia harus punya stok lebih dari 0.\n"
                    + "Isi stok, atau ubah status menjadi Habis.";
        }
        return null;
    }
}
