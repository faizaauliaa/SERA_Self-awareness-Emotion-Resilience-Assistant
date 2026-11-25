package id.ac.ub.filkom.sera.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import id.ac.ub.filkom.sera.data.model.*
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.viewmodel.SeraUiState
import java.util.Locale

@Composable
fun SelfCareAndSupportScreen(
    uiState: SeraUiState,
    onUpdatePlannerFilter: (
        emotion: EmotionType,
        intensity: Int,
        durationMinutes: Int,
        category: SelfCareCategory,
        bookmarksOnly: Boolean
    ) -> Unit,
    onStartActivitySession: (SelfCareActivity) -> Unit,
    onToggleMicroStep: (Int) -> Unit,
    onActivateGpsSensor: (Boolean) -> Unit,
    onDisableGpsSensor: () -> Unit,
    onSubmitEvaluation: (SelfCareActivity, EvaluationRating, String) -> Unit,
    onSelectContactToReach: (TrustedContact) -> Unit,
    onAddOrEditContactClick: (TrustedContact?) -> Unit
) {
    val context = LocalContext.current
    var selectedEvaluationRating by remember { mutableStateOf<EvaluationRating?>(null) }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onActivateGpsSensor(granted)
    }

    val targetActivityForTracker = uiState.lastCompletedActivity
        ?: uiState.adaptiveRecommendations.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. RUANG PEMULIHAN ENERGI HERO CARD
        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = SeraSurface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraRoseSoft
                            ) {
                                Text(
                                    text = "● ${uiState.plannerEmotion.label} • Level ${uiState.plannerIntensity}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraRose,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraPrimaryBlueLight
                            ) {
                                Text(
                                    text = "♡ 78 bpm • Tenang",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraPrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // AI Sync Circular Badge
                        Surface(
                            shape = CircleShape,
                            color = SeraPrimaryBlueLight,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "✨ AI\nSync",
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Ruang Pemulihan\nEnergi ✨",
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kurasi aksi lembut diselaraskan dengan detak nadi & hembusan napasmu saat ini.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = SeraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Filter Capsules
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelfCareCategory.entries.forEach { cat ->
                            val isSelected = uiState.selectedSelfCareCategory == cat
                            Surface(
                                onClick = {
                                    onUpdatePlannerFilter(
                                        uiState.plannerEmotion,
                                        uiState.plannerIntensity,
                                        uiState.availableDurationMinutes,
                                        cat,
                                        false
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) SeraPrimaryBlue else SeraSurface,
                                border = BorderStroke(1.dp, if (isSelected) SeraPrimaryBlue else SeraCardBorder)
                            ) {
                                Text(
                                    text = cat.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else SeraTextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. SENSOR 1: REKOMENDASI AKTIVITAS SELF-CARE BERBASIS LOKASI (GPS)
        item {
            val gpsState = uiState.gpsSensorState
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmotionCalmBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "📍", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Self-Care Berbasis Lokasi (GPS)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                                Text(
                                    text = gpsState.detectedAreaName,
                                    fontSize = 11.sp,
                                    color = SeraTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (gpsState.isLocationEnabled) EmotionCalmBg else SeraPrimaryBlueLight
                        ) {
                            Text(
                                text = if (gpsState.isLocationEnabled) "GPS Aktif" else "Siaga",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (gpsState.isLocationEnabled) SeraTeal else SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Menemukan ruang terbuka hijau atau area tenang di sekitarmu untuk berjalan santai & meredakan kejenuhan akademik.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = SeraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!gpsState.isLocationEnabled) {
                        Surface(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED

                                if (hasPerm) {
                                    onActivateGpsSensor(true)
                                } else {
                                    gpsPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = SeraTeal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📍  Ambil Koordinat GPS & Lihat Tempat Sekitar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Coordinate & Privacy Pill + Refresh & Google Maps Navigation
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = EmotionCalmBg,
                            border = BorderStroke(1.dp, EmotionCalmBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = String.format(
                                                Locale.US,
                                                "🛰️ Koordinat GPS Live: %.5f, %.5f",
                                                gpsState.latitude,
                                                gpsState.longitude
                                            ),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTeal
                                        )
                                        Text(
                                            text = "📍 ${gpsState.detectedAreaName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SeraTextPrimary
                                        )
                                        Text(
                                            text = gpsState.lastUpdatedLabel,
                                            fontSize = 10.sp,
                                            color = SeraTextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        onClick = { onActivateGpsSensor(true) },
                                        shape = RoundedCornerShape(50),
                                        color = SeraTeal,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "🔄 Perbarui Titik GPS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = {
                                            try {
                                                val mapUri = Uri.parse(
                                                    "geo:${gpsState.latitude},${gpsState.longitude}?q=${gpsState.latitude},${gpsState.longitude}(Lokasi+GPS+SERA)"
                                                )
                                                context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                                            } catch (_: Exception) {
                                                try {
                                                    val webMap = Uri.parse(
                                                        "https://www.google.com/maps/search/?api=1&query=${gpsState.latitude},${gpsState.longitude}"
                                                    )
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, webMap))
                                                } catch (_: Exception) {
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(50),
                                        color = SeraPrimaryBlue,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "🗺️ Buka Peta Sekitar",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = onDisableGpsSensor,
                                        shape = RoundedCornerShape(50),
                                        color = SeraRoseSoft
                                    ) {
                                        Text(
                                            text = "Matikan",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraRose,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        gpsState.nearbyRecommendations.forEach { place ->
                            Surface(
                                onClick = {
                                    val sensoryAct = uiState.adaptiveRecommendations.find {
                                        it.moduleType == InteractiveModuleType.SENSORY_RESET
                                    } ?: uiState.adaptiveRecommendations.first()
                                    onStartActivitySession(sensoryAct)
                                },
                                shape = RoundedCornerShape(22.dp),
                                color = SeraSurfaceSubtle,
                                border = BorderStroke(1.dp, SeraCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(EmotionCalmBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = place.iconEmoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = place.placeName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = SeraTextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = place.distanceText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = SeraTeal
                                                )
                                            }
                                            Text(
                                                text = "${place.activityTitle} • ${place.durationMinutes} Menit",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SeraPrimaryBlue
                                            )
                                            Text(
                                                text = place.description,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = SeraTextSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            onClick = {
                                                try {
                                                    val navUri = Uri.parse(
                                                        "geo:${place.latitude},${place.longitude}?q=${place.latitude},${place.longitude}(${Uri.encode(place.placeName)})"
                                                    )
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, navUri))
                                                } catch (_: Exception) {
                                                    try {
                                                        val webNav = Uri.parse(
                                                            "https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}"
                                                        )
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, webNav))
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(50),
                                            color = SeraPrimaryBlueLight
                                        ) {
                                            Text(
                                                text = "🧭 Rute Peta GPS (${String.format(Locale.US, "%.4f, %.4f", place.latitude, place.longitude)})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SeraPrimaryBlue,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
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

        // 3. RENCANA TINDAKAN TERPILIH HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rencana Tindakan Terpilih",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = "Dirancang adaptif untuk meredakan\nketeganganmu",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = SeraTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraPrimaryBlueLight
                ) {
                    Text(
                        text = "🔄 Adaptif",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 4. ADAPTIVE ACTION CARDS (MATCHING SERA - ADAPTIVE SELF-CARE & SUPPORT CIRCLE.PNG)
        items(uiState.adaptiveRecommendations, key = { it.id }) { activity ->
            OrganicPlannerActionCard(
                activity = activity,
                microSteps = uiState.microTaskSteps,
                onToggleMicroStep = onToggleMicroStep,
                onStartSession = { onStartActivitySession(activity) }
            )
        }

        // 5. WHAT-HELPED-ME TRACKER CARD
        if (targetActivityForTracker != null) {
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SeraPrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WHAT–HELPED–ME TRACKER",
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
                                    text = "Evaluasi Rasa",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bagaimana efektivitas aktivitas ini\nmembantumu?",
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Khusus untuk: ")
                                withStyle(
                                    SpanStyle(
                                        color = SeraPrimaryBlue,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) {
                                    append(targetActivityForTracker.title)
                                }
                            },
                            fontSize = 12.sp,
                            color = SeraTextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EvaluationRating.entries.forEach { rating ->
                                val isSelected = selectedEvaluationRating == rating
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            if (isSelected) SeraPrimaryBlueLight else SeraSurfaceSubtle
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SeraPrimaryBlue else SeraCardBorder,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clickable {
                                            selectedEvaluationRating = rating
                                            onSubmitEvaluation(
                                                targetActivityForTracker,
                                                rating,
                                                ""
                                            )
                                        }
                                        .padding(vertical = 16.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = rating.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = rating.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) SeraPrimaryBlue else SeraTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚛️", fontSize = 14.sp, color = SeraPrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Responmu otomatis melatih algoritma adaptif SERA untuk rekomendasi berikutnya.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = SeraTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 6. LINGKARAN DUKUNGAN (TRUSTED CONTACTS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lingkaran Dukungan",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF48FB1))
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Koneksi manusia nyata saat teknik mandiri terasa belum cukup.",
                        fontSize = 12.sp,
                        color = SeraTextSecondary
                    )
                }

                Surface(
                    onClick = { onAddOrEditContactClick(null) },
                    shape = CircleShape,
                    color = SeraSurface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "👥+",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue
                        )
                    }
                }
            }
        }

        items(uiState.trustedContacts, key = { it.id }) { contact ->
            OrganicTrustedContactCard(
                contact = contact,
                onReachClick = { onSelectContactToReach(contact) }
            )
        }

        // 7. FOOTER PRIVACY CAPSULE
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SeraSurface.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, SeraCardBorder)
                ) {
                    Text(
                        text = "🛡️  RUANG AMAN PRIBADI ENKRIPSI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraPrimaryBlue,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kondisi emosi, riwayat napas, dan orang yang kamu\nhubungi dijaga dengan privasi penuh. SERA adalah\npelabuhan tenangmu.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = SeraTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OrganicPlannerActionCard(
    activity: SelfCareActivity,
    microSteps: List<MicroTaskStep>,
    onToggleMicroStep: (Int) -> Unit,
    onStartSession: () -> Unit
) {
    val isBreathing = activity.moduleType == InteractiveModuleType.BREATHING_BOX
    val isMicro = activity.moduleType == InteractiveModuleType.MICRO_TASK

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
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isBreathing -> SeraPrimaryBlue
                                isMicro -> Color(0xFFF48FB1)
                                else -> SeraTeal
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isBreathing -> "🌬️"
                            isMicro -> "☑️"
                            else -> "🌿"
                        },
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isBreathing) EmotionCalmBg else SeraSurfaceSubtle
                        ) {
                            Text(
                                text = "${activity.durationMinutes} Menit",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isBreathing) SeraTeal else SeraTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isMicro) SeraRoseSoft else SeraPrimaryBlueLight
                        ) {
                            Text(
                                text = activity.secondaryTagLabel ?: "${activity.baseMatchPercentage}% Match",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isMicro) SeraRose else SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (isMicro) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraPrimaryBlueLight
                        ) {
                            Text(
                                text = "${activity.baseMatchPercentage}% Match",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activity.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = activity.description,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SeraTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            when {
                isBreathing -> {
                    // Inner Breathing Preview Capsule
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = SeraSurfaceSubtle,
                        border = BorderStroke(1.dp, SeraCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SeraPrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🧘", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Tarik Napas\nPelan...",
                                            fontSize = 12.sp,
                                            lineHeight = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTextPrimary
                                        )
                                        Text(
                                            text = "⏱ 04:32 tersisa",
                                            fontSize = 10.sp,
                                            color = SeraTextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    onClick = onStartSession,
                                    shape = RoundedCornerShape(50),
                                    color = SeraPrimaryBlue
                                ) {
                                    Text(
                                        text = "▷  Mulai Sesi Rileks",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(SeraCardBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.68f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(SeraTeal, SeraPrimaryBlue, Color(0xFFF48FB1))
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                isMicro -> {
                    // 3 Interactive Micro Steps Preview
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        microSteps.forEachIndexed { idx, step ->
                            val numBg = when (idx) {
                                0 -> SeraPrimaryBlueLight
                                1 -> SeraRoseSoft
                                else -> EmotionCalmBg
                            }
                            val numColor = when (idx) {
                                0 -> SeraPrimaryBlue
                                1 -> SeraRose
                                else -> SeraTeal
                            }
                            Surface(
                                onClick = { onToggleMicroStep(step.id) },
                                shape = RoundedCornerShape(18.dp),
                                color = SeraSurfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (step.isCompleted) SeraPrimaryBlue else Color.Transparent
                                            )
                                            .border(1.5.dp, SeraPrimaryBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (step.isCompleted) {
                                            Text(text = "✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(numBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = numColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = step.subtitle,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = SeraTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            onClick = onStartSession,
                            shape = RoundedCornerShape(50),
                            color = SeraPrimaryBlueLight
                        ) {
                            Text(
                                text = "Panduan Mikro Detail  →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                else -> {
                    // Sensory Reset Bottom Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁️  Bebas Layar • Sensori\nDingin",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTeal
                        )

                        Surface(
                            onClick = onStartSession,
                            shape = RoundedCornerShape(50),
                            color = SeraTeal
                        ) {
                            Text(
                                text = "Coba\nSekarang",
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganicTrustedContactCard(
    contact: TrustedContact,
    onReachClick: () -> Unit
) {
    val isService = contact.isVerifiedService

    Surface(
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
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isService) SeraPrimaryBlueLight else SeraRoseSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isService) "🎧" else contact.initials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isService) SeraPrimaryBlue else SeraRose
                    )
                }
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(SeraTeal)
                        .border(2.dp, SeraSurface, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isService) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SeraRoseSoft
                        ) {
                            Text(
                                text = "Gratis",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraRose,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(text = "♡", color = SeraRose, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = contact.relationSubtitle,
                    fontSize = 11.sp,
                    color = SeraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                onClick = onReachClick,
                shape = RoundedCornerShape(50),
                color = if (isService) SeraPrimaryBlue else SeraTeal
            ) {
                Text(
                    text = if (isService) "▷ Konseling" else "💬 WhatsApp",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
