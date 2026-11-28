package latihan.repository;

import latihan.db.DBConnection;
import latihan.entity.DiningTable;
import latihan.entity.Order;
import latihan.model.CartItemModel;
import latihan.model.OrderDetailModel;
import latihan.model.OrderHistoryModel;
import latihan.model.OrderModel;
import latihan.model.SalesReportModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderRepository {

    private static final String SELECT_ORDER_MODEL =
            "SELECT o.id, o.order_date, u.username, d.table_number, "
            + "o.status, o.total "
            + "FROM orders o "
            + "JOIN users u ON u.id = o.user_id "
            + "JOIN dining_tables d ON d.id = o.table_id "
            + "WHERE 1 = 1 ";

    // ===================== ADMIN =====================

    /**
     * @param finished    true = pesanan SELESAI, false = yang belum selesai
     * @param tableNumber nomor meja, atau null untuk semua meja
     */
    public List<OrderModel> findOrders(boolean finished, String tableNumber)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ORDER_MODEL);

        if (finished) {
            sql.append("AND o.status = 'SELESAI' ");
        } else {
            sql.append("AND o.status IN ('MENUNGGU', 'DIPROSES', 'SIAP DIANTAR') ");
        }
        if (tableNumber != null) {
            sql.append("AND d.table_number = ? ");
        }
        sql.append("ORDER BY o.id DESC");

        List<OrderModel> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (tableNumber != null) {
                stmt.setString(1, tableNumber);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderModel(rs));
                }
            }
        }
        return list;
    }

    /** from / to boleh null (tanpa batas). */
    public SalesReportModel getSalesReport(Date from, Date to)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_ORDER_MODEL);
        List<Date> params = new ArrayList<>();

        if (from != null) {
            sql.append("AND DATE(o.order_date) >= DATE(?) ");
            params.add(from);
        }
        if (to != null) {
            sql.append("AND DATE(o.order_date) <= DATE(?) ");
            params.add(to);
        }
        sql.append("ORDER BY o.order_date DESC");

        SalesReportModel report = new SalesReportModel();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setDate(i + 1, new java.sql.Date(params.get(i).getTime()));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    report.addOrder(mapOrderModel(rs));
                }
            }
        }
        return report;
    }

    /**
     * Mengubah status pesanan. Jika menjadi SELESAI, meja otomatis
     * dikembalikan menjadi TERSEDIA (dalam satu transaksi).
     */
    public void updateStatus(int orderId, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE orders SET status = ? WHERE id = ?")) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, orderId);
                    stmt.executeUpdate();
                }

                if (Order.SELESAI.equals(newStatus)) {
                    String releaseSql = "UPDATE dining_tables "
                            + "SET status = 'TERSEDIA' "
                            + "WHERE id = (SELECT table_id FROM orders WHERE id = ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(releaseSql)) {
                        stmt.setInt(1, orderId);
                        stmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ===================== USER =====================

    public List<OrderHistoryModel> findHistoryByUser(int userId)
            throws SQLException {
        String sql = "SELECT o.id, o.order_date, d.table_number, o.status, "
                + "o.total, o.paid, o.change_amount "
                + "FROM orders o "
                + "JOIN dining_tables d ON d.id = o.table_id "
                + "WHERE o.user_id = ? ORDER BY o.id DESC";
        List<OrderHistoryModel> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderHistoryModel(
                            rs.getInt("id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("table_number"),
                            rs.getString("status"),
                            rs.getDouble("total"),
                            rs.getDouble("paid"),
                            rs.getDouble("change_amount")
                    ));
                }
            }
        }
        return list;
    }

    /** Detail item pesanan. userId dipakai agar user hanya bisa lihat miliknya. */
    public List<OrderDetailModel> findDetails(int orderId, int userId)
            throws SQLException {
        String sql = "SELECT m.name, oi.quantity, oi.price, oi.subtotal "
                + "FROM order_items oi "
                + "JOIN menus m ON m.id = oi.menu_id "
                + "JOIN orders o ON o.id = oi.order_id "
                + "WHERE oi.order_id = ? AND o.user_id = ? "
                + "ORDER BY oi.id";
        List<OrderDetailModel> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderDetailModel(
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getDouble("subtotal")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Membuat pesanan + item, mengurangi stok, dan menandai meja TERISI
     * dalam satu transaksi. Jika ada yang gagal, semuanya di-rollback.
     *
     * @return id pesanan baru
     */
    public int createOrder(Order order, List<CartItemModel> items)
            throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                lockAvailableTable(conn, order.getTableId());
                int orderId = insertOrder(conn, order);
                insertItemsAndReduceStock(conn, orderId, items);
                markTableOccupied(conn, order.getTableId());

                conn.commit();
                return orderId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ===================== PRIVATE =====================

    private void lockAvailableTable(Connection conn, int tableId)
            throws SQLException {
        String sql = "SELECT status FROM dining_tables WHERE id = ? FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Meja tidak ditemukan.");
                }
                if (!DiningTable.STATUS_TERSEDIA.equalsIgnoreCase(
                        rs.getString("status"))) {
                    throw new SQLException(
                            "Meja sudah digunakan oleh pelanggan lain.");
                }
            }
        }
    }

    private int insertOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders "
                + "(user_id, table_id, status, total, paid, change_amount) "
                + "VALUES (?, ?, 'MENUNGGU', ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUserId());
            stmt.setInt(2, order.getTableId());
            stmt.setDouble(3, order.getTotal());
            stmt.setDouble(4, order.getPaid());
            stmt.setDouble(5, order.getChangeAmount());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Gagal mendapatkan ID pesanan.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertItemsAndReduceStock(Connection conn, int orderId,
                                           List<CartItemModel> items)
            throws SQLException {
        String itemSql = "INSERT INTO order_items "
                + "(order_id, menu_id, quantity, price, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";

        // Syarat "stock >= ?" mencegah stok minus kalau ada pembeli lain.
        String stockSql = "UPDATE menus SET stock = stock - ? "
                + "WHERE id = ? AND status = 'TERSEDIA' AND stock >= ?";

        // Kalau stok jadi 0 setelah dibeli, status otomatis HABIS.
        String soldOutSql = "UPDATE menus SET status = 'HABIS' "
                + "WHERE id = ? AND stock = 0";

        try (PreparedStatement itemStmt = conn.prepareStatement(itemSql);
             PreparedStatement stockStmt = conn.prepareStatement(stockSql);
             PreparedStatement soldOutStmt = conn.prepareStatement(soldOutSql)) {

            for (CartItemModel item : items) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getMenuId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.setDouble(5, item.getSubtotal());
                itemStmt.executeUpdate();

                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getMenuId());
                stockStmt.setInt(3, item.getQuantity());
                if (stockStmt.executeUpdate() == 0) {
                    throw new SQLException("Stok menu " + item.getName()
                            + " tidak mencukupi.");
                }

                soldOutStmt.setInt(1, item.getMenuId());
                soldOutStmt.executeUpdate();
            }
        }
    }

    private void markTableOccupied(Connection conn, int tableId)
            throws SQLException {
        String sql = "UPDATE dining_tables SET status = 'TERISI' WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            stmt.executeUpdate();
        }
    }

    private OrderModel mapOrderModel(ResultSet rs) throws SQLException {
        return new OrderModel(
                rs.getInt("id"),
                rs.getTimestamp("order_date"),
                rs.getString("username"),
                rs.getString("table_number"),
                rs.getString("status"),
                rs.getDouble("total")
        );
    }
}
