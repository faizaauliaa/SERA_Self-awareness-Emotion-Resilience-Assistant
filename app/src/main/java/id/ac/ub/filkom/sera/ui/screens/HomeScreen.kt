package id.ac.ub.filkom.sera.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ub.filkom.sera.data.model.*
import id.ac.ub.filkom.sera.ui.components.PomiBearMascot
import id.ac.ub.filkom.sera.ui.components.PomiMascotMood
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.viewmodel.SeraUiState

@Composable
fun HomeScreen(
    uiState: SeraUiState,
    onQuickEmotionSelect: (EmotionType) -> Unit,
    onOpenFullJournal: (EmotionType) -> Unit,
    onOpenPatternHistory: () -> Unit,
    onOpenDeepAnalysis: () -> Unit,
    onOpenAllSelfCare: () -> Unit,
    onStartSelfCareSession: (SelfCareActivity) -> Unit,
    onSelectSupportContact: (TrustedContact) -> Unit,
    onMascotGreeting: (String) -> Unit
) {
    var localPomiSpeech by remember {
        mutableStateOf("Ketuk Pomi kapan saja! Seluruh tubuh, tangan & kaki Pomi hidup 💜")
    }
    var selectedRhythmDayIdx by remember { mutableIntStateOf(6) }
    var showHomeCapacityFilter by remember { mutableStateOf(false) }
    var homeDurationFilter by remember { mutableIntStateOf(0) } // 0 = Semua, 5 = <=5m, 10 = 10m, 15 = 15m

    val sevenDayCapsules = remember(uiState.checkInHistory, uiState.todayQuickEmotion) {
        listOf(
            Triple("Sen", 0.56f, EmotionType.TENANG),
            Triple("Sel", 0.68f, EmotionType.SENANG),
            Triple("Rab", 0.46f, EmotionType.SEDIH),
            Triple("Kam", 0.88f, EmotionType.TENANG),
            Triple("Jum", 0.70f, EmotionType.CEMAS),
            Triple("Sab", 0.52f, EmotionType.TENANG),
            Triple("Min", 0.24f, uiState.todayQuickEmotion)
        )
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. FULL-VIEWPORT HERO SANCTUARY WITH LIVING PURPLE BEAR MASCOT (POMI)
        //    (Fills the entire initial Beranda screen so Check-in Emosi Cepat is only visible upon scrolling!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillParentMaxHeight(0.96f)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Streak Capsule
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, SeraCardBorder),
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = "✨  Senin, 24 Okt • Hari Ke-${uiState.streakDays} Streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }

                // Center Big Mascot Halo + Floating Badges + Speech Bubble + Greeting
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(275.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PomiBearMascot(
                            size = 265.dp,
                            mood = PomiMascotMood.HAPPY_HEART,
                            showOuterHalo = true,
                            onMascotTapped = { msg ->
                                localPomiSpeech = msg
                                onMascotGreeting(msg)
                            }
                        )

                        // Top-Left Floating Pill: "✨ Pomi Siap Mendengar"
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraSurface,
                            shadowElevation = 5.dp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 4.dp, y = 14.dp)
                        ) {
                            Text(
                                text = "✨ Pomi Siap Mendengar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Bottom-Right Floating Pill: "🌿 Rileks"
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraSurface,
                            shadowElevation = 5.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-14).dp)
                        ) {
                            Text(
                                text = "🌿 ${uiState.todayQuickEmotion.label}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTeal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Interactive Speech Bubble from Pomi
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = SeraSurface.copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, SeraIrisBorder),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = localPomiSpeech,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeraPrimaryBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Halo, ${uiState.userName}! ✨",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ambil satu napas tenang bersama SERA\nhari ini. Hatimu berharga.",
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = SeraTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                // Bottom Gentle Scroll Prompt (Check-in Emosi Cepat is below the fold!)
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(1)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = SeraSurface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, SeraCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⌄  Gulir ke bawah untuk Check-in Emosi Cepat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue
                        )
                    }
                }
            }
        }

        // 2. CHECK-IN EMOSI CEPAT (ORGANIC BUBBLES + VOICE JOURNAL LAUNCHER)
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 3.dp,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "♡", fontSize = 15.sp, color = SeraPrimaryBlue, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CHECK-IN EMOSI CEPAT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraSurfaceSubtle
                        ) {
                            Text(
                                text = "1–Ketukan",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeraTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bagaimana perasaanmu saat ini?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih gelembung suasana hati yang paling\nmewakili energimu hari ini.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = SeraTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5 Organic Pill Mood Bubbles
                    val orderedEmotions = listOf(
                        EmotionType.SENANG,
                        EmotionType.TENANG,
                        EmotionType.BIASA,
                        EmotionType.SEDIH,
                        EmotionType.CEMAS
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        orderedEmotions.forEach { emotion ->
                            val isSelected = uiState.todayQuickEmotion == emotion
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (isSelected) emotion.bgColor else SeraSurfaceSubtle
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) emotion.borderColor else SeraCardBorder,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { onQuickEmotionSelect(emotion) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(emotion.bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emotion.emoji, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = emotion.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) emotion.accentColor else SeraTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Purple Gradient "Dengarkan Hatiku & Simpan" Button (Opens Catat & Voice Emotion Journal)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        SeraPrimaryBlue,
                                        SeraIrisMedium
                                    )
                                )
                            )
                            .clickable { onOpenFullJournal(uiState.todayQuickEmotion) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎙️", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dengarkan Hatiku & Simpan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Soft Mint Confirmation Capsule
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = EmotionCalmBg,
                        border = BorderStroke(1.dp, EmotionCalmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🌿", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Tercatat: ")
                                    withStyle(
                                        SpanStyle(
                                            color = SeraTeal,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    ) {
                                        append("${uiState.todayQuickEmotion.label} ")
                                    }
                                    append("pagi ini. Sangat indah memberi jeda untuk dirimu.")
                                },
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = SeraTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. RITME ENERGIMU (ORGANIC CAPSULE PILLARS — REKAP 7 HARI TERAKHIR)
        item {
            val calmPct = uiState.weeklyReport?.calmHarmonyPercentage ?: 68
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SeraPrimaryBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "📊", fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ritme Energimu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = "Rekap 7 Hari Terakhir",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary
                                )
                            }
                        }

                        Surface(
                            onClick = onOpenPatternHistory,
                            shape = RoundedCornerShape(50),
                            color = EmotionCalmBg
                        ) {
                            Text(
                                text = "● $calmPct% Seimbang",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTeal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 7 Organic Capsule Pillars (Interactive per day!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        sevenDayCapsules.forEachIndexed { idx, (dayLabel, heightFrac, _) ->
                            val isSelectedDay = idx == selectedRhythmDayIdx
                            val pillarColor = when {
                                isSelectedDay -> SeraPrimaryBlue
                                idx == 1 -> EmotionHappyBg
                                idx == 2 -> SeraRoseSoft
                                idx == 4 -> SeraPrimaryBlueLight
                                else -> EmotionCalmBg
                            }
                            val dotColor = when {
                                isSelectedDay -> Color.White
                                idx == 1 -> EmotionHappyAccent
                                idx == 2 -> SeraRose
                                idx == 4 -> SeraPrimaryBlue
                                else -> SeraTeal
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedRhythmDayIdx = idx },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction = heightFrac)
                                        .clip(RoundedCornerShape(50))
                                        .background(pillarColor),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelectedDay) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelectedDay) SeraPrimaryBlue else SeraTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    val activeDayTuple = sevenDayCapsules[selectedRhythmDayIdx]
                    Surface(
                        onClick = onOpenPatternHistory,
                        shape = RoundedCornerShape(18.dp),
                        color = SeraSurfaceSubtle,
                        border = BorderStroke(1.dp, SeraCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeDayTuple.third.emoji} Hari ${activeDayTuple.first}: ${activeDayTuple.third.label} (Kapasitas ${(activeDayTuple.second * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeraTextPrimary
                            )
                            Text(
                                text = "Detail Pola ›",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        // 4. SERA EMPATHY AI CARD (MATCHING BERANDA IMPIAN MOCKUP)
        item {
            if (uiState.privacyState.allowPatternAnalysis) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = SeraSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, SeraCardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SeraPrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🧠", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "SERA EMPATHY AI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = "Pola Pekan Kuliah",
                                        fontSize = 10.sp,
                                        color = SeraTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Korelasi Tenggat & Napas Diafragma",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Menjelang tenggat skripsi & praktikum, ketegangan fisikmu meningkat. Latihan relaksasi 5 menit konsisten ")
                                withStyle(
                                    SpanStyle(
                                        color = SeraTeal,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) {
                                    append("menurunkan denyut cemas 34%")
                                }
                                append(".")
                            },
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = SeraTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Inner Sparkline Card (-34% Terkendali)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Penurunan Stres",
                                        fontSize = 11.sp,
                                        color = SeraTextSecondary
                                    )
                                    Text(
                                        text = "-34%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraRose
                                    )
                                    Text(
                                        text = "Terkendali",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraRose
                                    )
                                }

                                // Smooth Mini Sparkline Wave
                                Canvas(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(45.dp)
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    val path = Path().apply {
                                        moveTo(w * 0.05f, h * 0.78f)
                                        cubicTo(
                                            w * 0.30f, h * 0.72f,
                                            w * 0.45f, h * 0.35f,
                                            w * 0.65f, h * 0.58f
                                        )
                                        cubicTo(
                                            w * 0.80f, h * 0.72f,
                                            w * 0.88f, h * 0.25f,
                                            w * 0.94f, h * 0.22f
                                        )
                                    }
                                    drawPath(
                                        path = path,
                                        color = SeraRose,
                                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                                    )
                                    drawCircle(
                                        color = SeraPrimaryBlue,
                                        radius = 10f,
                                        center = Offset(w * 0.94f, h * 0.22f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Lihat Analisis Mendalam  →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenDeepAnalysis)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 5. REKOMENDASI ADAPTIF HEADER + INTERACTIVE CAPACITY FILTER
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rekomendasi Adaptif",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Text(
                            text = "Langkah kecil disesuaikan kapasitas energimu",
                            fontSize = 12.sp,
                            color = SeraTextSecondary
                        )
                    }
                    Surface(
                        onClick = { showHomeCapacityFilter = !showHomeCapacityFilter },
                        shape = CircleShape,
                        color = if (showHomeCapacityFilter || homeDurationFilter != 0) SeraPrimaryBlue else SeraSurface.copy(alpha = 0.9f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⇄",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showHomeCapacityFilter || homeDurationFilter != 0) Color.White else SeraPrimaryBlue
                            )
                        }
                    }
                }

                if (showHomeCapacityFilter) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "Semua",
                            5 to "⚡ 5 Menit",
                            10 to "🎧 10 Menit",
                            15 to "📚 15 Menit"
                        ).forEach { (dur, label) ->
                            val selected = homeDurationFilter == dur
                            Surface(
                                onClick = { homeDurationFilter = dur },
                                shape = RoundedCornerShape(50),
                                color = if (selected) SeraPrimaryBlue else SeraSurface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, if (selected) SeraPrimaryBlue else SeraCardBorder)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selected) Color.White else SeraTextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. REKOMENDASI ADAPTIF ORGANIC CAPSULE CARDS
        val homeFilteredRecs = if (homeDurationFilter == 0) {
            uiState.adaptiveRecommendations.take(3)
        } else {
            uiState.adaptiveRecommendations.filter { it.durationMinutes == homeDurationFilter }
                .ifEmpty { uiState.adaptiveRecommendations.take(2) }
        }
        items(homeFilteredRecs, key = { it.id }) { activity ->
            OrganicHomeSelfCareCard(
                activity = activity,
                onStartClick = { onStartSelfCareSession(activity) }
            )
        }

        // 7. BUTUH TEMAN CERITA? (LINGKAR DUKUNGAN)
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = SeraSurface.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🫂", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Butuh Teman Cerita?",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(SeraTeal)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kamu tidak perlu memikul segalanya sendirian. Ruang aman ini selalu siap mendengarkan tanpa menghakimi.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = SeraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val contacts = uiState.trustedContacts.take(2)
                        contacts.forEachIndexed { idx, contact ->
                            val isCounselor = contact.isVerifiedService || idx == 0
                            Surface(
                                onClick = { onSelectSupportContact(contact) },
                                shape = RoundedCornerShape(22.dp),
                                color = SeraSurfaceSubtle,
                                border = BorderStroke(1.dp, SeraCardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isCounselor) SeraRoseSoft else SeraPrimaryBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isCounselor) "🎧" else "💬",
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = contact.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = contact.availabilityText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCounselor) SeraTeal else SeraTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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

@Composable
private fun OrganicHomeSelfCareCard(
    activity: SelfCareActivity,
    onStartClick: () -> Unit
) {
    val isBreathing = activity.moduleType == InteractiveModuleType.BREATHING_BOX
    val isMicro = activity.moduleType == InteractiveModuleType.MICRO_TASK

    val iconBoxBg = when {
        isBreathing -> SeraRoseSoft
        isMicro -> SeraPrimaryBlueLight
        else -> EmotionCalmBg
    }
    val iconEmoji = when {
        isBreathing -> "🌬️"
        isMicro -> "☑️"
        else -> "🚶"
    }
    val durationTagBg = when {
        isBreathing -> SeraRoseSoft
        isMicro -> SeraPrimaryBlueLight
        else -> EmotionCalmBg
    }
    val durationTagText = when {
        isBreathing -> SeraRose
        isMicro -> SeraPrimaryBlue
        else -> SeraTeal
    }

    Surface(
        onClick = onStartClick,
        shape = RoundedCornerShape(28.dp),
        color = SeraSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, SeraCardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(iconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = durationTagBg
                    ) {
                        Text(
                            text = "${activity.durationMinutes} Menit",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = durationTagText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isBreathing) EmotionCalmBg else Color.Transparent
                    ) {
                        Text(
                            text = activity.socialProofText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isBreathing || !isMicro) SeraTeal else SeraRose,
                            modifier = Modifier.padding(
                                horizontal = if (isBreathing) 8.dp else 2.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activity.shortTitle.ifBlank { activity.title },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = SeraTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Circular Play / Arrow / Headphone Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isBreathing) SeraPrimaryBlue else SeraSurfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isBreathing -> "▶"
                        isMicro -> "›"
                        else -> "🎧"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBreathing) Color.White else SeraPrimaryBlue
                )
            }
        }
    }
}
