package latihan.model;

public class UserDashboardModel {
    private String myTable = "-";
    private int ordersInProgress;
    private int totalTransactions;

    public UserDashboardModel() {
    }

    public String getMyTable() { return myTable; }
    public void setMyTable(String myTable) { this.myTable = myTable; }

    public int getOrdersInProgress() { return ordersInProgress; }
    public void setOrdersInProgress(int ordersInProgress) { this.ordersInProgress = ordersInProgress; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
}
