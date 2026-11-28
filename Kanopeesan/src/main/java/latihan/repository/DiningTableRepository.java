package latihan.repository;

import latihan.db.DBConnection;
import latihan.entity.DiningTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiningTableRepository {

    public List<DiningTable> findAll() throws SQLException {
        String sql = "SELECT id, table_number, capacity, status "
                + "FROM dining_tables ORDER BY id";
        List<DiningTable> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Daftar nomor meja (untuk filter), diurutkan berdasarkan nomor. */
    public List<String> findAllNumbers() throws SQLException {
        String sql = "SELECT table_number FROM dining_tables ORDER BY table_number";
        List<String> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("table_number"));
            }
        }
        return list;
    }

    /** @return meja, atau null jika tidak ditemukan. */
    public DiningTable findByNumber(String tableNumber) throws SQLException {
        String sql = "SELECT id, table_number, capacity, status "
                + "FROM dining_tables WHERE table_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Meja baru selalu berstatus TERSEDIA. */
    public void insert(DiningTable table) throws SQLException {
        String sql = "INSERT INTO dining_tables (table_number, capacity, status) "
                + "VALUES (?, ?, 'TERSEDIA')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, table.getTableNumber());
            stmt.setInt(2, table.getCapacity());
            stmt.executeUpdate();
        }
    }

    public void update(DiningTable table) throws SQLException {
        String sql = "UPDATE dining_tables "
                + "SET table_number = ?, capacity = ?, status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, table.getTableNumber());
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            stmt.setInt(4, table.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM dining_tables WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private DiningTable map(ResultSet rs) throws SQLException {
        return new DiningTable(
                rs.getInt("id"),
                rs.getString("table_number"),
                rs.getInt("capacity"),
                rs.getString("status")
        );
    }
}
