package latihan.model;

/** Item di keranjang user (disimpan di memori, belum masuk database). */
public class CartItemModel {
    private int menuId;
    private String name;
    private double price;
    private int quantity;

    public CartItemModel() {
    }

    public CartItemModel(int menuId, String name, double price, int quantity) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        return price * quantity;
    }
}
