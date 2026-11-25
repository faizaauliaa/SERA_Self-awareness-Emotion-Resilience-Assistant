package id.ac.ub.filkom.sera.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ub.filkom.sera.data.model.ActivityContext
import id.ac.ub.filkom.sera.data.model.EmotionCheckIn
import id.ac.ub.filkom.sera.data.model.EmotionType
import id.ac.ub.filkom.sera.ui.components.EmotionBezierTrendChart
import id.ac.ub.filkom.sera.ui.components.PomiBearMascot
import id.ac.ub.filkom.sera.ui.components.PomiMascotMood
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.util.SeraVoiceJournalEngine
import id.ac.ub.filkom.sera.viewmodel.SeraUiState
import java.util.Locale

@Composable
fun HistoryPatternScreen(
    uiState: SeraUiState,
    onSelectPeriod: (String) -> Unit,
    onSelectCalendarDay: (Int) -> Unit,
    onSearchAndFilterChange: (String, ActivityContext?, EmotionType?) -> Unit,
    onOpenJournalDetail: (EmotionCheckIn) -> Unit,
    onEditEntry: (EmotionCheckIn) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onToggleAppreciate: (String) -> Unit,
    onOpenWeeklyReport: () -> Unit,
    onOpenDeepAnalysis: () -> Unit
) {
    val context = LocalContext.current
    var selectedChartNodeIndex by remember(uiState.selectedPatternPeriod) {
        mutableIntStateOf(
            when (uiState.selectedPatternPeriod) {
                "Harian" -> 1
                "Bulanan" -> 2
                else -> 3
            }
        )
    }
    var calendarPageIndex by remember { mutableIntStateOf(1) } // 0: W1-W2 (1..14 Okt), 1: W3-W4 (14..27 Okt)
    var currentlyPlayingEntryId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            SeraVoiceJournalEngine.stopPlayback()
        }
    }

    val period = uiState.selectedPatternPeriod
    val dynamicAvgIntensity = remember(period, uiState.checkInHistory) {
        when (period) {
            "Harian" -> 2.5f
            "Bulanan" -> 2.6f
            else -> {
                if (uiState.checkInHistory.isEmpty()) 2.8f
                else uiState.checkInHistory.take(7).map { it.intensity }.average().toFloat()
            }
        }
    }
    val dynamicRangeBadge = when (period) {
        "Harian" -> "Hari Ini • 24 Okt"
        "Bulanan" -> "1 – 31 Okt 2026"
        else -> "19 – 25 Okt"
    }
    val dynamicDeltaText = when (period) {
        "Harian" -> "📉 -0.6 usai napas kotak"
        "Bulanan" -> "📉 -0.8 vs September"
        else -> "📉 -0.4 vs minggu lalu"
    }
    val dominantEmotion = uiState.weeklyReport?.dominantEmotion ?: EmotionType.TENANG
    val dynamicDominantSub = when (period) {
        "Harian" -> "● 4 sesi tenang hari ini"
        "Bulanan" -> "● Tercatat 19 hari damai"
        else -> "● Tercatat ${uiState.weeklyReport?.dominantDaysCount ?: 4} hari damai"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. HEADER: ALUR DINAMIKA RASA + SHARE BUTTON
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraSurface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, SeraCardBorder)
                    ) {
                        Text(
                            text = "🌸  Peta Hatimu Pekan Ini  ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share Pattern Report Button
                        Surface(
                            onClick = {
                                val shareText = "🌸 Ringkasan Peta Pola Emosi SERA ($period - $dynamicRangeBadge):\n" +
                                    "• Rata-rata Intensitas: ${String.format(Locale.US, "%.1f", dynamicAvgIntensity)}/5.0 ($dynamicDeltaText)\n" +
                                    "• Emosi Dominan: ${dominantEmotion.label} ($dynamicDominantSub)\n" +
                                    "• Total Catatan Tersimpan: ${uiState.checkInHistory.size} refleksi\n" +
                                    "Dirawat dengan lembut bersama SERA & Pomi 💜"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Ringkasan Peta Pola Emosi SERA")
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Ringkasan Pola Emosi"))
                            },
                            shape = CircleShape,
                            color = SeraSurface.copy(alpha = 0.92f),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📤", fontSize = 15.sp)
                            }
                        }

                        // Deep Analysis Button
                        Surface(
                            onClick = onOpenDeepAnalysis,
                            shape = CircleShape,
                            color = SeraSurface.copy(alpha = 0.92f),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📊", fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Alur Dinamika Rasa",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Memahami dirimu dengan penuh penerimaan\ndan kasih 🕊️",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = SeraTextSecondary
                )
            }
        }

        // 2. SEGMENTED PERIOD SWITCHER (Harian | Mingguan | Bulanan)
        item {
            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, SeraCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Harian", "Mingguan", "Bulanan").forEach { itemPeriod ->
                        val isSelected = period == itemPeriod
                        Surface(
                            onClick = { onSelectPeriod(itemPeriod) },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) SeraPrimaryBlue else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = itemPeriod,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else SeraTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. GELOMBANG PERASAAN + DYNAMIC BEZIER CHART CARD
        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 3.dp,
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
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(SeraPrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gelombang $period",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraPrimaryBlueLight
                        ) {
                            Text(
                                text = dynamicRangeBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Two Organic Metric Cards Side-by-Side (Dynamic per period!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Rata-rata Intensitas",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SeraPrimaryBlue
                                            )
                                        ) {
                                            append(String.format(Locale.US, "%.1f", dynamicAvgIntensity))
                                        }
                                        withStyle(
                                            SpanStyle(
                                                fontSize = 12.sp,
                                                color = SeraTextSecondary
                                            )
                                        ) {
                                            append(" / 5.0")
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dynamicDeltaText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTeal
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Emosi Dominan",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌸", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dominantEmotion.label,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dynamicDominantSub,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraTeal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    EmotionBezierTrendChart(
                        period = period,
                        entries = uiState.checkInHistory,
                        selectedIndex = selectedChartNodeIndex,
                        onPointSelected = { idx ->
                            selectedChartNodeIndex = idx
                            if (period == "Mingguan") {
                                val weekRecent = uiState.checkInHistory.take(7).reversed()
                                val mappedDay = weekRecent.getOrNull(idx)?.dayOfMonth ?: (18 + idx)
                                onSelectCalendarDay(mappedDay)
                            }
                        }
                    )
                }
            }
        }

        // 4. MATRIKS GELEMBUNG MOOD (INTERACTIVE WEEK ARROWS & DAY SELECTION)
        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
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
                                Text(text = "🫧", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Matriks Gelembung\nMood",
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = if (calendarPageIndex == 0) "Oktober 2026 (Pekan 1–2)" else "Oktober 2026 (Pekan 3–4)",
                                    fontSize = 11.sp,
                                    color = SeraTextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { calendarPageIndex = if (calendarPageIndex == 0) 1 else 0 },
                                shape = CircleShape,
                                color = SeraSurfaceSubtle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "‹", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SeraTextPrimary)
                                }
                            }
                            Text(
                                text = if (calendarPageIndex == 0) "Okt\nW1-2" else "Okt\nW3-4",
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Surface(
                                onClick = { calendarPageIndex = if (calendarPageIndex == 1) 0 else 1 },
                                shape = CircleShape,
                                color = SeraSurfaceSubtle,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "›", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SeraTextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val dayHeaders = listOf("SEN", "SEL", "RAB", "KAM", "JUM", "SAB", "MIN")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        dayHeaders.forEach { d ->
                            Text(
                                text = d,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextSecondary,
                                modifier = Modifier.width(38.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val entriesByDay = remember(uiState.checkInHistory) {
                        uiState.checkInHistory.associateBy { it.dayOfMonth }
                    }

                    val twoWeeksDays = if (calendarPageIndex == 0) {
                        listOf(
                            listOf(1, 2, 3, 4, 5, 6, 7),
                            listOf(8, 9, 10, 11, 12, 13, 14)
                        )
                    } else {
                        listOf(
                            listOf(14, 15, 16, 17, 18, 19, 20),
                            listOf(21, 22, 23, 24, 25, 26, 27)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        twoWeeksDays.forEach { weekDays ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                weekDays.forEach { dayNum ->
                                    val dayEntry = entriesByDay[dayNum]
                                    val dayEmotion = dayEntry?.emotion
                                    val emoji = when (dayEmotion) {
                                        EmotionType.SENANG -> "✨"
                                        EmotionType.TENANG -> "🌸"
                                        EmotionType.BIASA -> "🍃"
                                        EmotionType.SEDIH -> "🥺"
                                        EmotionType.CEMAS -> "🌧️"
                                        null -> if (dayNum % 3 == 0) "✨" else if (dayNum % 2 == 0) "🌿" else "🌸"
                                    }
                                    val isSelected = uiState.selectedCalendarDay == dayNum
                                    val isLatestDay = uiState.checkInHistory.firstOrNull()?.dayOfMonth == dayNum
                                    val pillBg = when {
                                        isSelected -> SeraPrimaryBlue
                                        dayEmotion != null -> dayEmotion.bgColor
                                        else -> SeraSurfaceSubtle
                                    }

                                    Column(
                                        modifier = Modifier
                                            .size(width = 40.dp, height = 58.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(pillBg)
                                            .border(
                                                width = if (isSelected || dayEmotion != null) 1.8.dp else 1.dp,
                                                color = when {
                                                    isSelected -> Color(0xFFF48FB1)
                                                    dayEmotion != null -> dayEmotion.borderColor
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(50)
                                            )
                                            .clickable {
                                                onSelectCalendarDay(dayNum)
                                                // Also synchronize the chart node selection if day is in current week!
                                                val weekIdx = (dayNum - 18).coerceIn(0, 6)
                                                selectedChartNodeIndex = weekIdx
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isLatestDay) "AKTIF\n$dayNum" else dayNum.toString(),
                                            fontSize = if (isLatestDay) 7.sp else 11.sp,
                                            lineHeight = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) Color.White else SeraTextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(text = emoji, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Selected Date Info Box (Live Synchronized with Database & Chart!)
                    val matchedDayEntry = entriesByDay[uiState.selectedCalendarDay]
                    Surface(
                        onClick = {
                            if (matchedDayEntry != null) {
                                onOpenJournalDetail(matchedDayEntry)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = SeraSurfaceSubtle,
                        border = BorderStroke(1.dp, SeraCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🗓️ Tersinkron • Tanggal ${uiState.selectedCalendarDay} Oktober 2026",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue
                                )
                                Text(
                                    text = if (matchedDayEntry != null) {
                                        "${matchedDayEntry.emotion.emoji} ${matchedDayEntry.emotion.label} (Level ${matchedDayEntry.intensity}/5 • ${matchedDayEntry.contexts.joinToString { it.shortLabel }}) — ${matchedDayEntry.journalNote.take(65)}..."
                                    } else {
                                        "Belum ada catatan khusus di tanggal ini. Check-in baru dari Beranda/Catat akan langsung tersinkron ke tanggal ${uiState.selectedCalendarDay} Okt ini."
                                    },
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = SeraTextSecondary
                                )
                            }
                            if (matchedDayEntry != null) {
                                Text(
                                    text = "Detail ›",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(text = "🌸 Tenang", fontSize = 11.sp, color = SeraTextSecondary)
                        Text(text = "🌧️ Cemas", fontSize = 11.sp, color = SeraTextSecondary)
                        Text(text = "✨ Semangat", fontSize = 11.sp, color = SeraTextSecondary)
                        Text(text = "🍃 Lelah", fontSize = 11.sp, color = SeraTextSecondary)
                    }
                }
            }
        }

        // 5. POLA YANG DITEMUKAN SERA (AI INSIGHT)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SeraIris, Color(0xFF9F3A5E))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pola yang Ditemukan SERA",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }

                Surface(
                    onClick = onOpenDeepAnalysis,
                    shape = RoundedCornerShape(50),
                    color = SeraPrimaryBlueLight
                ) {
                    Text(
                        text = "AI Insight 🔮",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (uiState.privacyState.allowPatternAnalysis) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.personalInsights.forEach { insight ->
                        val isSleep = insight.isSleepFactor
                        Surface(
                            onClick = onOpenDeepAnalysis,
                            shape = RoundedCornerShape(30.dp),
                            color = SeraSurface.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, SeraCardBorder),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSleep) EmotionCalmBg else SeraRoseSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = if (isSleep) "🌙" else "🎓", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = insight.title,
                                            fontSize = 15.sp,
                                            lineHeight = 19.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isSleep) EmotionCalmBg else SeraRoseSoft
                                        ) {
                                            Text(
                                                text = insight.metricHighlight,
                                                fontSize = 10.sp,
                                                lineHeight = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSleep) SeraTeal else Color(0xFF9F2A56),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = insight.description,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = SeraTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. REFLEKSI MINGGUAN OTOMATIS CARD
        item {
            Surface(
                onClick = onOpenWeeklyReport,
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, SeraCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (SeraThemeMode.isDarkMode) {
                                listOf(
                                    Color(0xFF231E4A),
                                    Color(0xFF331F3D),
                                    Color(0xFF2B233D)
                                )
                            } else {
                                listOf(
                                    Color(0xFFEEE9FF),
                                    Color(0xFFFCE9F2),
                                    Color(0xFFFFF2E2)
                                )
                            }
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📖", fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Refleksi Mingguan Otomatis",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraSurface
                        ) {
                            Text(
                                text = "Okt W4",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.weeklyReport?.narrativeReflection
                            ?: "“Minggu ini kamu melalui gelombang rasa yang cukup kaya. Keberanianmu mengakui lelah tanpa menghakimi diri adalah wujud cinta terindah untuk jiwamu.”",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontStyle = FontStyle.Italic,
                        color = SeraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = SeraSurface.copy(alpha = 0.9f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Sering Dirasakan",
                                    fontSize = 10.sp,
                                    color = SeraTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🌿 Tenang & Bersyukur",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = SeraSurface.copy(alpha = 0.9f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Self-Care Terampuh",
                                    fontSize = 10.sp,
                                    color = SeraTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🎧 ${uiState.weeklyReport?.mostHelpfulActivityTitle ?: "Napas Ritmis 5m"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. RIWAYAT CATATAN BATIN HEADER, SEARCH & FILTER PILLS
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Riwayat Catatan Batin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(SeraPrimaryBlue)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraSurface.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "${uiState.filteredHistory.size} Catatan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeraTextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.historySearchQuery,
                    onValueChange = {
                        onSearchAndFilterChange(
                            it,
                            uiState.selectedContextFilter,
                            uiState.selectedEmotionFilter
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Cari refleksi, pemicu, atau jurnal...",
                            fontSize = 13.sp,
                            color = SeraTextMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurface.copy(alpha = 0.92f),
                        unfocusedContainerColor = SeraSurface.copy(alpha = 0.92f),
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = SeraCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allSelected =
                        uiState.selectedContextFilter == null && uiState.selectedEmotionFilter == null
                    FilterChipCapsule(
                        label = "Semua",
                        isSelected = allSelected,
                        onClick = { onSearchAndFilterChange(uiState.historySearchQuery, null, null) }
                    )
                    FilterChipCapsule(
                        label = "🌧️ Cemas",
                        isSelected = uiState.selectedEmotionFilter == EmotionType.CEMAS,
                        onClick = {
                            val next =
                                if (uiState.selectedEmotionFilter == EmotionType.CEMAS) null else EmotionType.CEMAS
                            onSearchAndFilterChange(uiState.historySearchQuery, null, next)
                        }
                    )
                    FilterChipCapsule(
                        label = "🌿 Tenang",
                        isSelected = uiState.selectedEmotionFilter == EmotionType.TENANG,
                        onClick = {
                            val next =
                                if (uiState.selectedEmotionFilter == EmotionType.TENANG) null else EmotionType.TENANG
                            onSearchAndFilterChange(uiState.historySearchQuery, null, next)
                        }
                    )
                    FilterChipCapsule(
                        label = "📚 Kuliah",
                        isSelected = uiState.selectedContextFilter == ActivityContext.PERKULIAHAN,
                        onClick = {
                            val next =
                                if (uiState.selectedContextFilter == ActivityContext.PERKULIAHAN) null else ActivityContext.PERKULIAHAN
                            onSearchAndFilterChange(uiState.historySearchQuery, next, null)
                        }
                    )
                }
            }
        }

        // 8. RIWAYAT CATATAN CARDS (WITH LIVE VOICE JOURNAL PLAYBACK!)
        items(uiState.filteredHistory, key = { it.id }) { entry ->
            val isThisEntryPlaying = currentlyPlayingEntryId == entry.id
            OrganicHistoryEntryCard(
                entry = entry,
                isPlayingVoice = isThisEntryPlaying,
                onPlayVoiceClick = {
                    if (isThisEntryPlaying) {
                        SeraVoiceJournalEngine.stopPlayback()
                        currentlyPlayingEntryId = null
                    } else {
                        currentlyPlayingEntryId = entry.id
                        SeraVoiceJournalEngine.playVoiceJournal(
                            context = context,
                            filePath = entry.voiceFilePath,
                            fallbackSpokenText = entry.journalNote,
                            durationSeconds = entry.voiceDurationSeconds.coerceAtLeast(4),
                            onComplete = {
                                if (currentlyPlayingEntryId == entry.id) {
                                    currentlyPlayingEntryId = null
                                }
                            }
                        )
                    }
                },
                onCardClick = { onOpenJournalDetail(entry) },
                onToggleAppreciate = { onToggleAppreciate(entry.id) }
            )
        }

        // 9. FOOTER APPRECIATION BANNER WITH POMI MASCOT
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SeraSurface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, SeraCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PomiBearMascot(
                        size = 56.dp,
                        mood = PomiMascotMood.HAPPY_HEART,
                        showOuterHalo = false
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Terima kasih sudah bertahan sejauh ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Text(
                            text = "Setiap gelombang rasa adalah tanda kamu manusia yang bertumbuh dengan indah 🌸",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = SeraTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipCapsule(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) SeraPrimaryBlue else SeraSurface.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, if (isSelected) SeraPrimaryBlue else SeraCardBorder)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else SeraTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun OrganicHistoryEntryCard(
    entry: EmotionCheckIn,
    isPlayingVoice: Boolean,
    onPlayVoiceClick: () -> Unit,
    onCardClick: () -> Unit,
    onToggleAppreciate: () -> Unit
) {
    val firstContext = entry.contexts.firstOrNull() ?: ActivityContext.PERKULIAHAN
    val isAnxiousOrSad = entry.emotion == EmotionType.CEMAS || entry.emotion == EmotionType.SEDIH

    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(30.dp),
        color = SeraSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, SeraCardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isAnxiousOrSad) SeraRoseSoft else EmotionCalmBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAnxiousOrSad) "🌧️" else "🌿",
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.emotion.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Level ${entry.intensity}/5",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isAnxiousOrSad) SeraRose else SeraTeal
                            )
                        }
                        Text(
                            text = entry.timeLabel,
                            fontSize = 11.sp,
                            color = SeraTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isAnxiousOrSad) SeraPrimaryBlueLight else SeraRoseSoft
                ) {
                    Text(
                        text = firstContext.shortLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAnxiousOrSad) SeraPrimaryBlue else SeraRose,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "“${entry.journalNote}”",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SeraTextPrimary
            )

            if (entry.voiceDurationSeconds > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onPlayVoiceClick,
                    shape = RoundedCornerShape(50),
                    color = if (isPlayingVoice) SeraTeal else SeraPrimaryBlueLight.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPlayingVoice) "🔊" else "🎙️",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingVoice) {
                                "Sedang Memutar Voice Journal (${entry.voiceDurationSeconds} dtk) • Ketuk Berhenti"
                            } else {
                                "▶ Putar Voice Emotion Journal (${entry.voiceDurationSeconds} dtk)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPlayingVoice) Color.White else SeraPrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAnxiousOrSad) "🪷" else "🎧",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = entry.supportiveActivityTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SeraTextSecondary
                    )
                }

                Surface(
                    onClick = onToggleAppreciate,
                    shape = RoundedCornerShape(50),
                    color = if (isAnxiousOrSad) SeraRoseSoft else EmotionCalmBg
                ) {
                    Text(
                        text = if (isAnxiousOrSad) "♡ Peluk Dirimu" else "✓ ${entry.appreciationLabel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isAnxiousOrSad) SeraRose else SeraTeal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
