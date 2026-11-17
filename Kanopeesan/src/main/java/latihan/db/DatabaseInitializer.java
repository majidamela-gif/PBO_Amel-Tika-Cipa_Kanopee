package latihan.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        try {
            createDatabase();
            createTables();
            insertDefaultAdmin();
            insertDefaultTables();
            insertDefaultMenus();
            System.out.println("Database Kanopeesan berhasil diinisialisasi.");

        } catch (SQLException e) {
            System.err.println("Gagal menginisialisasi database.");
            e.printStackTrace();
        }
    }

    private static void createDatabase() throws SQLException {
        String sql = """
                CREATE DATABASE IF NOT EXISTS kanopeesan
                CHARACTER SET utf8mb4
                COLLATE utf8mb4_unicode_ci
                """;

        try (Connection conn = DBConnection.getConnectionWithoutDatabase();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void createTables() throws SQLException {
        String users = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String diningTables = """
                CREATE TABLE IF NOT EXISTS dining_tables (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    table_number VARCHAR(10) NOT NULL UNIQUE,
                    capacity INT NOT NULL DEFAULT 4,
                    status ENUM('TERSEDIA', 'TERISI') NOT NULL DEFAULT 'TERSEDIA'
                )
                """;

        String menus = """
                CREATE TABLE IF NOT EXISTS menus (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    price DECIMAL(15,2) NOT NULL,
                    stock INT NOT NULL DEFAULT 0,
                    description TEXT,
                    status ENUM('TERSEDIA', 'HABIS') NOT NULL DEFAULT 'TERSEDIA',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String orders = """
                CREATE TABLE IF NOT EXISTS orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    table_id INT NOT NULL,
                    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status ENUM(
                        'MENUNGGU',
                        'DIPROSES',
                        'SIAP DIANTAR',
                        'SELESAI',
                        'DIBATALKAN'
                    ) NOT NULL DEFAULT 'MENUNGGU',
                    total DECIMAL(15,2) NOT NULL DEFAULT 0,
                    paid DECIMAL(15,2) NOT NULL DEFAULT 0,
                    change_amount DECIMAL(15,2) NOT NULL DEFAULT 0,

                    CONSTRAINT fk_orders_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE RESTRICT
                        ON UPDATE CASCADE,

                    CONSTRAINT fk_orders_table
                        FOREIGN KEY (table_id)
                        REFERENCES dining_tables(id)
                        ON DELETE RESTRICT
                        ON UPDATE CASCADE
                )
                """;

        String orderItems = """
                CREATE TABLE IF NOT EXISTS order_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT NOT NULL,
                    menu_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(15,2) NOT NULL,
                    subtotal DECIMAL(15,2) NOT NULL,

                    CONSTRAINT fk_order_items_order
                        FOREIGN KEY (order_id)
                        REFERENCES orders(id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,

                    CONSTRAINT fk_order_items_menu
                        FOREIGN KEY (menu_id)
                        REFERENCES menus(id)
                        ON DELETE RESTRICT
                        ON UPDATE CASCADE
                )
                """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(users);
            stmt.executeUpdate(diningTables);
            stmt.executeUpdate(menus);
            stmt.executeUpdate(orders);
            stmt.executeUpdate(orderItems);
        }
    }

    private static void insertDefaultAdmin() throws SQLException {
        String checkSql = """
                SELECT id
                FROM users
                WHERE username = ?
                """;

        String insertSql = """
                INSERT INTO users
                    (email, username, password, role)
                VALUES
                    (?, ?, ?, 'ADMIN')
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, "admin");
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement insert =
                                 conn.prepareStatement(insertSql)) {
                        insert.setString(1, "admin@kanopeesan.com");
                        insert.setString(2, "admin");
                        insert.setString(
                                3,
                                latihan.util.PasswordUtil.hashPassword("admin123")
                        );
                        insert.executeUpdate();
                    }
                }
            }
        }
    }

    private static void insertDefaultTables() throws SQLException {
        String checkSql = """
                SELECT COUNT(*)
                FROM dining_tables
                """;

        String insertSql = """
                INSERT INTO dining_tables
                    (table_number, capacity, status)
                VALUES
                    (?, ?, 'TERSEDIA')
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql);
             ResultSet rs = check.executeQuery()) {
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                try (PreparedStatement insert =
                             conn.prepareStatement(insertSql)) {
                    for (int i = 1; i <= 10; i++) {
                        insert.setString(
                                1,
                                String.format("K%02d", i)
                        );
                        insert.setInt(2, 4);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
        }
    }

    private static void insertDefaultMenus() throws SQLException {
        String checkSql = """
                SELECT COUNT(*)
                FROM menus
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql);
             ResultSet rs = check.executeQuery()) {
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                return;
            }
        }

        String insertSql = """
                INSERT INTO menus
                    (name, category, price, stock, description, status)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            addMenu(
                    stmt,
                    "Nasi Goreng",
                    "Makanan",
                    18000,
                    20,
                    "Nasi goreng khas Kanopee",
                    "TERSEDIA"
            );

            addMenu(
                    stmt,
                    "Mie Goreng",
                    "Makanan",
                    16000,
                    20,
                    "Mie goreng spesial",
                    "TERSEDIA"
            );

            addMenu(
                    stmt,
                    "Ayam Geprek",
                    "Makanan",
                    20000,
                    15,
                    "Ayam geprek dengan sambal",
                    "TERSEDIA"
            );

            addMenu(
                    stmt,
                    "Kentang Goreng",
                    "Snack",
                    12000,
                    20,
                    "Kentang goreng crispy",
                    "TERSEDIA"
            );

            addMenu(
                    stmt,
                    "Es Teh",
                    "Minuman",
                    5000,
                    50,
                    "Es teh manis",
                    "TERSEDIA"
            );

            addMenu(
                    stmt,
                    "Kopi",
                    "Minuman",
                    10000,
                    30,
                    "Kopi hitam",
                    "TERSEDIA"
            );

            stmt.executeBatch();
        }
    }

    private static void addMenu(
            PreparedStatement stmt,
            String name,
            String category,
            double price,
            int stock,
            String description,
            String status
    ) throws SQLException {

        stmt.setString(1, name);
        stmt.setString(2, category);
        stmt.setDouble(3, price);
        stmt.setInt(4, stock);
        stmt.setString(5, description);
        stmt.setString(6, status);
        stmt.addBatch();
    }
}