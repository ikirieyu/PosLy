# PosLy 🏪

**Aplikasi POS & Manajemen Finansial UMKM** — Native Android (Kotlin + Jetpack Compose)

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Backend-Supabase-3ECF8E?logo=supabase)](https://supabase.com)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

---

## ✨ Fitur Utama

- 🛒 **POS Kasir Adaptif** — Split-pane tablet / single-pane mobile
- 📦 **Manajemen Produk & Inventaris** — HPP, harga jual, stok otomatis
- 🖨️ **Cetak Struk Thermal** — Bluetooth SPP & Wi-Fi TCP/IP (ESC/POS)
- 📊 **Dashboard Finansial** — Omzet, HPP, Laba Kotor, Laba Bersih
- 💰 **Budget Allocation Engine** — Otomasi distribusi dana ke 4 pos
- 📁 **Ekspor Excel (.xlsx)** — Formula aktif via Apache POI
- 🔄 **Offline-First Sync** — Room DB lokal + WorkManager → Supabase
- 🔐 **RBAC** — Owner vs Worker dengan PIN approval untuk void

---

## 🏗️ Arsitektur

```
Clean Architecture + MVVM + MVI
├── Presentation (Jetpack Compose + ViewModel)
├── Domain (UseCases + Repository Interfaces)
└── Data (Room DB + Supabase Remote + WorkManager Sync)
```

## 🎨 Design System

| Token | Hex | Penggunaan |
|---|---|---|
| Background | `#FFFFFF` | Layar utama kasir |
| Surface/Card | `#F8FAFC` | Kartu produk |
| Primary | `#4F6BED` | Tombol Proses Bayar |
| Primary Container | `#EEF2FF` | Item terpilih |
| Text Primary | `#1E293B` | Judul, total harga |
| Text Secondary | `#64748B` | SKU, tanggal |
| Border | `#E2E8F0` | Garis pemisah |

## 📱 Adaptive Layout

- **Mobile (< 840dp)**: Bottom Nav + katalog 2-kolom + floating cart bar
- **Tablet (≥ 840dp)**: Navigation Rail + split-pane (katalog 60% | keranjang 40%)

## 🛠️ Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Architecture | Clean Arch, Hilt DI |
| Local DB | Room + SQLCipher |
| Remote | Supabase (Auth, PostgreSQL, Storage, Realtime) |
| Sync | WorkManager |
| Printing | ESC/POS over Bluetooth/Wi-Fi |
| Barcode | Google ML Kit |
| Charts | MPAndroidChart |
| Excel | Apache POI |

## 🚀 Setup

### 1. Clone

```bash
git clone https://github.com/ikirieyu/PosLy.git
cd PosLy
```

### 2. Konfigurasi Supabase

Buka aplikasi → **Pengaturan** → **Koneksi Database** → masukkan Supabase URL & Anon Key.

### 3. Setup Database Supabase

Jalankan script SQL di `supabase/schema.sql` pada Supabase SQL Editor kamu.

### 4. Build

Buka di **Android Studio** (Ladybug / Meerkat), sync Gradle, lalu Run.

## 📋 Requirement

- Min SDK: **26** (Android 8.0 Oreo)
- Target SDK: **35** (Android 15)
- Android Studio: **Ladybug 2024.2+**
- JDK: **17+**

## 🗺️ Roadmap

- [x] Sprint 1: Fondasi Arsitektur & Database
- [x] Sprint 2: POS Screen & Product Management
- [x] Sprint 3: Finansial & Ekspor Excel
- [x] Sprint 4: Security & Hardening

## 📄 License

MIT License — lihat [LICENSE](LICENSE).
