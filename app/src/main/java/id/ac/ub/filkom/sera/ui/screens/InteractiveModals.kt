package id.ac.ub.filkom.sera.ui.screens

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.ac.ub.filkom.sera.data.model.*
import id.ac.ub.filkom.sera.ui.components.PastelAuroraBubbleBackground
import id.ac.ub.filkom.sera.ui.components.PomiBearMascot
import id.ac.ub.filkom.sera.ui.components.PomiMascotMood
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.util.SeraSoundscapeEngine
import id.ac.ub.filkom.sera.util.SeraVoiceJournalEngine
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

// =============================================================================================
// 1. FULL-SCREEN INTERACTIVE SELF-CARE SESSIONS:
//    - SERA - Sesi Latihan Napas Kotak 4-4-4.png
//    - SERA - Panduan Dekonstruksi Tugas Mikro.png
//    - SERA - Sesi Interaktif Screen-Free Sensory Reset.png
// =============================================================================================

@Composable
fun InteractiveSelfCareSessionDialog(
    activity: SelfCareActivity,
    microTaskTitle: String,
    microTaskSteps: List<MicroTaskStep>,
    onToggleMicroStep: (Int) -> Unit,
    onUpdateMicroTaskTitle: (String) -> Unit,
    onSaveSpontaneousIdea: (String) -> Unit,
    onCompleteSession: (SelfCareActivity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PastelAuroraBubbleBackground {
            when (activity.moduleType) {
                InteractiveModuleType.BREATHING_BOX -> {
                    FullBoxBreathingSessionScreen(
                        activity = activity,
                        onComplete = { onCompleteSession(activity) },
                        onDismiss = onDismiss
                    )
                }
                InteractiveModuleType.MICRO_TASK -> {
                    FullMicroTaskDeconstructionScreen(
                        activity = activity,
                        taskTitle = microTaskTitle,
                        steps = microTaskSteps,
                        onToggleStep = onToggleMicroStep,
                        onUpdateTitle = onUpdateMicroTaskTitle,
                        onSaveIdea = onSaveSpontaneousIdea,
                        onComplete = { onCompleteSession(activity) },
                        onDismiss = onDismiss
                    )
                }
                else -> {
                    FullSensoryResetSessionScreen(
                        activity = activity,
                        onComplete = { onCompleteSession(activity) },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun FullBoxBreathingSessionScreen(
    activity: SelfCareActivity,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var isRunning by remember { mutableStateOf(true) }
    var isAudioMuted by remember { mutableStateOf(false) }
    var selectedSound by remember { mutableStateOf("Gerimis") }
    var phaseIndex by remember { mutableIntStateOf(0) } // 0: Tarik, 1: Tahan, 2: Hembuskan, 3: Jeda
    var totalSecondsRemaining by remember { mutableIntStateOf(251) } // 04:11

    val phaseBadges = listOf("🪷 TARIK NAPAS", "✨ TAHAN NAPAS", "🌬️ HEMBUSKAN", "🌿 JEDA TENANG")
    val phaseTitles = listOf(
        "Tarik napas lembut lewat hidung...",
        "Tahan napas dengan lembut di dada...",
        "Hembuskan perlahan lewat mulut...",
        "Biarkan tubuhmu beristirahat tenang..."
    )

    // Real audible soundscape playback during breathwork!
    LaunchedEffect(isRunning, isAudioMuted, selectedSound) {
        if (isRunning && !isAudioMuted) {
            SeraSoundscapeEngine.startOrSwitch(selectedSound, 0.65f)
        } else {
            SeraSoundscapeEngine.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            SeraSoundscapeEngine.stop()
        }
    }

    LaunchedEffect(isRunning) {
        var tick = 0
        while (isRunning && totalSecondsRemaining > 0) {
            delay(1000L)
            totalSecondsRemaining -= 1
            tick += 1
            if (tick % 4 == 0) {
                phaseIndex = (phaseIndex + 1) % 4
            }
        }
    }

    val ringScale by animateFloatAsState(
        targetValue = if (phaseIndex == 0 || phaseIndex == 1) 1.04f else 0.86f,
        animationSpec = tween(1800),
        label = "breath_ring_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    SeraSoundscapeEngine.stop()
                    onDismiss()
                },
                shape = CircleShape,
                color = SeraSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "✕", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                }
            }
            Text(
                text = "Active Breathwork Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Surface(
                onClick = { isAudioMuted = !isAudioMuted },
                shape = CircleShape,
                color = if (!isAudioMuted) SeraPrimaryBlueLight else SeraSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (isAudioMuted) "🔇" else "🔊", fontSize = 15.sp)
                }
            }
        }

        // Sub-header Pill + Audio Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "●  PERNAPASAN KOTAK • 4–4–4–4",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTeal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.9f)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    listOf("Gerimis", "432Hz").forEach { snd ->
                        val active = selectedSound == snd
                        Surface(
                            onClick = {
                                selectedSound = snd
                                isAudioMuted = false
                                SeraSoundscapeEngine.startOrSwitch(snd, 0.65f)
                            },
                            shape = RoundedCornerShape(50),
                            color = if (active) SeraPrimaryBlueLight else Color.Transparent
                        ) {
                            Text(
                                text = "🔊 $snd",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) SeraPrimaryBlue else SeraTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Breathing Orb Card with Living Pomi Bear Mascot inside!
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = SeraSurface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraPrimaryBlueLight
                ) {
                    Text(
                        text = phaseBadges[phaseIndex],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = (size.minDimension / 2f) * 0.88f * ringScale
                        drawCircle(
                            color = Color(0xFFD9ECFF),
                            radius = r,
                            style = Stroke(width = 24f)
                        )
                    }
                    PomiBearMascot(
                        size = 175.dp,
                        mood = PomiMascotMood.HAPPY_HEART,
                        showOuterHalo = false
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        ) {
                            append(
                                String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    totalSecondsRemaining / 60,
                                    totalSecondsRemaining % 60
                                )
                            )
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 12.sp,
                                color = SeraTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(" tersisa")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = phaseTitles[phaseIndex],
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Biarkan pundakmu rileks turun. Nikmati\nudara segar yang mengisi ruang dadamu.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = SeraTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .size(if (i == phaseIndex) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == phaseIndex) SeraPrimaryBlue else Color(0xFFD5CEF0)
                                )
                        )
                    }
                }
            }
        }

        // 2 Telemetry Cards (74 BPM & 95% Koherensi)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SeraRoseSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "♡", color = Color(0xFF9F2A56), fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraRoseSoft
                        ) {
                            Text(
                                text = "Menenang",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF9F2A56),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "74 BPM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Irama jantung stabil",
                        fontSize = 11.sp,
                        color = SeraTextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EmotionCalmBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧠", fontSize = 14.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmotionCalmBg
                        ) {
                            Text(
                                text = "Optimal",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTeal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "95% Koherensi",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keselarasan Vagus aktif",
                        fontSize = 11.sp,
                        color = SeraTextSecondary
                    )
                }
            }
        }

        // Bottom Controls: Jeda Sesi & Selesai
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = { isRunning = !isRunning },
                shape = RoundedCornerShape(50),
                color = Color(0xFF19182F),
                modifier = Modifier
                    .weight(1.6f)
                    .height(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isRunning) "⏸  Jeda Sesi" else "▶  Lanjutkan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Surface(
                onClick = onComplete,
                shape = RoundedCornerShape(50),
                color = SeraSurface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Selesai",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun FullMicroTaskDeconstructionScreen(
    activity: SelfCareActivity,
    taskTitle: String,
    steps: List<MicroTaskStep>,
    onToggleStep: (Int) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onSaveIdea: (String) -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var ideaText by remember { mutableStateOf("") }
    var microTimerSeconds by remember { mutableIntStateOf(225) } // 03:45
    var timerActive by remember { mutableStateOf(true) }

    LaunchedEffect(timerActive) {
        while (timerActive && microTimerSeconds > 0) {
            delay(1000L)
            microTimerSeconds -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = SeraSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "✕", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                }
            }
            Text(
                text = "Dekonstruksi Tugas Mikro",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Surface(
                shape = CircleShape,
                color = SeraSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "📋", fontSize = 15.sp)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = SeraPrimaryBlueLight
        ) {
            Text(
                text = "●  FOKUS 15 MENIT • ANTI–KEWALAHAN",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraPrimaryBlue,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dekonstruksi Beban",
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = SeraRoseSoft
            ) {
                Text(
                    text = "3 Langkah Mikro",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF9F2A56),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Misi Mikro Hari Ini Card
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "MISI MIKRO HARI INI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onUpdateTitle,
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurfaceSubtle,
                        unfocusedContainerColor = SeraSurfaceSubtle,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SeraSurfaceSubtle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SeraRoseSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Lvl 4", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF9F2A56))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = "Tingkat Sesak Awal", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SeraTextPrimary)
                                Text(text = "Kewalahan & Ragu", fontSize = 9.sp, color = SeraTextSecondary)
                            }
                        }
                        Text(text = "→", fontWeight = FontWeight.ExtraBold, color = SeraPrimaryBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmotionCalmBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Lvl 2", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SeraTeal)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = "Target Relaksasi", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SeraTeal)
                                Text(text = "Lega & Mengalir", fontSize = 9.sp, color = SeraTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Langkah Mikro (Step-by-step)
        val doneCount = steps.count { it.isCompleted }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Langkah Mikro (Step-by-step)",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Text(
                text = "$doneCount / ${steps.size} Selesai",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTeal
            )
        }

        steps.forEachIndexed { idx, step ->
            val isCurrentFocus = idx == 1
            Surface(
                onClick = { onToggleStep(step.id) },
                shape = RoundedCornerShape(28.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = if (isCurrentFocus) 1.8.dp else 1.dp,
                    color = if (isCurrentFocus) SeraPrimaryBlue else SeraCardBorder
                ),
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (step.isCompleted) SeraTeal else SeraPrimaryBlueLight
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (step.isCompleted) "✓" else "${idx + 1}",
                                    color = if (step.isCompleted) Color.White else SeraPrimaryBlue,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (step.isCompleted) EmotionCalmBg else SeraPrimaryBlueLight
                        ) {
                            Text(
                                text = step.durationLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (step.isCompleted) SeraTeal else SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = step.subtitle,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = SeraTextSecondary
                    )

                    if (isCurrentFocus) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = SeraSurfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "⏱ %02d:%02d",
                                            microTimerSeconds / 60,
                                            microTimerSeconds % 60
                                        ),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Text(
                                        text = "Tersisa di mikro sesi",
                                        fontSize = 10.sp,
                                        color = SeraTextSecondary
                                    )
                                }

                                Surface(
                                    onClick = { timerActive = !timerActive },
                                    shape = RoundedCornerShape(50),
                                    color = SeraPrimaryBlue
                                ) {
                                    Text(
                                        text = if (timerActive) "⏸ Fokus Sekarang" else "▷ Lanjut Fokus",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Catatan Cepat Ide Spontan
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Catatan Cepat Ide Spontan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ideaText,
                    onValueChange = { ideaText = it },
                    placeholder = {
                        Text("Tulis ide liar yang terlintas sejenak sebelum lupa...", fontSize = 12.sp)
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = { onSaveIdea(ideaText) },
                        shape = RoundedCornerShape(50),
                        color = SeraPrimaryBlueLight
                    ) {
                        Text(
                            text = "✍️ Simpan Coretan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Afirmasi SERA with Living Pomi Bear Mascot inside the circle!
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SeraSurface.copy(alpha = 0.90f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PomiBearMascot(
                    size = 68.dp,
                    mood = PomiMascotMood.HAPPY_HEART,
                    showOuterHalo = false
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AFIRMASI SERA ✨",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF9F2A56)
                    )
                    Text(
                        text = "“Kamu tidak perlu menyelesaikan semuanya sekarang, cukup langkah kecil ini saja.”",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SeraTextPrimary
                    )
                }
            }
        }

        Button(
            onClick = onComplete,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = SeraPrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "😊  Selesaikan & Evaluasi Rasa  →",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FullSensoryResetSessionScreen(
    activity: SelfCareActivity,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(581) } // 09:41
    var isRunning by remember { mutableStateOf(true) }
    var isAudioPlaying by remember { mutableStateOf(true) }
    var selectedAudio by remember { mutableStateOf("Hujan Rintik") }
    var volumeLevel by remember { mutableFloatStateOf(0.75f) }
    var activeSensoryStepIndex by remember { mutableIntStateOf(0) }

    // Real audible relaxation soundscape engine!
    LaunchedEffect(isRunning, isAudioPlaying, selectedAudio) {
        if (isRunning && isAudioPlaying) {
            SeraSoundscapeEngine.startOrSwitch(selectedAudio, volumeLevel)
        } else {
            SeraSoundscapeEngine.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            SeraSoundscapeEngine.stop()
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    SeraSoundscapeEngine.stop()
                    onDismiss()
                },
                shape = CircleShape,
                color = SeraSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "←", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                }
            }
            Text(
                text = "Sera Sesi Interaktif Screen Free",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Surface(
                onClick = { isAudioPlaying = !isAudioPlaying },
                shape = CircleShape,
                color = if (isAudioPlaying) SeraPrimaryBlueLight else SeraSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (isAudioPlaying) "🔊" else "🔇", fontSize = 14.sp)
                }
            }
        }

        // Soundscape Status Pill (Clickable to toggle Play/Mute)
        Surface(
            onClick = { isAudioPlaying = !isAudioPlaying },
            shape = RoundedCornerShape(50),
            color = SeraPrimaryBlueLight.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAudioPlaying && isRunning) {
                        "●  〰️ Soundscape Aktif: $selectedAudio • 432 Hz"
                    } else {
                        "○  〰️ Soundscape Dijeda: $selectedAudio (Ketuk Putar)"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue
                )
                Text(text = if (isAudioPlaying && isRunning) "🔊" else "🔇", fontSize = 13.sp)
            }
        }

        // Hero Card with Sleeping Pomi Bear Mascot wearing Headphones!
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = SeraSurface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, SeraCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    PomiBearMascot(
                        size = 175.dp,
                        mood = PomiMascotMood.SLEEPY_HEADPHONES,
                        showOuterHalo = true
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraSurface,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "🌙 Rileks",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF9F2A56),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        ) {
                            append(
                                String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    secondsLeft / 60,
                                    secondsLeft % 60
                                )
                            )
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 13.sp,
                                color = SeraTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(" / 10:00")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Jauhkan pandangan dari layar, tatap\npohon rindang atau bentang langit di luar\njendela.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = SeraTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraSurfaceSubtle
                ) {
                    Text(
                        text = "🌬️  Tarik napas lembut... 4 detik",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Panduan Langkah Sensori (3 Interactive Steps)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Panduan Langkah Sensori",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = SeraPrimaryBlueLight
            ) {
                Text(
                    text = "Tahap ${activeSensoryStepIndex + 1} dari 3",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        val sensorySteps = listOf(
            Triple("👁️", "1. Istirahatkan Mata", "Gunakan aturan 20–20–20. Pusatkan mata pada objek berjarak minimal 6 meter, lalu kedipkan mata perlahan 10 kali."),
            Triple("💧", "2. Basuh Wajah & Leher", "Sentuhan air sejuk memicu refleks penyelaman alami untuk memperlambat denyut jantung."),
            Triple("🧘", "3. Peregangan Bahu Lembut", "Putar kedua bahu ke belakang perlahan guna mengurai kekakuan akibat posisi duduk menatap gawai.")
        )
        sensorySteps.forEachIndexed { idx, (emoji, title, desc) ->
            val isStepActive = activeSensoryStepIndex == idx
            Surface(
                onClick = { activeSensoryStepIndex = idx },
                shape = RoundedCornerShape(28.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = if (isStepActive) 1.8.dp else 1.dp,
                    color = if (isStepActive) SeraPrimaryBlue else SeraCardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isStepActive) SeraPrimaryBlue else SeraSurfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = if (isStepActive) "Aktif • ${if (idx == 0) 4 else 3} Menit" else "Ketuk Pilih • 3 Menit",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isStepActive) SeraTeal else SeraTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = SeraTextSecondary
                        )
                    }
                }
            }
        }

        // Pilihan Audio Relaksasi (Real-Time Audio Switching & Volume!)
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = SeraSurface.copy(alpha = 0.92f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🪷 Pilihan Audio Relaksasi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = if (isAudioPlaying && isRunning) "🔊 Sedang Mengalun" else "🔇 Dijeda",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAudioPlaying && isRunning) SeraTeal else SeraTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "🌧️" to "Hujan Rintik",
                        "🌲" to "Hutan Bambu",
                        "〰️" to "432 Hz Solfeggio"
                    ).forEach { (emo, label) ->
                        val active = selectedAudio == label
                        Surface(
                            onClick = {
                                selectedAudio = label
                                isAudioPlaying = true
                                isRunning = true
                                SeraSoundscapeEngine.startOrSwitch(label, volumeLevel)
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (active) SeraPrimaryBlue else SeraSurfaceSubtle,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emo, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (active) Color.White else SeraTextPrimary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = volumeLevel,
                    onValueChange = {
                        volumeLevel = it
                        SeraSoundscapeEngine.setVolume(it)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = SeraPrimaryBlue,
                        activeTrackColor = SeraPrimaryBlue
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = { isRunning = !isRunning },
                shape = RoundedCornerShape(50),
                color = SeraSurface,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isRunning) "⏸ Jeda Sesi" else "▶ Lanjutkan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }

            Surface(
                onClick = onComplete,
                shape = RoundedCornerShape(50),
                color = SeraPrimaryBlue,
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "✓  Selesai & Catat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =============================================================================================
// 2. SERA - BANTUAN DARURAT & RUANG TENANG SOS.PNG (EMERGENCY SOS SANCTUARY)
// =============================================================================================

@Composable
fun EmergencySosSanctuaryDialog(
    trustedContacts: List<TrustedContact>,
    onReachContact: (TrustedContact) -> Unit,
    onDialEmergencyNumber: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isBreathing478 by remember { mutableStateOf(false) }
    var breath478Label by remember { mutableStateOf("TARIK") }
    var groundedSteps by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(isBreathing478) {
        while (isBreathing478) {
            breath478Label = "TARIK (4s)"
            delay(4000L)
            breath478Label = "TAHAN (7s)"
            delay(4000L)
            breath478Label = "HEMBUS (8s)"
            delay(4000L)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PastelAuroraBubbleBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = SeraSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "✕", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bantuan Darurat & Ruang Tenang SOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }

                // Hero Comfort Card with Living Pomi Bear Mascot inside the circle!
                Surface(
                    shape = RoundedCornerShape(34.dp),
                    color = SeraSurface.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, SeraCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PomiBearMascot(
                                size = 86.dp,
                                mood = PomiMascotMood.COMFORTING_SOS,
                                showOuterHalo = false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraRoseSoft
                                ) {
                                    Text(
                                        text = "● Ruang Aman Serap",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF9F2A56),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kamu Aman Di Sini 🌸",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = "Tarik napas perlahan. Sensasi ini akan berlalu, dan kamu tidak sendirian saat ini.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = SeraTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SeraSurfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "❤️", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "“Pomi ada di sini menemanimu bernapas. Satu detik demi satu detik.”",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraTextPrimary
                                )
                            }
                        }
                    }
                }

                // Napas 4-7-8 Penenang Card
                Surface(
                    shape = RoundedCornerShape(34.dp),
                    color = SeraPrimaryBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌬️  Napas 4–7–8\nPenenang",
                                fontSize = 17.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "1 Menit\nCepat",
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Biarkan detak jantungmu melambat perlahan mengikuti irama yang lembut.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            onClick = { isBreathing478 = !isBreathing478 },
                            shape = RoundedCornerShape(26.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(74.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = breath478Label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isBreathing478) "Ketuk untuk Jeda" else "Sentuh untuk Mulai Bernapas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Teknik Grounding 5-4-3-2-1
                Surface(
                    shape = RoundedCornerShape(34.dp),
                    color = SeraSurface.copy(alpha = 0.90f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🪷 Teknik Grounding 5–4–3–2–1",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = "Tap langkah selesai",
                                fontSize = 10.sp,
                                color = SeraTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val groundingItems = listOf(
                            Triple(5, "Lihat 5 benda di sekitarmu", "Sebutkan warna atau bentuknya dalam hati"),
                            Triple(4, "Rasakan 4 sentuhan kulit", "Tekstur baju, dinginnya meja, atau lantai"),
                            Triple(3, "Dengar 3 suara tenang", "Desau angin, napasmu, atau suara jauh"),
                            Triple(2, "Cium 2 aroma di udara", "Aroma ruangan, wangi teh, atau udara segar"),
                            Triple(1, "Sadar 1 rasa di lidahmu", "Teguk sedikit air hangat atau rasakan napas")
                        )

                        groundingItems.forEach { (num, title, sub) ->
                            val isChecked = groundedSteps.contains(num)
                            Surface(
                                onClick = {
                                    groundedSteps =
                                        if (isChecked) groundedSteps - num else groundedSteps + num
                                },
                                shape = RoundedCornerShape(22.dp),
                                color = SeraSurfaceSubtle,
                                border = BorderStroke(
                                    1.dp,
                                    if (isChecked) SeraTeal else SeraCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (num == 1) EmotionCalmBg else SeraPrimaryBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (num == 1) SeraTeal else SeraPrimaryBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTextPrimary
                                        )
                                        Text(
                                            text = sub,
                                            fontSize = 11.sp,
                                            color = SeraTextSecondary
                                        )
                                    }
                                    RadioButton(
                                        selected = isChecked,
                                        onClick = {
                                            groundedSteps =
                                                if (isChecked) groundedSteps - num else groundedSteps + num
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bantuan Manusia Segera
                Text(
                    text = "🎧  Bantuan Manusia Segera",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )

                val primaryContact = trustedContacts.firstOrNull()
                if (primaryContact != null) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = SeraSurface.copy(alpha = 0.92f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = primaryContact.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraRoseSoft
                                ) {
                                    Text(
                                        text = "Kontak Terpilih",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF9F2A56),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SeraSurfaceSubtle
                            ) {
                                Text(
                                    text = "“Hai ${primaryContact.name}, aku lagi ngerasa agak overwhelmed dan butuh teman mengobrol sebentar...”",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = SeraTextSecondary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = { onReachContact(primaryContact) },
                                    shape = RoundedCornerShape(50),
                                    color = SeraTeal,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "💬 Kirim WhatsApp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 11.dp)
                                    )
                                }
                                Surface(
                                    onClick = { onDialEmergencyNumber(primaryContact.phoneNumber) },
                                    shape = RoundedCornerShape(50),
                                    color = SeraPrimaryBlueLight,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "📞 Telepon Langsung",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 11.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Hotline Krisis Sejiwa 119 ext 8
                Surface(
                    onClick = { onDialEmergencyNumber("119") },
                    shape = RoundedCornerShape(26.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hotline Krisis Sejiwa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraRoseSoft
                                ) {
                                    Text(
                                        text = "119 ext 8",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraRose,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Kemenkes RI • Penanganan Darurat",
                                fontSize = 11.sp,
                                color = SeraTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF883955)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📞", fontSize = 14.sp)
                        }
                    }
                }

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    color = SeraPrimaryBlueLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓  Aku Sudah Merasa Lebih Tenang",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================================
// 3. SERA - ANALISIS POLA EMOSI MENDALAM.PNG (DEEP PATTERN EXPLORATION)
// =============================================================================================

@Composable
fun DeepEmotionPatternAnalysisDialog(
    checkInCount: Int,
    privacyState: PrivacyDataControlState,
    onTogglePatternAnalysis: (Boolean) -> Unit,
    onDeleteWeekEntries: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PastelAuroraBubbleBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = SeraSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "✕", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraRoseSoft
                    ) {
                        Text(
                            text = "🗓️ 19 – 25 Okt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraRose,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Text(
                    text = "Eksplorasi Pola Batin",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraSurface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "●  AI Engine Aktif • ${checkInCount + 15} Catatan Tervalidasi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTeal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Dual-Line Curve Card
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Dinamika Beban &\nKecemasan",
                                    fontSize = 16.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = "Korelasi ritme mingguan",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraRoseSoft
                            ) {
                                Text(
                                    text = "📈 Puncak\n+35%",
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraRose,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(text = "● Tingkat Kecemasan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SeraPrimaryBlue)
                            Text(text = "● Beban Tenggat Tugas", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF48FB1))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val p1 = Path().apply {
                                moveTo(0f, h * 0.78f)
                                cubicTo(w * 0.25f, h * 0.72f, w * 0.32f, h * 0.18f, w * 0.42f, h * 0.18f)
                                cubicTo(w * 0.55f, h * 0.18f, w * 0.70f, h * 0.75f, w, h * 0.80f)
                            }
                            val p2 = Path().apply {
                                moveTo(0f, h * 0.70f)
                                cubicTo(w * 0.25f, h * 0.65f, w * 0.32f, h * 0.12f, w * 0.42f, h * 0.12f)
                                cubicTo(w * 0.55f, h * 0.12f, w * 0.70f, h * 0.68f, w, h * 0.74f)
                            }
                            drawPath(p2, color = Color(0xFFF48FB1), style = Stroke(width = 5f, cap = StrokeCap.Round))
                            drawPath(p1, color = SeraPrimaryBlue, style = Stroke(width = 6.5f, cap = StrokeCap.Round))
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = SeraSurfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ⓘ  Lonjakan kecemasan konsisten menyertai akumulasi batas tenggat di pertengahan minggu.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = SeraTextPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Temuan Pola Spesifik SERA
                Text(
                    text = "🧠  Temuan Pola Spesifik SERA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📋  Pemicu: Tugas & Deadline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = "Muncul 8×",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeraPrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Rasa kewalahan terpicu saat akumulasi >3 tugas dalam tempo 48 jam. Diredam hingga 75% saat menerapkan dekonstruksi tugas mikro 15 menit.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = SeraTextSecondary
                        )
                    }
                }

                // Refleksi Sahabat SERA with Living Pomi Bear Mascot inside the circle!
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraPrimaryBlueLight.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, SeraCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        PomiBearMascot(
                            size = 80.dp,
                            mood = PomiMascotMood.HAPPY_HEART,
                            showOuterHalo = false
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "❝  REFLEKSI SAHABAT SERA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "“Bebanmu bukan ukuran kapasitasmu. Beri jeda 10 menit sebelum jam sibuk Rabu besok.”",
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Langkah kecil: Tarik napas 4–7–8 saat tugas pertama selesai dikirim.",
                                fontSize = 11.sp,
                                color = SeraPrimaryBlue
                            )
                        }
                    }
                }

                // Kontrol Privasi Data Mandiri
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🛡️ Kontrol Privasi Data Mandiri",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Analisis Pola Otomatis",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = "Biarkan SERA mengenali tren mingguan",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary
                                )
                            }
                            Switch(
                                checked = privacyState.allowPatternAnalysis,
                                onCheckedChange = onTogglePatternAnalysis,
                                colors = SwitchDefaults.colors(checkedTrackColor = SeraPrimaryBlue)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = onDeleteWeekEntries,
                            shape = RoundedCornerShape(50),
                            color = SeraRoseSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🗑️  Hapus Catatan Pekan Ini",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraRose,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================================
// 4. SERA - DETAIL JURNAL & REFLEKSI BATIN.PNG (WITH VOICE JOURNAL PLAYBACK)
// =============================================================================================

@Composable
fun JournalEntryDetailDialog(
    entry: EmotionCheckIn,
    onRepeatBreathwork: () -> Unit,
    onReachSupportCircle: () -> Unit,
    onEditEntry: (EmotionCheckIn) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isPlayingVoice by remember { mutableStateOf(false) }
    var biometricLocked by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            SeraVoiceJournalEngine.stopPlayback()
        }
    }

    fun toggleVoicePlayback() {
        if (isPlayingVoice) {
            SeraVoiceJournalEngine.stopPlayback()
            isPlayingVoice = false
        } else {
            isPlayingVoice = true
            SeraVoiceJournalEngine.playVoiceJournal(
                context = context,
                filePath = entry.voiceFilePath,
                fallbackSpokenText = entry.journalNote,
                durationSeconds = entry.voiceDurationSeconds.coerceAtLeast(5),
                onComplete = { isPlayingVoice = false }
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PastelAuroraBubbleBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = SeraSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "←", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                    Text(
                        text = "Detail Jurnal & Refleksi Batin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Surface(
                        onClick = { onEditEntry(entry) },
                        shape = CircleShape,
                        color = SeraSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "✎", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                }

                // Waktu Pencatatan Banner
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = SeraSurface.copy(alpha = 0.88f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Waktu Pencatatan", fontSize = 11.sp, color = SeraTextSecondary)
                            Text(
                                text = entry.timeLabel,
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
                                text = "🔒 Privat",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Main Emotion & Context Card
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SeraRoseSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = entry.emotion.emoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraRoseSoft
                                ) {
                                    Text(
                                        text = "TINGKAT ${entry.intensity}/5",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraRose,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.emotion.cardHeadline,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            entry.contexts.forEach { ctx ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraPrimaryBlueLight
                                ) {
                                    Text(
                                        text = "${ctx.iconEmoji} ${ctx.label}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SeraPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraRoseSoft
                            ) {
                                Text(
                                    text = "🌙 ${entry.sleepQuality.badgeSummary}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraRose,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Catatan Refleksi Mandiri + Voice Emotion Journal Audio Player
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "✍️  Catatan Refleksi Mandiri",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SeraSurfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“${entry.journalNote}”",
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                fontStyle = FontStyle.Italic,
                                color = SeraTextPrimary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        // Voice Emotion Journal Player Box (Always playable!)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            onClick = { toggleVoicePlayback() },
                            shape = RoundedCornerShape(22.dp),
                            color = if (isPlayingVoice) EmotionCalmBg else SeraPrimaryBlueLight.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (isPlayingVoice) "🔊" else "🎙️",
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (entry.voiceDurationSeconds > 0) {
                                                "Rekaman Voice Emotion Journal"
                                            } else {
                                                "Dengarkan Refleksi Suara SERA"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTextPrimary
                                        )
                                        Text(
                                            text = if (entry.voiceDurationSeconds > 0) {
                                                "Durasi: ${entry.voiceDurationSeconds} detik • Sensor Mikrofon"
                                            } else {
                                                "Pembacaan refleksi & harmoni 432 Hz"
                                            },
                                            fontSize = 10.sp,
                                            color = if (isPlayingVoice) SeraTeal else SeraPrimaryBlue
                                        )
                                    }
                                }
                                Surface(
                                    onClick = { toggleVoicePlayback() },
                                    shape = RoundedCornerShape(50),
                                    color = if (isPlayingVoice) SeraTeal else SeraPrimaryBlue
                                ) {
                                    Text(
                                        text = if (isPlayingVoice) "⏹ Berhenti" else "▶ Putar Suara",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Insight Personal SERA (Pomi mencatat)
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraPrimaryBlueLight.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧠  Insight Personal SERA",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraPrimaryBlue
                            ) {
                                Text(
                                    text = "AI INSIGHT ENGINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = SeraSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                PomiBearMascot(
                                    size = 56.dp,
                                    mood = PomiMascotMood.HAPPY_HEART,
                                    showOuterHalo = false
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = SeraPrimaryBlue, fontWeight = FontWeight.ExtraBold)) {
                                            append("Pomi mencatat: ")
                                        }
                                        append("Sensasi ini sering muncul saat kamu tidur kurang dari 6 jam sebelum hari bimbingan atau tenggat kuliah. Kamu sudah sangat hebat mau mengakui rasa ini dan melambat sejenak! 🌱")
                                    },
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = SeraTextPrimary
                                )
                            }
                        }
                    }
                }

                // Aktivitas Pendukung yang Dilakukan
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SeraTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🌬️", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Latihan Napas Kotak",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = "Durasi: 5 Menit • Sangat Membantu",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary
                                )
                            }
                        }
                        Surface(
                            onClick = onRepeatBreathwork,
                            shape = RoundedCornerShape(50),
                            color = SeraPrimaryBlue
                        ) {
                            Text(
                                text = "Ulangi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Privasi & Kontrol Rekaman Ini
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🛡️ Privasi & Kontrol Rekaman Ini",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )

                        Surface(
                            onClick = { biometricLocked = !biometricLocked },
                            shape = RoundedCornerShape(20.dp),
                            color = SeraSurfaceSubtle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (biometricLocked) "🔒 Terkunci dengan Biometrik" else "Kunci dengan Biometrik",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Text(
                                        text = "Wajibkan Face ID / Sidik Jari untuk membuka",
                                        fontSize = 11.sp,
                                        color = SeraTextSecondary
                                    )
                                }
                                Text(text = "›", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                            }
                        }

                        Surface(
                            onClick = onReachSupportCircle,
                            shape = RoundedCornerShape(20.dp),
                            color = EmotionCalmBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Hubungi Teman Berbagi / Dukungan",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Text(
                                        text = "Buka Lingkar Dukungan dengan kendali privasi",
                                        fontSize = 11.sp,
                                        color = SeraTeal
                                    )
                                }
                                Text(text = "↗", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SeraTeal)
                            }
                        }

                        Surface(
                            onClick = { onDeleteEntry(entry.id) },
                            shape = RoundedCornerShape(20.dp),
                            color = SeraRoseSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Hapus Catatan Ini Saja",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraRose
                                    )
                                    Text(
                                        text = "Hapus riwayat tanggal ini tanpa merusak data lainnya",
                                        fontSize = 11.sp,
                                        color = SeraTextSecondary
                                    )
                                }
                                Text(text = "🗑️", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================================
// 5. WEEKLY REFLECTION REPORT DIALOG
// =============================================================================================

@Composable
fun WeeklyReflectionReportDialog(
    report: WeeklyReflectionReportData,
    onSelectNextWeekCommitment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var chosenCommitment by remember(report.selectedNextWeekCommitment) {
        mutableStateOf(report.selectedNextWeekCommitment)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = SeraSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraPrimaryBlueLight
                    ) {
                        Text(
                            text = "📋 LAPORAN REFLEKSI MINGGUAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = SeraSurfaceSubtle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ringkasan Perjalanan Batinmu",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Text(
                    text = report.periodLabel,
                    fontSize = 12.sp,
                    color = SeraTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SeraSurfaceSubtle,
                    border = BorderStroke(1.dp, SeraCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = report.narrativeReflection,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontStyle = FontStyle.Italic,
                            color = SeraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "✨ Self-Care Terampuh: ${report.mostHelpfulActivityTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = report.guidingQuestion,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                report.availableCommitments.forEach { commitment ->
                    val isSelected = chosenCommitment == commitment
                    Surface(
                        onClick = { chosenCommitment = commitment },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) SeraPrimaryBlueLight else SeraSurfaceSubtle,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SeraPrimaryBlue else SeraCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSelected) "◉" else "○",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SeraPrimaryBlue else SeraTextSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = commitment,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = SeraTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSelectNextWeekCommitment(chosenCommitment)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = SeraPrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "✓  Simpan Rencana Minggu Depan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =============================================================================================
// 6. SERA - KONFIRMASI DUKUNGAN & PRIVASI AMAN.PNG
// =============================================================================================

@Composable
fun SupportCircleConfirmDialog(
    contact: TrustedContact,
    userName: String = "Nur",
    userIdentitySubtitle: String = "Mahasiswi Tingkat Akhir • TIF FILKOM UB",
    onConfirmLaunchWhatsApp: (TrustedContact, String) -> Unit,
    onDismiss: () -> Unit
) {
    val structuredTemplates = remember(contact, userName, userIdentitySubtitle) {
        listOf(
            "Halo ${contact.name} 👋,\n\nPerkenalkan identitas saya:\n• Nama: $userName\n• Identitas: $userIdentitySubtitle\n\nSaya menghubungi melalui aplikasi SERA (Self-Awareness, Emotion, & Resilience Assistant). Saat ini saya sedang butuh teman bercerita / dukungan.\n\nAlasan menghubungi: ",
            "Halo ${contact.name}, selamat pagi/siang/sore 🙏.\n\nIdentitas Mahasiswa:\n• Nama: $userName\n• Status: $userIdentitySubtitle\n\nSaya ingin berdiskusi / berkonsultasi sebentar terkait kondisi emosi & perkuliahan saya melalui SERA.\n\nAlasan menghubungi: "
        )
    }

    var selectedTemplate by remember(structuredTemplates) {
        mutableStateOf(structuredTemplates.first())
    }
    var reasonInput by remember { mutableStateOf("") }

    val finalPreviewMessage = remember(selectedTemplate, reasonInput) {
        if (reasonInput.isBlank()) {
            selectedTemplate + "..."
        } else {
            selectedTemplate + reasonInput.trim()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = SeraSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Online Pill
                Box(contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SeraRoseSoft)
                            .border(3.dp, SeraSurface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.initials,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraRose
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraTeal
                    ) {
                        Text(
                            text = "● +62 812-2590-5930",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${contact.name} 💜",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Text(
                    text = "${contact.badgeText} • WhatsApp Direct (+6281225905930)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeraPrimaryBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SeraSurfaceSubtle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🔒 Konfirmasi Koneksi WhatsApp & Identitas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTeal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("SERA akan langsung membuka WhatsApp ke nomor ")
                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = SeraPrimaryBlue)) {
                                    append("+6281225905930 ")
                                }
                                append("dengan sapaan Halo, identitas lengkapmu, dan alasan menghubungi.")
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = SeraTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pilih Format Sapaan & Identitas:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = "Otomatis Terisi",
                        fontSize = 10.sp,
                        color = SeraTeal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                structuredTemplates.forEachIndexed { index, tmpl ->
                    val isSelected = selectedTemplate == tmpl
                    Surface(
                        onClick = { selectedTemplate = tmpl },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) SeraPrimaryBlueLight else SeraSurfaceSubtle,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) SeraPrimaryBlue else SeraCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = if (index == 0) "💬" else "🎓", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tmpl + if (reasonInput.isBlank()) "..." else reasonInput,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = SeraTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Tulis alasan menghubungi (opsional, bisa lanjut di WA)", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Butuh teman cerita soal tugas akhir...", fontSize = 11.sp) },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurfaceSubtle,
                        unfocusedContainerColor = SeraSurfaceSubtle,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = SeraCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        color = SeraSurfaceSubtle,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Tetap di SERA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        }
                    }

                    Surface(
                        onClick = { onConfirmLaunchWhatsApp(contact, finalPreviewMessage) },
                        shape = RoundedCornerShape(50),
                        color = SeraPrimaryBlue,
                        modifier = Modifier
                            .weight(1.45f)
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "➢  Kirim via WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ⓘ Langsung menuju WhatsApp +6281225905930",
                    fontSize = 10.sp,
                    color = SeraTextSecondary
                )
            }
        }
    }
}

// =============================================================================================
// 7. SERA - NOTIFIKASI & AKSES CEPAT DUKUNGAN.PNG
// =============================================================================================

@Composable
fun NotificationAndSupportSheetDialog(
    notifications: List<SeraNotificationItem>,
    trustedContacts: List<TrustedContact>,
    onSelectSupportContact: (TrustedContact) -> Unit,
    onOpenWeeklyReport: () -> Unit,
    onStartSensoryReset: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PastelAuroraBubbleBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = SeraSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "←", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Notifikasi & Pengingat Jiwa",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }

                // Top Hero Card with Living Pomi Bear Mascot
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraSurface.copy(alpha = 0.90f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PomiBearMascot(
                            size = 76.dp,
                            mood = PomiMascotMood.HAPPY_HEART,
                            showOuterHalo = false
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraRoseSoft
                            ) {
                                Text(
                                    text = "● ${notifications.count { it.isUnread }} Pengingat Hari Ini",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraRose,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notifikasi & Pengingat\nJiwa",
                                fontSize = 19.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = "Ruang tenang untuk terhubung & bernapas sejenak",
                                fontSize = 11.sp,
                                color = SeraTextSecondary
                            )
                        }
                    }
                }

                // Akses Cepat Support Circle Card
                Surface(
                    shape = RoundedCornerShape(34.dp),
                    color = SeraPrimaryBlueLight.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, SeraCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🤲  Akses Cepat Support Circle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Butuh Teman Bicara Sekarang?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Text(
                            text = "Lingkaran dukunganmu siap mendengarkan tanpa menghakimi.",
                            fontSize = 12.sp,
                            color = SeraTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        trustedContacts.forEachIndexed { idx, contact ->
                            val isService = contact.isVerifiedService
                            Surface(
                                onClick = {
                                    onDismiss()
                                    onSelectSupportContact(contact)
                                },
                                shape = RoundedCornerShape(24.dp),
                                color = SeraSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isService) EmotionCalmBg else SeraRoseSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isService) "🛡️" else contact.initials,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isService) SeraTeal else SeraRose
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = contact.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SeraTextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (isService) EmotionCalmBg else SeraPrimaryBlueLight
                                            ) {
                                                Text(
                                                    text = contact.badgeText,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isService) SeraTeal else SeraPrimaryBlue,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = contact.availabilityText,
                                            fontSize = 11.sp,
                                            color = SeraTextSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = when {
                                            isService -> SeraTeal
                                            idx == 0 -> SeraPrimaryBlue
                                            else -> SeraPrimaryBlueLight
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                isService -> "🗓️ Buat Janji"
                                                idx == 0 -> "🔒 Kirim Pesan"
                                                else -> "💬 WhatsApp"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (idx == 1 && !isService) SeraPrimaryBlue else Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🛡️ Dialog konfirmasi privasi akan muncul sebelum aplikasi pesan eksternal terbuka. Catatan emosimu tetap aman.",
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = SeraTextSecondary
                        )
                    }
                }

                // Pengingat Lembut Hari Ini
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengingat Lembut Hari\nIni",
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    TextButton(onClick = onMarkAllRead) {
                        Text(
                            text = "Tandai Sudah\nDibaca",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeraPrimaryBlue
                        )
                    }
                }

                notifications.forEachIndexed { idx, notif ->
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = SeraSurface.copy(alpha = 0.92f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (idx == 0) SeraRoseSoft else SeraPrimaryBlueLight
                                ) {
                                    Text(
                                        text = notif.categoryBadge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (idx == 0) SeraRose else SeraPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(text = notif.timeLabel, fontSize = 10.sp, color = SeraTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = notif.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.body,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = SeraTextSecondary
                            )
                            if (idx < 2) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        onClick = {
                                            onDismiss()
                                            if (idx == 0) onStartSensoryReset() else onOpenWeeklyReport()
                                        },
                                        shape = RoundedCornerShape(50),
                                        color = if (idx == 0) Color(0xFF7B3B54) else SeraPrimaryBlue
                                    ) {
                                        Text(
                                            text = notif.actionButtonText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================================
// 8. SERA - TAMBAH KONTAK TERPERCAYA BARU.PNG
// =============================================================================================

@Composable
fun TrustedContactEditorDialog(
    editingContact: TrustedContact?,
    onSave: (editingId: String?, name: String, badge: String, relation: String, availability: String, phone: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(editingContact) { mutableStateOf(editingContact?.name ?: "") }
    var selectedCategory by remember(editingContact) {
        mutableStateOf(editingContact?.badgeText ?: "Sahabat Dekat")
    }
    var phone by remember(editingContact) {
        mutableStateOf(editingContact?.phoneNumber?.removePrefix("62") ?: "81225905930")
    }
    var selectedTemplateStyle by remember { mutableStateOf("💭 Cerita Santai") }
    var templateBody by remember(name, selectedTemplateStyle) {
        val targetName = name.ifBlank { "[Nama]" }
        mutableStateOf(
            when (selectedTemplateStyle) {
                "☕ Ajak Ketemu" -> "Hai $targetName, sore ini luang kah? Yuk ngopi atau duduk santai sebentar di sekitar kampus..."
                "🆘 Butuh Teman" -> "Hai $targetName, aku lagi ngerasa cukup kewalahan hari ini. Boleh minta waktumu sebentar untuk cerita?"
                else -> "Hai $targetName, lagi senggang kah? Boleh ngobrol bentar, aku lagi butuh teman cerita santai..."
            }
        )
    }
    var isPrioritySos by remember(editingContact) {
        mutableStateOf(editingContact?.isPrioritySos ?: true)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PastelAuroraBubbleBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = SeraSurface,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "←", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                        }
                    }
                    Text(
                        text = if (editingContact != null) "Ubah Kontak Terpercaya" else "Tambah Kontak Terpercaya",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraRoseSoft
                    ) {
                        Text(
                            text = "🛡️ 100% Privat",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraRose,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Ruang Aman Tanpa Beban Card
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = SeraSurface.copy(alpha = 0.90f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SeraPrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "💜", fontSize = 17.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ruang Aman Tanpa Beban",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kontak ini hanya akan dihubungi jika kamu memilihnya saat butuh teman bicara atau bantuan darurat. Catatan emosimu tidak akan dibagikan secara otomatis.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = SeraTextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = "Nama Panggilan / Alias  (Wajib)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Contoh: Kak Sarah / Dian") },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurface,
                        unfocusedContainerColor = SeraSurface,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = SeraCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Kategori Hubungan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )

                val categories = listOf(
                    "🫂 Sahabat Dekat",
                    "👨‍👩‍👧 Keluarga",
                    "💬 Teman Cerita Sebaya",
                    "🎧 Konselor / Wali"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { cat ->
                                val cleanCat = cat.substringAfter(" ")
                                val active = selectedCategory == cleanCat
                                Surface(
                                    onClick = { selectedCategory = cleanCat },
                                    shape = RoundedCornerShape(50),
                                    color = if (active) SeraPrimaryBlue else SeraSurface,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (active) Color.White else SeraTextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Nomor WhatsApp Aktif",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraSurface
                    ) {
                        Text(
                            text = "+62 ▾",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)
                        )
                    }
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SeraSurface,
                            unfocusedContainerColor = SeraSurface,
                            focusedBorderColor = SeraPrimaryBlue,
                            unfocusedBorderColor = SeraCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Template Pesan WhatsApp",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("💭 Cerita Santai", "☕ Ajak Ketemu", "🆘 Butuh Teman").forEach { style ->
                        val active = selectedTemplateStyle == style
                        Surface(
                            onClick = { selectedTemplateStyle = style },
                            shape = RoundedCornerShape(50),
                            color = if (active) SeraPrimaryBlueLight else SeraSurface
                        ) {
                            Text(
                                text = style,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) SeraPrimaryBlue else SeraTextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = templateBody,
                    onValueChange = { templateBody = it },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurface,
                        unfocusedContainerColor = SeraSurface,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = SeraCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Kontak Prioritas SOS
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = SeraSurface.copy(alpha = 0.90f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✳️  Kontak Prioritas SOS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = "Muncul di akses cepat saat situasi genting",
                                fontSize = 11.sp,
                                color = SeraTextSecondary
                            )
                        }
                        Switch(
                            checked = isPrioritySos,
                            onCheckedChange = { isPrioritySos = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = SeraPrimaryBlue)
                        )
                    }
                }

                Button(
                    onClick = {
                        val digits = phone.filter { it.isDigit() }
                        val normalizedPhone = if (digits.startsWith("62")) digits else "62$digits"
                        onSave(
                            editingContact?.id,
                            name.ifBlank { "Kak Sarah" },
                            selectedCategory,
                            "$selectedCategory • WhatsApp",
                            "Tersedia via WhatsApp",
                            normalizedPhone
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = SeraPrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "👥+  Simpan ke Lingkar Dukungan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
