package latihan.repository;

import latihan.db.DBConnection;
import latihan.model.AdminDashboardModel;
import latihan.model.UserDashboardModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardRepository {

    private static final String PENDING_STATUS =
            "('MENUNGGU', 'DIPROSES', 'SIAP DIANTAR')";

    public AdminDashboardModel getAdminDashboard() throws SQLException {
        AdminDashboardModel model = new AdminDashboardModel();

        try (Connection conn = DBConnection.getConnection()) {
            model.setTotalTables(
                    count(conn, "SELECT COUNT(*) FROM dining_tables", null));
            model.setOccupiedTables(
                    count(conn, "SELECT COUNT(*) FROM dining_tables "
                            + "WHERE status = 'TERISI'", null));
            model.setTotalMenus(
                    count(conn, "SELECT COUNT(*) FROM menus", null));
            model.setTotalTransactions(
                    count(conn, "SELECT COUNT(*) FROM orders", null));
            model.setPendingOrders(
                    count(conn, "SELECT COUNT(*) FROM orders "
                            + "WHERE status <> 'SELESAI' "
                            + "AND status <> 'DIBATALKAN'", null));

            String revenueSql = "SELECT COALESCE(SUM(total), 0) FROM orders "
                    + "WHERE status = 'SELESAI'";
            try (PreparedStatement stmt = conn.prepareStatement(revenueSql);
                 ResultSet rs = stmt.executeQuery()) {
                rs.next();
                model.setTotalRevenue(rs.getDouble(1));
            }
        }
        return model;
    }

    public UserDashboardModel getUserDashboard(int userId) throws SQLException {
        UserDashboardModel model = new UserDashboardModel();

        String tableSql = "SELECT d.table_number FROM dining_tables d "
                + "JOIN orders o ON o.table_id = d.id "
                + "WHERE o.user_id = ? AND o.status IN " + PENDING_STATUS + " "
                + "ORDER BY o.id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(tableSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        model.setMyTable(rs.getString("table_number"));
                    }
                }
            }

            model.setOrdersInProgress(
                    count(conn, "SELECT COUNT(*) FROM orders "
                            + "WHERE user_id = ? AND status IN "
                            + PENDING_STATUS, userId));
            model.setTotalTransactions(
                    count(conn, "SELECT COUNT(*) FROM orders "
                            + "WHERE user_id = ?", userId));
        }
        return model;
    }

    private int count(Connection conn, String sql, Integer userId)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
