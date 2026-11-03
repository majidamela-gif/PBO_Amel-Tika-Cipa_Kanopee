# Kanopeesan
### Sistem Pemesanan Makanan dan Minuman Berbasis Java Swing dan MySQL untuk cafe Kanopee

Kanopee merupakan salah satu cafe yang berada di Jatinangor, lebih tepatnya di Komplek Puri Indah, Jl. Kolonel Achmad Syam No.8 Blok B1, Cikeruh, Kec. Jatinangor, Kabupaten Sumedang, Jawa Barat 45363. Kami menggunakan Kanopee sebagai acuan pembuatan web yang memungkinkan pelanggan melakukan pemesanan secara mandiri melalui web setelah mendapatkan tempat duduk di cafe, tanpa perlu datang langsung ke kasir untuk memesan.

Sistem ini memiliki dua jenis pengguna, yaitu **Pelanggan (Customer)** dan **Admin**, dengan hak akses dan fitur yang berbeda.

---

## Latar Belakang

Proses pemesanan makanan dan minuman pada cafe umumnya mengharuskan pelanggan datang ke kasir untuk melihat menu dan melakukan pemesanan. Hal ini dapat menyebabkan antrean, terutama ketika cafe sedang ramai.

Oleh karena itu, Kanopeesan dikembangkan sebagai solusi untuk mempermudah proses pemesanan. Pelanggan cukup datang ke cafe, memilih tempat duduk, kemudian melakukan pemesanan melalui aplikasi dengan memasukkan nomor meja yang ditempati. Pesanan akan diterima oleh admin untuk diproses dan diantarkan langsung ke meja pelanggan.

Dengan adanya aplikasi ini, diharapkan proses pemesanan menjadi lebih praktis, terorganisir, dan efisien.

## Tujuan Project

1. Mempermudah pelanggan dalam melakukan pemesanan makanan dan minuman.
2. Mengurangi antrean pemesanan di kasir.
3. Membantu admin mengelola menu dan pesanan pelanggan.
4. Mempermudah pencatatan transaksi penjualan.
5. Menyediakan riwayat pemesanan dan transaksi secara terstruktur.

---

## Hak Akses Pengguna

### 1. Pelanggan (Customer)

Pelanggan merupakan pengguna yang datang langsung ke cafe dan ingin memesan makanan atau minuman dari meja tempat mereka duduk.

Fitur pelanggan:

- **Register dan Login** — Membuat akun dan masuk ke aplikasi.
- **Pilih Nomor Meja** — Memasukkan nomor meja yang sedang ditempati.
- **Lihat Menu** — Melihat daftar makanan dan minuman beserta harga dan ketersediaannya.
- **Keranjang** — Menambahkan menu, mengubah jumlah pesanan, dan menghapus item.
- **Checkout** — Melihat rincian pesanan dan total harga.
- **Pembayaran** — Memasukkan nominal uang pembayaran untuk divalidasi oleh sistem.
- **Riwayat Transaksi** — Melihat daftar pesanan dan transaksi yang pernah dilakukan.

### 2. Admin

Admin merupakan pengguna yang bertanggung jawab mengelola operasional pemesanan cafe.

Fitur admin:

- **Login Admin** — Masuk ke halaman dashboard admin.
- **Dashboard** — Melihat ringkasan menu, pesanan, dan transaksi.
- **Kelola Menu** — Menambah, melihat, mengubah, dan menghapus data makanan dan minuman.
- **Kelola Kategori** — Mengelola kategori makanan dan minuman.
- **Kelola Meja** — Mengelola nomor meja dan status penggunaannya.
- **Kelola Pesanan** — Melihat pesanan pelanggan berdasarkan nomor meja.
- **Update Status Pesanan** — Mengubah status pesanan menjadi Menunggu, Diproses, Siap Diantar, atau Selesai.
- **Riwayat Transaksi** — Melihat seluruh transaksi pelanggan.
- **Laporan Penjualan** — Melihat rekap transaksi dan total pendapatan.

---

## Alur Penggunaan Aplikasi

### Alur Pelanggan

1. Pelanggan datang langsung ke Kanopee.
2. Pelanggan mencari dan memilih tempat duduk yang tersedia.
3. Pelanggan membuka aplikasi Kanopee dan melakukan login.
4. Pelanggan memasukkan nomor meja sesuai tempat duduknya.
5. Pelanggan melihat daftar menu makanan dan minuman.
6. Pelanggan memilih menu dan memasukkannya ke keranjang.
7. Pelanggan mengatur jumlah pesanan sesuai keinginan.
8. Pelanggan melakukan checkout dan melihat total harga.
9. Pelanggan memasukkan nominal pembayaran.
10. Sistem memvalidasi pembayaran dan menghitung kembalian.
11. Setelah pembayaran berhasil, pesanan masuk ke sistem admin.
12. Admin memproses pesanan dan mengantarkan makanan atau minuman ke meja pelanggan.
13. Pelanggan dapat melihat status dan riwayat transaksinya.

### Alur Admin

1. Admin login ke aplikasi.
2. Admin masuk ke dashboard.
3. Admin mengelola data meja, kategori, dan menu.
4. Admin menerima pesanan baru dari pelanggan.
5. Admin melihat rincian pesanan dan nomor meja tujuan.
6. Admin memproses pesanan.
7. Admin mengubah status pesanan sesuai proses pengerjaan.
8. Admin mengantarkan pesanan ke meja pelanggan.
9. Admin dapat melihat riwayat transaksi dan laporan penjualan.

---

## Database

Database yang digunakan bernama `kanopee`.

Rancangan tabel:

| Tabel | Fungsi |
|---|---|
| `users` | Menyimpan data akun pelanggan dan admin |
| `meja` | Menyimpan data nomor meja dan status meja |
| `kategori_menu` | Menyimpan kategori makanan dan minuman |
| `menu` | Menyimpan informasi makanan dan minuman |
| `keranjang` | Menyimpan menu yang dipilih pelanggan sebelum checkout |
| `transaksi` | Menyimpan informasi transaksi pelanggan |
| `detail_transaksi` | Menyimpan rincian menu dalam setiap transaksi |

### Relasi Database

- Satu user dapat memiliki banyak transaksi.
- Satu meja dapat digunakan untuk banyak transaksi pada waktu berbeda.
- Satu kategori menu dapat memiliki banyak menu.
- Satu transaksi memiliki banyak detail transaksi.
- Satu menu dapat muncul dalam banyak detail transaksi.
- Satu user dapat memiliki data keranjang sebelum checkout.

Setiap transaksi menyimpan nomor meja agar admin dapat mengetahui tujuan pengantaran pesanan.

---

## Teknologi yang Digunakan

| Teknologi | Kegunaan |
|---|---|
| Java | Bahasa pemrograman utama |
| Java Swing | Membangun antarmuka aplikasi |
| NetBeans IDE | IDE pengembangan aplikasi |
| MySQL | Database management |
| Laragon | Menjalankan server database lokal |
| HeidiSQL | Mengelola database MySQL |
| Maven | Mengelola project dan dependency |
| JDBC | Menghubungkan Java dengan MySQL |