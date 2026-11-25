# SERA — Self-Awareness, Emotion, & Resilience Assistant 🌱🧸

Aplikasi mobile berbasis **Kotlin & Jetpack Compose (Material 3)** untuk pemantauan ritme emosi mahasiswa, pemetaan pola mingguan, perencana *self-care* adaptif berbasis kondisi & lokasi **Sensor GPS**, pencatatan **Voice Emotion Journal (Sensor Mikrofon)**, serta lingkar dukungan terpadu (*Support Circle*).

Dirancang untuk **Project Akhir Praktikum Pengembangan Aplikasi Perangkat Bergerak (PAPB) — TIF-E — Fakultas Ilmu Komputer Universitas Brawijaya (2026)**.

---

## 👥 Tim Pengembang (Kelompok 4)

| No | Nama & NIM | Tanggung Jawab Fungsi Utama (Problem Solving) |
|---|---|---|
| 1 | **Muhammad Nur Daffa Nashrullah** (`245150207111081`) | **Fungsi 6**: Pengelolaan Privasi & Kontrol Data (*Private Mode & Data Control*) & **Fungsi 5**: Deteksi Pola & Insight Personal (Bagian 2) |
| 2 | **Anindhita Faiza Aulia** (`245150201111042`) | **Fungsi 2**: Rekomendasi Aktivitas Self-Care Adaptif (*Adaptive Self-Care Planner* + Sensor GPS) & **Fungsi 3**: Evaluasi Efektivitas Personal Aktivitas (*What-Helped-Me Tracker*) |
| 3 | **Aisha Maryam** (`245150201111040`) | **Fungsi 4**: Laporan Refleksi Mingguan Otomatis (*Weekly Reflection Report*) & **Fungsi 7**: Rencana Dukungan Personal (*Support Circle* & Ruang Tenang SOS) |
| 4 | **Shafa Rizwana Zarin** (`245150207111071`) | **Fungsi 1**: Pemetaan Pola Emosi (*Emotion Pattern Map* + Voice Emotion Journal Sensor Mikrofon) & **Fungsi 5**: Deteksi Pola & Insight Personal (Bagian 1) |

---

## 🧸 Pembaruan Desain Estetik Organik & Maskot Interaktif Pomi

1. **Latar Belakang Gradasi Pastel Aurora & Gelembung Mengambang Ringan ([`PastelAuroraBubbleBackground`](app/src/main/java/id/ac/ub/filkom/sera/ui/components/SeraCustomComponents.kt#L76-L194))**
   - Memadukan gradasi pastel lembut (**Hijau Mint `#E8FAF2`**, **Kuning Pastel `#FFF9E6`**, **Pink Blossom `#FEEEF5`**, **Biru Muda `#E7F6FF`**, dan **Lavender `#F3EEFF`**) dengan gelembung-gelembung kaca transparan yang bergerak perlahan di satu layer `Canvas` tanpa alokasi objek per-frame sehingga sangat ringan (60 FPS) dan menenangkan.
2. **Maskot Boneka Beruang Ungu Interaktif — Pomi ([`PomiBearMascot`](app/src/main/java/id/ac/ub/filkom/sera/ui/components/SeraCustomComponents.kt#L209-L888))**
   - Mengisi seluruh lingkaran maskot pada *Splash Screen*, *Onboarding*, *Sign In*, *Sign Up*, *Beranda Sanctuary*, *Catat Emosi*, *Sesi Napas Kotak 4–4–4*, *Dekonstruksi Tugas Mikro*, *Screen-Free Sensory Reset* (dengan headphone), *Analisis Pola Mendalam*, *Detail Jurnal*, *Notifikasi*, *Profil*, dan *Ruang Tenang SOS*.
   - **Hidup & Responsif**: Berkedip otomatis secara alami, bernapas halus, dan apabila **diklik/disentuh**, Pomi akan melompat membal (*spring bounce*), menggoyangkan telinga, memancarkan partikel hati, serta memberikan sapaan empatik.

---

## 📡 Dokumentasi Implementasi Sensor pada Aplikasi SERA

1. **Sensor 1: GPS (*Global Positioning System*) — Rekomendasi Aktivitas Self-Care Berbasis Lokasi**
   - **Izin Manifest**: `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION` di [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml).
   - **Kode Implementasi**: [`SeraViewModel.activateGpsAndFetchRecommendations()`](app/src/main/java/id/ac/ub/filkom/sera/viewmodel/SeraViewModel.kt#L449-L532) & kartu lokasi GPS di [`SelfCareAndSupportScreen.kt`](app/src/main/java/id/ac/ub/filkom/sera/ui/screens/SelfCareAndSupportScreen.kt).
   - **Perlindungan Privasi**: Hanya aktif saat pengguna menekan tombol *Ambil Koordinat GPS*, tidak melacak di latar belakang, dapat dimatikan kapan saja, dan tetap menyediakan rekomendasi alternatif apabila izin ditolak.

2. **Sensor 2: Mikrofon (*Microphone*) — Voice Emotion Journal**
   - **Izin Manifest**: `RECORD_AUDIO` di [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml).
   - **Kode Implementasi**: Perekaman `MediaRecorder` & pemutaran `MediaPlayer` beserta visualisasi gelombang suara (`VoiceWaveformVisualizer`) di [`CheckInJournalScreen.kt`](app/src/main/java/id/ac/ub/filkom/sera/ui/screens/CheckInJournalScreen.kt) dan [`JournalEntryDetailDialog`](app/src/main/java/id/ac/ub/filkom/sera/ui/screens/InteractiveModals.kt#L2117).
   - **Perlindungan Privasi**: Hanya merekam saat pengguna menekan tombol *Mulai Rekam Suara*, disimpan di penyimpanan internal privat aplikasi, dan pengguna tetap dapat menulis jurnal teks biasa tanpa izin mikrofon.
