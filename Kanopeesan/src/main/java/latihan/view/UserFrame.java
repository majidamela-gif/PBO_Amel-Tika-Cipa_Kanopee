/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package latihan.view;

import latihan.db.DBConnection;
import latihan.util.Session;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Atika
 */
public class UserFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UserFrame.class.getName());
    private final List<CartItem> cart = new ArrayList<>();
    private int selectedTableId = -1;
    private String selectedTableNumber = null;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("id", "ID")
            );

    private static class CartItem {
        int menuId;
        String name;
        double price;
        int quantity;

        CartItem(
                int menuId,
                String name,
                double price,
                int quantity
        ) {
            this.menuId = menuId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        double getSubtotal() {
            return price * quantity;
        }
    }

    /**
     * Creates new form UserFrame
     */
    public UserFrame() {
        initComponents();
        setLocationRelativeTo(null);

        // Spinner minimal 1
        JumlahPesananSpinner.setModel(
                new SpinnerNumberModel(
                        1,
                        1,
                        100,
                        1
                )
        );

        JumlahSpinner.setModel(
                new SpinnerNumberModel(
                        1,
                        1,
                        100,
                        1
                )
        );

        // Dialog pembayaran
        jDialog1.setModal(true);
        jDialog1.setResizable(false);
        jDialog1.pack();
        jDialog1.setLocationRelativeTo(this);

        // Kategori
        KategoriMenuComboBox.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "Semua",
                            "Makanan",
                            "Minuman",
                            "Snack"
                        }
                )
        );

        // Klik history untuk melihat detail
        RiwayatTransaksiTabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            loadSelectedTransactionDetail();
                        }
                    }
                }
        );

        // Klik tabel meja untuk memilih nomor meja
        PilihMejaTabel.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int selectedRow = PilihMejaTabel.getSelectedRow();
                    if (selectedRow != -1) {
                        String nomorMeja = PilihMejaTabel.getValueAt(selectedRow, 0).toString();
                        NomorMejaField.setSelectedItem(nomorMeja);
                    }
                }
            }
        );

        loadTables();
        loadMenus();
        loadCart();
        loadHistory();
        updateDashboard();
    }
    
    private String formatRupiah(double amount) {
        return currencyFormat.format(amount).replace("Rp", "Rp ");
    }

    private void loadTables() {
        DefaultTableModel model =
                (DefaultTableModel) PilihMejaTabel.getModel();
        model.setRowCount(0);
        NomorMejaField.removeAllItems();

        String sql = """
                SELECT id, table_number, capacity, status
                FROM dining_tables
                ORDER BY id
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String number = rs.getString("table_number");
                int capacity = rs.getInt("capacity");
                String status = rs.getString("status");
                model.addRow(new Object[]{
                    number,
                    capacity,
                    status
                });

                if ("TERSEDIA".equalsIgnoreCase(status)) {
                    NomorMejaField.addItem(number);
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void loadMenus() {
        loadMenusByCategory(
                KategoriMenuComboBox.getSelectedItem() == null
                        ? "Semua"
                        : KategoriMenuComboBox
                                .getSelectedItem()
                                .toString()
        );
    }

    private void loadMenusByCategory(String category) {
        DefaultTableModel model =
                (DefaultTableModel) DaftarMenuTabel.getModel();

        model.setRowCount(0);
        String sql;
        boolean filter =
                category != null
                && !category.equalsIgnoreCase("Semua");

        if (filter) {
            sql = """
                    SELECT
                        id,
                        name,
                        category,
                        price,
                        stock,
                        description,
                        status
                    FROM menus
                    WHERE category = ?
                    ORDER BY id
                    """;
        } else {
            sql = """
                    SELECT
                        id,
                        name,
                        category,
                        price,
                        stock,
                        description,
                        status
                    FROM menus
                    ORDER BY id
                    """;
        }

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            if (filter) {
                stmt.setString(1, category);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int stock = rs.getInt("stock");
                    String status = rs.getString("status");
                    String availability =
                            stock > 0
                                    && "TERSEDIA".equalsIgnoreCase(status)
                                    ? "Tersedia"
                                    : "Habis";

                    model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("category"),
                        (int) rs.getDouble("price"),
                        stock,
                        availability,
                        rs.getString("description")
                    });
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void selectTable() {
        Object selected = NomorMejaField.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Silakan pilih nomor meja.",
                    "Pilih Meja",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String tableNumber = selected.toString();

        String sql = """
                SELECT id, table_number, capacity, status
                FROM dining_tables
                WHERE table_number = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setString(1, tableNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Meja tidak ditemukan.",
                            "Pilih Meja",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                String status = rs.getString("status");

                if (!"TERSEDIA".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Meja tersebut sedang digunakan.",
                            "Pilih Meja",
                            JOptionPane.WARNING_MESSAGE
                    );
                    loadTables();
                    return;
                }

                selectedTableId = rs.getInt("id");
                selectedTableNumber = rs.getString("table_number");

                MejaSayaField.setText(
                        selectedTableNumber
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Meja "
                        + selectedTableNumber
                        + " berhasil dipilih.",
                        "Pilih Meja",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void addToCart() {
        int selectedRow = DaftarMenuTabel.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Silakan pilih menu terlebih dahulu.",
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedTableId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Silakan pilih meja terlebih dahulu.",
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int quantity =
                ((Number) JumlahPesananSpinner
                        .getValue())
                        .intValue();

        String menuName =
                DaftarMenuTabel
                        .getValueAt(
                                selectedRow,
                                0
                        )
                        .toString();

        double price =
                Double.parseDouble(
                        DaftarMenuTabel
                                .getValueAt(
                                        selectedRow,
                                        2
                                )
                                .toString()
                );

        int stock =
                Integer.parseInt(
                        DaftarMenuTabel
                                .getValueAt(
                                        selectedRow,
                                        3
                                )
                                .toString()
                );

        String availability =
                DaftarMenuTabel
                        .getValueAt(
                                selectedRow,
                                4
                        )
                        .toString();

        if (!"Tersedia".equalsIgnoreCase(
                availability
        )) {
            JOptionPane.showMessageDialog(
                    this,
                    "Menu tersebut sedang habis.",
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (quantity > stock) {
            JOptionPane.showMessageDialog(
                    this,
                    "Jumlah melebihi stok.\n"
                    + "Stok tersedia: "
                    + stock,
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int menuId = getMenuIdByName(menuName);

        if (menuId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Menu tidak ditemukan di database.",
                    "Keranjang",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Kalau menu sudah ada di cart
        for (CartItem item : cart) {
            if (item.menuId == menuId) {
                int newQuantity = item.quantity + quantity;
                if (newQuantity > stock) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Jumlah pesanan melebihi stok.",
                            "Keranjang",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                item.quantity = newQuantity;
                loadCart();
                return;
            }
        }

        cart.add(
                new CartItem(
                        menuId,
                        menuName,
                        price,
                        quantity
                )
        );

        loadCart();

        JOptionPane.showMessageDialog(
                this,
                menuName
                + " berhasil ditambahkan ke keranjang.",
                "Keranjang",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private int getMenuIdByName(String name) {
        String sql = """
                SELECT id
                FROM menus
                WHERE name = ?
                LIMIT 1
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
        return -1;
    }

    private void loadCart() {
        DefaultTableModel model =
                (DefaultTableModel) KeranjangTabel.getModel();

        model.setRowCount(0);
        double total = 0;

        for (CartItem item : cart) {
            double subtotal = item.getSubtotal();
            total += subtotal;

            model.addRow(new Object[]{
                item.name,
                (int) item.price,
                item.quantity,
                (int) subtotal
            });
        }

        TotalKeranjangField.setText(
                formatRupiah(total)
        );
    }

    private void updateCartItem() {
        int selectedRow =
                KeranjangTabel.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih menu di keranjang terlebih dahulu.",
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int newQuantity =
                ((Number) JumlahSpinner
                        .getValue())
                        .intValue();

        CartItem item =
                cart.get(selectedRow);

        int stock =
                getMenuStock(item.menuId);

        if (stock < 0) {
            return;
        }

        if (newQuantity > stock) {
            JOptionPane.showMessageDialog(
                    this,
                    "Jumlah melebihi stok.\n"
                    + "Stok tersedia: "
                    + stock,
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        item.quantity = newQuantity;
        loadCart();
    }

    private int getMenuStock(int menuId) {
        String sql = """
                SELECT stock
                FROM menus
                WHERE id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, menuId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock");
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
        return -1;
    }

    private void removeCartItem() {
        int selectedRow =
                KeranjangTabel.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih menu yang ingin dihapus.",
                    "Keranjang",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        CartItem item =
                cart.get(selectedRow);

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Hapus "
                        + item.name
                        + " dari keranjang?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION
                );

        if (confirm ==
                JOptionPane.YES_OPTION) {
            cart.remove(selectedRow);
            loadCart();
        }
    }

    private double getCartTotal() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void openCheckoutDialog() {
        if (selectedTableId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Silakan pilih meja terlebih dahulu.",
                    "Checkout",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Keranjang masih kosong.",
                    "Checkout",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        PelangganTextField.setText(
                Session.getUsername()
        );

        NomorMejaTextField.setText(
                selectedTableNumber
        );

        double total =
                getCartTotal();

        TotalHargaTextField.setText(
                formatRupiah(total)
        );

        UangBayarField.setText("");

        DefaultTableModel model =
                (DefaultTableModel) jTable6.getModel();

        model.setRowCount(0);

        for (CartItem item : cart) {
            model.addRow(new Object[]{
                item.name,
                item.quantity,
                (int) item.price,
                (int) item.getSubtotal()
            });
        }

        jDialog1.pack();
        jDialog1.setLocationRelativeTo(this);
        jDialog1.setVisible(true);
    }

    private void processPayment() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Keranjang kosong.",
                    "Pembayaran",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedTableId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Meja belum dipilih.",
                    "Pembayaran",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String paidText =
                UangBayarField.getText()
                        .trim()
                        .replace("Rp", "")
                        .replace(".", "")
                        .replace(",", "");

        if (paidText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Masukkan nominal pembayaran.",
                    "Pembayaran",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double paid;

        try {
            paid = Double.parseDouble(
                    paidText
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nominal pembayaran tidak valid.",
                    "Pembayaran",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double total = getCartTotal();

        if (paid < total) {
            JOptionPane.showMessageDialog(
                    this,
                    "Uang yang dibayar kurang.\n"
                    + "Total: "
                    + formatRupiah(total),
                    "Pembayaran",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double change = paid - total;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            /*
            * Lock meja selama transaksi.
            */
            String tableSql = """
                    SELECT status
                    FROM dining_tables
                    WHERE id = ?
                    FOR UPDATE
                    """;

            try (
                    PreparedStatement tableStmt =
                            conn.prepareStatement(
                                    tableSql
                            )
            ) {
                tableStmt.setInt(
                        1,
                        selectedTableId
                );
                try (
                        ResultSet rs =
                                tableStmt.executeQuery()
                ) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Meja tidak ditemukan."
                        );
                    }

                    if (!"TERSEDIA".equalsIgnoreCase(
                            rs.getString("status")
                    )) {

                        throw new SQLException(
                                "Meja sudah digunakan oleh pelanggan lain."
                        );
                    }
                }
            }

            /*
            * Buat order.
            */
            String orderSql = """
                    INSERT INTO orders
                        (
                            user_id,
                            table_id,
                            status,
                            total,
                            paid,
                            change_amount
                        )
                    VALUES
                        (?, ?, 'MENUNGGU', ?, ?, ?)
                    """;

            int orderId;
            try (
                    PreparedStatement orderStmt =
                            conn.prepareStatement(
                                    orderSql,
                                    java.sql.Statement.RETURN_GENERATED_KEYS
                            )
            ) {
                orderStmt.setInt(
                        1,
                        Session.getUserId()
                );

                orderStmt.setInt(
                        2,
                        selectedTableId
                );

                orderStmt.setDouble(
                        3,
                        total
                );

                orderStmt.setDouble(
                        4,
                        paid
                );

                orderStmt.setDouble(
                        5,
                        change
                );

                orderStmt.executeUpdate();
                try (
                        ResultSet keys =
                                orderStmt.getGeneratedKeys()
                ) {
                    if (!keys.next()) {
                        throw new SQLException(
                                "Gagal mendapatkan ID pesanan."
                        );
                    }
                    orderId = keys.getInt(1);
                }
            }

            /*
            * Masukkan item pesanan.
            */
            String itemSql = """
                    INSERT INTO order_items
                        (
                            order_id,
                            menu_id,
                            quantity,
                            price,
                            subtotal
                        )
                    VALUES
                        (?, ?, ?, ?, ?)
                    """;

            /*
            * Kurangi stok.
            */
            String stockSql = """
                    UPDATE menus
                    SET stock = stock - ?
                    WHERE id = ?
                    AND stock >= ?
                    """;

            /*
            * Tandai meja terisi.
            */
            String tableUpdateSql = """
                    UPDATE dining_tables
                    SET status = 'TERISI'
                    WHERE id = ?
                    """;

            try (
                    PreparedStatement itemStmt =
                            conn.prepareStatement(itemSql);
                    PreparedStatement stockStmt =
                            conn.prepareStatement(stockSql);
                    PreparedStatement tableUpdateStmt =
                            conn.prepareStatement(
                                    tableUpdateSql
                            )
            ) {
                for (CartItem item : cart) {
                    itemStmt.setInt(
                            1,
                            orderId
                    );

                    itemStmt.setInt(
                            2,
                            item.menuId
                    );

                    itemStmt.setInt(
                            3,
                            item.quantity
                    );

                    itemStmt.setDouble(
                            4,
                            item.price
                    );

                    itemStmt.setDouble(
                            5,
                            item.getSubtotal()
                    );

                    itemStmt.executeUpdate();

                    stockStmt.setInt(
                            1,
                            item.quantity
                    );

                    stockStmt.setInt(
                            2,
                            item.menuId
                    );

                    stockStmt.setInt(
                            3,
                            item.quantity
                    );

                    int updated =
                            stockStmt.executeUpdate();

                    if (updated == 0) {
                        throw new SQLException(
                                "Stok menu "
                                + item.name
                                + " tidak mencukupi."
                        );
                    }
                }

                tableUpdateStmt.setInt(
                        1,
                        selectedTableId
                );
                tableUpdateStmt.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(
                    this,
                    "Pembayaran berhasil!\n"
                    + "No. Pesanan: #"
                    + orderId
                    + "\n"
                    + "Total: "
                    + formatRupiah(total)
                    + "\n"
                    + "Kembalian: "
                    + formatRupiah(change),
                    "Pembayaran Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            jDialog1.setVisible(false);

            cart.clear();

            selectedTableId = -1;
            selectedTableNumber = null;

            MejaSayaField.setText("-");

            loadTables();
            loadMenus();
            loadCart();
            loadHistory();
            updateDashboard();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Pembayaran gagal.\n"
                    + e.getMessage(),
                    "Pembayaran Gagal",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadHistory() {
        DefaultTableModel model =
                (DefaultTableModel)
                        RiwayatTransaksiTabel
                                .getModel();

        model.setRowCount(0);

        if (!Session.isLoggedIn()) {
            return;
        }

        String sql = """
                SELECT
                    o.id,
                    o.order_date,
                    d.table_number,
                    o.status,
                    o.total,
                    o.paid,
                    o.change_amount
                FROM orders o
                JOIN dining_tables d
                    ON d.id = o.table_id
                WHERE o.user_id = ?
                ORDER BY o.id DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    Session.getUserId()
            );
            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getTimestamp("order_date"),
                        rs.getString("table_number"),
                        rs.getString("status"),
                        formatRupiah(
                                rs.getDouble("total")
                        ),
                        formatRupiah(
                                rs.getDouble("paid")
                        ),
                        formatRupiah(
                                rs.getDouble("change_amount")
                        )
                    });
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void loadSelectedTransactionDetail() {
        int selectedRow =
                RiwayatTransaksiTabel
                        .getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int orderId =
                Integer.parseInt(
                        RiwayatTransaksiTabel
                                .getValueAt(
                                        selectedRow,
                                        0
                                )
                                .toString()
                );

        DefaultTableModel model =
                (DefaultTableModel)
                        DetailTransaksiTabel
                                .getModel();

        model.setRowCount(0);

        String sql = """
                SELECT
                    m.name,
                    oi.quantity,
                    oi.price,
                    oi.subtotal
                FROM order_items oi
                JOIN menus m
                    ON m.id = oi.menu_id
                JOIN orders o
                    ON o.id = oi.order_id
                WHERE oi.order_id = ?
                AND o.user_id = ?
                ORDER BY oi.id
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    orderId
            );

            stmt.setInt(
                    2,
                    Session.getUserId()
            );
            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        (int) rs.getDouble("price"),
                        (int) rs.getDouble("subtotal")
                    });
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void updateDashboard() {
        if (!Session.isLoggedIn()) {
            return;
        }

        String tableSql = """
                SELECT
                    d.table_number
                FROM dining_tables d
                JOIN orders o
                    ON o.table_id = d.id
                WHERE o.user_id = ?
                AND o.status IN (
                        'MENUNGGU',
                        'DIPROSES',
                        'SIAP DIANTAR'
                )
                ORDER BY o.id DESC
                LIMIT 1
                """;

        String processSql = """
                SELECT COUNT(*)
                FROM orders
                WHERE user_id = ?
                AND status IN (
                        'MENUNGGU',
                        'DIPROSES',
                        'SIAP DIANTAR'
                )
                """;

        String transactionSql = """
                SELECT COUNT(*)
                FROM orders
                WHERE user_id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection()
        ) {
            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    tableSql
                            )
            ) {
                stmt.setInt(
                        1,
                        Session.getUserId()
                );
                try (
                        ResultSet rs =
                                stmt.executeQuery()
                ) {
                    if (rs.next()) {
                        MejaSayaField.setText(
                                rs.getString(
                                        "table_number"
                                )
                        );
                    } else {
                        MejaSayaField.setText("-");
                    }
                }
            }
            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    processSql
                            )
            ) {
                stmt.setInt(
                        1,
                        Session.getUserId()
                );
                try (
                        ResultSet rs =
                                stmt.executeQuery()
                ) {
                    rs.next();
                    PesananDalamProsesField.setText(
                            String.valueOf(
                                    rs.getInt(1)
                            )
                    );
                }
            }
            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    transactionSql
                            )
            ) {
                stmt.setInt(
                        1,
                        Session.getUserId()
                );
                try (
                        ResultSet rs =
                                stmt.executeQuery()
                ) {
                    rs.next();
                    JumlahTransaksiField.setText(
                            String.valueOf(
                                    rs.getInt(1)
                            )
                    );
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Terjadi kesalahan database.\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jPanel7 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        PelangganTextField = new javax.swing.JTextField();
        NomorMejaTextField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        UangBayarField = new javax.swing.JTextField();
        BatalBayarButton = new javax.swing.JButton();
        BayarSekarangButton = new javax.swing.JButton();
        TotalHargaTextField = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        MejaSayaField = new javax.swing.JTextField();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        PesananDalamProsesField = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        JumlahTransaksiField = new javax.swing.JTextField();
        LogoutUserButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        NomorMejaField = new javax.swing.JComboBox<>();
        PilihMejaButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        PilihMejaTabel = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        KategoriMenuComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        DaftarMenuTabel = new javax.swing.JTable();
        jLabel34 = new javax.swing.JLabel();
        JumlahPesananSpinner = new javax.swing.JSpinner();
        TambahKeranjangButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        HapusKeranjangButton = new javax.swing.JButton();
        CheckoutButton = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        TotalKeranjangField = new javax.swing.JFormattedTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        KeranjangTabel = new javax.swing.JTable();
        jLabel19 = new javax.swing.JLabel();
        JumlahSpinner = new javax.swing.JSpinner();
        UbahKeranjangButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        RiwayatTransaksiTabel = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        DetailTransaksiTabel = new javax.swing.JTable();
        MuatUlangButton = new javax.swing.JButton();

        jPanel7.setBackground(new java.awt.Color(255, 251, 239));

        jLabel20.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(47, 75, 58));
        jLabel20.setText("Checkout dan Pembayaran");

        jLabel21.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(51, 41, 31));
        jLabel21.setText("Pelanggan:");

        jLabel22.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(51, 41, 31));
        jLabel22.setText("Nomor  Meja:");

        PelangganTextField.setEditable(false);

        NomorMejaTextField.setEditable(false);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PelangganTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NomorMejaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(9, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel22)
                    .addComponent(PelangganTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NomorMejaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel23.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(51, 41, 31));
        jLabel23.setText("Rincian Pesanan");

        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Menu", "Jumlah", "Harga Satuan", "Subtotal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(jTable6);

        jLabel24.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(51, 41, 31));
        jLabel24.setText("Total Harga             :");

        jLabel25.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(51, 41, 31));
        jLabel25.setText("Uang Dibayar (Rp) :");

        UangBayarField.addActionListener(this::UangBayarFieldActionPerformed);

        BatalBayarButton.setBackground(new java.awt.Color(233, 220, 192));
        BatalBayarButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        BatalBayarButton.setForeground(new java.awt.Color(51, 41, 31));
        BatalBayarButton.setText("Batal");
        BatalBayarButton.addActionListener(this::BatalBayarButtonActionPerformed);

        BayarSekarangButton.setBackground(new java.awt.Color(47, 75, 58));
        BayarSekarangButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        BayarSekarangButton.setForeground(new java.awt.Color(255, 255, 255));
        BayarSekarangButton.setText("Bayar Sekarang");
        BayarSekarangButton.addActionListener(this::BayarSekarangButtonActionPerformed);

        TotalHargaTextField.setEditable(false);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel24)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(TotalHargaTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(UangBayarField)))
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addComponent(BatalBayarButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(BayarSekarangButton))
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel23)
                            .addComponent(jLabel20)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(TotalHargaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(UangBayarField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BayarSekarangButton)
                    .addComponent(BatalBayarButton))
                .addGap(36, 36, 36))
        );

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setBackground(new java.awt.Color(47, 75, 58));
        jTabbedPane1.setForeground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 251, 239));
        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 500));

        jPanel11.setBackground(new java.awt.Color(47, 75, 58));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setPreferredSize(new java.awt.Dimension(400, 170));

        jLabel28.setBackground(new java.awt.Color(255, 251, 239));
        jLabel28.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(51, 40, 30));
        jLabel28.setText("Cara Pesan:");

        jLabel29.setBackground(new java.awt.Color(255, 251, 239));
        jLabel29.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(51, 40, 30));
        jLabel29.setText("1. Pilih meja");

        jLabel30.setBackground(new java.awt.Color(255, 251, 239));
        jLabel30.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(51, 40, 30));
        jLabel30.setText("2. Pilih daftar menu → tambahkan menu ke keranjang");

        jLabel31.setBackground(new java.awt.Color(255, 251, 239));
        jLabel31.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(51, 40, 30));
        jLabel31.setText("3. Pilih keranjang → pastikan pesanan telah sesuai");

        jLabel32.setBackground(new java.awt.Color(255, 251, 239));
        jLabel32.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(51, 40, 30));
        jLabel32.setText("4. Pilih pembayaran → pesanan akan diproses setelah pembayaran berhasil");

        jLabel33.setBackground(new java.awt.Color(255, 251, 239));
        jLabel33.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(51, 40, 30));
        jLabel33.setText("5. Pilih riwayat → melihat seluruh status transaksi");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29)
                    .addComponent(jLabel28)
                    .addComponent(jLabel31)
                    .addComponent(jLabel30)
                    .addComponent(jLabel32)
                    .addComponent(jLabel33))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel33)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(47, 75, 58));
        jLabel4.setText("Beranda");

        jLabel5.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(123, 111, 96));
        jLabel5.setText("Selamat Datang di Kanopee!");

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel6.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel18.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(123, 111, 96));
        jLabel6.setText("Meja Saya");

        MejaSayaField.setEditable(false);
        MejaSayaField.setBackground(new java.awt.Color(255, 255, 255));
        MejaSayaField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        MejaSayaField.setText("-");
        MejaSayaField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(MejaSayaField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 197, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MejaSayaField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel13.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel14.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel13.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(123, 111, 96));
        jLabel13.setText("Pesanan dalam proses");

        PesananDalamProsesField.setEditable(false);
        PesananDalamProsesField.setBackground(new java.awt.Color(255, 255, 255));
        PesananDalamProsesField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        PesananDalamProsesField.setText("-");
        PesananDalamProsesField.setPreferredSize(new java.awt.Dimension(65, 35));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(PesananDalamProsesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 140, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PesananDalamProsesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel15.setBackground(new java.awt.Color(255, 255, 255));
        jPanel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel15.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel16.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel15.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(123, 111, 96));
        jLabel15.setText("Jumlah transaksi");

        JumlahTransaksiField.setEditable(false);
        JumlahTransaksiField.setBackground(new java.awt.Color(255, 255, 255));
        JumlahTransaksiField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        JumlahTransaksiField.setText("-");
        JumlahTransaksiField.setPreferredSize(new java.awt.Dimension(65, 35));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(JumlahTransaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 174, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JumlahTransaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        LogoutUserButton.setBackground(new java.awt.Color(233, 220, 192));
        LogoutUserButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        LogoutUserButton.setForeground(new java.awt.Color(51, 41, 31));
        LogoutUserButton.setText("Logout");
        LogoutUserButton.addActionListener(this::LogoutUserButtonActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(42, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LogoutUserButton)
                .addGap(30, 30, 30))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(LogoutUserButton)
                .addGap(30, 30, 30))
        );

        jTabbedPane1.addTab("Beranda", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 251, 239));

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(47, 75, 58));
        jLabel7.setText("Pilih Meja");

        jLabel8.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(123, 111, 96));
        jLabel8.setText("Masukkan nomor meja yang tertera pada meja Anda");

        jLabel9.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 41, 31));
        jLabel9.setText("Nomor Meja");

        NomorMejaField.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        NomorMejaField.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "K01", "K02", "K03", "K04", "K05", "K06", "K07", "K08", "K09", "K10" }));
        NomorMejaField.addActionListener(this::NomorMejaFieldActionPerformed);

        PilihMejaButton.setBackground(new java.awt.Color(47, 75, 58));
        PilihMejaButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        PilihMejaButton.setForeground(new java.awt.Color(255, 255, 255));
        PilihMejaButton.setText("Pilih Meja");
        PilihMejaButton.addActionListener(this::PilihMejaButtonActionPerformed);

        PilihMejaTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nomor Meja", "Kapasitas", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(PilihMejaTabel);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(NomorMejaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(PilihMejaButton))
                    .addComponent(jLabel8)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(300, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(NomorMejaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PilihMejaButton))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pilih Meja", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 251, 239));
        jPanel3.setPreferredSize(new java.awt.Dimension(1000, 500));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(47, 75, 58));
        jLabel1.setText("Daftar Menu");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(123, 111, 96));
        jLabel2.setText("Pilih menu dan jumlahnya, lalu tambahkan ke keranjang");

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 41, 31));
        jLabel3.setText("Tampilkan");

        KategoriMenuComboBox.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        KategoriMenuComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua", "Makanan", "Minuman", "Snack" }));
        KategoriMenuComboBox.setSelectedItem(KategoriMenuComboBox);
        KategoriMenuComboBox.addActionListener(this::KategoriMenuComboBoxActionPerformed);

        DaftarMenuTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Menu", "Kategori", "Harga", "Stok", "Ketersediaan", "Deskripsi"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(DaftarMenuTabel);

        jLabel34.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(51, 41, 31));
        jLabel34.setText("Jumlah");

        TambahKeranjangButton.setBackground(new java.awt.Color(47, 75, 58));
        TambahKeranjangButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TambahKeranjangButton.setForeground(new java.awt.Color(255, 255, 255));
        TambahKeranjangButton.setText("Tambah ke Keranjang");
        TambahKeranjangButton.addActionListener(this::TambahKeranjangButtonActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JumlahPesananSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TambahKeranjangButton))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2)
                        .addComponent(jLabel1)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(KategoriMenuComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(KategoriMenuComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(JumlahPesananSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TambahKeranjangButton))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Daftar Menu", jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 251, 239));

        HapusKeranjangButton.setBackground(new java.awt.Color(168, 69, 58));
        HapusKeranjangButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        HapusKeranjangButton.setForeground(new java.awt.Color(255, 255, 255));
        HapusKeranjangButton.setText("Hapus");
        HapusKeranjangButton.addActionListener(this::HapusKeranjangButtonActionPerformed);

        CheckoutButton.setBackground(new java.awt.Color(47, 75, 58));
        CheckoutButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        CheckoutButton.setForeground(new java.awt.Color(255, 255, 255));
        CheckoutButton.setText("Checkout");
        CheckoutButton.addActionListener(this::CheckoutButtonActionPerformed);

        jLabel14.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(47, 75, 58));
        jLabel14.setText("Keranjang");

        TotalKeranjangField.setEditable(false);
        TotalKeranjangField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
        TotalKeranjangField.addActionListener(this::TotalKeranjangFieldActionPerformed);

        jLabel16.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(123, 111, 96));
        jLabel16.setText("Periksa pesanan Anda sebelum checkout");

        jLabel17.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(51, 41, 31));
        jLabel17.setText("Total: Rp");

        KeranjangTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Menu", "Harga Satuan", "Jumlah", "Subtotal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(KeranjangTabel);

        jLabel19.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(51, 41, 31));
        jLabel19.setText("Jumlah baru");

        UbahKeranjangButton.setBackground(new java.awt.Color(122, 82, 48));
        UbahKeranjangButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        UbahKeranjangButton.setForeground(new java.awt.Color(255, 255, 255));
        UbahKeranjangButton.setText("Ubah");
        UbahKeranjangButton.addActionListener(this::UbahKeranjangButtonActionPerformed);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(jLabel14)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel19)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(JumlahSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(UbahKeranjangButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(HapusKeranjangButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel17)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(TotalKeranjangField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(CheckoutButton))
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 890, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(JumlahSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(UbahKeranjangButton)
                    .addComponent(HapusKeranjangButton)
                    .addComponent(CheckoutButton)
                    .addComponent(TotalKeranjangField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addContainerGap(149, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Keranjang", jPanel4);

        jPanel5.setBackground(new java.awt.Color(255, 251, 239));
        jPanel5.setPreferredSize(new java.awt.Dimension(1000, 500));

        jLabel10.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(47, 75, 58));
        jLabel10.setText("Riwayat Transaksi");

        jLabel11.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(123, 111, 96));
        jLabel11.setText("Pesanan Anda serta status pesanan (Menunggu, Diproses, Siap Diantar, Selesai)");

        RiwayatTransaksiTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No. Pesanan", "Tanggal", "Meja", "Status", "Total", "Dibayar", "Kembalian"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(RiwayatTransaksiTabel);
        if (RiwayatTransaksiTabel.getColumnModel().getColumnCount() > 0) {
            RiwayatTransaksiTabel.getColumnModel().getColumn(0).setHeaderValue("No. Pesanan");
            RiwayatTransaksiTabel.getColumnModel().getColumn(1).setResizable(false);
            RiwayatTransaksiTabel.getColumnModel().getColumn(1).setHeaderValue("Tanggal");
            RiwayatTransaksiTabel.getColumnModel().getColumn(2).setResizable(false);
            RiwayatTransaksiTabel.getColumnModel().getColumn(2).setHeaderValue("Meja");
        }

        jLabel12.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(51, 41, 31));
        jLabel12.setText("Detail menu pada pesanan yang dipilih");

        DetailTransaksiTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Menu", "Jumlah", "Harga Satuan", "Sub Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(DetailTransaksiTabel);

        MuatUlangButton.setBackground(new java.awt.Color(47, 75, 58));
        MuatUlangButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        MuatUlangButton.setForeground(new java.awt.Color(255, 255, 255));
        MuatUlangButton.setText("Muat Ulang Status");
        MuatUlangButton.addActionListener(this::MuatUlangButtonActionPerformed);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MuatUlangButton)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 875, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MuatUlangButton)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Riwayat", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void KategoriMenuComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KategoriMenuComboBoxActionPerformed
        // TODO add your handling code here:
        loadMenus();
    }//GEN-LAST:event_KategoriMenuComboBoxActionPerformed

    private void NomorMejaFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NomorMejaFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NomorMejaFieldActionPerformed

    private void PilihMejaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PilihMejaButtonActionPerformed
        // TODO add your handling code here:
        selectTable();
    }//GEN-LAST:event_PilihMejaButtonActionPerformed

    private void MuatUlangButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MuatUlangButtonActionPerformed
        // TODO add your handling code here:
        loadTables();
        loadMenus();
        loadCart();
        loadHistory();
        updateDashboard();

        JOptionPane.showMessageDialog(
                this,
                "Data berhasil dimuat ulang.",
                "Muat Ulang",
                JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_MuatUlangButtonActionPerformed

    private void CheckoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckoutButtonActionPerformed
        // TODO add your handling code here:
        openCheckoutDialog();
    }//GEN-LAST:event_CheckoutButtonActionPerformed

    private void TotalKeranjangFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalKeranjangFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TotalKeranjangFieldActionPerformed

    private void UbahKeranjangButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UbahKeranjangButtonActionPerformed
        // TODO add your handling code here:
        updateCartItem();
    }//GEN-LAST:event_UbahKeranjangButtonActionPerformed

    private void UangBayarFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UangBayarFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UangBayarFieldActionPerformed

    private void BayarSekarangButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BayarSekarangButtonActionPerformed
        // TODO add your handling code here:
        processPayment();
    }//GEN-LAST:event_BayarSekarangButtonActionPerformed

    private void TambahKeranjangButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TambahKeranjangButtonActionPerformed
        // TODO add your handling code here:
        addToCart();
    }//GEN-LAST:event_TambahKeranjangButtonActionPerformed

    private void HapusKeranjangButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HapusKeranjangButtonActionPerformed
        // TODO add your handling code here:
        removeCartItem();
    }//GEN-LAST:event_HapusKeranjangButtonActionPerformed

    private void BatalBayarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BatalBayarButtonActionPerformed
        // TODO add your handling code here:
        jDialog1.setVisible(false);
    }//GEN-LAST:event_BatalBayarButtonActionPerformed

    private void LogoutUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutUserButtonActionPerformed
        // TODO add your handling code here:
        Session.logout();

        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_LogoutUserButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new UserFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BatalBayarButton;
    private javax.swing.JButton BayarSekarangButton;
    private javax.swing.JButton CheckoutButton;
    private javax.swing.JTable DaftarMenuTabel;
    private javax.swing.JTable DetailTransaksiTabel;
    private javax.swing.JButton HapusKeranjangButton;
    private javax.swing.JSpinner JumlahPesananSpinner;
    private javax.swing.JSpinner JumlahSpinner;
    private javax.swing.JTextField JumlahTransaksiField;
    private javax.swing.JComboBox<String> KategoriMenuComboBox;
    private javax.swing.JTable KeranjangTabel;
    private javax.swing.JButton LogoutUserButton;
    private javax.swing.JTextField MejaSayaField;
    private javax.swing.JButton MuatUlangButton;
    private javax.swing.JComboBox<String> NomorMejaField;
    private javax.swing.JTextField NomorMejaTextField;
    private javax.swing.JTextField PelangganTextField;
    private javax.swing.JTextField PesananDalamProsesField;
    private javax.swing.JButton PilihMejaButton;
    private javax.swing.JTable PilihMejaTabel;
    private javax.swing.JTable RiwayatTransaksiTabel;
    private javax.swing.JButton TambahKeranjangButton;
    private javax.swing.JTextField TotalHargaTextField;
    private javax.swing.JFormattedTextField TotalKeranjangField;
    private javax.swing.JTextField UangBayarField;
    private javax.swing.JButton UbahKeranjangButton;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable6;
    // End of variables declaration//GEN-END:variables
}
