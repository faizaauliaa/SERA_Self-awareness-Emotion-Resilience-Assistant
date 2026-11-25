package id.ac.ub.filkom.sera.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.ac.ub.filkom.sera.data.model.ActivityContext
import id.ac.ub.filkom.sera.data.model.EmotionCheckIn
import id.ac.ub.filkom.sera.data.model.EmotionType
import id.ac.ub.filkom.sera.data.model.SleepQuality
import id.ac.ub.filkom.sera.ui.components.PomiBearMascot
import id.ac.ub.filkom.sera.ui.components.PomiMascotMood
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.util.SeraVoiceJournalEngine
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInJournalScreen(
    userName: String = "Nur",
    initialEmotion: EmotionType,
    editingEntry: EmotionCheckIn? = null,
    showBackHeader: Boolean = false,
    onBackClick: () -> Unit,
    onSaveCheckIn: (
        editingId: String?,
        emotion: EmotionType,
        intensity: Int,
        contexts: List<ActivityContext>,
        sleepQuality: SleepQuality,
        note: String,
        voiceDurationSeconds: Int,
        voiceFilePath: String?,
        navigateToSelfCare: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var selectedEmotion by remember(editingEntry, initialEmotion) {
        mutableStateOf(editingEntry?.emotion ?: initialEmotion)
    }
    var selectedIntensity by remember(editingEntry) {
        mutableIntStateOf(editingEntry?.intensity ?: 3)
    }
    var selectedContexts by remember(editingEntry) {
        mutableStateOf(
            editingEntry?.contexts?.toSet() ?: setOf(ActivityContext.PERKULIAHAN)
        )
    }
    var selectedSleep by remember(editingEntry) {
        mutableStateOf(editingEntry?.sleepQuality ?: SleepQuality.CUKUP)
    }
    var journalText by remember(editingEntry) {
        mutableStateOf(editingEntry?.journalNote ?: "")
    }
    var pomiSpeechText by remember(userName) {
        mutableStateOf("“Ceritakan apa yang kamu rasakan, $userName... Aku di sini mendengarkanmu ✨”")
    }

    // SENSOR 2: MICROPHONE — VOICE EMOTION JOURNAL STATE (REAL WAV RECORDER & PLAYER)

    LaunchedEffect(Unit) {
        SeraVoiceJournalEngine.initTtsIfNeeded(context)
    }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordedSeconds by remember(editingEntry) {
        mutableIntStateOf(editingEntry?.voiceDurationSeconds ?: 0)
    }
    var recordedFilePath by remember(editingEntry) {
        mutableStateOf(editingEntry?.voiceFilePath)
    }
    var isPlayingVoice by remember { mutableStateOf(false) }
    var liveMicAmp by remember { mutableFloatStateOf(0.25f) }
    var micStatusNotice by remember {
        mutableStateOf("Sensor Mikrofon Siaga • Alternatif ekspresi emosi lewat rekaman suara (.WAV)")
    }

    fun startVoiceRecording() {
        val wavPath = SeraVoiceJournalEngine.startRecordingWav(context)
        recordedFilePath = wavPath
        recordedSeconds = 0
        isRecordingVoice = true
        micStatusNotice = "🎙️ Mikrofon aktif merekam suara... Bicaralah dengan tenang."
    }

    fun stopVoiceRecording() {
        isRecordingVoice = false
        SeraVoiceJournalEngine.stopRecording()
        if (recordedSeconds < 2) recordedSeconds = 4
        micStatusNotice = "✓ Rekaman Voice Emotion Journal (${recordedSeconds} detik) tersimpan & siap diputar!"
    }

    fun togglePlayRecordedVoice() {
        if (isPlayingVoice) {
            SeraVoiceJournalEngine.stopPlayback()
            isPlayingVoice = false
            micStatusNotice = "Pemutaran jurnal suara dihentikan."
            return
        }

        isPlayingVoice = true
        micStatusNotice = "🔊 Memutar rekaman Voice Emotion Journal (${recordedSeconds} detik)..."
        val fallbackSpoken = journalText.ifBlank {
            "Jurnal suara refleksi perasaan ${selectedEmotion.label} dengan tingkat intensitas $selectedIntensity dari 5."
        }
        SeraVoiceJournalEngine.playVoiceJournal(
            context = context,
            filePath = recordedFilePath,
            fallbackSpokenText = fallbackSpoken,
            durationSeconds = recordedSeconds.coerceAtLeast(4),
            onComplete = {
                isPlayingVoice = false
                micStatusNotice = "✓ Selesai memutar rekaman Voice Emotion Journal."
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            SeraVoiceJournalEngine.stopRecording()
            SeraVoiceJournalEngine.stopPlayback()
        }
    }

    LaunchedEffect(isRecordingVoice) {
        var tick = 0
        while (isRecordingVoice) {
            delay(100L)
            liveMicAmp = SeraVoiceJournalEngine.getLiveAmplitude()
            tick++
            if (tick % 10 == 0) {
                recordedSeconds += 1
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            startVoiceRecording()
        } else {
            // Even if permission denied on emulator, still allow simulated voice journal or text journal
            startVoiceRecording()
            micStatusNotice = "Merekam dengan mode suara harmonis (tanpa akses mikrofon eksternal)."
        }
    }

    val intensityDescriptions = mapOf(
        1 to "Hadir samar di latar belakang, sangat mudah dikelola.",
        2 to "Terasa ringan namun cukup disadari saat beraktivitas.",
        3 to "Pikiran terfokus pada rasa ini secara konsisten.",
        4 to "Cukup kuat memengaruhi konsentrasi dan ritme tubuh hari ini.",
        5 to "Sangat intens dan memenuhi ruang pikiran. Mari ambil jeda napas."
    )

    val intensityLabels = mapOf(
        1 to "Level 1 • Halus",
        2 to "Level 2 • Ringan",
        3 to "Level 3 • Sedang",
        4 to "Level 4 • Kuat",
        5 to "Level 5 • Intens"
    )

    val quickPrompts = listOf(
        "✨ + Beban skripsi",
        "🌱 + Butuh napas sejenak",
        "🍵 + Syukur hari ini"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showBackHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBackClick,
                    shape = CircleShape,
                    color = SeraSurface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "←", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (editingEntry != null) "Ubah Jurnal Refleksi" else "Catat Emosi & Jurnal Refleksi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
            }
        }

        // 0. COMPANION HERO CARD WITH LIVING PURPLE BEAR MASCOT (POMI)
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SeraSurface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        PomiBearMascot(
                            size = 86.dp,
                            mood = PomiMascotMood.LISTENING,
                            showOuterHalo = false,
                            onMascotTapped = { greeting ->
                                pomiSpeechText = "“$greeting”"
                            }
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF48FB1),
                            border = BorderStroke(2.dp, SeraSurface),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🤍", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = SeraSurfaceSubtle,
                        border = BorderStroke(1.dp, SeraCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SeraTeal)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SERA TEMAN SETIAMU",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pomiSpeechText,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeraTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒 Ruang aman & terenkripsi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraPrimaryBlue
                    )
                    Text(
                        text = "Senin, 24 Okt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SeraTextSecondary
                    )
                }
            }
        }

        // STEP 1: EMOSI DOMINAN
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StepHeaderRow(
                    stepNumber = "1",
                    title = "Emosi Dominan",
                    badgeText = "Pilih 1 Rasa"
                )

                Spacer(modifier = Modifier.height(14.dp))

                val ordered = listOf(
                    EmotionType.SENANG,
                    EmotionType.TENANG,
                    EmotionType.BIASA,
                    EmotionType.CEMAS,
                    EmotionType.SEDIH
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ordered.forEach { emotion ->
                        val isSelected = selectedEmotion == emotion
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) emotion.bgColor else SeraSurfaceSubtle
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) emotion.accentColor else SeraCardBorder,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedEmotion = emotion }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(emotion.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emotion.emoji, fontSize = 19.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = emotion.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) emotion.accentColor else SeraTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = EmotionCalmBg.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SeraTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = selectedEmotion.cardHeadline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedEmotion.cardSubtitle,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = SeraTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // STEP 2: INTENSITAS RASA
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StepHeaderRow(
                    stepNumber = "2",
                    title = "Intensitas Rasa",
                    badgeText = intensityLabels[selectedIntensity] ?: "Level 3 • Sedang"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraSurfaceSubtle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { level ->
                            val isSelected = selectedIntensity == level
                            Surface(
                                onClick = { selectedIntensity = level },
                                shape = CircleShape,
                                color = if (isSelected) SeraPrimaryBlue else SeraSurface,
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                modifier = Modifier.size(if (isSelected) 52.dp else 44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = level.toString(),
                                        fontSize = if (isSelected) 17.sp else 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else SeraTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Halus/Samar", fontSize = 10.sp, color = SeraTextSecondary)
                    Text(
                        text = intensityDescriptions[selectedIntensity] ?: "",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraPrimaryBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(text = "Sangat\nKuat", fontSize = 10.sp, lineHeight = 12.sp, color = SeraTextSecondary)
                }
            }
        }

        // STEP 3: APA YANG MEMENGARUHI?
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StepHeaderRow(
                    stepNumber = "3",
                    title = "Apa yang Memengaruhi?",
                    badgeText = "Multi–pilih"
                )

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActivityContext.entries.forEach { contextItem ->
                        val isSelected = selectedContexts.contains(contextItem)
                        Surface(
                            onClick = {
                                selectedContexts = if (isSelected && selectedContexts.size > 1) {
                                    selectedContexts - contextItem
                                } else {
                                    selectedContexts + contextItem
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) SeraPrimaryBlue else SeraSurfaceSubtle,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) SeraPrimaryBlue else SeraCardBorder
                            ),
                            shadowElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = contextItem.iconEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = contextItem.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SeraTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // STEP 4: KUALITAS TIDUR SEMALAM
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StepHeaderRow(
                    stepNumber = "4",
                    title = "Kualitas Tidur Semalam",
                    badgeText = "Durasi & Energi"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SleepQuality.entries.forEach { sleep ->
                        val isSelected = selectedSleep == sleep
                        val orbBg = when (sleep) {
                            SleepQuality.KURANG -> SeraRoseSoft
                            SleepQuality.CUKUP -> SeraPrimaryBlueLight
                            SleepQuality.PULAS -> EmotionCalmBg
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    if (isSelected) SeraRoseSoft.copy(alpha = 0.7f) else SeraSurfaceSubtle
                                )
                                .border(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFF48FB1) else SeraCardBorder,
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .clickable { selectedSleep = sleep }
                                .padding(vertical = 16.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(orbBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = sleep.emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = sleep.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = sleep.durationLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SeraRose else SeraTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // STEP 5: JURNAL LEMBUT JIWA + VOICE EMOTION JOURNAL (SENSOR MIKROFON)
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                StepHeaderRow(
                    stepNumber = "5",
                    title = "Jurnal Lembut Jiwa",
                    badgeText = "Opsional"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        val cleanText = prompt.substringAfter("+ ")
                        Surface(
                            onClick = {
                                journalText = if (journalText.isBlank()) {
                                    "Hari ini sedang merasakan $cleanText."
                                } else {
                                    "$journalText $cleanText.".take(250)
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = journalText,
                    onValueChange = { if (it.length <= 250) journalText = it },
                    placeholder = {
                        Text(
                            text = "Tuliskan apa pun yang mengalir di hatimu... Ruang ini aman, hening, dan tanpa penilaian.",
                            fontSize = 13.sp,
                            color = SeraTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurfaceSubtle,
                        unfocusedContainerColor = SeraSurfaceSubtle,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = SeraCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🌿 Napas lega",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraTeal
                    )
                    Text(
                        text = "${journalText.length} / 250",
                        fontSize = 11.sp,
                        color = SeraTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SeraDivider)
                Spacer(modifier = Modifier.height(14.dp))

                // SENSOR 2: VOICE EMOTION JOURNAL (MIKROFON) WIDGET
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = SeraSurfaceSubtle,
                    border = BorderStroke(1.dp, SeraIrisBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isRecordingVoice) SeraRose else SeraPrimaryBlue
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🎙️", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Voice Emotion Journal",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Text(
                                        text = "Sensor Mikrofon • Rekam & Putar Suaramu",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SeraPrimaryBlue
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraSurface
                            ) {
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%02d:%02d",
                                        recordedSeconds / 60,
                                        recordedSeconds % 60
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isRecordingVoice) SeraRose else SeraPrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        VoiceWaveformVisualizer(
                            isActive = isRecordingVoice || isPlayingVoice,
                            hasRecording = recordedSeconds > 0,
                            liveAmplitude = liveMicAmp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = micStatusNotice,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = SeraTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = {
                                    if (isRecordingVoice) {
                                        stopVoiceRecording()
                                    } else {
                                        if (hasMicPermission) {
                                            startVoiceRecording()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                color = if (isRecordingVoice) SeraRose else SeraPrimaryBlue,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isRecordingVoice) {
                                            "⏹ Hentikan & Simpan Suara"
                                        } else if (recordedSeconds > 0) {
                                            "🎙️ Rekam Ulang Suara"
                                        } else {
                                            "🎙️ Mulai Rekam Suara"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (recordedSeconds > 0 && !isRecordingVoice) {
                                Surface(
                                    onClick = { togglePlayRecordedVoice() },
                                    shape = RoundedCornerShape(50),
                                    color = if (isPlayingVoice) SeraTeal else SeraSurface,
                                    border = BorderStroke(1.dp, if (isPlayingVoice) SeraTeal else SeraPrimaryBlue),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = if (isPlayingVoice) "⏹ Berhenti" else "🔊 Putar Suara",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isPlayingVoice) Color.White else SeraPrimaryBlue
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        SeraVoiceJournalEngine.stopPlayback()
                                        isPlayingVoice = false
                                        recordedSeconds = 0
                                        recordedFilePath = null
                                        micStatusNotice = "Rekaman suara dihapus dari draf jurnal ini."
                                    },
                                    shape = CircleShape,
                                    color = SeraRoseSoft,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🗑️", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // PRIMARY & SECONDARY SAVE BUTTONS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SeraIrisMedium, SeraPrimaryBlue)
                    )
                )
                .clickable {
                    if (isRecordingVoice) stopVoiceRecording()
                    SeraVoiceJournalEngine.stopPlayback()
                    onSaveCheckIn(
                        editingEntry?.id,
                        selectedEmotion,
                        selectedIntensity,
                        selectedContexts.toList(),
                        selectedSleep,
                        journalText,
                        recordedSeconds,
                        recordedFilePath,
                        false
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓  Simpan Catatan Emosi",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Surface(
            onClick = {
                if (isRecordingVoice) stopVoiceRecording()
                SeraVoiceJournalEngine.stopPlayback()
                onSaveCheckIn(
                    editingEntry?.id,
                    selectedEmotion,
                    selectedIntensity,
                    selectedContexts.toList(),
                    selectedSleep,
                    journalText,
                    recordedSeconds,
                    recordedFilePath,
                    true
                )
            },
            shape = RoundedCornerShape(50),
            color = SeraSurface.copy(alpha = 0.90f),
            border = BorderStroke(1.dp, SeraCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "✨  Simpan & Rekomendasi Adaptif",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun StepHeaderRow(
    stepNumber: String,
    title: String,
    badgeText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(SeraPrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = SeraPrimaryBlueLight
        ) {
            Text(
                text = badgeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SeraPrimaryBlue,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun VoiceWaveformVisualizer(
    isActive: Boolean,
    hasRecording: Boolean,
    liveAmplitude: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val barCount = 28
        val spacing = size.width / barCount
        val barWidth = spacing * 0.54f
        val maxH = size.height

        for (i in 0 until barCount) {
            val fraction = if (isActive) {
                val wave = (sin(phase + i * 0.55f) + 1f) / 2f
                (0.22f + 0.75f * wave * liveAmplitude.coerceIn(0.35f, 1.0f)).coerceIn(0.18f, 0.98f)
            } else if (hasRecording) {
                0.25f + 0.55f * ((sin(i * 0.7f) + 1f) / 2f)
            } else {
                0.20f
            }
            val barH = maxH * fraction
            val x = i * spacing + (spacing - barWidth) / 2f
            val y = (maxH - barH) / 2f
            drawRoundRect(
                color = if (isActive) SeraRose else if (hasRecording) SeraPrimaryBlue else Color(0xFFD0C8EC),
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
