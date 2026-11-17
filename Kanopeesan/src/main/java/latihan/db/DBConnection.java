package latihan.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "kanopeesan";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false"
            + "&serverTimezone=Asia/Jakarta"
            + "&allowPublicKeyRetrieval=true";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static Connection getConnectionWithoutDatabase() throws SQLException {
        String url =
                "jdbc:mysql://" + HOST + ":" + PORT
                + "?useSSL=false"
                + "&serverTimezone=Asia/Jakarta"
                + "&allowPublicKeyRetrieval=true";

        return DriverManager.getConnection(url, USERNAME, PASSWORD);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Database berhasil terhubung.");
        } catch (SQLException e) {
            System.err.println("Database gagal terhubung.");
            e.printStackTrace();
        }
    }
}