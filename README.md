# 🌱🧸 SERA: Self-Awareness, Emotion, & Resilience Assistant

**SERA (Self-Awareness, Emotion, & Resilience Assistant)** adalah aplikasi mobile Android berbasis **Kotlin** dan **Jetpack Compose (Material 3)** yang dirancang untuk membantu mahasiswa memantau kondisi emosional harian, mengenali pola emosi yang berulang, merencanakan aktivitas perawatan diri (*self-care*) secara adaptif berdasarkan kondisi dan lokasi, mengevaluasi efektivitas aktivitas *self-care*, serta terhubung dengan lingkar dukungan tepercaya (*Support Circle*).

Proyek ini dikembangkan untuk memenuhi **Project Akhir Praktikum Pengembangan Aplikasi Perangkat Bergerak (PAPB) — Kelas TIF-E**  
**Program Studi Teknik Informatika — Fakultas Ilmu Komputer — Universitas Brawijaya (2026)**  
- **Dosen Pengampu**: Fais Al Huda, S.Kom., M.Kom.  
- **Asisten Praktikum**: Ihtishamul Hasan & Yosefina Agustine Kartini Ofong

---

## 👥 Tim Pengembang & Pembagian Tugas (Kelompok 4)

| No | Nama Lengkap | NIM | Kode | Pembagian Tugas & Fungsi Utama (Problem Solving) | File Kode Utama |
|:--:|---|---|:--:|---|---|
| 1 | **Muhammad Nur Daffa Nashrullah** | `245150207111081` | `[MNDN]` | • Flowchart Sistem (Per Fungsi Utama)<br>• **Fungsi 6**: Pengelolaan Privasi & Kontrol Data (*Private Mode & Data Control*)<br>• **Fungsi 5 (Bagian 2)**: Deteksi Pola & Insight Personal (*Personal Insight Engine* & State Management) | `ProfilePrivacyScreen.kt`, `SeraViewModel.kt`, `MainActivity.kt` |
| 2 | **Anindhita Faiza Aulia** | `245150201111042` | `[AFA]` | • Definisi Fungsi Utama (*Problem Solving*) & Arsitektur Dasar<br>• **Fungsi 2**: Rekomendasi Aktivitas Self-Care Adaptif (*Adaptive Self-Care Planner* + Sensor GPS)<br>• **Fungsi 3**: Evaluasi Efektivitas Personal Aktivitas (*What-Helped-Me Tracker*) | `SelfCareAndSupportScreen.kt`, `InteractiveModals.kt`, `SeraLocalStorage.kt`, `SeraModels.kt`, `SeraAudioAndVoiceEngine.kt` |
| 3 | **Aisha Maryam** | `245150201111040` | `[AM]` | • Konsep & Desain Antarmuka (UI/UX) serta *Screen Flow* Aplikasi<br>• **Fungsi 4**: Laporan Refleksi Mingguan Otomatis (*Weekly Reflection Report*)<br>• **Fungsi 7**: Rencana Dukungan Personal (*Support Circle* & Ruang Tenang SOS) | `AuthAndOnboardingScreen.kt`, `HomeScreen.kt` |
| 4 | **Shafa Rizwana Zarin** | `245150207111071` | `[SRZ]` | • Use Case Diagram (Keseluruhan Aplikasi)<br>• **Fungsi 1**: Pemetaan Pola Emosi (*Emotion Pattern Map* + *Voice Emotion Journal* Sensor Mikrofon)<br>• **Fungsi 5 (Bagian 1)**: Deteksi Pola & Insight Personal (*Personal Insight Engine* UI & Visualisasi Tren) | `CheckInJournalScreen.kt`, `HistoryPatternScreen.kt` |

---

## 🔄 Alur Penggunaan Aplikasi (Application Flow)

```text
[Splash Screen Animasi]
        │
        ▼
[Pengguna Baru?] ──(Ya)──► [Onboarding 3 Langkah & Preferensi Awal]
        │                                  │
     (Tidak)                               ▼
        └──────────────────► [Login / Sign Up / Google Sign-In]
                                           │
                                           ▼
                         [Beranda / Dashboard Sanctuary Pomi]
                                           │
      ┌────────────────────┬───────────────┴───────────────┬────────────────────┐
      ▼                    ▼                               ▼                    ▼
  [1. Catat]         [2. Riwayat]                   [3. Self-Care]         [4. Profil]
Check-In Emosi     Emotion Pattern Map           Adaptive Self-Care      Private Mode &
5 Langkah +        • Matriks Gelembung Mood      • Filter Durasi/Emosi   Data Control
Voice Journal      • Grafik Tren Harian/Minggu   • Radar Lokasi GPS      • Toggle Analisis
(Mikrofon)         • Personal Insight Engine     • Sesi Napas & Sensori  • Dark Mode
                   • Weekly Reflection Report    • Evaluasi Aktivitas    • Ekspor & Hapus Data
                                                 • Support Circle & SOS
```

1. **Splash Screen & Autentikasi**:
   - Menampilkan animasi indikator pemuatan dan maskot interaktif **Pomi** (*Ruang Nyaman Mahasiswa*).
   - Pengguna baru diarahkan ke *Onboarding* untuk mengenal fitur utama dan memilih fokus preferensi awal, lalu masuk melalui halaman *Sign In*, *Sign Up*, atau *Google Sign-In*.
2. **Beranda (*Dashboard Sanctuary*)**:
   - Menampilkan maskot beruang ungu interaktif **Pomi** secara penuh yang hidup (berkedip otomatis, bernapas lembut, serta bergerak seluruh tubuh apabila disentuh).
   - Saat digulir ke bawah, menampilkan **Check-In Emosi Cepat**, **Rekomendasi Self-Care Prioritas**, **Ringkasan Laporan Refleksi Mingguan (*Weekly Reflection Report*)**, dan akses cepat **Ruang Tenang SOS** di bilah atas.
3. **Catat (*Check-In Emosi & Jurnal Lembut Jiwa*)**:
   - Pencatatan terstruktur dalam 5 langkah: **(1) Emosi Dominan** (*Senang, Tenang, Biasa, Cemas, Sedih, Lelah*), **(2) Intensitas Rasa** (Level 1–5), **(3) Konteks Aktivitas** (*Tugas Kuliah, Organisasi, Sosial, Keluarga, Istirahat*), **(4) Kualitas Tidur Semalam**, dan **(5) Jurnal Teks / Rekaman Suara (*Voice Emotion Journal*)**.
   - Seluruh entri langsung disimpan secara persisten ke dalam database lokal **SQLite (`sera_sanctuary_sqlite.db`)**.
4. **Riwayat (*Emotion Pattern Map & Personal Insight Engine*)**:
   - Menyajikan visualisasi **Grafik Tren Gelombang Emosi** (*Harian, Mingguan, Bulanan*), **Matriks Gelembung Mood** yang tersinkronisasi langsung dengan riwayat *check-in*, serta kartu **Personal Insight Engine** yang mendeteksi korelasi antara emosi, konteks aktivitas, dan kualitas tidur.
5. **Self-Care & Support Circle (*Adaptive Self-Care Planner & What-Helped-Me Tracker*)**:
   - Menyaring rekomendasi aktivitas *self-care* secara *rule-based* berdasarkan emosi saat ini, durasi waktu luang (5/10/15+ menit), lokasi sekitar (**Sensor GPS**), dan riwayat evaluasi pengguna.
   - Dilengkapi sesi interaktif (*Latihan Napas Kotak 4–4–4*, *Dekonstruksi Tugas Mikro*, dan *Screen-Free Sensory Reset* dengan *audio soundscape* 432 Hz / Hujan Rintik).
   - Setelah sesi selesai, pengguna menilai efektivitas aktivitas pada **What-Helped-Me Tracker** (*Sangat Membantu*, *Cukup Membantu*, atau *Kurang Cocok*) untuk memperbarui prioritas rekomendasi berikutnya.
   - Menyediakan **Support Circle** untuk menghubungi kontak tepercaya langsung melalui **WhatsApp** dengan format pesan terstruktur serta direktori layanan konseling kampus.
6. **Profil & Privasi (*Private Mode & Data Control*)**:
   - Mengelola identitas dan foto profil pengguna, *toggle* **Mode Gelap (*Dark Mode*)**, pengaturan izin **Analisis Pola Personal**, ekspor ringkasan jurnal, serta penghapusan catatan secara selektif maupun permanen.

---

## ✨ 7 Fungsi Utama (Problem Solving)

1. **Fungsi 1 — Emotion Pattern Map (`[SRZ]`)**: Mengelompokkan data *check-in* emosi, intensitas, konteks kegiatan, dan kualitas istirahat ke dalam matriks kalender emosi dan grafik tren multi-periode.
2. **Fungsi 2 — Adaptive Self-Care Planner (`[AFA]`)**: Mesin rekomendasi berbasis aturan (*rule-based recommendation*) yang mencocokkan kondisi emosi, waktu luang, konteks, lokasi GPS, dan bobot efektivitas historis.
3. **Fungsi 3 — What-Helped-Me Tracker (`[AFA]`)**: Mencatat umpan balik evaluasi pasca-aktivitas *self-care* dan memperbarui skor kecocokan personal untuk rekomendasi selanjutnya.
4. **Fungsi 4 — Weekly Reflection Report (`[AM]`)**: Merangkum frekuensi emosi mingguan, pemicu konteks dominan, kualitas istirahat rata-rata, dan aktivitas *self-care* paling efektif menjadi laporan refleksi non-diagnostik.
5. **Fungsi 5 — Personal Insight Engine (`[SRZ]` & `[MNDN]`)**: Menganalisis data historis menggunakan perhitungan frekuensi bersyarat (*conditional frequency*) dengan ambang batas minimum data untuk menghasilkan *insight* pola emosional yang objektif.
6. **Fungsi 6 — Private Mode & Data Control (`[MNDN]`)**: Memberikan kendali penuh atas privasi data lokal (SQLite), izin analitik pola, manajemen catatan individu, ekspor jurnal, dan penghapusan data.
7. **Fungsi 7 — Support Circle (`[AM]`)**: Mengelola kontak darurat/tepercaya dengan dialog konfirmasi privasi sebelum mengarahkan pengguna ke WhatsApp atau panggilan telepon beserta rute layanan konseling kampus.

---

## 📡 Pemanfaatan Sensor Perangkat & Database Lokal

* **Sensor GPS (`LocationManager` & `Geocoder`)**: Mendeteksi koordinat lokasi pengguna secara *real-time* saat diaktifkan untuk menghitung jarak ke titik ruang tenang di sekitar kampus (seperti Taman Hutan Kampus, Gazebo Perpustakaan, dan Ruang Konseling ULTKSP) serta membuka rute navigasi Google Maps.
* **Sensor Mikrofon & Audio Sintetis (`AudioRecord` & `AudioTrack`)**: Merekam jurnal suara pengguna dalam format WAV PCM 16-bit lengkap dengan visualisasi gelombang suara waktu-nyata, serta menghasilkan *audio soundscape* relaksasi (Hujan Rintik, Hutan Bambu, 432 Hz Solfeggio).
* **Penyimpanan Persisten SQLite (`SQLiteOpenHelper`)**: Seluruh riwayat *check-in* emosi, evaluasi *self-care*, kontak tepercaya, langkah tugas mikro, dan pengaturan privasi disimpan di tabel-tabel relasional lokal (`checkin_entries`, `selfcare_evaluations`, `trusted_contacts`, `micro_task_steps`, `kv_store`) sehingga data tetap aman meskipun aplikasi ditutup.

---

## 🛠️ Cara Menjalankan Project di Android Studio

1. **Clone Repository**:
   ```bash
   git clone https://github.com/faizaauliaa/SERA_Self-awareness-Emotion-Resilience-Assistant.git
   ```
2. Buka **Android Studio**, pilih **Open**, lalu arahkan ke folder `SERA`.
3. Tunggu proses **Gradle Sync** selesai (menggunakan **JDK 21**).
4. Jalankan aplikasi pada **Emulator Android / Perangkat Fisik** (minimal **Android 7.0 / API 24**, target **API 35**) dengan menekan tombol **Run (`▶`)** atau melalui terminal:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
