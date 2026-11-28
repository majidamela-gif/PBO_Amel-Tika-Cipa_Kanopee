package latihan.model;

/** Detail item pesanan (hasil JOIN order_items + menus). */
public class OrderDetailModel {
    private String menuName;
    private int quantity;
    private double price;
    private double subtotal;

    public OrderDetailModel() {
    }

    public OrderDetailModel(String menuName, int quantity,
                            double price, double subtotal) {
        this.menuName = menuName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
