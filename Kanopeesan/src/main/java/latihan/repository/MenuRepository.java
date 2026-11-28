package latihan.repository;

import latihan.db.DBConnection;
import latihan.entity.Menu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuRepository {

    private static final String SELECT_MENU =
            "SELECT id, name, category, price, stock, description, status FROM menus ";

    public List<Menu> findAll() throws SQLException {
        return query(SELECT_MENU + "ORDER BY id", null);
    }

    /** category null / "Semua" berarti semua kategori. */
    public List<Menu> findByCategory(String category) throws SQLException {
        if (category == null || "Semua".equalsIgnoreCase(category)) {
            return findAll();
        }
        return query(SELECT_MENU + "WHERE category = ? ORDER BY id", category);
    }

    /** @return menu, atau null jika tidak ditemukan. */
    public Menu findById(int id) throws SQLException {
        String sql = SELECT_MENU + "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Menu menu) throws SQLException {
        String sql = "INSERT INTO menus "
                + "(name, category, price, stock, status, description) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, menu.getName());
            stmt.setString(2, menu.getCategory());
            stmt.setDouble(3, menu.getPrice());
            stmt.setInt(4, menu.getStock());
            stmt.setString(5, menu.getStatus());
            stmt.setString(6, menu.getDescription());
            stmt.executeUpdate();
        }
    }

    public void update(Menu menu) throws SQLException {
        String sql = "UPDATE menus SET name = ?, category = ?, price = ?, "
                + "stock = ?, status = ?, description = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, menu.getName());
            stmt.setString(2, menu.getCategory());
            stmt.setDouble(3, menu.getPrice());
            stmt.setInt(4, menu.getStock());
            stmt.setString(5, menu.getStatus());
            stmt.setString(6, menu.getDescription());
            stmt.setInt(7, menu.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM menus WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private List<Menu> query(String sql, String param) throws SQLException {
        List<Menu> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) {
                stmt.setString(1, param);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Menu map(ResultSet rs) throws SQLException {
        return new Menu(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getString("description"),
                rs.getString("status")
        );
    }
}
