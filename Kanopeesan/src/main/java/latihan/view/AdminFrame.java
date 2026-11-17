/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package latihan.view;

import latihan.db.DBConnection;
import latihan.util.Session;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author ameld
 */
public class AdminFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminFrame.class.getName());
    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("id", "ID")
            );
    private final List<Integer> menuIdsByRow = new ArrayList<>();
    private final List<Integer> tableIdsByRow = new ArrayList<>();

    /**
     * Creates new form AdminFrame
     */
    public AdminFrame() {
        initComponents();
        setLocationRelativeTo(null);
        setupComboBoxes();
        setupTableListeners();
        TambahMenuButton.addActionListener(
                e -> addMenu()
        );

        UbahMenuButton.addActionListener(
                e -> updateMenu()
        );

        HapusMenuButton.addActionListener(
                e -> deleteMenu()
        );

        TambahMejaButton.addActionListener(
                e -> addTable()
        );

        UbahMejaButton.addActionListener(
                e -> updateTable()
        );

        HapusMejaButton.addActionListener(
                e -> deleteTable()
        );

        TampilkanLaporanButton.addActionListener(
                e -> loadReport(
                        DariDateChooser.getDate(),
                        SampaiDateChooser.getDate()
                )
        );

        SemuaTanggalButton.addActionListener(
                e -> {

                    DariDateChooser.setDate(null);
                    SampaiDateChooser.setDate(null);

                    loadReport(null, null);
                }
        );

        StatusPesananComboBox.addActionListener(
                e -> loadOrders()
        );

        MejaComboBox.addActionListener(e -> {
            if (MejaComboBox.getSelectedItem() != null) {
                loadOrders();
            }
        });

        loadDashboard();
        loadOrders();
        loadMenus();
        loadTables();
        loadTableFilter();
        loadReports();
    }

    private void setupComboBoxes() {
        StatusPesananComboBox.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "Selesai",
                            "Belum Selesai"
                        }
                )
        );

        KategoriComboBox.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "Makanan",
                            "Minuman",
                            "Snack"
                        }
                )
        );

        StatusMenuComboBox.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "Tersedia",
                            "Habis"
                        }
                )
        );

        StatusMejaComboBox.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                            "Tersedia",
                            "Terisi"
                        }
                )
        );
    }

    private String formatRupiah(double amount) {
        return currencyFormat.format(amount)
                .replace("Rp", "Rp ");
    }

    private void setupTableListeners() {
        KelolaPesananTabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            updateSelectedOrderStatus();
                        }
                    }
                }
        );

        KelolaMenuTabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            loadSelectedMenuToForm();
                        }
                    }
                }
        );

        KelolaMejaTabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            loadSelectedTableToForm();
                        }
                    }
                }
        );
    }

    private void loadDashboard() {
        String totalMejaSql = """
                SELECT COUNT(*)
                FROM dining_tables
                """;

        String mejaTerisiSql = """
                SELECT COUNT(*)
                FROM dining_tables
                WHERE status = 'TERISI'
                """;

        String totalMenuSql = """
                SELECT COUNT(*)
                FROM menus
                """;

        String transaksiSql = """
                SELECT COUNT(*)
                FROM orders
                """;

        String pendingSql = """
                SELECT COUNT(*)
                FROM orders
                WHERE status <> 'SELESAI'
                AND status <> 'DIBATALKAN'
                """;

        String pendapatanSql = """
                SELECT COALESCE(SUM(total), 0)
                FROM orders
                WHERE status = 'SELESAI'
                """;

        try (
                Connection conn =
                        DBConnection.getConnection()
        ) {
            TotalMejaField.setText(
                    String.valueOf(
                            executeCount(
                                    conn,
                                    totalMejaSql
                            )
                    )
            );

            MejaTerisiField.setText(
                    String.valueOf(
                            executeCount(
                                    conn,
                                    mejaTerisiSql
                            )
                    )
            );

            TotalMenuField.setText(
                    String.valueOf(
                            executeCount(
                                    conn,
                                    totalMenuSql
                            )
                    )
            );

            JumlahTransaksiField.setText(
                    String.valueOf(
                            executeCount(
                                    conn,
                                    transaksiSql
                            )
                    )
            );

            PesananMenungguField.setText(
                    String.valueOf(
                            executeCount(
                                    conn,
                                    pendingSql
                            )
                    )
            );

            TotalPendapatanField.setText(
                    formatRupiah(
                            executeDouble(
                                    conn,
                                    pendapatanSql
                            )
                    )
            );

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private int executeCount(
            Connection conn,
            String sql
    ) throws SQLException {
        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        stmt.executeQuery()
        ) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private double executeDouble(
            Connection conn,
            String sql
    ) throws SQLException {
        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        stmt.executeQuery()
        ) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private void loadOrders() {
        DefaultTableModel model =
                (DefaultTableModel)
                        KelolaPesananTabel.getModel();

        model.setRowCount(0);

        String selectedStatus =
                StatusPesananComboBox
                        .getSelectedItem()
                        .toString();

        Object selectedMejaItem = MejaComboBox.getSelectedItem();
        String selectedMeja = selectedMejaItem == null
                ? "Semua Meja"
                : selectedMejaItem.toString();

        StringBuilder sql =
                new StringBuilder("""
                    SELECT
                        o.id,
                        o.order_date,
                        u.username,
                        d.table_number,
                        o.status,
                        o.total
                    FROM orders o
                    JOIN users u
                        ON u.id = o.user_id
                    JOIN dining_tables d
                        ON d.id = o.table_id
                    WHERE 1 = 1
                    """);

        List<Object> params =
                new ArrayList<>();

        if ("Belum Selesai".equals(
                selectedStatus
        )) {
            sql.append("""
                    AND o.status IN (
                        'MENUNGGU',
                        'DIPROSES',
                        'SIAP DIANTAR'
                    )
                    """);

        } else if ("Selesai".equals(
                selectedStatus
        )) {
            sql.append("""
                    AND o.status = 'SELESAI'
                    """);
        }

        if (!"Semua Meja".equals(
                selectedMeja
        )) {
            sql.append(
                    " AND d.table_number = ? "
            );
            params.add(selectedMeja);
        }

        sql.append(
                " ORDER BY o.id DESC"
        );

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(
                                sql.toString()
                        )
        ) {
            for (int i = 0;
                i < params.size();
                i++) {
                stmt.setObject(
                        i + 1,
                        params.get(i)
                );
            }

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getTimestamp(
                                "order_date"
                        ),
                        rs.getString("username"),
                        rs.getString(
                                "table_number"
                        ),
                        convertOrderStatus(
                                rs.getString("status")
                        ),
                        formatRupiah(
                                rs.getDouble("total")
                        )
                    });
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private String convertOrderStatus(
            String status
    ) {
        return switch (status) {
            case "MENUNGGU" ->
                    "Menunggu";

            case "DIPROSES" ->
                    "Diproses";

            case "SIAP DIANTAR" ->
                    "Siap Diantar";

            case "SELESAI" ->
                    "Selesai";

            case "DIBATALKAN" ->
                    "Dibatalkan";

            default ->
                    status;
        };
    }

    private void updateSelectedOrderStatus() {
        int selectedRow =
                KelolaPesananTabel.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int orderId =
                Integer.parseInt(
                        KelolaPesananTabel
                                .getValueAt(
                                        selectedRow,
                                        0
                                )
                                .toString()
                );

        String currentStatus =
                KelolaPesananTabel
                        .getValueAt(
                                selectedRow,
                                4
                        )
                        .toString();

        String nextStatus =
                getNextStatus(currentStatus);

        if (nextStatus == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pesanan ini sudah selesai.",
                    "Status Pesanan",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Ubah status pesanan #"
                        + orderId
                        + " menjadi "
                        + nextStatus
                        + "?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION
                );

        if (confirm !=
                JOptionPane.YES_OPTION) {
            return;
        }

        String dbStatus =
                convertStatusToDatabase(
                        nextStatus
                );

        String sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    dbStatus
            );

            stmt.setInt(
                    2,
                    orderId
            );

            stmt.executeUpdate();

            /*
            * Kalau pesanan selesai,
            * meja dikembalikan menjadi TERSEDIA.
            */
            if ("SELESAI".equals(
                    dbStatus
            )) {
                releaseOrderTable(
                        conn,
                        orderId
                );
            }

            loadOrders();
            loadDashboard();
            loadTables();

            JOptionPane.showMessageDialog(
                    this,
                    "Status pesanan berhasil diperbarui.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private String getNextStatus(
            String status
    ) {
        return switch (status) {
            case "Menunggu" ->
                    "Diproses";

            case "Diproses" ->
                    "Siap Diantar";

            case "Siap Diantar" ->
                    "Selesai";

            default ->
                    null;
        };
    }

    private String convertStatusToDatabase(
            String status
    ) {
        return switch (status) {
            case "Menunggu" ->
                    "MENUNGGU";

            case "Diproses" ->
                    "DIPROSES";

            case "Siap Diantar" ->
                    "SIAP DIANTAR";

            case "Selesai" ->
                    "SELESAI";

            default ->
                    status;
        };
    }

    private void releaseOrderTable(
            Connection conn,
            int orderId
    ) throws SQLException {
        String sql = """
                UPDATE dining_tables
                SET status = 'TERSEDIA'
                WHERE id = (
                    SELECT table_id
                    FROM orders
                    WHERE id = ?
                )
                """;

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    orderId
            );
            stmt.executeUpdate();
        }
    }
    
    private void loadTableFilter() {
        MejaComboBox.removeAllItems();
        MejaComboBox.addItem(
                "Semua Meja"
        );

        String sql = """
                SELECT table_number
                FROM dining_tables
                ORDER BY table_number
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        stmt.executeQuery()
        ) {
            while (rs.next()) {
                MejaComboBox.addItem(
                        rs.getString(
                                "table_number"
                        )
                );
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void loadMenus() {
        DefaultTableModel model =
                (DefaultTableModel)
                        KelolaMenuTabel.getModel();

        model.setRowCount(0);
        menuIdsByRow.clear();

        String sql = """
                SELECT
                    id,
                    name,
                    category,
                    price,
                    stock,
                    status
                FROM menus
                ORDER BY id
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        stmt.executeQuery()
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String status = rs.getString("status");
                menuIdsByRow.add(id);

                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("category"),
                    formatRupiah(
                            rs.getDouble("price")
                    ),
                    rs.getInt("stock"),
                    convertMenuStatus(
                            status
                    )
                });
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private String convertMenuStatus(String status) {
        if ("TERSEDIA".equalsIgnoreCase(status)) {
                return "Tersedia";
        }
            return "Habis";
    }

    private void loadSelectedMenuToForm() {
        int row =
                KelolaMenuTabel
                        .getSelectedRow();

        if (row == -1
                || row >= menuIdsByRow.size()) {
            return;
        }

        int menuId =
                menuIdsByRow.get(row);

        String sql = """
                SELECT
                    name,
                    category,
                    price,
                    stock,
                    status,
                    description
                FROM menus
                WHERE id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {
            stmt.setInt(
                    1,
                    menuId
            );
            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {
                if (rs.next()) {
                    NamaMenuField.setText(
                            rs.getString("name")
                    );

                    KategoriComboBox.setSelectedItem(
                            rs.getString("category")
                    );

                    HargaField.setText(
                            String.valueOf(
                                    rs.getDouble("price")
                            )
                    );

                    StokField.setText(
                            String.valueOf(
                                    rs.getInt("stock")
                            )
                    );

                    StatusMenuComboBox.setSelectedItem(
                            convertMenuStatus(
                                    rs.getString("status")
                            )
                    );

                    DeskripsiField.setText(
                            rs.getString("description")
                                    == null
                                    ? ""
                                    : rs.getString(
                                            "description"
                                    )
                    );
                }
            }

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private MenuFormData getMenuFormData() {
        String name =
                NamaMenuField.getText().trim();

        String category =
                KategoriComboBox
                        .getSelectedItem()
                        .toString();

        String priceText =
                HargaField.getText().trim();

        String stockText =
                StokField.getText().trim();

        String status =
                StatusMenuComboBox
                        .getSelectedItem()
                        .toString();

        String description =
                DeskripsiField.getText().trim();

        if (name.isEmpty()
                || priceText.isEmpty()
                || stockText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nama, harga, dan stok wajib diisi.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        double price;
        int stock;

        try {
            price =
                    Double.parseDouble(
                            priceText
                    );

            stock =
                    Integer.parseInt(
                            stockText
                    );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Harga dan stok harus berupa angka.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        if (price < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Harga tidak boleh negatif.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        if (stock < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Stok tidak boleh negatif.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        String dbStatus =
                "Tersedia".equalsIgnoreCase(
                        status
                )
                ? "TERSEDIA"
                : "HABIS";

        return new MenuFormData(
                name,
                category,
                price,
                stock,
                dbStatus,
                description
        );
    }
    
    private static class MenuFormData {
        String name;
        String category;
        double price;
        int stock;
        String status;
        String description;

        MenuFormData(
                String name,
                String category,
                double price,
                int stock,
                String status,
                String description
        ) {

            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.status = status;
            this.description = description;
        }
    }

    private void addMenu() {
        MenuFormData data =
                getMenuFormData();

        if (data == null) {
            return;
        }

        String sql = """
                INSERT INTO menus
                    (
                        name,
                        category,
                        price,
                        stock,
                        status,
                        description
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    data.name
            );

            stmt.setString(
                    2,
                    data.category
            );

            stmt.setDouble(
                    3,
                    data.price
            );

            stmt.setInt(
                    4,
                    data.stock
            );

            stmt.setString(
                    5,
                    data.status
            );

            stmt.setString(
                    6,
                    data.description
            );

            stmt.executeUpdate();

            clearMenuForm();
            loadMenus();
            loadDashboard();

            JOptionPane.showMessageDialog(
                    this,
                    "Menu berhasil ditambahkan.",
                    "Menu",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    private void updateMenu() {
        int row =
                KelolaMenuTabel
                        .getSelectedRow();

        if (row == -1
                || row >= menuIdsByRow.size()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih menu yang ingin diubah.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        MenuFormData data =
                getMenuFormData();

        if (data == null) {
            return;
        }

        int menuId =
                menuIdsByRow.get(row);

        String sql = """
                UPDATE menus
                SET
                    name = ?,
                    category = ?,
                    price = ?,
                    stock = ?,
                    status = ?,
                    description = ?
                WHERE id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    data.name
            );

            stmt.setString(
                    2,
                    data.category
            );

            stmt.setDouble(
                    3,
                    data.price
            );

            stmt.setInt(
                    4,
                    data.stock
            );

            stmt.setString(
                    5,
                    data.status
            );

            stmt.setString(
                    6,
                    data.description
            );

            stmt.setInt(
                    7,
                    menuId
            );

            stmt.executeUpdate();

            clearMenuForm();
            loadMenus();
            loadDashboard();

            JOptionPane.showMessageDialog(
                    this,
                    "Menu berhasil diubah.",
                    "Menu",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    private void deleteMenu() {
        int row =
                KelolaMenuTabel
                        .getSelectedRow();

        if (row == -1
                || row >= menuIdsByRow.size()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih menu yang ingin dihapus.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int menuId =
                menuIdsByRow.get(row);

        String menuName =
                KelolaMenuTabel
                        .getValueAt(
                                row,
                                0
                        )
                        .toString();

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Hapus menu "
                        + menuName
                        + "?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION
                );

        if (confirm !=
                JOptionPane.YES_OPTION) {

            return;
        }

        String sql = """
                DELETE FROM menus
                WHERE id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    menuId
            );

            stmt.executeUpdate();

            clearMenuForm();
            loadMenus();
            loadDashboard();

            JOptionPane.showMessageDialog(
                    this,
                    "Menu berhasil dihapus.",
                    "Menu",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Menu tidak dapat dihapus.\n"
                    + "Kemungkinan menu tersebut sudah digunakan dalam transaksi.",
                    "Menu",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void clearMenuForm() {
        NamaMenuField.setText("");
        HargaField.setText("");
        StokField.setText("");
        DeskripsiField.setText("");

        KategoriComboBox.setSelectedIndex(0);
        StatusMenuComboBox.setSelectedIndex(0);

        KelolaMenuTabel.clearSelection();
    }

    private void loadTables() {
        DefaultTableModel model =
                (DefaultTableModel)
                        KelolaMejaTabel.getModel();

        model.setRowCount(0);

        tableIdsByRow.clear();

        String sql = """
                SELECT
                    id,
                    table_number,
                    capacity,
                    status
                FROM dining_tables
                ORDER BY id
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        stmt.executeQuery()
        ) {

            while (rs.next()) {

                int id =
                        rs.getInt("id");

                tableIdsByRow.add(id);

                model.addRow(new Object[]{
                    rs.getString(
                            "table_number"
                    ),
                    rs.getInt(
                            "capacity"
                    ),
                    convertTableStatus(
                            rs.getString(
                                    "status"
                            )
                    )
                });
            }

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    private String convertTableStatus(
            String status
    ) {
        if ("TERSEDIA".equalsIgnoreCase(
                status
        )) {

            return "Tersedia";
        }

        return "Terisi";
    }

    private void loadSelectedTableToForm() {
        int row =
                KelolaMejaTabel
                        .getSelectedRow();

        if (row == -1
                || row >= tableIdsByRow.size()) {

            return;
        }

        NomorMejaField.setText(
                KelolaMejaTabel
                        .getValueAt(
                                row,
                                0
                        )
                        .toString()
        );

        KapasitasField.setText(
                KelolaMejaTabel
                        .getValueAt(
                                row,
                                1
                        )
                        .toString()
        );

        StatusMejaComboBox.setSelectedItem(
                KelolaMejaTabel
                        .getValueAt(
                                row,
                                2
                        )
                        .toString()
        );
    }

    private void addTable() {
        String number =
                NomorMejaField
                        .getText()
                        .trim();

        String capacityText =
                KapasitasField
                        .getText()
                        .trim();

        if (number.isEmpty()
                || capacityText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Nomor meja dan kapasitas wajib diisi.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int capacity;

        try {

            capacity =
                    Integer.parseInt(
                            capacityText
                    );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Kapasitas harus berupa angka.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (capacity <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Kapasitas harus lebih dari 0.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String sql = """
                INSERT INTO dining_tables
                    (
                        table_number,
                        capacity,
                        status
                    )
                VALUES
                    (?, ?, 'TERSEDIA')
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    number
            );

            stmt.setInt(
                    2,
                    capacity
            );

            stmt.executeUpdate();

            clearTableForm();

            loadTables();
            loadTableFilter();
            loadDashboard();

            JOptionPane.showMessageDialog(
                    this,
                    "Meja berhasil ditambahkan.",
                    "Meja",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Nomor meja sudah digunakan.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void updateTable() {
        int row =
                KelolaMejaTabel
                        .getSelectedRow();

        if (row == -1
                || row >= tableIdsByRow.size()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih meja yang ingin diubah.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String number =
                NomorMejaField
                        .getText()
                        .trim();

        String capacityText =
                KapasitasField
                        .getText()
                        .trim();

        if (number.isEmpty()
                || capacityText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Nomor meja dan kapasitas wajib diisi.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int capacity;

        try {

            capacity =
                    Integer.parseInt(
                            capacityText
                    );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Kapasitas harus berupa angka.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int tableId =
                tableIdsByRow.get(row);

        String status =
                StatusMejaComboBox
                        .getSelectedItem()
                        .toString();

        String dbStatus =
                "Tersedia".equalsIgnoreCase(
                        status
                )
                ? "TERSEDIA"
                : "TERISI";

        String sql = """
                UPDATE dining_tables
                SET
                    table_number = ?,
                    capacity = ?,
                    status = ?
                WHERE id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    number
            );

            stmt.setInt(
                    2,
                    capacity
            );

            stmt.setString(
                    3,
                    dbStatus
            );

            stmt.setInt(
                    4,
                    tableId
            );

            stmt.executeUpdate();

            clearTableForm();

            loadTables();
            loadTableFilter();
            loadDashboard();

            JOptionPane.showMessageDialog(
                    this,
                    "Meja berhasil diubah.",
                    "Meja",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    private void deleteTable() {
        int row =
                KelolaMejaTabel
                        .getSelectedRow();

        if (row == -1
                || row >= tableIdsByRow.size()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih meja yang ingin dihapus.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int tableId =
                tableIdsByRow.get(row);

        String tableNumber =
                KelolaMejaTabel
                        .getValueAt(
                                row,
                                0
                        )
                        .toString();

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Hapus meja "
                        + tableNumber
                        + "?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION
                );

        if (confirm !=
                JOptionPane.YES_OPTION) {

            return;
        }

        String sql = """
                DELETE FROM dining_tables
                WHERE id = ?
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    tableId
            );

            stmt.executeUpdate();

            clearTableForm();

            loadTables();
            loadTableFilter();
            loadDashboard();

            JOptionPane.showMessageDialog(
                    this,
                    "Meja berhasil dihapus.",
                    "Meja",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Meja tidak dapat dihapus karena sudah digunakan dalam transaksi.",
                    "Meja",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void clearTableForm() {
        NomorMejaField.setText("");
        KapasitasField.setText("");

        StatusMejaComboBox.setSelectedIndex(0);

        KelolaMejaTabel.clearSelection();
    }

    private void loadReports() {
        loadReport(
                DariDateChooser.getDate(),
                SampaiDateChooser.getDate()
        );
    }

    private void loadReport(
            java.util.Date from,
            java.util.Date to
    ) {
        DefaultTableModel model =
                (DefaultTableModel)
                        LaporanPenjualanTabel
                                .getModel();

        model.setRowCount(0);

        StringBuilder sql =
                new StringBuilder("""
                    SELECT
                        o.id,
                        o.order_date,
                        u.username,
                        d.table_number,
                        o.status,
                        o.total
                    FROM orders o
                    JOIN users u
                        ON u.id = o.user_id
                    JOIN dining_tables d
                        ON d.id = o.table_id
                    WHERE 1 = 1
                    """);

        List<java.util.Date> params =
                new ArrayList<>();

        if (from != null) {

            sql.append(
                    " AND DATE(o.order_date) >= DATE(?) "
            );

            params.add(from);
        }

        if (to != null) {

            sql.append(
                    " AND DATE(o.order_date) <= DATE(?) "
            );

            params.add(to);
        }

        sql.append(
                " ORDER BY o.order_date DESC"
        );

        double totalPendapatan = 0;

        int totalTransaksi = 0;

        try (
                Connection conn =
                        DBConnection.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(
                                sql.toString()
                        )
        ) {

            for (int i = 0;
                i < params.size();
                i++) {

                stmt.setDate(
                        i + 1,
                        new java.sql.Date(
                                params.get(i)
                                        .getTime()
                        )
                );
            }

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    double total =
                            rs.getDouble(
                                    "total"
                            );

                    totalPendapatan += total;

                    totalTransaksi++;

                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getTimestamp(
                                "order_date"
                        ),
                        rs.getString(
                                "username"
                        ),
                        rs.getString(
                                "table_number"
                        ),
                        convertOrderStatus(
                                rs.getString(
                                        "status"
                                )
                        ),
                        formatRupiah(
                                total
                        )
                    });
                }
            }

            LaporanPendapatanField.setText(
                    formatRupiah(
                            totalPendapatan
                    )
            );

            LaporanTransaksiField.setText(
                    String.valueOf(
                            totalTransaksi
                    )
            );

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Terjadi kesalahan database:\n" + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AdminPanel = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        TotalMejaField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        TotalPendapatanField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        TotalMenuField = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        JumlahTransaksiField = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        PesananMenungguField = new javax.swing.JTextField();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        MejaTerisiField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        StatusPesananComboBox = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        MejaComboBox = new javax.swing.JComboBox<>();
        MuatUlangButtons = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        KelolaPesananTabel = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        KelolaMenuTabel = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        NamaMenuField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        KategoriComboBox = new javax.swing.JComboBox<>();
        jLabel21 = new javax.swing.JLabel();
        HargaField = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        StokField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        StatusMenuComboBox = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        DeskripsiField = new javax.swing.JTextField();
        TambahMenuButton = new javax.swing.JButton();
        UbahMenuButton = new javax.swing.JButton();
        HapusMenuButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        KelolaMejaTabel = new javax.swing.JTable();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        KapasitasField = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        StatusMejaComboBox = new javax.swing.JComboBox<>();
        TambahMejaButton = new javax.swing.JButton();
        UbahMejaButton = new javax.swing.JButton();
        HapusMejaButton = new javax.swing.JButton();
        NomorMejaField = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        LaporanPendapatanField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        TampilkanLaporanButton = new javax.swing.JButton();
        SemuaTanggalButton = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        LaporanTransaksiField = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        LaporanPenjualanTabel = new javax.swing.JTable();
        DariDateChooser = new com.toedter.calendar.JDateChooser();
        SampaiDateChooser = new com.toedter.calendar.JDateChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        AdminPanel.setBackground(new java.awt.Color(47, 75, 58));
        AdminPanel.setForeground(new java.awt.Color(255, 255, 255));
        AdminPanel.setPreferredSize(new java.awt.Dimension(1000, 500));

        jPanel1.setBackground(new java.awt.Color(255, 251, 239));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel9.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel10.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel13.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(123, 111, 96));
        jLabel13.setText("Total Meja");

        TotalMejaField.setEditable(false);
        TotalMejaField.setBackground(new java.awt.Color(255, 255, 255));
        TotalMejaField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TotalMejaField.setText("-");
        TotalMejaField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(TotalMejaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TotalMejaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(47, 75, 58));
        jLabel3.setText("Beranda Admin");

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

        jLabel16.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(123, 111, 96));
        jLabel16.setText("Total Pendapatan");

        TotalPendapatanField.setEditable(false);
        TotalPendapatanField.setBackground(new java.awt.Color(255, 255, 255));
        TotalPendapatanField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TotalPendapatanField.setText("-");
        TotalPendapatanField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(TotalPendapatanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 90, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TotalPendapatanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel4.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(123, 111, 96));
        jLabel4.setText("Ringkasan operasional Kanopee");

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel5.setPreferredSize(new java.awt.Dimension(220, 80));

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

        jLabel5.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(123, 111, 96));
        jLabel5.setText("Total Menu");

        TotalMenuField.setEditable(false);
        TotalMenuField.setBackground(new java.awt.Color(255, 255, 255));
        TotalMenuField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TotalMenuField.setText("-");
        TotalMenuField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TotalMenuField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TotalMenuField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel11.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel12.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel14.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(123, 111, 96));
        jLabel14.setText("Jumlah Transaksi");

        JumlahTransaksiField.setEditable(false);
        JumlahTransaksiField.setBackground(new java.awt.Color(255, 255, 255));
        JumlahTransaksiField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        JumlahTransaksiField.setText("-");
        JumlahTransaksiField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(JumlahTransaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 89, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JumlahTransaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel19.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel6.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(123, 111, 96));
        jLabel6.setText("Pesanan Menunggu");

        PesananMenungguField.setEditable(false);
        PesananMenungguField.setBackground(new java.awt.Color(255, 255, 255));
        PesananMenungguField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        PesananMenungguField.setText("-");
        PesananMenungguField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(PesananMenungguField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 78, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PesananMenungguField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jLabel15.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(123, 111, 96));
        jLabel15.setText("Meja Terisi");

        MejaTerisiField.setEditable(false);
        MejaTerisiField.setBackground(new java.awt.Color(255, 255, 255));
        MejaTerisiField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        MejaTerisiField.setText("-");
        MejaTerisiField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(MejaTerisiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MejaTerisiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))))
                .addContainerGap(284, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(163, Short.MAX_VALUE))
        );

        AdminPanel.addTab("Beranda", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 251, 239));
        jPanel2.setPreferredSize(new java.awt.Dimension(1000, 500));

        jLabel7.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(123, 111, 96));
        jLabel7.setText("Kelola Pesanan yang masuk");

        jLabel8.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 41, 31));
        jLabel8.setText("Status");

        StatusPesananComboBox.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        StatusPesananComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Menunggu", "Diproses", "Siap Diantar", "Selesai", "Dibatalkan" }));
        StatusPesananComboBox.setSelectedItem(StatusPesananComboBox);

        jLabel9.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 41, 31));
        jLabel9.setText("Meja");

        MejaComboBox.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        MejaComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua Meja", "K01", "K02", "K03", "K04", "K05", "K06", "K07", "K08", "K09", "K10" }));

        MuatUlangButtons.setBackground(new java.awt.Color(233, 220, 192));
        MuatUlangButtons.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        MuatUlangButtons.setForeground(new java.awt.Color(51, 41, 31));
        MuatUlangButtons.setText("Muat Ulang");
        MuatUlangButtons.addActionListener(this::MuatUlangButtonsActionPerformed);

        KelolaPesananTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Waktu", "Pelanggan", "Meja", "Status", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
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
        jScrollPane1.setViewportView(KelolaPesananTabel);

        jLabel10.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(47, 75, 58));
        jLabel10.setText("Kelola Pesanan");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel10)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(StatusPesananComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MejaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(MuatUlangButtons))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 875, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(StatusPesananComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(MejaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MuatUlangButtons))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        AdminPanel.addTab("Kelola Pesanan", jPanel2);

        jPanel23.setBackground(new java.awt.Color(255, 251, 239));
        jPanel23.setPreferredSize(new java.awt.Dimension(1000, 500));

        jLabel17.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(47, 75, 58));
        jLabel17.setText("Kelola Menu");

        jLabel18.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(123, 111, 96));
        jLabel18.setText("Kelola menu yang dijual di Kanopee");

        KelolaMenuTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama", "Kategori", "Jenis", "Harga", "Stok", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(KelolaMenuTabel);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel19.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(51, 41, 31));
        jLabel19.setText("Nama Menu");

        jLabel20.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(51, 41, 31));
        jLabel20.setText("Kategori");

        KategoriComboBox.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        KategoriComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Makanan", "Minuman", "Snack" }));

        jLabel21.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(51, 41, 31));
        jLabel21.setText("Harga (Rp)");

        jLabel22.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(51, 41, 31));
        jLabel22.setText("Stok");

        jLabel23.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(51, 41, 31));
        jLabel23.setText("Status");

        StatusMenuComboBox.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        StatusMenuComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tersedia", "Habis" }));
        StatusMenuComboBox.setSelectedItem(StatusMenuComboBox);

        jLabel24.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(51, 41, 31));
        jLabel24.setText("Deskripsi");

        TambahMenuButton.setBackground(new java.awt.Color(47, 75, 58));
        TambahMenuButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TambahMenuButton.setForeground(new java.awt.Color(255, 255, 255));
        TambahMenuButton.setText("Tambah");

        UbahMenuButton.setBackground(new java.awt.Color(122, 82, 48));
        UbahMenuButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        UbahMenuButton.setForeground(new java.awt.Color(255, 255, 255));
        UbahMenuButton.setText("Ubah");

        HapusMenuButton.setBackground(new java.awt.Color(168, 69, 58));
        HapusMenuButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        HapusMenuButton.setForeground(new java.awt.Color(255, 255, 255));
        HapusMenuButton.setText("Hapus");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(StatusMenuComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(TambahMenuButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(UbahMenuButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(StokField, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)
                            .addComponent(HargaField, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21)
                            .addComponent(KategoriComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel20)
                            .addComponent(jLabel19)
                            .addComponent(NamaMenuField, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(DeskripsiField, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(HapusMenuButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NamaMenuField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(KategoriComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(HargaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StokField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(StatusMenuComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(TambahMenuButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel24))
                    .addComponent(UbahMenuButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(HapusMenuButton)
                    .addComponent(DeskripsiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jLabel17)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        AdminPanel.addTab("Kelola Menu", jPanel23);

        jPanel3.setBackground(new java.awt.Color(255, 251, 239));
        jPanel3.setPreferredSize(new java.awt.Dimension(1000, 500));

        KelolaMejaTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No. Meja", "Kapasitas", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(KelolaMejaTabel);

        jLabel33.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(47, 75, 58));
        jLabel33.setText("Kelola Meja");

        jLabel34.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(123, 111, 96));
        jLabel34.setText("Kelola nomor meja dan status penggunaannya");

        jPanel24.setBackground(new java.awt.Color(255, 255, 255));
        jPanel24.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));

        jLabel35.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(51, 41, 31));
        jLabel35.setText("Nomor Meja");

        jLabel36.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(51, 41, 31));
        jLabel36.setText("Kapasitas");

        jLabel37.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(51, 41, 31));
        jLabel37.setText("Status");

        StatusMejaComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tersedia", "Terisi" }));
        StatusMejaComboBox.setSelectedItem(StatusMejaComboBox);

        TambahMejaButton.setBackground(new java.awt.Color(47, 75, 58));
        TambahMejaButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TambahMejaButton.setForeground(new java.awt.Color(255, 255, 255));
        TambahMejaButton.setText("Tambah");

        UbahMejaButton.setBackground(new java.awt.Color(122, 82, 48));
        UbahMejaButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        UbahMejaButton.setForeground(new java.awt.Color(255, 255, 255));
        UbahMejaButton.setText("Ubah");

        HapusMejaButton.setBackground(new java.awt.Color(168, 69, 58));
        HapusMejaButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        HapusMejaButton.setForeground(new java.awt.Color(255, 255, 255));
        HapusMejaButton.setText("Hapus");

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel24Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(NomorMejaField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(KapasitasField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel24Layout.createSequentialGroup()
                            .addComponent(TambahMejaButton)
                            .addGap(18, 18, 18)
                            .addComponent(UbahMejaButton)
                            .addGap(18, 18, 18)
                            .addComponent(HapusMejaButton))
                        .addComponent(StatusMejaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NomorMejaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(KapasitasField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StatusMejaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TambahMejaButton)
                    .addComponent(UbahMejaButton)
                    .addComponent(HapusMejaButton))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel34)
                    .addComponent(jLabel33)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 552, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        AdminPanel.addTab("Kelola Meja", jPanel3);

        jPanel7.setBackground(new java.awt.Color(255, 251, 239));

        jPanel20.setBackground(new java.awt.Color(255, 255, 255));
        jPanel20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel20.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel21.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel29.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(123, 111, 96));
        jLabel29.setText("Total Pendapatan");

        LaporanPendapatanField.setEditable(false);
        LaporanPendapatanField.setBackground(new java.awt.Color(255, 255, 255));
        LaporanPendapatanField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        LaporanPendapatanField.setText("-");
        LaporanPendapatanField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(LaporanPendapatanField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 180, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LaporanPendapatanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(51, 41, 31));
        jLabel11.setText("Dari");

        jLabel12.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(51, 41, 31));
        jLabel12.setText("Sampai");

        TampilkanLaporanButton.setBackground(new java.awt.Color(47, 75, 58));
        TampilkanLaporanButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        TampilkanLaporanButton.setForeground(new java.awt.Color(255, 255, 255));
        TampilkanLaporanButton.setText("Tampilkan");

        SemuaTanggalButton.setBackground(new java.awt.Color(233, 220, 192));
        SemuaTanggalButton.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        SemuaTanggalButton.setForeground(new java.awt.Color(51, 41, 31));
        SemuaTanggalButton.setText("Semua Tanggal");

        jPanel22.setBackground(new java.awt.Color(255, 255, 255));
        jPanel22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(233, 220, 192)));
        jPanel22.setPreferredSize(new java.awt.Dimension(220, 80));

        jPanel4.setBackground(new java.awt.Color(47, 75, 58));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jLabel30.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(123, 111, 96));
        jLabel30.setText("Total Transaksi");

        LaporanTransaksiField.setEditable(false);
        LaporanTransaksiField.setBackground(new java.awt.Color(255, 255, 255));
        LaporanTransaksiField.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        LaporanTransaksiField.setText("-");
        LaporanTransaksiField.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(LaporanTransaksiField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 191, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LaporanTransaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel31.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(47, 75, 58));
        jLabel31.setText("Laporan Penjualan");

        jLabel32.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(123, 111, 96));
        jLabel32.setText("Filter berdasarkan tanggal transaksi");

        LaporanPenjualanTabel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Tanggal", "Pelanggan", "Meja", "Status", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
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
        jScrollPane2.setViewportView(LaporanPenjualanTabel);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel32)
                    .addComponent(jLabel31)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DariDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SampaiDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(TampilkanLaporanButton)
                        .addGap(18, 18, 18)
                        .addComponent(SemuaTanggalButton))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 890, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(DariDateChooser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SampaiDateChooser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel11)
                                .addComponent(jLabel12)
                                .addComponent(TampilkanLaporanButton)
                                .addComponent(SemuaTanggalButton)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        AdminPanel.addTab("Laporan Penjualan", jPanel7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AdminPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AdminPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MuatUlangButtonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MuatUlangButtonsActionPerformed
        // TODO add your handling code here:
        loadDashboard();
        loadOrders();
        loadMenus();
        loadTables();
        loadTableFilter();
        loadReports();

        JOptionPane.showMessageDialog(
                this,
                "Semua data berhasil dimuat ulang.",
                "Muat Ulang",
                JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_MuatUlangButtonsActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new AdminFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane AdminPanel;
    private com.toedter.calendar.JDateChooser DariDateChooser;
    private javax.swing.JTextField DeskripsiField;
    private javax.swing.JButton HapusMejaButton;
    private javax.swing.JButton HapusMenuButton;
    private javax.swing.JTextField HargaField;
    private javax.swing.JTextField JumlahTransaksiField;
    private javax.swing.JTextField KapasitasField;
    private javax.swing.JComboBox<String> KategoriComboBox;
    private javax.swing.JTable KelolaMejaTabel;
    private javax.swing.JTable KelolaMenuTabel;
    private javax.swing.JTable KelolaPesananTabel;
    private javax.swing.JTextField LaporanPendapatanField;
    private javax.swing.JTable LaporanPenjualanTabel;
    private javax.swing.JTextField LaporanTransaksiField;
    private javax.swing.JComboBox<String> MejaComboBox;
    private javax.swing.JTextField MejaTerisiField;
    private javax.swing.JButton MuatUlangButtons;
    private javax.swing.JTextField NamaMenuField;
    private javax.swing.JTextField NomorMejaField;
    private javax.swing.JTextField PesananMenungguField;
    private com.toedter.calendar.JDateChooser SampaiDateChooser;
    private javax.swing.JButton SemuaTanggalButton;
    private javax.swing.JComboBox<String> StatusMejaComboBox;
    private javax.swing.JComboBox<String> StatusMenuComboBox;
    private javax.swing.JComboBox<String> StatusPesananComboBox;
    private javax.swing.JTextField StokField;
    private javax.swing.JButton TambahMejaButton;
    private javax.swing.JButton TambahMenuButton;
    private javax.swing.JButton TampilkanLaporanButton;
    private javax.swing.JTextField TotalMejaField;
    private javax.swing.JTextField TotalMenuField;
    private javax.swing.JTextField TotalPendapatanField;
    private javax.swing.JButton UbahMejaButton;
    private javax.swing.JButton UbahMenuButton;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    // End of variables declaration//GEN-END:variables
}
