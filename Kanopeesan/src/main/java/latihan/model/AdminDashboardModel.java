package latihan.model;

public class AdminDashboardModel {
    private int totalTables;
    private int occupiedTables;
    private int totalMenus;
    private int totalTransactions;
    private int pendingOrders;
    private double totalRevenue;

    public AdminDashboardModel() {
    }

    public int getTotalTables() { return totalTables; }
    public void setTotalTables(int totalTables) { this.totalTables = totalTables; }

    public int getOccupiedTables() { return occupiedTables; }
    public void setOccupiedTables(int occupiedTables) { this.occupiedTables = occupiedTables; }

    public int getTotalMenus() { return totalMenus; }
    public void setTotalMenus(int totalMenus) { this.totalMenus = totalMenus; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}
