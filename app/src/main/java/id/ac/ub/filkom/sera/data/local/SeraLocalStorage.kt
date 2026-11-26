package id.ac.ub.filkom.sera.data.local

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import id.ac.ub.filkom.sera.data.model.*

/**
 * Relational SQLite Database + SharedPreferences Persistence Layer for SERA.
 * Menyimpan seluruh catatan check-in emosi, jurnal refleksi, rekaman Voice Emotion Journal,
 * evaluasi What-Helped-Me Tracker, kontak Lingkar Dukungan, serta profil akun pengguna
 * secara permanen di database lokal SQLite (sera_sanctuary_sqlite.db) agar tidak hilang
 * meskipun pengguna keluar akun atau menutup aplikasi.
 */
class SeraLocalStorage(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sera_sanctuary_local_db_v3", Context.MODE_PRIVATE)

    companion object {
        private const val DATABASE_NAME = "sera_sanctuary_sqlite.db"
        private const val DATABASE_VERSION = 3

        const val DEFAULT_WHATSAPP_NUMBER = "6281225905930"
        const val UB_IAM_SSO_URL =
            "https://iam.ub.ac.id/auth/realms/ub/protocol/saml?SAMLRequest=fVLLTsMwEPyVyPfESQghsdpKpRWiUoGqLRy4IMfZUkuJHbw2j7%2FHTUBAJXqzdmdmd2Y9Qt42HZs6u1dreHGANnhvG4Wsb4yJM4ppjhKZ4i0gs4JtpjdLlkYx64y2WuiG%2FKKcZnBEMFZqRYLFfEye0ozzIssyyOMcKlHmu1qIMo2LsigKqOqLMsnTbFdUBQkewKBnjokX8nREBwuFlivrS3Gah0kSpufbOGPnJTuLH0kw926k4rZn7a3tkFEqeRu5KuIikjXl3jc1wJsWqavotyF6cEKC6fe2M63QtWA2YF6lgPv18kevMlrBkeKBnlLshkfIBUbdvjuCkmD1Ne5Sqlqq59PRVQMI2fV2uwpXd5stmYwO%2BqyPwkz%2Bqo%2Fo795oOPOtV13MV7qR4iO40qbl9v%2BhSZT0FVmHux7KnMIOhNxJ8MtPm0a%2FzXx2FsbEGgeEToahf7%2FT5BM%3D&RelayState=https%3A%2F%2Fbrone.ub.ac.id%2Fmy%2F&SigAlg=http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha256&Signature=qq1jDBE1PsMjy7BSnZk8L7T3lDRCtlI6yJi4uWalvWpju7NkGrRNQyPYiP112dw8sOqBbvk7fi02zMd8likerEQWLrTi6PWnYuA1yRDm5kK5T02BJ%2F7jzZcRD8Mxm2vfw3xI1bnCUlfjdMWIAlang0KM7dXH49ld%2BI%2Br3L6aJi1VmQDOiiwD2IHq5RCrI30TEs9H0wKAjI6njqZZxOBuotfDLrnPuSTBB7eE3hnHV%2B6GeyYaHV%2B%2BtIedGgNg0oTEFqWmDWikDb3ZAG1Obn6buwOJq11MxMRP0fhCbHDTS9Xufp84pNXdui6rl1Kr%2BY3WpkrQc5Yf7pCRjZsP9TXAlg%3D%3D"

        // SQLite Tables
        private const val TABLE_CHECKINS = "emotion_checkins"
        private const val TABLE_EVALUATIONS = "selfcare_evaluations"
        private const val TABLE_CONTACTS = "trusted_contacts"

        // Preference Keys
        private const val KEY_INITIALIZED = "sqlite_seeded_v3"
        private const val KEY_AUTH_REQUIRED_V4 = "auth_required_v4"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_NIM = "user_nim"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO_URI = "user_photo_uri"
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
        private const val KEY_STREAK_DAYS = "streak_days"
        private const val KEY_NEXT_WEEK_COMMITMENT = "next_week_commitment"
        private const val KEY_SPONTANEOUS_IDEA = "spontaneous_idea_note"
        private const val KEY_BOOKMARKS_SET = "bookmarked_activities"
        private const val KEY_PRIVACY_ANALYSIS = "privacy_allow_analysis"
        private const val KEY_PRIVACY_MASK_HOME = "privacy_mask_home"
        private const val KEY_PRIVACY_REMINDER = "privacy_daily_reminder"
        private const val KEY_PRIVACY_MORNING_TIME = "privacy_morning_time"
        private const val KEY_PRIVACY_REMINDER_TIME = "privacy_reminder_time"
        private const val KEY_PRIVACY_SUPPORT_PROMPT = "privacy_support_prompt"
        private const val KEY_PRIVACY_SOUNDSCAPE = "privacy_soundscape"
        private const val KEY_PRIVACY_BIOMETRIC = "privacy_biometric"
        private const val KEY_PRIVACY_DARK_MODE = "privacy_dark_mode"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_CHECKINS (
                seq_order INTEGER PRIMARY KEY AUTOINCREMENT,
                id TEXT UNIQUE,
                day_short TEXT,
                day_of_month INTEGER,
                month_name TEXT,
                year INTEGER,
                time_label TEXT,
                emotion TEXT,
                intensity INTEGER,
                contexts_csv TEXT,
                sleep_quality TEXT,
                journal_note TEXT,
                appreciation_label TEXT,
                is_appreciated INTEGER,
                voice_duration_sec INTEGER,
                voice_file_path TEXT,
                supportive_activity_tag TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_EVALUATIONS (
                seq_order INTEGER PRIMARY KEY AUTOINCREMENT,
                id TEXT UNIQUE,
                activity_id TEXT,
                activity_title TEXT,
                timestamp_label TEXT,
                rating TEXT,
                reflection_note TEXT,
                emotion_at_time TEXT,
                intensity_at_time INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_CONTACTS (
                seq_order INTEGER PRIMARY KEY AUTOINCREMENT,
                id TEXT UNIQUE,
                initials TEXT,
                name TEXT,
                badge_text TEXT,
                relation_subtitle TEXT,
                availability_text TEXT,
                phone_number TEXT,
                is_verified_service INTEGER,
                is_priority_sos INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    init {
        // Ensure tables exist and seed initial data if first time
        val db = writableDatabase
        onCreate(db)
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            seedDefaultStudentData()
        } else {
            // Ensure WhatsApp contact numbers use +6281225905930 as requested
            enforceDefaultWhatsAppNumber()
        }
        // Ensure default state requires user login first instead of auto-logging in as "Nur"
        if (!prefs.getBoolean(KEY_AUTH_REQUIRED_V4, false)) {
            val currentSavedName = prefs.getString(KEY_USER_NAME, "") ?: ""
            val resetName = if (currentSavedName.equals("Nur", ignoreCase = true)) "" else currentSavedName
            prefs.edit()
                .putBoolean(KEY_AUTH_REQUIRED_V4, true)
                .putBoolean(KEY_USER_LOGGED_IN, false)
                .putString(KEY_USER_NAME, resetName)
                .apply()
        }
    }

    private fun enforceDefaultWhatsAppNumber() {
        try {
            val cv = ContentValues().apply {
                put("phone_number", DEFAULT_WHATSAPP_NUMBER)
            }
            writableDatabase.update(
                TABLE_CONTACTS,
                cv,
                "phone_number IN ('6281234567890', '6281298765432', '628113600123')",
                null
            )
        } catch (_: Exception) {
        }
    }

    fun seedDefaultStudentData() {
        val defaultCheckIns = defaultCheckInHistory()
        val defaultEvaluations = defaultEvaluationHistory()
        val defaultContacts = defaultTrustedContacts()

        saveCheckIns(defaultCheckIns)
        saveEvaluations(defaultEvaluations)
        saveTrustedContacts(defaultContacts)

        prefs.edit()
            .putBoolean(KEY_INITIALIZED, true)
            .putBoolean(KEY_AUTH_REQUIRED_V4, true)
            .putString(KEY_USER_NAME, "")
            .putString(KEY_USER_NIM, "Mahasiswa • Teknik Informatika FILKOM UB")
            .putString(KEY_USER_EMAIL, "")
            .putBoolean(KEY_USER_LOGGED_IN, false)
            .putInt(KEY_STREAK_DAYS, 14)
            .putString(
                KEY_NEXT_WEEK_COMMITMENT,
                "Tarik napas 4–7–8 selama 5 menit sebelum jam sibuk kuliah Rabu & Kamis"
            )
            .putBoolean(KEY_PRIVACY_ANALYSIS, true)
            .putBoolean(KEY_PRIVACY_MASK_HOME, false)
            .putBoolean(KEY_PRIVACY_REMINDER, true)
            .putString(KEY_PRIVACY_MORNING_TIME, "08:00")
            .putString(KEY_PRIVACY_REMINDER_TIME, "21:00")
            .putBoolean(KEY_PRIVACY_SUPPORT_PROMPT, true)
            .putString(KEY_PRIVACY_SOUNDSCAPE, "432 Hz Solfeggio Serenity")
            .putBoolean(KEY_PRIVACY_BIOMETRIC, false)
            .putStringSet(KEY_BOOKMARKS_SET, setOf("act_breathing_box", "act_micro_task"))
            .apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun setUserProfile(name: String, nimOrRole: String, loggedIn: Boolean, photoUri: String? = null) {
        val currentName = prefs.getString(KEY_USER_NAME, "") ?: ""
        val currentNim =
            prefs.getString(KEY_USER_NIM, "Mahasiswa • TIF FILKOM UB")
                ?: "Mahasiswa • TIF FILKOM UB"
        val currentPhoto = prefs.getString(KEY_USER_PHOTO_URI, null)

        prefs.edit()
            .putString(KEY_USER_NAME, name.ifBlank { currentName })
            .putString(KEY_USER_NIM, nimOrRole.ifBlank { currentNim })
            .putString(KEY_USER_PHOTO_URI, photoUri ?: currentPhoto)
            .putBoolean(KEY_USER_LOGGED_IN, loggedIn)
            .apply()
    }

    fun getUserRoleSubtitle(): String =
        prefs.getString(KEY_USER_NIM, "Mahasiswa • TIF FILKOM UB")
            ?: "Mahasiswa • TIF FILKOM UB"

    fun getUserPhotoUri(): String? =
        prefs.getString(KEY_USER_PHOTO_URI, null)?.takeIf { it.isNotBlank() }

    fun saveUserPhotoUri(uri: String?) {
        prefs.edit().putString(KEY_USER_PHOTO_URI, uri ?: "").apply()
    }

    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_USER_LOGGED_IN, false)

    fun getStreakDays(): Int = prefs.getInt(KEY_STREAK_DAYS, 14)

    fun incrementStreakIfNeeded() {
        val current = getStreakDays()
        prefs.edit().putInt(KEY_STREAK_DAYS, current).apply()
    }

    fun getNextWeekCommitment(): String =
        prefs.getString(
            KEY_NEXT_WEEK_COMMITMENT,
            "Tarik napas 4–7–8 selama 5 menit sebelum jam sibuk kuliah Rabu & Kamis"
        ) ?: "Tarik napas 4–7–8 selama 5 menit sebelum jam sibuk kuliah Rabu & Kamis"

    fun saveNextWeekCommitment(commitment: String) {
        prefs.edit().putString(KEY_NEXT_WEEK_COMMITMENT, commitment).apply()
    }

    fun getSpontaneousIdeaNote(): String =
        prefs.getString(KEY_SPONTANEOUS_IDEA, "") ?: ""

    fun saveSpontaneousIdeaNote(note: String) {
        prefs.edit().putString(KEY_SPONTANEOUS_IDEA, note).apply()
    }

    // =========================================================================================
    // SQLITE CRUD: EMOTION CHECK-INS & JOURNALS
    // =========================================================================================

    fun loadCheckIns(): List<EmotionCheckIn> {
        val list = mutableListOf<EmotionCheckIn>()
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_CHECKINS,
                null,
                null,
                null,
                null,
                null,
                "seq_order ASC"
            )
            cursor.use { c ->
                while (c.moveToNext()) {
                    val contextsCsv = c.getString(c.getColumnIndexOrThrow("contexts_csv")) ?: ""
                    val contexts = contextsCsv
                        .split(",")
                        .filter { it.isNotBlank() }
                        .map { ActivityContext.fromId(it) }
                        .ifEmpty { listOf(ActivityContext.PERKULIAHAN) }
                    val vPath = c.getString(c.getColumnIndexOrThrow("voice_file_path")) ?: ""

                    list.add(
                        EmotionCheckIn(
                            id = c.getString(c.getColumnIndexOrThrow("id")),
                            dayShort = c.getString(c.getColumnIndexOrThrow("day_short")),
                            dayOfMonth = c.getInt(c.getColumnIndexOrThrow("day_of_month")),
                            monthName = c.getString(c.getColumnIndexOrThrow("month_name")),
                            year = c.getInt(c.getColumnIndexOrThrow("year")),
                            timeLabel = c.getString(c.getColumnIndexOrThrow("time_label")),
                            emotion = EmotionType.fromId(c.getString(c.getColumnIndexOrThrow("emotion"))),
                            intensity = c.getInt(c.getColumnIndexOrThrow("intensity")),
                            contexts = contexts,
                            sleepQuality = SleepQuality.fromId(
                                c.getString(c.getColumnIndexOrThrow("sleep_quality"))
                            ),
                            journalNote = c.getString(c.getColumnIndexOrThrow("journal_note")),
                            appreciationLabel = c.getString(c.getColumnIndexOrThrow("appreciation_label")),
                            isAppreciated = c.getInt(c.getColumnIndexOrThrow("is_appreciated")) == 1,
                            voiceDurationSeconds = c.getInt(c.getColumnIndexOrThrow("voice_duration_sec")),
                            voiceFilePath = vPath.ifBlank { null },
                            supportiveActivityTag = c.getString(c.getColumnIndexOrThrow("supportive_activity_tag"))
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveCheckIns(list: List<EmotionCheckIn>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_CHECKINS, null, null)
            list.forEach { item ->
                val cv = ContentValues().apply {
                    put("id", item.id)
                    put("day_short", item.dayShort)
                    put("day_of_month", item.dayOfMonth)
                    put("month_name", item.monthName)
                    put("year", item.year)
                    put("time_label", item.timeLabel)
                    put("emotion", item.emotion.id)
                    put("intensity", item.intensity)
                    put("contexts_csv", item.contexts.joinToString(",") { it.id })
                    put("sleep_quality", item.sleepQuality.id)
                    put("journal_note", item.journalNote)
                    put("appreciation_label", item.appreciationLabel)
                    put("is_appreciated", if (item.isAppreciated) 1 else 0)
                    put("voice_duration_sec", item.voiceDurationSeconds)
                    put("voice_file_path", item.voiceFilePath ?: "")
                    put("supportive_activity_tag", item.supportiveActivityTag)
                }
                db.insertWithOnConflict(TABLE_CHECKINS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // =========================================================================================
    // SQLITE CRUD: SELF-CARE EVALUATIONS (WHAT-HELPED-ME TRACKER)
    // =========================================================================================

    fun loadEvaluations(): List<ActivityEvaluation> {
        val list = mutableListOf<ActivityEvaluation>()
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_EVALUATIONS,
                null,
                null,
                null,
                null,
                null,
                "seq_order ASC"
            )
            cursor.use { c ->
                while (c.moveToNext()) {
                    list.add(
                        ActivityEvaluation(
                            id = c.getString(c.getColumnIndexOrThrow("id")),
                            activityId = c.getString(c.getColumnIndexOrThrow("activity_id")),
                            activityTitle = c.getString(c.getColumnIndexOrThrow("activity_title")),
                            timestampLabel = c.getString(c.getColumnIndexOrThrow("timestamp_label")),
                            rating = EvaluationRating.fromId(c.getString(c.getColumnIndexOrThrow("rating"))),
                            reflectionNote = c.getString(c.getColumnIndexOrThrow("reflection_note")),
                            emotionAtTime = EmotionType.fromId(
                                c.getString(c.getColumnIndexOrThrow("emotion_at_time"))
                            ),
                            intensityAtTime = c.getInt(c.getColumnIndexOrThrow("intensity_at_time"))
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveEvaluations(list: List<ActivityEvaluation>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_EVALUATIONS, null, null)
            list.forEach { item ->
                val cv = ContentValues().apply {
                    put("id", item.id)
                    put("activity_id", item.activityId)
                    put("activity_title", item.activityTitle)
                    put("timestamp_label", item.timestampLabel)
                    put("rating", item.rating.id)
                    put("reflection_note", item.reflectionNote)
                    put("emotion_at_time", item.emotionAtTime.id)
                    put("intensity_at_time", item.intensityAtTime)
                }
                db.insertWithOnConflict(TABLE_EVALUATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // =========================================================================================
    // SQLITE CRUD: TRUSTED CONTACTS (SUPPORT CIRCLE)
    // =========================================================================================

    fun loadTrustedContacts(): List<TrustedContact> {
        val list = mutableListOf<TrustedContact>()
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_CONTACTS,
                null,
                null,
                null,
                null,
                null,
                "seq_order ASC"
            )
            cursor.use { c ->
                while (c.moveToNext()) {
                    list.add(
                        TrustedContact(
                            id = c.getString(c.getColumnIndexOrThrow("id")),
                            initials = c.getString(c.getColumnIndexOrThrow("initials")),
                            name = c.getString(c.getColumnIndexOrThrow("name")),
                            badgeText = c.getString(c.getColumnIndexOrThrow("badge_text")),
                            relationSubtitle = c.getString(c.getColumnIndexOrThrow("relation_subtitle")),
                            availabilityText = c.getString(c.getColumnIndexOrThrow("availability_text")),
                            phoneNumber = c.getString(c.getColumnIndexOrThrow("phone_number"))
                                .ifBlank { DEFAULT_WHATSAPP_NUMBER },
                            isVerifiedService = c.getInt(c.getColumnIndexOrThrow("is_verified_service")) == 1,
                            isPrioritySos = c.getInt(c.getColumnIndexOrThrow("is_priority_sos")) == 1
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return if (list.isEmpty()) defaultTrustedContacts() else list
    }

    fun saveTrustedContacts(list: List<TrustedContact>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_CONTACTS, null, null)
            list.forEach { item ->
                val cv = ContentValues().apply {
                    put("id", item.id)
                    put("initials", item.initials)
                    put("name", item.name)
                    put("badge_text", item.badgeText)
                    put("relation_subtitle", item.relationSubtitle)
                    put("availability_text", item.availabilityText)
                    put("phone_number", item.phoneNumber.ifBlank { DEFAULT_WHATSAPP_NUMBER })
                    put("is_verified_service", if (item.isVerifiedService) 1 else 0)
                    put("is_priority_sos", if (item.isPrioritySos) 1 else 0)
                }
                db.insertWithOnConflict(TABLE_CONTACTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun loadBookmarkedIds(): Set<String> =
        prefs.getStringSet(KEY_BOOKMARKS_SET, setOf("act_breathing_box", "act_micro_task"))
            ?: emptySet()

    fun saveBookmarkedIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_BOOKMARKS_SET, ids).apply()
    }

    fun loadPrivacyState(): PrivacyDataControlState = PrivacyDataControlState(
        allowPatternAnalysis = prefs.getBoolean(KEY_PRIVACY_ANALYSIS, true),
        maskJournalPreviewOnHome = prefs.getBoolean(KEY_PRIVACY_MASK_HOME, false),
        dailyCheckInReminder = prefs.getBoolean(KEY_PRIVACY_REMINDER, true),
        morningReminderTime = prefs.getString(KEY_PRIVACY_MORNING_TIME, "08:00") ?: "08:00",
        reminderTimeLabel = prefs.getString(KEY_PRIVACY_REMINDER_TIME, "21:00") ?: "21:00",
        gentleSupportSuggestion = prefs.getBoolean(KEY_PRIVACY_SUPPORT_PROMPT, true),
        selectedSoundscape = prefs.getString(KEY_PRIVACY_SOUNDSCAPE, "432 Hz Solfeggio Serenity")
            ?: "432 Hz Solfeggio Serenity",
        biometricLockEnabled = prefs.getBoolean(KEY_PRIVACY_BIOMETRIC, false),
        isDarkModeEnabled = prefs.getBoolean(KEY_PRIVACY_DARK_MODE, false)
    )

    fun savePrivacyState(state: PrivacyDataControlState) {
        prefs.edit()
            .putBoolean(KEY_PRIVACY_ANALYSIS, state.allowPatternAnalysis)
            .putBoolean(KEY_PRIVACY_MASK_HOME, state.maskJournalPreviewOnHome)
            .putBoolean(KEY_PRIVACY_REMINDER, state.dailyCheckInReminder)
            .putString(KEY_PRIVACY_MORNING_TIME, state.morningReminderTime)
            .putString(KEY_PRIVACY_REMINDER_TIME, state.reminderTimeLabel)
            .putBoolean(KEY_PRIVACY_SUPPORT_PROMPT, state.gentleSupportSuggestion)
            .putString(KEY_PRIVACY_SOUNDSCAPE, state.selectedSoundscape)
            .putBoolean(KEY_PRIVACY_BIOMETRIC, state.biometricLockEnabled)
            .putBoolean(KEY_PRIVACY_DARK_MODE, state.isDarkModeEnabled)
            .apply()
    }

    private fun defaultCheckInHistory(): List<EmotionCheckIn> = listOf(
        EmotionCheckIn(
            id = "chk_24_morning",
            dayShort = "Kam",
            dayOfMonth = 24,
            monthName = "Okt",
            timeLabel = "Kamis, 24 Okt • 08:30 WIB",
            emotion = EmotionType.CEMAS,
            intensity = 3,
            contexts = listOf(ActivityContext.PERKULIAHAN),
            sleepQuality = SleepQuality.KURANG,
            journalNote = "Pagi ini bangun tidur rasanya dada agak sesak mikirin revisi Bab 2 yang belum kelar. Sempat mikir kalau aku nggak mampu nyelesaiin tepat waktu. Tapi setelah coba napas 4–4–4 selama 3 menit, rasanya otak lebih bisa diajak mikir tenang. Langkah kecil hari ini: cuma buka laptop dan nulis judul subbab dulu.",
            appreciationLabel = "Peluk Dirimu",
            isAppreciated = true,
            voiceDurationSeconds = 18,
            supportiveActivityTag = "Relaksasi Bahu (3m)"
        ),
        EmotionCheckIn(
            id = "chk_23_night",
            dayShort = "Rab",
            dayOfMonth = 23,
            monthName = "Okt",
            timeLabel = "Rabu, 23 Okt • 20:15 WIB",
            emotion = EmotionType.TENANG,
            intensity = 2,
            contexts = listOf(ActivityContext.PERSONAL),
            sleepQuality = SleepQuality.PULAS,
            journalNote = "Mendengarkan instrumental hujan sambil menyeduh teh kamomil hangat. Rasanya beban hari ini perlahan menguap bersama uap cangkir.",
            appreciationLabel = "Tercatat Lembut",
            isAppreciated = true,
            voiceDurationSeconds = 0,
            supportiveActivityTag = "Suara Alam (15m)"
        ),
        EmotionCheckIn(
            id = "chk_22_afternoon",
            dayShort = "Sel",
            dayOfMonth = 22,
            monthName = "Okt",
            timeLabel = "Selasa, 22 Okt • 16:20 WIB",
            emotion = EmotionType.BIASA,
            intensity = 3,
            contexts = listOf(ActivityContext.PERKULIAHAN, ActivityContext.MAGANG),
            sleepQuality = SleepQuality.CUKUP,
            journalNote = "Menyelesaikan revisi modul praktikum pengembangan aplikasi bergerak bersama kelompok 4.",
            appreciationLabel = "Tercatat Lembut",
            isAppreciated = false,
            voiceDurationSeconds = 12,
            supportiveActivityTag = "Jalan Santai Kampus (10m)"
        ),
        EmotionCheckIn(
            id = "chk_21_evening",
            dayShort = "Sen",
            dayOfMonth = 21,
            monthName = "Okt",
            timeLabel = "Senin, 21 Okt • 19:40 WIB",
            emotion = EmotionType.SENANG,
            intensity = 2,
            contexts = listOf(ActivityContext.PERKULIAHAN, ActivityContext.PERTEMANAN),
            sleepQuality = SleepQuality.PULAS,
            journalNote = "Presentasi progres UI/UX berjalan lancar dan mendapat apresiasi positif dari asisten praktikum.",
            appreciationLabel = "Tercatat Lembut",
            isAppreciated = true,
            voiceDurationSeconds = 0,
            supportiveActivityTag = "Napas Ritmis (5m)"
        ),
        EmotionCheckIn(
            id = "chk_20_evening",
            dayShort = "Min",
            dayOfMonth = 20,
            monthName = "Okt",
            timeLabel = "Minggu, 20 Okt • 19:40 WIB",
            emotion = EmotionType.SEDIH,
            intensity = 3,
            contexts = listOf(ActivityContext.PERTEMANAN),
            sleepQuality = SleepQuality.KURANG,
            journalNote = "Salah paham kecil dengan teman kelompok praktikum. Butuh waktu mencerna dan menenangkan pikiran sejenak.",
            appreciationLabel = "Peluk Dirimu",
            isAppreciated = true,
            voiceDurationSeconds = 24,
            supportiveActivityTag = "Jurnal Suara (2m)"
        ),
        EmotionCheckIn(
            id = "chk_19_morning",
            dayShort = "Sab",
            dayOfMonth = 19,
            monthName = "Okt",
            timeLabel = "Sabtu, 19 Okt • 09:15 WIB",
            emotion = EmotionType.TENANG,
            intensity = 2,
            contexts = listOf(ActivityContext.PERSONAL, ActivityContext.KELUARGA),
            sleepQuality = SleepQuality.PULAS,
            journalNote = "Jalan pagi di sekitar taman kampus UB sambil menikmati udara sejuk tanpa membuka notifikasi grup tugas.",
            appreciationLabel = "Tercatat Lembut",
            isAppreciated = true,
            voiceDurationSeconds = 0,
            supportiveActivityTag = "Jalan Taman GPS (15m)"
        ),
        EmotionCheckIn(
            id = "chk_18_morning",
            dayShort = "Jum",
            dayOfMonth = 18,
            monthName = "Okt",
            timeLabel = "Jumat, 18 Okt • 08:00 WIB",
            emotion = EmotionType.TENANG,
            intensity = 2,
            contexts = listOf(ActivityContext.PERSONAL),
            sleepQuality = SleepQuality.PULAS,
            journalNote = "Memulai akhir pekan dengan sarapan tenang dan menyusun prioritas langkah mikro.",
            appreciationLabel = "Tercatat Lembut",
            isAppreciated = true,
            voiceDurationSeconds = 0,
            supportiveActivityTag = "Napas Kotak (5m)"
        )
    )

    private fun defaultEvaluationHistory(): List<ActivityEvaluation> = listOf(
        ActivityEvaluation(
            id = "eval_1",
            activityId = "act_breathing_box",
            activityTitle = "Latihan Pernapasan Kotak 4–4–4",
            timestampLabel = "Hari ini, 11:20 WIB",
            rating = EvaluationRating.MEMBANTU,
            reflectionNote = "Detak jantung lebih stabil setelah 4 siklus napas sebelum kuis.",
            emotionAtTime = EmotionType.CEMAS,
            intensityAtTime = 3
        ),
        ActivityEvaluation(
            id = "eval_2",
            activityId = "act_micro_task",
            activityTitle = "Memecah Tugas Jadi Mikro",
            timestampLabel = "21 Okt, 20:10 WIB",
            rating = EvaluationRating.MEMBANTU,
            reflectionNote = "Mengerjakan 1 sub-bab dulu membuat rasa kewalahan berkurang drastis.",
            emotionAtTime = EmotionType.CEMAS,
            intensityAtTime = 4
        ),
        ActivityEvaluation(
            id = "eval_3",
            activityId = "act_sensory_reset",
            activityTitle = "Screen-Free Sensory Reset",
            timestampLabel = "19 Okt, 15:45 WIB",
            rating = EvaluationRating.MEMBANTU,
            reflectionNote = "Mata tidak lagi tegang setelah menatap kode Android Studio seharian.",
            emotionAtTime = EmotionType.TENANG,
            intensityAtTime = 2
        )
    )

    private fun defaultTrustedContacts(): List<TrustedContact> = listOf(
        TrustedContact(
            id = "contact_sarah",
            initials = "KS",
            name = "Kak Sarah",
            badgeText = "Sahabat",
            relationSubtitle = "Sahabat Prioritas • +62 812-2590-5930",
            availabilityText = "Siap Ngobrol • WhatsApp",
            phoneNumber = DEFAULT_WHATSAPP_NUMBER,
            isVerifiedService = false,
            isPrioritySos = true
        ),
        TrustedContact(
            id = "contact_alya",
            initials = "DA",
            name = "Sahabat (Alya)",
            badgeText = "Teman Cerita",
            relationSubtitle = "Teman Satu Prodi TIF • +62 812-2590-5930",
            availabilityText = "WhatsApp Teman",
            phoneNumber = DEFAULT_WHATSAPP_NUMBER,
            isVerifiedService = false,
            isPrioritySos = true
        ),
        TrustedContact(
            id = "contact_konselo",
            initials = "BK",
            name = "Layanan Konselor Kampus",
            badgeText = "Gratis",
            relationSubtitle = "Saluran Resmi 24 Jam • +62 812-2590-5930",
            availabilityText = "Online Cepat",
            phoneNumber = DEFAULT_WHATSAPP_NUMBER,
            isVerifiedService = true,
            isPrioritySos = true
        )
    )
}
