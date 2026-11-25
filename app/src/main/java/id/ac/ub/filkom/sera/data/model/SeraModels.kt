package id.ac.ub.filkom.sera.data.model

import androidx.compose.ui.graphics.Color
import id.ac.ub.filkom.sera.ui.theme.*

enum class EmotionType(
    val id: String,
    val label: String,
    val emoji: String,
    val cardHeadline: String,
    val cardSubtitle: String,
    val harmonyGroup: String,
    val bgColor: Color,
    val accentColor: Color,
    val borderColor: Color,
    val dotColor: Color
) {
    SENANG(
        id = "SENANG",
        label = "Senang",
        emoji = "😊",
        cardHeadline = "Hati Terasa Hangat & Bersemangat",
        cardSubtitle = "Ada energi positif dan rasa syukur yang menemani langkahmu hari ini.",
        harmonyGroup = "Harmonis",
        bgColor = EmotionHappyBg,
        accentColor = EmotionHappyAccent,
        borderColor = EmotionHappyBorder,
        dotColor = Color(0xFFE07A2E)
    ),
    TENANG(
        id = "TENANG",
        label = "Tenang",
        emoji = "😌",
        cardHeadline = "Hati Terasa Rileks & Tenang",
        cardSubtitle = "Energi stabil, nafas lega, dan pikiran jernih untuk menjalani aktivitas dengan penuh kesadaran.",
        harmonyGroup = "Harmonis",
        bgColor = EmotionCalmBg,
        accentColor = EmotionCalmAccent,
        borderColor = EmotionCalmBorder,
        dotColor = Color(0xFF159A7C)
    ),
    BIASA(
        id = "BIASA",
        label = "Biasa",
        emoji = "😐",
        cardHeadline = "Ritme Hati Cukup Netral",
        cardSubtitle = "Tidak terlalu bergejolak, berjalan mengalir seperti biasa.",
        harmonyGroup = "Netral",
        bgColor = EmotionNeutralBg,
        accentColor = EmotionNeutralAccent,
        borderColor = EmotionNeutralBorder,
        dotColor = Color(0xFFD97706)
    ),
    SEDIH(
        id = "SEDIH",
        label = "Sedih",
        emoji = "🥺",
        cardHeadline = "Hati Sedang Butuh Ruang Teduh",
        cardSubtitle = "Wajar merasa lelah atau sendu. Izinkan dirimu beristirahat tanpa menghakimi.",
        harmonyGroup = "Tegangan Tinggi",
        bgColor = EmotionSadBg,
        accentColor = EmotionSadAccent,
        borderColor = EmotionSadBorder,
        dotColor = Color(0xFFE34D7B)
    ),
    CEMAS(
        id = "CEMAS",
        label = "Cemas",
        emoji = "😰",
        cardHeadline = "Cemas & Overthinking",
        cardSubtitle = "Beban tugas atau ekspektasi terasa menekan. Mari urai perlahan satu per satu bersama Pomi.",
        harmonyGroup = "Tegangan Tinggi",
        bgColor = EmotionAnxiousBg,
        accentColor = EmotionAnxiousAccent,
        borderColor = EmotionAnxiousBorder,
        dotColor = Color(0xFF6653E0)
    );

    companion object {
        fun fromId(id: String): EmotionType =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: TENANG
    }
}

enum class ActivityContext(
    val id: String,
    val label: String,
    val shortLabel: String,
    val iconEmoji: String
) {
    PERKULIAHAN("PERKULIAHAN", "Perkuliahan / Tugas", "Perkuliahan", "🎓"),
    PERTEMANAN("PERTEMANAN", "Pertemanan & Cinta", "Relasi & Teman", "♡"),
    KELUARGA("KELUARGA", "Keluarga / Rumah", "Keluarga", "🏠"),
    MAGANG("MAGANG", "Karier & Magang", "Karier & Magang", "💼"),
    PERSONAL("PERSONAL", "Ekspektasi Diri", "Me-Time", "🧘"),
    FINANSIAL("FINANSIAL", "Finansial / Biaya", "Finansial", "💳");

    companion object {
        fun fromId(id: String): ActivityContext =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: PERKULIAHAN
    }
}

enum class SleepQuality(
    val id: String,
    val label: String,
    val durationLabel: String,
    val badgeSummary: String,
    val emoji: String,
    val numericScore: Int
) {
    KURANG(
        id = "KURANG",
        label = "Kurang",
        durationLabel = "< 5 Jam",
        badgeSummary = "Tidur Kurang (5 jam)",
        emoji = "🌙",
        numericScore = 1
    ),
    CUKUP(
        id = "CUKUP",
        label = "Cukup",
        durationLabel = "6 - 7 Jam",
        badgeSummary = "Tidur Cukup (6-7 jam)",
        emoji = "🌙",
        numericScore = 2
    ),
    PULAS(
        id = "PULAS",
        label = "Pulas",
        durationLabel = "8+ Jam",
        badgeSummary = "Tidur Pulas (8 jam)",
        emoji = "🛌",
        numericScore = 3
    );

    companion object {
        fun fromId(id: String): SleepQuality =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: CUKUP
    }
}

data class EmotionCheckIn(
    val id: String,
    val dayShort: String,       // "Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"
    val dayOfMonth: Int,        // e.g. 24
    val monthName: String,      // "Okt"
    val year: Int = 2026,
    val timeLabel: String,      // "Kamis, 24 Okt • 08:30 WIB"
    val emotion: EmotionType,
    val intensity: Int,         // 1..5
    val contexts: List<ActivityContext>,
    val sleepQuality: SleepQuality,
    val journalNote: String,
    val appreciationLabel: String = "Peluk Dirimu",
    val isAppreciated: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val voiceFilePath: String? = null,
    val supportiveActivityTag: String = "Relaksasi Bahu (3m)"
)

enum class SelfCareCategory(val id: String, val label: String, val emoji: String) {
    SEMUA("SEMUA", "Semua", "✨"),
    PERNAPASAN("PERNAPASAN", "Pernapasan", "🌬️"),
    AKADEMIK("AKADEMIK", "Dekonstruksi Tugas", "📋"),
    SENSORI("SENSORI", "Relaksasi Sensori", "🌿"),
    LOKASI_GPS("LOKASI_GPS", "Sekitar Kampus (GPS)", "📍")
}

enum class InteractiveModuleType {
    BREATHING_BOX,
    MICRO_TASK,
    SENSORY_RESET,
    REFLECTIVE_WRITING
}

data class SelfCareActivity(
    val id: String,
    val title: String,
    val shortTitle: String,
    val category: SelfCareCategory,
    val categoryTagLabel: String,
    val secondaryTagLabel: String?,
    val durationMinutes: Int,
    val description: String,
    val detailedSteps: List<String>,
    val targetEmotions: List<EmotionType>,
    val targetContexts: List<ActivityContext>,
    val baseMatchPercentage: Int,
    val socialProofText: String,
    val actionButtonText: String,
    val moduleType: InteractiveModuleType,
    val isBookmarked: Boolean = false,
    val isGpsBased: Boolean = false,
    val gpsLocationHint: String? = null
)

// Sensor 1: GPS Location-Based Self-Care Recommendation Model
data class LocationSelfCarePlace(
    val id: String,
    val placeName: String,
    val activityTitle: String,
    val distanceText: String,
    val durationMinutes: Int,
    val environmentTag: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val iconEmoji: String
)

data class GpsSensorState(
    val isPermissionGranted: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val latitude: Double = -7.9526,
    val longitude: Double = 112.6138,
    val detectedAreaName: String = "Area Kampus FILKOM Universitas Brawijaya",
    val nearbyRecommendations: List<LocationSelfCarePlace> = emptyList(),
    val lastUpdatedLabel: String = "Sensor GPS Siaga (Hanya Aktif Saat Dipilih)"
)

// Sensor 2: Microphone Voice Emotion Journal Model
data class VoiceJournalRecord(
    val id: String,
    val title: String,
    val durationSeconds: Int,
    val timestampLabel: String,
    val emotion: EmotionType,
    val filePath: String?,
    val summaryNote: String
)

enum class EvaluationRating(
    val id: String,
    val label: String,
    val emoji: String,
    val scoreDelta: Int
) {
    MEMBANTU("MEMBANTU", "Membantu", "👍", +8),
    BIASA_SAJA("BIASA_SAJA", "Biasa Saja", "😐", +1),
    KURANG_COCOK("KURANG_COCOK", "Kurang Cocok", "👎", -10);

    companion object {
        fun fromId(id: String): EvaluationRating =
            entries.find { it.id == id } ?: MEMBANTU
    }
}

data class ActivityEvaluation(
    val id: String,
    val activityId: String,
    val activityTitle: String,
    val timestampLabel: String,
    val rating: EvaluationRating,
    val reflectionNote: String,
    val emotionAtTime: EmotionType,
    val intensityAtTime: Int
)

data class MicroTaskStep(
    val id: Int,
    val title: String,
    val subtitle: String,
    val durationLabel: String,
    val isCompleted: Boolean = false
)

data class TrustedContact(
    val id: String,
    val initials: String,
    val name: String,
    val badgeText: String,
    val relationSubtitle: String,
    val availabilityText: String,
    val phoneNumber: String,
    val isVerifiedService: Boolean = false,
    val isPrioritySos: Boolean = true,
    val customTemplates: List<String> = listOf(
        "Hai Sarah, lagi senggang kah? Boleh ngobrol sebentar, lagi agak overthinking nih...",
        "Lagi butuh teman cerita santai sejenak, kabari kalau kamu luang ya.",
        "Halo Layanan Konseling, saya mahasiswa FILKOM ingin menanyakan jadwal sesi konsultasi sebaya minggu ini."
    )
)

data class PersonalInsight(
    val id: String,
    val badgeLabel: String,
    val engineTag: String,
    val title: String,
    val description: String,
    val highlightPhrase: String,
    val metricHighlight: String,
    val supportingEntryCount: Int,
    val isSleepFactor: Boolean = false,
    val relatedContext: ActivityContext? = null
)

data class WeeklyReflectionReportData(
    val periodLabel: String,
    val totalEntries: Int,
    val averageIntensity: Float,
    val dominantEmotion: EmotionType,
    val dominantDaysCount: Int,
    val calmHarmonyPercentage: Int,
    val emotionBreakdown: Map<EmotionType, Int>,
    val topContexts: List<Pair<ActivityContext, Int>>,
    val mostHelpfulActivityTitle: String,
    val mostHelpfulCount: Int,
    val narrativeReflection: String,
    val guidingQuestion: String,
    val selectedNextWeekCommitment: String,
    val availableCommitments: List<String>
)

data class PrivacyDataControlState(
    val allowPatternAnalysis: Boolean = true,
    val maskJournalPreviewOnHome: Boolean = false,
    val dailyCheckInReminder: Boolean = true,
    val morningReminderTime: String = "08:00",
    val reminderTimeLabel: String = "21:00",
    val gentleSupportSuggestion: Boolean = true,
    val selectedSoundscape: String = "432 Hz Solfeggio Serenity",
    val biometricLockEnabled: Boolean = false,
    val isDarkModeEnabled: Boolean = false
)

enum class DataDeletionScope(
    val id: String,
    val title: String,
    val impactDescription: String
) {
    EVALUATIONS_ONLY(
        id = "EVALUATIONS_ONLY",
        title = "Hapus Riwayat Evaluasi Self-Care",
        impactDescription = "Menghapus catatan umpan balik What-Helped-Me Tracker dan mengembalikan bobot rekomendasi ke pengaturan awal. Jurnal emosi tetap aman."
    ),
    JOURNALS_TEXT_ONLY(
        id = "JOURNALS_TEXT_ONLY",
        title = "Hapus Riwayat Catatan Pekan Ini",
        impactDescription = "Menghapus isi tulisan cerita pribadi & jurnal suara pada riwayat check-in pekan ini dengan aman."
    ),
    ALL_USER_DATA(
        id = "ALL_USER_DATA",
        title = "Hapus Seluruh Data & Reset Ruang Aman",
        impactDescription = "Menghapus permanen seluruh riwayat check-in emosi, jurnal suara, evaluasi self-care, dan preferensi dari penyimpanan perangkat ini."
    )
}

data class SeraNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timeLabel: String,
    val categoryBadge: String,
    val actionButtonText: String = "Lihat Pola",
    val isUnread: Boolean = true,
    val isSupportQuickAccess: Boolean = false
)
