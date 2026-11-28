package latihan.repository;

import latihan.db.DBConnection;
import latihan.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    /** @return user, atau null jika username tidak ditemukan. */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, password, role "
                + "FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        }
    }

    public boolean existsByUsernameOrEmail(String username, String email)
            throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? OR email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Mendaftarkan akun baru dengan role USER. Password harus sudah di-hash. */
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (email, username, password, role) "
                + "VALUES (?, ?, ?, 'USER')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.executeUpdate();
        }
    }
}
