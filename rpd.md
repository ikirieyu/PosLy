# Product Requirement Document (PRD) & Blueprint Arsitektur
# Sistem Kasir (POS) & Manajemen Finansial UMKM Berbasis Native Android & Supabase

---

## 1. Ringkasan Eksekutif & Visi Produk

Aplikasi POS & Finansial UMKM adalah solusi digital *all-in-one* berbasis **Native Android (Kotlin / Jetpack Compose)** yang dirancang untuk perangkat smartphone dan tablet. Sistem mengintegrasikan proses operasional kasir harian, manajemen inventaris bahan baku & stok, pencetakan struk termal (Bluetooth & Wi-Fi), serta otomasi kalkulasi finansial (omzet, HPP, laba kotor, beban operasional, laba bersih, dan alokasi pos anggaran).

Sistem menggunakan arsitektur **Offline-First (Room Database)** dengan sinkronisasi otomatis ke cloud backend **Supabase (PostgreSQL, Auth, RLS, Storage)**, sehingga operasional penjualan di toko tetap dapat berjalan tanpa hambatan saat koneksi internet terputus.

---

## 2. Spesifikasi Arsitektur & Tech Stack

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Jetpack Compose UI (Adaptive Mobile & Tablet Screens)│
│                        ViewModel                       │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                      Domain Layer                      │
│       UseCases (CalculateProfit, PrintReceipt, etc.)   │
│               Repository Interfaces & Models           │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                       Data Layer                       │
│ ┌──────────────────────────┐  ┌──────────────────────┐ │
│ │ Local DataSource (Room)  │◄─┤ Sync Manager         │ │
│ └────────────┬─────────────┘  │ (WorkManager Engine) │ │
│              │                └──────────┬───────────┘ │
│              ▼                           │             │
│    SQLite (Offline Cache)                ▼             │
│                               Remote DataSource        │
│                           (Supabase Kotlin Client)     │
└────────────────────────────────────────────────────────┘
```

### 2.1 Technology Stack
* **Platform:** Native Android (Min SDK 26 / Android 8.0 Oreo, Target SDK 34/35).
* **Language & UI Toolkit:** Kotlin 2.x, Jetpack Compose, Material 3, Compose Navigation.
* **Architecture Pattern:** Clean Architecture + MVVM + MVI pattern.
* **Dependency Injection:** Dagger Hilt.
* **Local Persistence (SSOT):** Android Room Database (SQLite) terenkripsi SQLCipher.
* **Remote Backend:** Supabase (PostgreSQL 15+, Supabase Auth, Row Level Security, Realtime, Storage).
* **Background Processing:** Android WorkManager (Sync periodik & offline queue dispatch).
* **Hardware Interfacing:** 
  * Bluetooth Classic SPP (`BluetoothSocket` - RFCOMM UUID `00001101-0000-1000-8000-00805F9B34FB`).
  * Wi-Fi / Ethernet TCP Socket (`java.net.Socket` port default 9100).
* **Report Generation:** Apache POI (`org.apache.poi:poi-ooxml`) untuk ekspor format `.xlsx` dengan formula aktif.

---

## 3. Matriks Hak Akses & Peran (Role-Based Access Control)

| Modul / Fitur | Owner (Pemilik Usaha) | Pekerja (Kasir / Staf) |
| :--- | :---: | :---: |
| Login Akun & Ubah Password/PIN Sendiri | Ya | Ya |
| Kelola Akun Staf (Tambah, Hapus, Nonaktifkan Kasir) | Ya | Tidak |
| Konfigurasi Usaha (Nama Toko, Logo, Alamat, Footer Struk) | Ya | Tidak |
| Konfigurasi Database Supabase (URL & API Key) & Backup DB | Ya | Tidak |
| Manajemen Produk (Tambah/Edit Nama, SKU, Kategori) | Ya | Tidak |
| Pengaturan Harga Modal (HPP) & Harga Jual | Ya | Tidak |
| Manajemen Stok & Alert Batas Minimum Stok | Ya | Tidak |
| Pengaturan Diskon & Skema Promosi | Ya | Tidak |
| Buka Kasir / Shift Kasir | Ya | Ya |
| Pembuatan Pesanan & Edit Pesanan Aktif (Sebelum Bayar) | Ya | Ya |
| Proses Pembayaran (Cash, QRIS) | Ya | Ya |
| Cetak Struk Transaksi (Bluetooth / Wi-Fi) | Ya | Ya |
| Pembatalan Pesanan / Void Transaksi Selesai | Ya (Langsung) | Ya (Wajib Otorisasi PIN Owner) |
| Pencatatan Beban Operasional / Bahan Baku / Transport | Ya | Terbatas (Sesuai Izin) |
| Dashboard Finansial (Omzet, HPP, Laba Bersih, Pos Dana) | Ya | Tidak |
| Ekspor Laporan Excel (.xlsx berformula) | Ya | Tidak |

---

## 4. Rincian Kebutuhan Fungsional (Functional Requirements)

### 4.1 Modul Otentikasi & Pengaturan Toko
* **Otentikasi Aman:** Login menggunakan email/username dan password/PIN.
* **Manajemen Akun Kasir:** Owner dapat mendaftarkan beberapa akun pekerja dengan hak akses terbatas.
* **Konfigurasi Identitas Toko:**
  * Pengaturan nama usaha, slogan, alamat fisik, nomor WhatsApp, dan upload logo.
  * Logo otomatis dikonversi menjadi monokrom/bitmap biner 1-bit untuk pencetakan thermal printer.
  * Pesan kustom pada footer struk (misal: "Terima kasih atas kunjungan Anda").
* **Koneksi Supabase Fleksibel:** Pengaturan URL project dan API Anon Key dari dalam aplikasi.
* **Backup & Restore Data:** Ekspor snapshot database lokal/remote ke format JSON/SQL file terenkripsi untuk arsip manual maupun cloud storage.

### 4.2 Modul Produk, Biaya (HPP) & Inventaris
* **Katalog Produk:** Input nama produk, kategori, deskripsi singkat, foto produk, SKU, dan barcode.
* **Struktur Penetapan Harga:**
  * Input **Harga Modal (HPP)** per unit produk.
  * Input **Harga Jual** per unit produk.
  * Kalkulasi margin keuntungan per unit secara real-time saat penentuan harga.
* **Manajemen Bahan Baku & Stok:**
  * Pengurangan kuantitas stok otomatis setiap kali transaksi berhasil difinalisasi.
  * Notifikasi visual saat stok berada di bawah batas minimum (*Low Stock Indicator*).
  * Riwayat penyesuaian stok (*Stock In / Stock Out / Stock Adjustment*).
* **Diskon & Promosi:**
  * Diskon berbasis persentase (`%`) atau potongan nominal tetap (`Rp`).
  * Penerapan diskon per baris item produk atau diskon global pada total nilai belanja.

### 4.3 Modul Kasir (POS) & Transaksi
* **Antarmuka Kasir Responsif:** Katalog produk berbasis grid kategori dengan fungsi pencarian cepat dan pemindaian barcode kamera (Google ML Kit).
* **Keranjang Belanja Dinamis:**
  * Tambah item, ubah kuantitas, beri catatan khusus per item (misal: *Less sugar*, *Pedas sedang*).
  * **Fitur Edit Order:** Kemudahan mengubah, menambah, atau menghapus item pesanan sebelum struk dicetak atau pembayaran diselesaikan.
* **Metode Pembayaran:**
  * **Tunai (Cash):** Tombol cepat pecahan uang pas (Rp 10rb, 20rb, 50rb, 100rb) dan kalkulasi otomatis nominal uang kembalian.
  * **QRIS:** Tampilan QRIS statis toko (dari pengaturan) atau QR dinamis yang siap dipindai pelanggan.
* **Penyimpanan Draft Pesanan (Hold / Resume Order):** Kemampuan menyimpan transaksi sementara jika pelanggan ingin menambah pesanan atau menunda pembayaran.

### 4.4 Modul Cetak Struk Termal (ESC/POS Engine)
* **Konektivitas Ganda:**
  * **Bluetooth (SPP):** Pairing otomatis dan manajemen koneksi ke printer thermal 58mm atau 80mm.
  * **Wi-Fi / LAN:** Koneksi soket TCP/IP langsung ke printer kasir berbasis IP Address lokal.
* **Tata Letak Struk Standar:**
  * Header: Logo Toko + Nama Usaha + Alamat + Nomor Telepon.
  * Metadata: Nomor Invoice, Tanggal & Waktu, Nama Kasir.
  * Body: Daftar item produk, kuantitas, harga satuan, dan subtotal.
  * Summary: Subtotal, Diskon, Total Tagihan, Metode Pembayaran, Nominal Bayar, Kembalian.
  * Footer: Catatan terima kasih dan informasi media sosial/website.

### 4.5 Modul Finansial & Alokasi Pos Anggaran
* **Filter Periode Laporan:** Harian, Mingguan, Bulanan, dan Tahunan.
* **Metrik Finansial Utama:**
  * **Omzet Penjualan (Gross Revenue):** Total seluruh uang masuk dari transaksi selesai.
  * **Total HPP (Cost of Goods Sold):** Akumulasi harga modal dari seluruh item yang terjual.
  * **Laba Kotor (Gross Profit):** $	ext{Omzet Penjualan} - 	ext{Total HPP}$.
  * **Beban Pengeluaran:** Pencatatan belanja bahan baku, biaya transport, biaya utilitas/listrik, dan pengeluaran darurat.
  * **Laba Bersih (Net Profit):** $	ext{Laba Kotor} - 	ext{Total Beban Pengeluaran}$.
* **Otomasi Distribusi Alokasi Anggaran (Budget Allocation Engine):**
  Owner dapat mengonfigurasi persentase pemisahan dana dari keuntungan bersih ke dalam 4 pos utama:
  1. **Pos Belanja Bahan Baku (Restock Fund):** Menjaga keberlangsungan stok barang jualan.
  2. **Pos Tabungan Bisnis (Retained Earnings):** Modal cadangan untuk ekspansi dan investasi alat.
  3. **Pos Dana Darurat / Mendesak (Emergency Buffer):** Proteksi risiko operasional atau perbaikan mendadak.
  4. **Pos Biaya Transport & Operasional:** Anggaran mobilitas, logistik belanja, dan biaya utilitas rutin.

### 4.6 Modul Ekspor Laporan Excel (.xlsx Berformula)
* File Excel diekspor menggunakan pustaka Apache POI dengan tata letak profesional (Font Segoe UI / Calibri, zebra striping, format mata uang `Rp #,##0`).
* **Formula Excel Dinamis Terpasang:**
  * Omzet Total: `=SUM(B5:B50)`
  * Total HPP: `=SUM(C5:C50)`
  * Laba Kotor: `=B51-C51`
  * Total Beban: `=SUM(E5:E20)`
  * Laba Bersih: `=D51-F51`
  * Alokasi Tabungan (misal 30%): `=G51*0.30`
  * Alokasi Dana Darurat (misal 20%): `=G51*0.20`
  * Alokasi Belanja Bahan Baku (misal 35%): `=G51*0.35`
  * Alokasi Transport (misal 15%): `=G51*0.15`

---

## 5. Perancangan Skema Database (Supabase / PostgreSQL)

```sql
-- 1. Profiles & Hak Akses
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    role TEXT CHECK (role IN ('owner', 'worker')) DEFAULT 'worker',
    pin_code VARCHAR(6), -- PIN untuk otorisasi cepat
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Pengaturan Usaha & Parameter Alokasi Dana
CREATE TABLE store_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_name TEXT NOT NULL,
    address TEXT,
    phone TEXT,
    logo_url TEXT,
    receipt_footer TEXT DEFAULT 'Terima kasih atas kunjungan Anda!',
    savings_percent NUMERIC(5,2) DEFAULT 30.00,
    emergency_percent NUMERIC(5,2) DEFAULT 20.00,
    restock_percent NUMERIC(5,2) DEFAULT 35.00,
    transport_percent NUMERIC(5,2) DEFAULT 15.00,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 3. Kategori Produk
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Master Produk & HPP
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    sku TEXT UNIQUE,
    barcode TEXT,
    cost_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,    -- Harga Modal
    selling_price NUMERIC(12,2) NOT NULL DEFAULT 0.00, -- Harga Jual
    stock INT NOT NULL DEFAULT 0,
    min_stock_alert INT DEFAULT 5,
    image_url TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 5. Transaksi / Pesanan
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number TEXT UNIQUE NOT NULL,
    cashier_id UUID REFERENCES profiles(id),
    total_amount NUMERIC(12,2) NOT NULL,
    total_cost NUMERIC(12,2) NOT NULL,                -- Total HPP
    discount_amount NUMERIC(12,2) DEFAULT 0.00,
    paid_amount NUMERIC(12,2) NOT NULL,
    change_amount NUMERIC(12,2) DEFAULT 0.00,
    payment_method TEXT CHECK (payment_method IN ('CASH', 'QRIS')) NOT NULL,
    status TEXT CHECK (status IN ('COMPLETED', 'VOID', 'REFUNDED')) DEFAULT 'COMPLETED',
    void_reason TEXT,
    void_approved_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 6. Detail Item Pesanan
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id),
    product_name TEXT NOT NULL,
    quantity INT NOT NULL,
    unit_cost NUMERIC(12,2) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    discount_per_item NUMERIC(12,2) DEFAULT 0.00,
    subtotal NUMERIC(12,2) NOT NULL
);

-- 7. Pencatatan Beban Pengeluaran (Expenses)
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category TEXT CHECK (category IN ('BAHAN_BAKU', 'TRANSPORT', 'OPERASIONAL', 'DARURAT', 'LAINNYA')) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    notes TEXT,
    receipt_image_url TEXT,
    created_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 8. Row Level Security (RLS) Policies
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE expenses ENABLE ROW LEVEL SECURITY;

-- Policy: Produk dapat dibaca oleh kasir dan owner, tetapi hanya bisa diubah oleh owner
CREATE POLICY "Public read products" ON products FOR SELECT USING (true);
CREATE POLICY "Owner write products" ON products FOR ALL USING (
    EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'owner')
);

-- Policy: Orders dapat diinsert oleh kasir dan owner
CREATE POLICY "Cashier & Owner insert orders" ON orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Cashier & Owner read orders" ON orders FOR SELECT USING (true);
```

---

## 6. Desain Layout Adaptif UI/UX (Mobile vs Tablet)

Aplikasi dibangun menggunakan Jetpack Compose dengan breakpoint `WindowWidthSizeClass`:
* **Compact / Medium (< 840dp) - Smartphone:**
  * Navigasi bawah (*Bottom Navigation Bar*): Kasir, Riwayat, Laporan, Pengaturan.
  * Tampilan kasir: Grid katalog 2 kolom dengan *Floating Bottom Bar* ringkasan keranjang.
  * Halaman checkout & pembayaran ditampilkan dalam bentuk *Full Screen Modal*.
* **Expanded (≥ 840dp) - Tablet POS Mode:**
  * Layar terbagi (*Split Screen Dual-Pane*):
    * Sisi Kiri (60%): Tab Kategori & Grid Katalog Produk (4–5 kolom).
    * Sisi Kanan (40%): Keranjang belanja aktif permanen, rincian diskon, tombol edit order, dan panel kalkulasi pembayaran.
  * Dashboard Owner: Multi-card view menampilkan grafik omzet, kartu saldo alokasi dana, dan tabel transaksi secara berdampingan.

---

## 7. Audit Trail, Keamanan & Rekomendasi Unggulan

1. **Proteksi Otorisasi Void / Pembatalan:**
   * Pekerja kasir tidak dapat membatalkan struk yang telah selesai dicetak tanpa memasukkan PIN 6-digit Owner.
   * Setiap pembatalan wajib mencantumkan alasan (misal: *Salah input item*, *Pelanggan batal beli*) dan tercatat di tabel audit log.
2. **Rekonsiliasi Kasir & Pergantian Shift (*Cash Drawer Management*):**
   * Sebelum memulai shift, kasir mencatat modal awal kas (*Starting Cash*).
   * Pada akhir shift, kasir melakukan rekonsiliasi total uang tunai fisik yang ada di laci kasir terhadap akumulasi transaksi kasir di aplikasi.
3. **Pemindaian Barcode via Kamera HP (Google ML Kit):**
   * Pemanfaatan modul kamera perangkat untuk memindai barcode produk secara instan, menghemat biaya pembelian scanner barcode eksternal.
4. **Sinkronisasi Dua Arah Offline-First:**
   * Setiap transaksi disimpan secara lokal di SQLite Room dengan flag `sync_status = PENDING`.
   * Background service WorkManager mendengarkan status konektivitas jaringan (`NetworkCallback`) dan secara otomatis mengirimkan antrean data ke Supabase begitu perangkat terhubung kembali ke internet.

---

## 8. Rencana Tahapan Eksekusi (Roadmap Pengembangan)

```
SPRINT 1: Fondasi Arsitektur & Setup Basis Data
├── Setup Android Clean Architecture (Compose, Hilt, Room DB, Supabase SDK).
├── Konfigurasi Skema PostgreSQL, RLS Policies, dan Auth di Supabase.
└── Implementasi Modul Otentikasi & Role-Based Access Control (Owner vs Worker).

SPRINT 2: Manajemen Produk, Inventaris & POS Kasir
├── Modul CRUD Produk, Kategori, Harga Modal (HPP), dan Harga Jual (Owner).
├── Adaptive POS Screen (Grid Katalog + Keranjang Belanja Dinamis).
├── Fitur Edit Order, Diskon Produk / Transaksi, dan Proses Pembayaran (Cash & QRIS).
└── Integrasi Driver ESC/POS Thermal Printer (Bluetooth SPP & Wi-Fi LAN Socket).

SPRINT 3: Finansial, Alokasi Dana & Ekspor Excel
├── Modul Pencatatan Beban Biaya (Bahan Baku, Transport, Operasional, Darurat).
├── Engine Kalkulasi Finansial (Omzet, HPP, Laba Kotor, Laba Bersih, Pos Distribusi).
├── Engine Ekspor Excel (.xlsx) dengan Apache POI lengkap dengan formula aktif.
└── Implementasi Background Sync Engine via WorkManager.

SPRINT 4: Fitur Keamanan, Hardening & Uji Coba Lapangan
├── Fitur Rekonsiliasi Kasir / Tutup Shift & Proteksi PIN Approval untuk Void Order.
├── Pengujian menyeluruh konektivitas printer thermal di berbagai tipe perangkat.
├── Uji coba ketahanan sistem offline-mode (simulasi pemutusan jaringan).
└── Finalisasi build release APK & optimasi performa.
```