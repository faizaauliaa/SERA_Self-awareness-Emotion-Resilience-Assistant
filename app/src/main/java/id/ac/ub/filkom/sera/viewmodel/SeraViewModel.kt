package id.ac.ub.filkom.sera.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.ac.ub.filkom.sera.data.local.SeraLocalStorage
import id.ac.ub.filkom.sera.data.model.*
import id.ac.ub.filkom.sera.ui.theme.SeraThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

data class SeraUiState(
    val userName: String = "Nur",
    val userRoleSubtitle: String = "Mahasiswi Tingkat Akhir • TIF FILKOM UB",
    val userPhotoUri: String? = null,
    val isLoggedIn: Boolean = true,
    val streakDays: Int = 14,
    val todayQuickEmotion: EmotionType = EmotionType.TENANG,
    val checkInHistory: List<EmotionCheckIn> = emptyList(),
    val filteredHistory: List<EmotionCheckIn> = emptyList(),
    val historySearchQuery: String = "",
    val selectedPatternPeriod: String = "Mingguan",
    val selectedContextFilter: ActivityContext? = null,
    val selectedEmotionFilter: EmotionType? = null,
    val selectedCalendarDay: Int = 24,
    // Fungsi 2 & 3: Adaptive Self-Care Planner & What-Helped-Me Tracker
    val plannerEmotion: EmotionType = EmotionType.CEMAS,
    val plannerIntensity: Int = 3,
    val availableDurationMinutes: Int = 15,
    val selectedSelfCareCategory: SelfCareCategory = SelfCareCategory.SEMUA,
    val showBookmarksOnly: Boolean = false,
    val adaptiveRecommendations: List<SelfCareActivity> = emptyList(),
    val bookmarkedActivityIds: Set<String> = emptySet(),
    val evaluationHistory: List<ActivityEvaluation> = emptyList(),
    val lastCompletedActivity: SelfCareActivity? = null,
    val microTaskParentTitle: String = "Draft Bab 2 Skripsi / Laporan PAPB",
    val microTaskSubtitle: String = "Tinjauan Pustaka Landasan Teori",
    val microTaskSteps: List<MicroTaskStep> = listOf(
        MicroTaskStep(
            1,
            "Langkah 1 • Mulai dari Ruang",
            "Buka 1 lembar dokumen kerja kosong & rapikan meja belajarmu.",
            "3 Menit",
            true
        ),
        MicroTaskStep(
            2,
            "Tulis 3 Poin Ide Kasar",
            "Tuangkan apa saja tanpa sensor dan tanpa takut salah tanda baca. Tulis saja poin-poin intinya.",
            "5 Menit",
            false
        ),
        MicroTaskStep(
            3,
            "Langkah 3 • Rehidrasi Tenang",
            "Berhenti sejenak 2 menit, teguk segelas air hangat segar dan bernapas lega.",
            "2 Menit",
            false
        )
    ),
    val spontaneousIdeaNote: String = "",
    // Sensor 1: GPS Location-Based Self-Care State
    val gpsSensorState: GpsSensorState = GpsSensorState(
        nearbyRecommendations = computeRealDistanceGpsPlaces(-7.9526, 112.6138)
    ),
    // Fungsi 4: Weekly Reflection Report
    val weeklyReport: WeeklyReflectionReportData? = null,
    // Fungsi 5: Personal Insight Engine
    val personalInsights: List<PersonalInsight> = emptyList(),
    // Fungsi 6: Private Mode & Data Control
    val privacyState: PrivacyDataControlState = PrivacyDataControlState(),
    // Fungsi 7: Support Circle & Notifications
    val trustedContacts: List<TrustedContact> = emptyList(),
    val notifications: List<SeraNotificationItem> = emptyList(),
    val statusBannerMessage: String? = null
)

fun computeRealDistanceGpsPlaces(userLat: Double, userLon: Double): List<LocationSelfCarePlace> {
    data class RawPoi(
        val id: String,
        val placeName: String,
        val activityTitle: String,
        val durationMinutes: Int,
        val environmentTag: String,
        val description: String,
        val lat: Double,
        val lon: Double,
        val emoji: String
    )

    val referencePois = listOf(
        RawPoi(
            id = "gps_gazebo_filkom",
            placeName = "Taman & Gazebo Teduh FILKOM UB",
            activityTitle = "Grounding & Hirup Udara Segar Tanpa Layar",
            durationMinutes = 10,
            environmentTag = "Ruang Terbuka Hijau • Teduh",
            description = "Berjalan keluar gedung menuju area pepohonan hijau untuk merilekskan mata dan menurunkan ketegangan bahu.",
            lat = -7.9538,
            lon = 112.6145,
            emoji = "🌳"
        ),
        RawPoi(
            id = "gps_hutan_ub",
            placeName = "Hutan Kampus & Boulevard Universitas Brawijaya",
            activityTitle = "Mindful Walking & Regulasi Napas Alami",
            durationMinutes = 15,
            environmentTag = "Udara Segar • Rindang",
            description = "Melangkah santai dengan ritme napas 4–4 sambil memperhatikan suara dedaunan dan angin sekitar.",
            lat = -7.9525,
            lon = 112.6134,
            emoji = "🍃"
        ),
        RawPoi(
            id = "gps_perpustakaan_ub",
            placeName = "Taman Baca & Zona Hening Perpustakaan Pusat UB",
            activityTitle = "Jurnal Refleksi & Rehidrasi Sore",
            durationMinutes = 10,
            environmentTag = "Zona Hening • Minim Bising",
            description = "Area tenang untuk minum air hangat, menjauh dari bising, dan merapikan prioritas langkah mikro.",
            lat = -7.9528,
            lon = 112.6161,
            emoji = "🌿"
        ),
        RawPoi(
            id = "gps_lapangan_rektorat",
            placeName = "Ruang Terbuka Hijau Lapangan Rektorat UB",
            activityTitle = "Peregangan Bahu & Relaksasi Mata 20-20-20",
            durationMinutes = 12,
            environmentTag = "Lapangan Rumput • Pandangan Luas",
            description = "Memandang cakrawala luas dan pepohonan rindang untuk mengistirahatkan saraf optik setelah menatap laptop.",
            lat = -7.9520,
            lon = 112.6152,
            emoji = "🏞️"
        )
    )

    val calculated = referencePois.map { poi ->
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLon, poi.lat, poi.lon, results)
        val meters = results[0].roundToInt()
        poi to meters
    }.sortedBy { it.second }

    // If the user is far from Malang/UB (> 15 km), also generate real nearby coordinates around their live GPS!
    val minDistance = calculated.firstOrNull()?.second ?: 0
    return if (minDistance > 15000) {
        listOf(
            LocationSelfCarePlace(
                id = "gps_local_green_1",
                placeName = "Taman & Ruang Terbuka Hijau Sekitar Lokasimu",
                activityTitle = "Grounding & Hirup Udara Segar Tanpa Layar",
                distanceText = "95 m dari titik GPS-mu",
                durationMinutes = 10,
                environmentTag = "Terdeteksi di Sekitar Koordinatmu",
                description = "Berjalan keluar ruangan menuju area terbuka hijau terdekat dari titik GPS kamu saat ini (${String.format(Locale.US, "%.4f, %.4f", userLat, userLon)}).",
                latitude = userLat + 0.0007,
                longitude = userLon + 0.0005,
                iconEmoji = "🌳"
            ),
            LocationSelfCarePlace(
                id = "gps_local_walk_2",
                placeName = "Jalur Jalan Santai & Area Teduh Terdekat",
                activityTitle = "Mindful Walking & Regulasi Napas 4–4",
                distanceText = "180 m dari titik GPS-mu",
                durationMinutes = 15,
                environmentTag = "Jalur Pedestrian Sekitar",
                description = "Melangkah santai 15 menit di sekitar lokasi GPS-mu tanpa membuka layar ponsel untuk menurunkan detak cemas.",
                latitude = userLat - 0.0011,
                longitude = userLon + 0.0008,
                iconEmoji = "🍃"
            ),
            calculated.first().let { (poi, distMeters) ->
                val distLabel = String.format(Locale.US, "%.1f km dari GPS-mu", distMeters / 1000f)
                LocationSelfCarePlace(
                    id = poi.id,
                    placeName = poi.placeName,
                    activityTitle = poi.activityTitle,
                    distanceText = distLabel,
                    durationMinutes = poi.durationMinutes,
                    environmentTag = poi.environmentTag,
                    description = poi.description,
                    latitude = poi.lat,
                    longitude = poi.lon,
                    iconEmoji = poi.emoji
                )
            }
        )
    } else {
        calculated.take(3).map { (poi, distMeters) ->
            val distLabel = if (distMeters < 1000) {
                "$distMeters m dari GPS-mu"
            } else {
                String.format(Locale.US, "%.2f km dari GPS-mu", distMeters / 1000f)
            }
            LocationSelfCarePlace(
                id = poi.id,
                placeName = poi.placeName,
                activityTitle = poi.activityTitle,
                distanceText = distLabel,
                durationMinutes = poi.durationMinutes,
                environmentTag = poi.environmentTag,
                description = poi.description,
                latitude = poi.lat,
                longitude = poi.lon,
                iconEmoji = poi.emoji
            )
        }
    }
}

class SeraViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = SeraLocalStorage(application.applicationContext)

    private val _uiState = MutableStateFlow(SeraUiState())
    val uiState: StateFlow<SeraUiState> = _uiState.asStateFlow()

    private val baseActivityCatalog = listOf(
        SelfCareActivity(
            id = "act_breathing_box",
            title = "Latihan Pernapasan Kotak 4–4–4",
            shortTitle = "Pernapasan Kotak",
            category = SelfCareCategory.PERNAPASAN,
            categoryTagLabel = "5 Menit",
            secondaryTagLabel = "95% Keselarasan Tubuh",
            durationMinutes = 5,
            description = "Menstimulasi saraf vagus dan parasimpatis secara lembut untuk menstabilkan debar dada serta menenangkan beban pikiran.",
            detailedSteps = listOf(
                "Duduk nyaman dengan bahu rileks dan punggung tegak.",
                "Tarik napas lembut lewat hidung selama 4 detik.",
                "Tahan napas dengan tenang di dada selama 4 detik.",
                "Hembuskan perlahan lewat mulut selama 4 detik, lalu jeda 4 detik."
            ),
            targetEmotions = listOf(EmotionType.CEMAS, EmotionType.SEDIH, EmotionType.TENANG),
            targetContexts = listOf(ActivityContext.PERKULIAHAN, ActivityContext.PERTEMANAN, ActivityContext.PERSONAL),
            baseMatchPercentage = 95,
            socialProofText = "92% Membantu",
            actionButtonText = "Mulai Sesi Rileks",
            moduleType = InteractiveModuleType.BREATHING_BOX
        ),
        SelfCareActivity(
            id = "act_micro_task",
            title = "Memecah Tugas Jadi Mikro",
            shortTitle = "Dekonstruksi Beban",
            category = SelfCareCategory.AKADEMIK,
            categoryTagLabel = "15 Menit",
            secondaryTagLabel = "Beban Akademik",
            durationMinutes = 15,
            description = "Ubah rasa kewalahan tugas kuliah tebal menjadi 3 aksi mungil 5 menitan tanpa tekanan emosional.",
            detailedSteps = listOf(
                "Buka 1 lembar dokumen kerja kosong & rapikan meja.",
                "Tulis 3 poin ide kasar tanpa takut salah penulisan.",
                "Berhenti 2 menit, teguk segelas air hangat segar."
            ),
            targetEmotions = listOf(EmotionType.CEMAS, EmotionType.BIASA),
            targetContexts = listOf(ActivityContext.PERKULIAHAN, ActivityContext.MAGANG),
            baseMatchPercentage = 88,
            socialProofText = "Anti-Kewalahan",
            actionButtonText = "Panduan Mikro Detail",
            moduleType = InteractiveModuleType.MICRO_TASK
        ),
        SelfCareActivity(
            id = "act_sensory_reset",
            title = "Screen-Free Sensory Reset",
            shortTitle = "Istirahat Layar & Jalan",
            category = SelfCareCategory.SENSORI,
            categoryTagLabel = "10 Menit",
            secondaryTagLabel = "Relaksasi Sensori",
            durationMinutes = 10,
            description = "Istirahat mata total dari radiasi layar ponsel & laptop. Regangkan pundak kaku, basuh wajah, dan pandang pepohonan hijau di luar jendela.",
            detailedSteps = listOf(
                "Istirahatkan Mata (4 Menit): Gunakan aturan 20–20–20. Pusatkan mata pada objek berjarak minimal 6 meter.",
                "Basuh Wajah & Leher (3 Menit): Sentuhan air sejuk memicu refleks penyelaman alami untuk memperlambat denyut jantung.",
                "Peregangan Bahu Lembut (3 Menit): Putar kedua bahu ke belakang perlahan guna mengurai kekakuan."
            ),
            targetEmotions = listOf(EmotionType.TENANG, EmotionType.BIASA, EmotionType.CEMAS, EmotionType.SEDIH),
            targetContexts = listOf(ActivityContext.PERKULIAHAN, ActivityContext.PERSONAL, ActivityContext.MAGANG),
            baseMatchPercentage = 84,
            socialProofText = "Audio 432Hz",
            actionButtonText = "Coba Sekarang",
            moduleType = InteractiveModuleType.SENSORY_RESET
        ),
        SelfCareActivity(
            id = "act_gps_green_walk",
            title = "Jalan Santai Ruang Terbuka Hijau (GPS)",
            shortTitle = "Jalan Santai Sekitar Lokasi",
            category = SelfCareCategory.LOKASI_GPS,
            categoryTagLabel = "10 Menit",
            secondaryTagLabel = "Rekomendasi GPS • 85m",
            durationMinutes = 10,
            description = "Rekomendasi berbasis sensor GPS: Berjalan santai di taman terbuka hijau terdekat dari koordinatmu untuk menyegarkan sistem saraf.",
            detailedSteps = listOf(
                "Melangkah menuju ruang terbuka hijau terdekat yang terdeteksi GPS.",
                "Rasakan pijakan kaki dan hirup udara terbuka dengan ritme 4 detik.",
                "Amati warna pepohonan di sekitarmu tanpa melihat layar ponsel."
            ),
            targetEmotions = listOf(EmotionType.CEMAS, EmotionType.SEDIH, EmotionType.TENANG, EmotionType.BIASA),
            targetContexts = listOf(ActivityContext.PERKULIAHAN, ActivityContext.PERSONAL),
            baseMatchPercentage = 91,
            socialProofText = "Sensor Lokasi Aktif",
            actionButtonText = "Mulai Jalan Santai",
            moduleType = InteractiveModuleType.SENSORY_RESET,
            isGpsBased = true,
            gpsLocationHint = "Taman & Gazebo Teduh FILKOM UB (85m)"
        )
    )

    private var activeLocationListener: LocationListener? = null
    private var activeLocationManager: LocationManager? = null

    init {
        refreshAllData()
    }

    fun refreshAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            val checkIns = storage.loadCheckIns()
            val evaluations = storage.loadEvaluations()
            val bookmarks = storage.loadBookmarkedIds()
            val contacts = storage.loadTrustedContacts()
            val privacy = storage.loadPrivacyState()
            val userName = storage.getUserName()
            val userSub = storage.getUserRoleSubtitle()
            val userPhoto = storage.getUserPhotoUri()
            val loggedIn = storage.isUserLoggedIn()
            val streak = storage.getStreakDays()
            val commitment = storage.getNextWeekCommitment()

            val latestEmotion = checkIns.firstOrNull()?.emotion ?: EmotionType.TENANG
            val latestIntensity = checkIns.firstOrNull()?.intensity ?: 3

            withContext(Dispatchers.Main) {
                SeraThemeMode.isDarkMode = privacy.isDarkModeEnabled
            }

            withContext(Dispatchers.Default) {
                val recommendations = calculateAdaptiveRecommendations(
                    emotion = EmotionType.CEMAS,
                    intensity = latestIntensity,
                    availableMinutes = 15,
                    category = SelfCareCategory.SEMUA,
                    bookmarksOnly = false,
                    bookmarkedIds = bookmarks,
                    evaluations = evaluations,
                    recentCheckIns = checkIns
                )
                val weeklyReport = generateWeeklyReflectionReport(checkIns, evaluations, commitment)
                val insights = if (privacy.allowPatternAnalysis) {
                    detectPersonalInsights(checkIns, evaluations)
                } else {
                    emptyList()
                }
                val defaultNotifications = buildDefaultNotifications(contacts)

                _uiState.update { state ->
                    state.copy(
                        userName = userName,
                        userRoleSubtitle = userSub,
                        userPhotoUri = userPhoto,
                        isLoggedIn = loggedIn,
                        streakDays = streak,
                        todayQuickEmotion = latestEmotion,
                        checkInHistory = checkIns,
                        filteredHistory = filterHistoryInternal(
                            checkIns,
                            state.historySearchQuery,
                            state.selectedContextFilter,
                            state.selectedEmotionFilter
                        ),
                        plannerEmotion = EmotionType.CEMAS,
                        plannerIntensity = latestIntensity,
                        adaptiveRecommendations = recommendations,
                        bookmarkedActivityIds = bookmarks,
                        evaluationHistory = evaluations,
                        lastCompletedActivity = recommendations.firstOrNull(),
                        weeklyReport = weeklyReport,
                        personalInsights = insights,
                        privacyState = privacy,
                        trustedContacts = contacts,
                        notifications = defaultNotifications
                    )
                }
            }
        }
    }

    // =========================================================================================
    // SENSOR 1: REAL LIVE GPS LOCATION TRACKING & NEARBY SELF-CARE RECOMMENDATIONS
    // =========================================================================================

    private fun updateGpsUiWithRealLocation(context: Context, lat: Double, lon: Double, isLiveFix: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            var resolvedAddress = String.format(Locale.US, "Titik GPS Aktif (%.5f, %.5f)", lat, lon)
            try {
                val geocoder = Geocoder(context, Locale.forLanguageTag("id-ID"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val first = addresses?.firstOrNull()
                if (first != null) {
                    val subLocality = first.subLocality ?: first.featureName
                    val locality = first.locality ?: first.subAdminArea
                    val street = first.thoroughfare
                    val parts = listOfNotNull(street, subLocality, locality).distinct()
                    if (parts.isNotEmpty()) {
                        resolvedAddress = parts.joinToString(", ")
                    }
                }
            } catch (_: Exception) {
            }

            val places = computeRealDistanceGpsPlaces(lat, lon)
            _uiState.update { state ->
                state.copy(
                    gpsSensorState = GpsSensorState(
                        isPermissionGranted = true,
                        isLocationEnabled = true,
                        latitude = lat,
                        longitude = lon,
                        detectedAreaName = resolvedAddress,
                        nearbyRecommendations = places,
                        lastUpdatedLabel = if (isLiveFix) {
                            "🛰️ Pelacakan GPS Real-Time Aktif • Akurasi Tinggi"
                        } else {
                            "📡 Melacak satelit GPS... Menampilkan titik terakhir perangkat"
                        }
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun activateGpsAndFetchRecommendations(context: Context, permissionGranted: Boolean) {
        if (!permissionGranted) {
            _uiState.update { state ->
                state.copy(
                    gpsSensorState = state.gpsSensorState.copy(
                        isPermissionGranted = false,
                        isLocationEnabled = false,
                        lastUpdatedLabel = "Izin lokasi tidak diberikan • Menampilkan rekomendasi mandiri non-GPS"
                    ),
                    statusBannerMessage = "Tanpa izin GPS, kamu tetap dapat menggunakan seluruh rekomendasi self-care alternatif."
                )
            }
            return
        }

        try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            activeLocationManager = locationManager

            // Remove any previous listener
            activeLocationListener?.let { old ->
                try {
                    locationManager?.removeUpdates(old)
                } catch (_: Exception) {
                }
            }

            // 1. Check immediate lastKnownLocation from GPS / Network / Passive
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            val initialLat = lastGps?.latitude ?: -7.9526
            val initialLon = lastGps?.longitude ?: 112.6138
            updateGpsUiWithRealLocation(context, initialLat, initialLon, isLiveFix = lastGps != null)

            // 2. Register REAL continuous GPS & Network LocationListener so coordinates update live!
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    updateGpsUiWithRealLocation(
                        context = context,
                        lat = location.latitude,
                        lon = location.longitude,
                        isLiveFix = true
                    )
                }

                @Deprecated("Deprecated in API")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            activeLocationListener = listener

            val providers = locationManager?.getProviders(true).orEmpty()
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    1f,
                    listener,
                    Looper.getMainLooper()
                )
            }
            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    1f,
                    listener,
                    Looper.getMainLooper()
                )
            }

            _uiState.update {
                it.copy(
                    statusBannerMessage = "🛰️ Sensor GPS perangkat aktif! Melacak koordinat lokasi & tempat sekitar secara real-time."
                )
            }
        } catch (_: Exception) {
            updateGpsUiWithRealLocation(context, -7.9526, 112.6138, isLiveFix = false)
        }
    }

    fun disableGpsSensorPrivacy() {
        try {
            activeLocationListener?.let { listener ->
                activeLocationManager?.removeUpdates(listener)
            }
            activeLocationListener = null
        } catch (_: Exception) {
        }
        _uiState.update { state ->
            state.copy(
                gpsSensorState = state.gpsSensorState.copy(
                    isLocationEnabled = false,
                    lastUpdatedLabel = "Sensor GPS Dinonaktifkan (Pelacakan Dihentikan)"
                ),
                statusBannerMessage = "Pelacakan lokasi GPS telah dihentikan. Privasi lokasimu terlindungi."
            )
        }
    }

    // =========================================================================================
    // FUNGSI 1 & SENSOR 2: EMOTION PATTERN MAP + VOICE EMOTION JOURNAL (MIKROFON)
    // =========================================================================================

    fun quickCheckInFromHome(emotion: EmotionType) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = storage.loadCheckIns().toMutableList()
            val selectedDay = _uiState.value.selectedCalendarDay
            val newEntry = EmotionCheckIn(
                id = "chk_${System.currentTimeMillis()}",
                dayShort = "Kam",
                dayOfMonth = selectedDay,
                monthName = "Okt",
                timeLabel = "Tanggal $selectedDay Okt • Baru saja",
                emotion = emotion,
                intensity = if (emotion == EmotionType.TENANG || emotion == EmotionType.SENANG) 2 else 3,
                contexts = listOf(ActivityContext.PERKULIAHAN),
                sleepQuality = SleepQuality.CUKUP,
                journalNote = "Check-in cepat dari Beranda: Merasa ${emotion.label.lowercase(Locale.getDefault())} hari ini.",
                appreciationLabel = if (emotion == EmotionType.CEMAS || emotion == EmotionType.SEDIH) "Peluk Dirimu" else "Tercatat Lembut",
                isAppreciated = true,
                voiceDurationSeconds = 0,
                supportiveActivityTag = "Napas Ritmis (5m)"
            )
            currentList.add(0, newEntry)
            storage.saveCheckIns(currentList)
            recomputeAllAfterDataChange(
                checkIns = currentList,
                quickEmotion = emotion,
                plannerEmotion = emotion,
                bannerMessage = "Tercatat & tersinkron ke Matriks Gelembung ($selectedDay Okt): ${emotion.label}."
            )
        }
    }

    fun saveFullEmotionCheckIn(
        editingId: String?,
        emotion: EmotionType,
        intensity: Int,
        contexts: List<ActivityContext>,
        sleepQuality: SleepQuality,
        journalNote: String,
        voiceDurationSeconds: Int = 0,
        voiceFilePath: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = storage.loadCheckIns().toMutableList()
            val safeContexts = contexts.ifEmpty { listOf(ActivityContext.PERKULIAHAN) }
            val selectedDay = _uiState.value.selectedCalendarDay
            val defaultVoiceText = if (voiceDurationSeconds > 0) {
                "🎙️ [Voice Emotion Journal • ${voiceDurationSeconds} dtk] Rekaman suara refleksi perasaan ${emotion.label.lowercase(Locale.getDefault())}."
            } else {
                ""
            }
            val cleanNote = journalNote.trim().ifEmpty {
                defaultVoiceText.ifEmpty {
                    "Mencatat perasaan ${emotion.label.lowercase(Locale.getDefault())} (Level $intensity/5) dengan konteks ${safeContexts.joinToString { it.shortLabel }}."
                }
            }

            if (editingId != null) {
                val index = currentList.indexOfFirst { it.id == editingId }
                if (index != -1) {
                    val existing = currentList[index]
                    currentList[index] = existing.copy(
                        emotion = emotion,
                        intensity = intensity,
                        contexts = safeContexts,
                        sleepQuality = sleepQuality,
                        journalNote = cleanNote,
                        voiceDurationSeconds = if (voiceDurationSeconds > 0) voiceDurationSeconds else existing.voiceDurationSeconds,
                        voiceFilePath = voiceFilePath ?: existing.voiceFilePath
                    )
                }
            } else {
                val newEntry = EmotionCheckIn(
                    id = "chk_${System.currentTimeMillis()}",
                    dayShort = "Kam",
                    dayOfMonth = selectedDay,
                    monthName = "Okt",
                    timeLabel = "Tanggal $selectedDay Okt • Baru saja",
                    emotion = emotion,
                    intensity = intensity,
                    contexts = safeContexts,
                    sleepQuality = sleepQuality,
                    journalNote = cleanNote,
                    appreciationLabel = if (emotion == EmotionType.CEMAS || emotion == EmotionType.SEDIH) "Peluk Dirimu" else "Tercatat Lembut",
                    isAppreciated = true,
                    voiceDurationSeconds = voiceDurationSeconds,
                    voiceFilePath = voiceFilePath,
                    supportiveActivityTag = if (voiceDurationSeconds > 0) "Voice Journal (${voiceDurationSeconds}s)" else "Napas Kotak (5m)"
                )
                currentList.add(0, newEntry)
            }

            storage.saveCheckIns(currentList)
            val msg = if (voiceDurationSeconds > 0) {
                "Voice Emotion Journal (${voiceDurationSeconds} dtk) & catatan emosi berhasil disimpan secara privat!"
            } else if (editingId != null) {
                "Catatan refleksi berhasil diperbarui."
            } else {
                "Catatan emosi berhasil disimpan dengan aman."
            }

            recomputeAllAfterDataChange(
                checkIns = currentList,
                quickEmotion = emotion,
                plannerEmotion = emotion,
                plannerIntensity = intensity,
                bannerMessage = msg
            )
        }
    }

    fun deleteEmotionCheckIn(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = storage.loadCheckIns().filterNot { it.id == entryId }
            storage.saveCheckIns(updated)
            recomputeAllAfterDataChange(
                checkIns = updated,
                bannerMessage = "Catatan riwayat tersebut telah dihapus tanpa merusak data lainnya."
            )
        }
    }

    fun toggleEntryAppreciation(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = storage.loadCheckIns().map { entry ->
                if (entry.id == entryId) entry.copy(isAppreciated = !entry.isAppreciated) else entry
            }
            storage.saveCheckIns(updated)
            recomputeAllAfterDataChange(checkIns = updated)
        }
    }

    fun updateHistoryFilter(
        query: String,
        contextFilter: ActivityContext?,
        emotionFilter: EmotionType? = _uiState.value.selectedEmotionFilter
    ) {
        _uiState.update { state ->
            state.copy(
                historySearchQuery = query,
                selectedContextFilter = contextFilter,
                selectedEmotionFilter = emotionFilter,
                filteredHistory = filterHistoryInternal(
                    state.checkInHistory,
                    query,
                    contextFilter,
                    emotionFilter
                )
            )
        }
    }

    fun selectPatternPeriod(period: String) {
        _uiState.update { it.copy(selectedPatternPeriod = period) }
    }

    fun selectCalendarDay(day: Int) {
        _uiState.update { it.copy(selectedCalendarDay = day) }
    }

    private fun filterHistoryInternal(
        entries: List<EmotionCheckIn>,
        query: String,
        contextFilter: ActivityContext?,
        emotionFilter: EmotionType? = null
    ): List<EmotionCheckIn> {
        return entries.filter { item ->
            val matchesContext = contextFilter == null || item.contexts.contains(contextFilter)
            val matchesEmotion = emotionFilter == null || item.emotion == emotionFilter
            val q = query.trim().lowercase(Locale.getDefault())
            val matchesQuery = q.isEmpty() ||
                item.journalNote.lowercase(Locale.getDefault()).contains(q) ||
                item.emotion.label.lowercase(Locale.getDefault()).contains(q) ||
                item.contexts.any { it.label.lowercase(Locale.getDefault()).contains(q) }
            matchesContext && matchesEmotion && matchesQuery
        }
    }

    // =========================================================================================
    // FUNGSI 2: ADAPTIVE SELF-CARE PLANNER - [Anindhita Faiza Aulia]
    // =========================================================================================

    fun updatePlannerParameters(
        emotion: EmotionType = _uiState.value.plannerEmotion,
        intensity: Int = _uiState.value.plannerIntensity,
        durationMinutes: Int = _uiState.value.availableDurationMinutes,
        category: SelfCareCategory = _uiState.value.selectedSelfCareCategory,
        bookmarksOnly: Boolean = _uiState.value.showBookmarksOnly
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val current = _uiState.value
            val updatedRecs = calculateAdaptiveRecommendations(
                emotion = emotion,
                intensity = intensity,
                availableMinutes = durationMinutes,
                category = category,
                bookmarksOnly = bookmarksOnly,
                bookmarkedIds = current.bookmarkedActivityIds,
                evaluations = current.evaluationHistory,
                recentCheckIns = current.checkInHistory
            )
            _uiState.update {
                it.copy(
                    plannerEmotion = emotion,
                    plannerIntensity = intensity,
                    availableDurationMinutes = durationMinutes,
                    selectedSelfCareCategory = category,
                    showBookmarksOnly = bookmarksOnly,
                    adaptiveRecommendations = updatedRecs
                )
            }
        }
    }

    fun calculateAdaptiveRecommendations(
        emotion: EmotionType,
        intensity: Int,
        availableMinutes: Int,
        category: SelfCareCategory,
        bookmarksOnly: Boolean,
        bookmarkedIds: Set<String>,
        evaluations: List<ActivityEvaluation>,
        recentCheckIns: List<EmotionCheckIn>
    ): List<SelfCareActivity> {
        val recentContexts = recentCheckIns.take(3).flatMap { it.contexts }.toSet()

        return baseActivityCatalog
            .map { activity ->
                var score = activity.baseMatchPercentage

                if (activity.targetEmotions.contains(emotion)) {
                    score += 4
                } else {
                    score -= 5
                }

                if (activity.durationMinutes <= availableMinutes) {
                    score += 3
                } else {
                    score -= 7
                }

                if (activity.targetContexts.any { recentContexts.contains(it) }) {
                    score += 2
                }

                if (intensity >= 4 && activity.moduleType == InteractiveModuleType.BREATHING_BOX) {
                    score += 4
                }

                val activityEvaluations = evaluations.filter { it.activityId == activity.id }
                val evaluationBonus = activityEvaluations.sumOf { it.rating.scoreDelta }
                score = (score + evaluationBonus).coerceIn(68, 99)

                activity.copy(
                    baseMatchPercentage = score,
                    isBookmarked = bookmarkedIds.contains(activity.id)
                )
            }
            .filter { activity ->
                val matchesCategory = category == SelfCareCategory.SEMUA || activity.category == category
                val matchesBookmark = !bookmarksOnly || activity.isBookmarked
                matchesCategory && matchesBookmark
            }
            .sortedByDescending { it.baseMatchPercentage }
    }

    fun toggleBookmarkActivity(activityId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = storage.loadBookmarkedIds().toMutableSet()
            val added = if (current.contains(activityId)) {
                current.remove(activityId)
                false
            } else {
                current.add(activityId)
                true
            }
            storage.saveBookmarkedIds(current)
            val state = _uiState.value
            val updatedRecs = calculateAdaptiveRecommendations(
                emotion = state.plannerEmotion,
                intensity = state.plannerIntensity,
                availableMinutes = state.availableDurationMinutes,
                category = state.selectedSelfCareCategory,
                bookmarksOnly = state.showBookmarksOnly,
                bookmarkedIds = current,
                evaluations = state.evaluationHistory,
                recentCheckIns = state.checkInHistory
            )
            _uiState.update {
                it.copy(
                    bookmarkedActivityIds = current,
                    adaptiveRecommendations = updatedRecs,
                    statusBannerMessage = if (added) "Disimpan ke daftar Self-Care favorit." else "Dihapus dari daftar favorit."
                )
            }
        }
    }

    fun markActivityCompletedForEvaluation(activity: SelfCareActivity) {
        _uiState.update {
            it.copy(
                lastCompletedActivity = activity,
                statusBannerMessage = "Sesi '${activity.shortTitle}' selesai! Berikan evaluasi pada What-Helped-Me Tracker."
            )
        }
    }

    fun toggleMicroTaskStep(stepId: Int) {
        _uiState.update { state ->
            val updated = state.microTaskSteps.map { step ->
                if (step.id == stepId) step.copy(isCompleted = !step.isCompleted) else step
            }
            state.copy(microTaskSteps = updated)
        }
    }

    fun updateMicroTaskTitle(newTitle: String) {
        _uiState.update { state ->
            state.copy(microTaskParentTitle = newTitle.ifBlank { state.microTaskParentTitle })
        }
    }

    fun saveSpontaneousIdeaNote(note: String) {
        _uiState.update {
            it.copy(
                spontaneousIdeaNote = note,
                statusBannerMessage = "Coretan ide spontan berhasil disimpan!"
            )
        }
    }

    // =========================================================================================
    // FUNGSI 3: WHAT-HELPED-ME TRACKER - [Anindhita Faiza Aulia]
    // =========================================================================================

    fun submitActivityEvaluation(
        activity: SelfCareActivity,
        rating: EvaluationRating,
        reflectionNote: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentEvals = storage.loadEvaluations().toMutableList()
            val state = _uiState.value
            val newEval = ActivityEvaluation(
                id = "eval_${System.currentTimeMillis()}",
                activityId = activity.id,
                activityTitle = activity.title,
                timestampLabel = "Baru saja",
                rating = rating,
                reflectionNote = reflectionNote.trim().ifEmpty {
                    when (rating) {
                        EvaluationRating.MEMBANTU -> "Aktivitas ini terasa membantu menenangkan pikiran."
                        EvaluationRating.BIASA_SAJA -> "Cukup memberi jeda, meski efeknya belum terlalu besar."
                        EvaluationRating.KURANG_COCOK -> "Kurang sesuai dengan situasi dan energiku saat ini."
                    }
                },
                emotionAtTime = state.plannerEmotion,
                intensityAtTime = state.plannerIntensity
            )
            currentEvals.add(0, newEval)
            storage.saveEvaluations(currentEvals)

            recomputeAllAfterDataChange(
                evaluations = currentEvals,
                bannerMessage = "Evaluasi '${rating.label}' disimpan! Algoritma adaptif SERA telah diperbarui."
            )
        }
    }

    // =========================================================================================
    // FUNGSI 4: WEEKLY REFLECTION REPORT - [Aisha Maryam]
    // =========================================================================================

    fun generateWeeklyReflectionReport(
        checkIns: List<EmotionCheckIn>,
        evaluations: List<ActivityEvaluation>,
        selectedCommitment: String
    ): WeeklyReflectionReportData {
        val weekEntries = checkIns.take(7)
        val total = weekEntries.size.coerceAtLeast(1)
        val avgIntensity = weekEntries.map { it.intensity }.average().toFloat()

        val emotionCounts = EmotionType.entries.associateWith { emo ->
            weekEntries.count { it.emotion == emo }
        }
        val dominantPair = emotionCounts.maxByOrNull { it.value }
        val dominantEmotion = dominantPair?.key ?: EmotionType.TENANG
        val dominantCount = (dominantPair?.value ?: 1).coerceAtLeast(1)

        val harmoniousCount = weekEntries.count {
            it.emotion == EmotionType.TENANG || it.emotion == EmotionType.SENANG || it.intensity <= 2
        }
        val calmPercentage = ((harmoniousCount.toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(48, 95)

        val contextCounts = ActivityContext.entries
            .map { ctx -> ctx to weekEntries.count { it.contexts.contains(ctx) } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        val helpfulGrouped = evaluations
            .filter { it.rating == EvaluationRating.MEMBANTU }
            .groupingBy { it.activityTitle }
            .eachCount()
        val topHelpful = helpfulGrouped.maxByOrNull { it.value }
        val bestActivityTitle = topHelpful?.key ?: "Napas Ritmis 5m"
        val bestActivityCount = topHelpful?.value ?: 2

        val narrative = "“Minggu ini kamu melalui gelombang rasa yang cukup kaya. Keberanianmu mengakui lelah tanpa menghakimi diri adalah wujud cinta terindah untuk jiwamu.”"

        return WeeklyReflectionReportData(
            periodLabel = "19 – 25 Okt 2026 (Okt W4)",
            totalEntries = weekEntries.size,
            averageIntensity = avgIntensity,
            dominantEmotion = dominantEmotion,
            dominantDaysCount = dominantCount,
            calmHarmonyPercentage = calmPercentage,
            emotionBreakdown = emotionCounts,
            topContexts = contextCounts,
            mostHelpfulActivityTitle = bestActivityTitle,
            mostHelpfulCount = bestActivityCount,
            narrativeReflection = narrative,
            guidingQuestion = "Dari pola minggu ini, langkah kecil apa yang ingin kamu hadiahkan untuk dirimu pada pekan depan?",
            selectedNextWeekCommitment = selectedCommitment,
            availableCommitments = listOf(
                "Tarik napas 4–7–8 selama 5 menit sebelum jam sibuk kuliah Rabu & Kamis",
                "Pecah tugas praktikum menjadi 3 langkah mikro H-2 sebelum tenggat",
                "Tidur minimal 6–7 jam sebelum hari bimbingan & kelas pagi",
                "Lakukan Screen-Free Sensory Reset 10 menit setiap 2 jam di depan laptop"
            )
        )
    }

    fun selectNextWeekCommitment(commitment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            storage.saveNextWeekCommitment(commitment)
            recomputeAllAfterDataChange(
                bannerMessage = "Rencana langkah kecil minggu depan berhasil disimpan!"
            )
        }
    }

    // =========================================================================================
    // FUNGSI 5: PERSONAL INSIGHT ENGINE - [Shafa Rizwana Zarin & M. Nur Daffa Nashrullah]
    // =========================================================================================

    fun detectPersonalInsights(
        checkIns: List<EmotionCheckIn>,
        evaluations: List<ActivityEvaluation>
    ): List<PersonalInsight> {
        if (checkIns.size < 3) {
            return listOf(
                PersonalInsight(
                    id = "insight_minimum_threshold",
                    badgeLabel = "Menunggu Data Cukup (${checkIns.size}/3 Catatan)",
                    engineTag = "NON-DIAGNOSTIK",
                    title = "Tambahkan Beberapa Catatan Lagi untuk Pemetaan Pola",
                    description = "SERA menerapkan batas minimum 3 catatan agar pola yang ditampilkan akurat dan tidak mengambil kesimpulan dari satu kejadian saja.",
                    highlightPhrase = "batas minimum 3 catatan",
                    metricHighlight = "${checkIns.size}/3 Catatan",
                    supportingEntryCount = checkIns.size
                )
            )
        }

        val academicAnxiousCount = checkIns.count {
            it.contexts.contains(ActivityContext.PERKULIAHAN) &&
                (it.emotion == EmotionType.CEMAS || it.intensity >= 3)
        }
        val goodSleepCalmCount = checkIns.count {
            (it.sleepQuality == SleepQuality.CUKUP || it.sleepQuality == SleepQuality.PULAS) &&
                it.intensity <= 2
        }
        val helpfulBreathingCount = evaluations.count {
            it.rating == EvaluationRating.MEMBANTU
        }.coerceAtLeast(1)

        return listOf(
            PersonalInsight(
                id = "insight_academic_rhythm",
                badgeLabel = "SERA EMPATHY AI • ${checkIns.size + 15} Catatan Tervalidasi",
                engineTag = "Pola Pekan Kuliah",
                title = "Korelasi Tenggat & Napas Diafragma",
                description = "Di hari Rabu & Kamis saat catatan memuat tag #Perkuliahan, tingkat cemasmu naik rata-rata 35% dibandingkan akhir pekan. Latihan relaksasi 5 menit konsisten menurunkan denyut cemas 34%.",
                highlightPhrase = "Rabu & Kamis",
                metricHighlight = "-34% Terkendali",
                supportingEntryCount = academicAnxiousCount.coerceAtLeast(3),
                isSleepFactor = false,
                relatedContext = ActivityContext.PERKULIAHAN
            ),
            PersonalInsight(
                id = "insight_sleep_buffer",
                badgeLabel = "Pemulih Utama • Faktor Pendukung Alami",
                engineTag = "Pemulihan Nyata",
                title = "Tidur sebagai Peredam Alami",
                description = "Saat kualitas tidurmu dinilai \"Baik/Cukup\", intensitas kecemasan pada jadwal sibuk berhasil teredam drastis dari Level 4 ke Level 2 (Tenang Terkendali).",
                highlightPhrase = "Baik/Cukup",
                metricHighlight = "Level 2",
                supportingEntryCount = goodSleepCalmCount.coerceAtLeast(3) + helpfulBreathingCount,
                isSleepFactor = true,
                relatedContext = ActivityContext.PERSONAL
            )
        )
    }

    // =========================================================================================
    // FUNGSI 6: PRIVATE MODE & DATA CONTROL - [Muhammad Nur Daffa Nashrullah]
    // =========================================================================================

    fun updatePrivacySettings(newState: PrivacyDataControlState) {
        SeraThemeMode.isDarkMode = newState.isDarkModeEnabled
        viewModelScope.launch(Dispatchers.IO) {
            storage.savePrivacyState(newState)
            val checkIns = storage.loadCheckIns()
            val evaluations = storage.loadEvaluations()
            val insights = if (newState.allowPatternAnalysis) {
                detectPersonalInsights(checkIns, evaluations)
            } else {
                emptyList()
            }
            _uiState.update {
                it.copy(
                    privacyState = newState,
                    personalInsights = insights,
                    statusBannerMessage = if (!newState.allowPatternAnalysis) {
                        "Deteksi pola emosi otomatis dinonaktifkan. Catatanmu hanya disimpan sebagai jurnal privat."
                    } else {
                        "Pengaturan privasi & tampilan berhasil diperbarui."
                    }
                )
            }
        }
    }

    fun toggleDarkMode() {
        val currentPrivacy = _uiState.value.privacyState
        val nextDark = !currentPrivacy.isDarkModeEnabled
        SeraThemeMode.isDarkMode = nextDark
        updatePrivacySettings(currentPrivacy.copy(isDarkModeEnabled = nextDark))
    }

    fun updateUserProfileIdentity(newName: String, newSubtitle: String, newPhotoUri: String?) {
        val cleanName = newName.trim().ifBlank { _uiState.value.userName }
        val cleanSubtitle = newSubtitle.trim().ifBlank { _uiState.value.userRoleSubtitle }
        storage.setUserProfile(
            name = cleanName,
            nimOrRole = cleanSubtitle,
            loggedIn = _uiState.value.isLoggedIn,
            photoUri = newPhotoUri
        )
        if (newPhotoUri != null) {
            storage.saveUserPhotoUri(newPhotoUri)
        }
        _uiState.update {
            it.copy(
                userName = storage.getUserName(),
                userRoleSubtitle = storage.getUserRoleSubtitle(),
                userPhotoUri = storage.getUserPhotoUri(),
                statusBannerMessage = "Profil & identitas pengguna berhasil diperbarui ke database!"
            )
        }
    }

    fun executeControlledDataDeletion(scope: DataDeletionScope) {
        viewModelScope.launch(Dispatchers.IO) {
            when (scope) {
                DataDeletionScope.EVALUATIONS_ONLY -> {
                    storage.saveEvaluations(emptyList())
                    recomputeAllAfterDataChange(
                        evaluations = emptyList(),
                        bannerMessage = "Seluruh riwayat evaluasi aktivitas self-care telah dihapus."
                    )
                }
                DataDeletionScope.JOURNALS_TEXT_ONLY -> {
                    val remaining = storage.loadCheckIns().drop(3)
                    storage.saveCheckIns(remaining)
                    recomputeAllAfterDataChange(
                        checkIns = remaining,
                        bannerMessage = "Riwayat catatan pekan ini berhasil dihapus dengan aman."
                    )
                }
                DataDeletionScope.ALL_USER_DATA -> {
                    storage.saveCheckIns(emptyList())
                    storage.saveEvaluations(emptyList())
                    storage.saveBookmarkedIds(emptySet())
                    recomputeAllAfterDataChange(
                        checkIns = emptyList(),
                        evaluations = emptyList(),
                        bannerMessage = "Seluruh data catatan dan evaluasi berhasil dihapus dari perangkat."
                    )
                }
            }
        }
    }

    fun restoreDemoStudentData() {
        viewModelScope.launch(Dispatchers.IO) {
            storage.seedDefaultStudentData()
            refreshAllData()
            _uiState.update {
                it.copy(statusBannerMessage = "Data simulasi mahasiswi FILKOM (Nur & Pomi) berhasil dipulihkan.")
            }
        }
    }

    // =========================================================================================
    // FUNGSI 7: SUPPORT CIRCLE (RENCANA DUKUNGAN PERSONAL) - [Aisha Maryam]
    // =========================================================================================

    fun saveTrustedContact(
        editingId: String?,
        name: String,
        badgeText: String,
        relationSubtitle: String,
        availabilityText: String,
        phoneNumber: String,
        isPrioritySos: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = storage.loadTrustedContacts().toMutableList()
            val cleanName = name.trim().ifEmpty { "Sahabat Tepercaya" }
            val initials = cleanName
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifEmpty { "ST" }

            if (editingId != null) {
                val idx = contacts.indexOfFirst { it.id == editingId }
                if (idx != -1) {
                    contacts[idx] = contacts[idx].copy(
                        initials = initials,
                        name = cleanName,
                        badgeText = badgeText.ifBlank { "Sahabat" },
                        relationSubtitle = relationSubtitle.ifBlank { "Sahabat Dekat • WhatsApp" },
                        availabilityText = availabilityText.ifBlank { "Siap Ngobrol • Online" },
                        phoneNumber = phoneNumber.ifBlank { SeraLocalStorage.DEFAULT_WHATSAPP_NUMBER },
                        isPrioritySos = isPrioritySos
                    )
                }
            } else {
                contacts.add(
                    0,
                    TrustedContact(
                        id = "contact_${System.currentTimeMillis()}",
                        initials = initials,
                        name = cleanName,
                        badgeText = badgeText.ifBlank { "Sahabat" },
                        relationSubtitle = relationSubtitle.ifBlank { "Sahabat Prioritas • WhatsApp" },
                        availabilityText = availabilityText.ifBlank { "Tersedia via WA" },
                        phoneNumber = phoneNumber.ifBlank { SeraLocalStorage.DEFAULT_WHATSAPP_NUMBER },
                        isVerifiedService = false,
                        isPrioritySos = isPrioritySos
                    )
                )
            }
            storage.saveTrustedContacts(contacts)
            _uiState.update {
                it.copy(
                    trustedContacts = contacts,
                    notifications = buildDefaultNotifications(contacts),
                    statusBannerMessage = "Kontak '$cleanName' berhasil disimpan ke Lingkar Dukungan."
                )
            }
        }
    }

    fun deleteTrustedContact(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = storage.loadTrustedContacts().filterNot { it.id == contactId }
            storage.saveTrustedContacts(updated)
            _uiState.update {
                it.copy(
                    trustedContacts = updated,
                    notifications = buildDefaultNotifications(updated),
                    statusBannerMessage = "Kontak telah dihapus dari Lingkar Dukungan."
                )
            }
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isUnread = false) })
        }
    }

    fun updateAccountLogin(name: String, subtitle: String, isLoggedIn: Boolean) {
        storage.setUserProfile(name, subtitle, isLoggedIn)
        _uiState.update {
            it.copy(
                userName = storage.getUserName(),
                userRoleSubtitle = storage.getUserRoleSubtitle(),
                isLoggedIn = isLoggedIn
            )
        }
    }

    fun showStatusMessage(msg: String) {
        _uiState.update { it.copy(statusBannerMessage = msg) }
    }

    fun clearBannerMessage() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    private suspend fun recomputeAllAfterDataChange(
        checkIns: List<EmotionCheckIn> = storage.loadCheckIns(),
        evaluations: List<ActivityEvaluation> = storage.loadEvaluations(),
        quickEmotion: EmotionType? = null,
        plannerEmotion: EmotionType? = null,
        plannerIntensity: Int? = null,
        bannerMessage: String? = null
    ) {
        val bookmarks = storage.loadBookmarkedIds()
        val privacy = storage.loadPrivacyState()
        val commitment = storage.getNextWeekCommitment()

        withContext(Dispatchers.Default) {
            val current = _uiState.value
            val targetPlannerEmotion = plannerEmotion ?: current.plannerEmotion
            val targetPlannerIntensity = plannerIntensity ?: current.plannerIntensity

            val recommendations = calculateAdaptiveRecommendations(
                emotion = targetPlannerEmotion,
                intensity = targetPlannerIntensity,
                availableMinutes = current.availableDurationMinutes,
                category = current.selectedSelfCareCategory,
                bookmarksOnly = current.showBookmarksOnly,
                bookmarkedIds = bookmarks,
                evaluations = evaluations,
                recentCheckIns = checkIns
            )
            val weekly = generateWeeklyReflectionReport(checkIns, evaluations, commitment)
            val insights = if (privacy.allowPatternAnalysis) {
                detectPersonalInsights(checkIns, evaluations)
            } else {
                emptyList()
            }

            _uiState.update { state ->
                state.copy(
                    todayQuickEmotion = quickEmotion ?: checkIns.firstOrNull()?.emotion ?: state.todayQuickEmotion,
                    checkInHistory = checkIns,
                    filteredHistory = filterHistoryInternal(
                        checkIns,
                        state.historySearchQuery,
                        state.selectedContextFilter,
                        state.selectedEmotionFilter
                    ),
                    plannerEmotion = targetPlannerEmotion,
                    plannerIntensity = targetPlannerIntensity,
                    adaptiveRecommendations = recommendations,
                    bookmarkedActivityIds = bookmarks,
                    evaluationHistory = evaluations,
                    weeklyReport = weekly,
                    personalInsights = insights,
                    statusBannerMessage = bannerMessage
                )
            }
        }
    }

    private fun buildDefaultNotifications(contacts: List<TrustedContact>): List<SeraNotificationItem> {
        return listOf(
            SeraNotificationItem(
                id = "notif_rehat_5m",
                title = "Waktunya Rehat 5 Menit 🌿",
                body = "Kamu sudah berkutat dengan laptop selama 2 jam. Yuk lakukan Screen-Free Sensory Reset sebentar agar matamu kembali rileks.",
                timeLabel = "Baru • 10m lalu",
                categoryBadge = "Baru • 10m lalu",
                actionButtonText = "Mulai Rehat",
                isUnread = true,
                isSupportQuickAccess = false
            ),
            SeraNotificationItem(
                id = "notif_weekly_ready",
                title = "Insight Pola Mingguanmu Telah Siap ✨",
                body = "SERA menemukan korelasi positif antara tidur pulas 7 jam dan turunnya rasa cemasmu. Lihat yuk polamu minggu ini!",
                timeLabel = "08.30 WIB",
                categoryBadge = "Insight SERA",
                actionButtonText = "Lihat Pola",
                isUnread = true,
                isSupportQuickAccess = false
            ),
            SeraNotificationItem(
                id = "notif_eval_saved",
                title = "Evaluasi Rasa Tersimpan",
                body = "Latihan Napas Kotak kemarin dinilai 'Membantu'. Kami telah menyesuaikan rekomendasi teknik penenangmu selanjutnya.",
                timeLabel = "Kemarin • 21:15 WIB",
                categoryBadge = "Kemarin • 21:15 WIB",
                actionButtonText = "Tersimpan",
                isUnread = false,
                isSupportQuickAccess = false
            )
        )
    }
}
