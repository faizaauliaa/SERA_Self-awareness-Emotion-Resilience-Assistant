package id.ac.ub.filkom.sera.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ub.filkom.sera.data.model.DataDeletionScope
import id.ac.ub.filkom.sera.data.model.PrivacyDataControlState
import id.ac.ub.filkom.sera.data.model.TrustedContact
import id.ac.ub.filkom.sera.ui.components.PomiBearMascot
import id.ac.ub.filkom.sera.ui.components.PomiMascotMood
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.util.SeraSoundscapeEngine
import id.ac.ub.filkom.sera.viewmodel.SeraUiState
import java.io.File

@Composable
fun ProfilePrivacyScreen(
    uiState: SeraUiState,
    onBackClick: () -> Unit,
    onUpdateUserProfile: (name: String, subtitle: String, photoUri: String?) -> Unit,
    onUpdatePrivacyState: (PrivacyDataControlState) -> Unit,
    onExecuteDeletion: (DataDeletionScope) -> Unit,
    onRestoreDemoData: () -> Unit,
    onExportPdfSummary: () -> Unit,
    onAddOrEditContact: (TrustedContact?) -> Unit,
    onOpenWeeklyReport: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    var pendingDeletionScope by remember { mutableStateOf<DataDeletionScope?>(null) }
    var previewingSoundscape by remember { mutableStateOf<String?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var morningTime by remember(uiState.privacyState.morningReminderTime) {
        mutableStateOf(uiState.privacyState.morningReminderTime)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { pickedUri: Uri? ->
        if (pickedUri != null) {
            try {
                // Copy selected photo into app internal storage so it persists permanently
                val destFile = File(context.filesDir, "sera_profile_avatar_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(pickedUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                onUpdateUserProfile(
                    uiState.userName,
                    uiState.userRoleSubtitle,
                    Uri.fromFile(destFile).toString()
                )
            } catch (_: Exception) {
                onUpdateUserProfile(
                    uiState.userName,
                    uiState.userRoleSubtitle,
                    pickedUri.toString()
                )
            }
        }
    }

    val profileImageBitmap: ImageBitmap? = remember(uiState.userPhotoUri) {
        val uriStr = uiState.userPhotoUri
        if (uriStr.isNullOrBlank()) {
            null
        } else {
            try {
                val uri = Uri.parse(uriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            SeraSoundscapeEngine.stop()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 0. TOP BAR ROW + QUICK DARK MODE BUTTON
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Profil & Kontrol Privasi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }

                // Quick Dark Mode Toggle Pill
                Surface(
                    onClick = {
                        val nextDark = !uiState.privacyState.isDarkModeEnabled
                        onUpdatePrivacyState(uiState.privacyState.copy(isDarkModeEnabled = nextDark))
                    },
                    shape = RoundedCornerShape(50),
                    color = SeraPrimaryBlueLight,
                    border = BorderStroke(1.dp, SeraCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.privacyState.isDarkModeEnabled) "☀️ Terang" else "🌙 Dark Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue
                        )
                    }
                }
            }
        }

        // 1. PROFILE SANCTUARY CARD WITH CUSTOM PHOTO, EDITABLE SUBTITLE & LIVING POMI BEAR MASCOT
        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = SeraSurface.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Tappable Profile Photo Avatar with Camera Edit Badge
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.clickable {
                                    photoPickerLauncher.launch("image/*")
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(SeraIrisSoft, SeraRoseSoft)
                                            )
                                        )
                                        .border(2.5.dp, SeraSurface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (profileImageBitmap != null) {
                                        Image(
                                            bitmap = profileImageBitmap,
                                            contentDescription = "Foto Profil",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = uiState.userName.take(1).uppercase(),
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraPrimaryBlue
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(SeraPrimaryBlue)
                                        .border(2.dp, SeraSurface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📷", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showEditProfileDialog = true }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.userName,
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✎",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue
                                    )
                                }
                                Text(
                                    text = uiState.userRoleSubtitle,
                                    fontSize = 12.sp,
                                    color = SeraTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        onClick = { showEditProfileDialog = true },
                                        shape = RoundedCornerShape(50),
                                        color = SeraPrimaryBlueLight
                                    ) {
                                        Text(
                                            text = "✎ Ubah Profil & Deskripsi",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraPrimaryBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Living Pomi Bear Mascot Pill on Top-Right
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PomiBearMascot(
                                    size = 46.dp,
                                    mood = PomiMascotMood.HAPPY_HEART,
                                    showOuterHalo = false
                                )
                                Text(
                                    text = "Pomi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        onClick = onOpenWeeklyReport,
                        shape = RoundedCornerShape(50),
                        color = SeraSurfaceSubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ ${uiState.streakDays} Hari Rutin Check-in",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SeraSurface
                            ) {
                                Text(
                                    text = "Database Tersinkron",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. KONTROL PRIVASI DATA & TAMPILAN (DARK MODE)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛡️", fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kontrol Privasi & Tampilan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PrivacyToggleRow(
                        title = "🌙 Mode Gelap (Dark Mode)",
                        subtitle = "Ubah nuansa ruang SERA menjadi tema malam yang teduh dan ramah untuk mata sebelum tidur.",
                        checked = uiState.privacyState.isDarkModeEnabled,
                        onCheckedChange = {
                            onUpdatePrivacyState(uiState.privacyState.copy(isDarkModeEnabled = it))
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = SeraDivider
                    )

                    PrivacyToggleRow(
                        title = "🧠 Deteksi Pola Emosi Otomatis",
                        subtitle = "Izinkan sistem menganalisis korelasi ritme stres dan tugas kuliah secara privat tanpa melibatkan profil eksternal.",
                        checked = uiState.privacyState.allowPatternAnalysis,
                        onCheckedChange = {
                            onUpdatePrivacyState(uiState.privacyState.copy(allowPatternAnalysis = it))
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = SeraDivider
                    )

                    PrivacyToggleRow(
                        title = "🔒 Mode Privat Penuh",
                        subtitle = "Hanya simpan rekaman jurnal di memori ponsel lokal dengan proteksi sandi & biometrik.",
                        checked = uiState.privacyState.maskJournalPreviewOnHome,
                        onCheckedChange = {
                            onUpdatePrivacyState(uiState.privacyState.copy(maskJournalPreviewOnHome = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ekspor Ringkasan Jurnal (Tanpa Tulisan PDF)
                    Surface(
                        onClick = onExportPdfSummary,
                        shape = RoundedCornerShape(24.dp),
                        color = SeraPrimaryBlueLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📄", fontSize = 18.sp)
                            Text(
                                text = "Ekspor & Bagikan\nRingkasan Jurnal",
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(text = "⬇", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SeraPrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hapus Riwayat Catatan Pekan Ini
                    Surface(
                        onClick = { pendingDeletionScope = DataDeletionScope.JOURNALS_TEXT_ONLY },
                        shape = RoundedCornerShape(24.dp),
                        color = SeraRoseSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🗑️", fontSize = 17.sp)
                            Text(
                                text = "Hapus Riwayat Catatan\nPekan Ini",
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF9F1D35),
                                textAlign = TextAlign.Center
                            )
                            Text(text = "🗑️", fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // 3. LINGKAR DUKUNGAN
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🫂", fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lingkar Dukungan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Orang terpercaya yang dapat kamu hubungi dengan cepat ketika tombol pertolongan pertama diaktifkan.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = SeraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    uiState.trustedContacts.forEach { contact ->
                        val isService = contact.isVerifiedService
                        Surface(
                            onClick = { onAddOrEditContact(contact) },
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isService) EmotionCalmBg else SeraRoseSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isService) "🏥" else contact.initials.take(1),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isService) SeraTeal else Color(0xFF9F2A56)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = contact.relationSubtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isService) SeraTeal else Color(0xFF9F2A56)
                                    )
                                }
                                Text(text = "✎", fontSize = 15.sp, color = SeraTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // + Tambah Kontak Tepercaya Baru Button
                    Surface(
                        onClick = { onAddOrEditContact(null) },
                        shape = RoundedCornerShape(50),
                        color = SeraPrimaryBlueLight.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "👥+  Tambah Kontak Tepercaya Baru",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PrivacyToggleRow(
                        title = "Konfirmasi Buka WhatsApp",
                        subtitle = "Tanya konfirmasi aman sebelum membuka aplikasi pihak ketiga",
                        checked = uiState.privacyState.gentleSupportSuggestion,
                        onCheckedChange = {
                            onUpdatePrivacyState(uiState.privacyState.copy(gentleSupportSuggestion = it))
                        }
                    )
                }
            }
        }

        // 4. RITME ISTIRAHAT & SUARA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧘", fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ritme Istirahat & Suara",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraCardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Pengingat Check-in Harian",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = {
                                val nextMorning = when (morningTime) {
                                    "07:00" -> "08:00"
                                    "08:00" -> "09:00"
                                    else -> "07:00"
                                }
                                morningTime = nextMorning
                                onUpdatePrivacyState(uiState.privacyState.copy(morningReminderTime = nextMorning))
                            },
                            shape = RoundedCornerShape(50),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "☀️ Pagi", fontSize = 12.sp, color = SeraTextSecondary)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraPrimaryBlueLight
                                ) {
                                    Text(
                                        text = "$morningTime ▾",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = {
                                val nextEvening = when (uiState.privacyState.reminderTimeLabel) {
                                    "20:00" -> "21:00"
                                    "21:00" -> "22:00"
                                    else -> "20:00"
                                }
                                onUpdatePrivacyState(uiState.privacyState.copy(reminderTimeLabel = nextEvening))
                            },
                            shape = RoundedCornerShape(50),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(1.dp, SeraCardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌙 Malam", fontSize = 12.sp, color = SeraTextSecondary)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SeraPrimaryBlueLight
                                ) {
                                    Text(
                                        text = "${uiState.privacyState.reminderTimeLabel} ▾",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Soundscape Relaksasi Pilihan (Ketuk untuk Dengar)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val soundscapes = listOf(
                        Triple("432 Hz Solfeggio Serenity", "Frekuensi harmonis penenang sistem saraf", "〰️"),
                        Triple("Hujan Lembut di Jendela", "Suara alam ritmik untuk pernapasan lambat", "🌧️")
                    )
                    soundscapes.forEach { (title, sub, emoji) ->
                        val isSelected = uiState.privacyState.selectedSoundscape == title
                        val isPreviewing = previewingSoundscape == title
                        Surface(
                            onClick = {
                                onUpdatePrivacyState(uiState.privacyState.copy(selectedSoundscape = title))
                                if (isPreviewing) {
                                    SeraSoundscapeEngine.stop()
                                    previewingSoundscape = null
                                } else {
                                    previewingSoundscape = title
                                    SeraSoundscapeEngine.startOrSwitch(title, 0.7f)
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurfaceSubtle,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) SeraPrimaryBlue else SeraCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Text(
                                        text = sub,
                                        fontSize = 11.sp,
                                        color = SeraTextSecondary
                                    )
                                }
                                Surface(
                                    onClick = {
                                        onUpdatePrivacyState(uiState.privacyState.copy(selectedSoundscape = title))
                                        if (isPreviewing) {
                                            SeraSoundscapeEngine.stop()
                                            previewingSoundscape = null
                                        } else {
                                            previewingSoundscape = title
                                            SeraSoundscapeEngine.startOrSwitch(title, 0.7f)
                                        }
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = if (isPreviewing) SeraTeal else SeraPrimaryBlueLight
                                ) {
                                    Text(
                                        text = if (isPreviewing) "⏹ Berhenti" else "🔊 Putar",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isPreviewing) Color.White else SeraPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. KOMITMEN ETIS SERA & DEMO RESET / LOGOUT
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SeraPrimaryBlueLight.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, SeraCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "💠", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Komitmen Etis SERA: Data emosi dan jurnal pribadimu dienkripsi secara mandiri. Kami tidak pernah membagikan atau memperjualbelikan rekaman perasaanmu kepada pihak ketiga mana pun.",
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                            color = SeraTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onRestoreDemoData,
                            shape = RoundedCornerShape(50),
                            color = SeraSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🔄 Reset Data Demo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraPrimaryBlue,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }

                        Surface(
                            onClick = onLogoutClick,
                            shape = RoundedCornerShape(50),
                            color = SeraRoseSoft,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Keluar Akun",
                                fontSize = 11.sp,
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

    // EDIT PROFILE & PHOTO DIALOG
    if (showEditProfileDialog) {
        var editedName by remember(uiState.userName) { mutableStateOf(uiState.userName) }
        var editedSubtitle by remember(uiState.userRoleSubtitle) {
            mutableStateOf(uiState.userRoleSubtitle)
        }
        val quickRolePresets = listOf(
            "Mahasiswi Tingkat Akhir • TIF FILKOM UB",
            "Mahasiswi Teknik Informatika • FILKOM UB",
            "Mahasiswa Sistem Informasi • FILKOM UB",
            "Pejuang Skripsi & Magang • Semester 7",
            "Teman Setia Pomi • Ruang Tenang SERA"
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = SeraSurface,
            shape = RoundedCornerShape(30.dp),
            title = {
                Text(
                    text = "Ubah Profil & Foto Identitas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(SeraIrisSoft, SeraRoseSoft)
                                    )
                                )
                                .clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageBitmap != null) {
                                Image(
                                    bitmap = profileImageBitmap,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = editedName.take(1).uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(50),
                                color = SeraPrimaryBlueLight
                            ) {
                                Text(
                                    text = "📷 Pilih Foto dari Galeri",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SeraPrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            if (!uiState.userPhotoUri.isNullOrBlank()) {
                                Text(
                                    text = "Hapus Foto (Pakai Inisial)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeraRose,
                                    modifier = Modifier.clickable {
                                        onUpdateUserProfile(editedName, editedSubtitle, "")
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nama Panggilan", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedSubtitle,
                        onValueChange = { editedSubtitle = it },
                        label = { Text("Deskripsi / Status Mahasiswa", fontSize = 11.sp) },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Pilihan Cepat Deskripsi:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraTextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        quickRolePresets.forEach { preset ->
                            Surface(
                                onClick = { editedSubtitle = preset },
                                shape = RoundedCornerShape(14.dp),
                                color = if (editedSubtitle == preset) SeraPrimaryBlueLight else SeraSurfaceSubtle,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    fontWeight = if (editedSubtitle == preset) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (editedSubtitle == preset) SeraPrimaryBlue else SeraTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUserProfile(editedName, editedSubtitle, uiState.userPhotoUri)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeraPrimaryBlue),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Simpan Profil", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal", color = SeraTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // CONFIRMATION DIALOG FOR DATA DELETION
    if (pendingDeletionScope != null) {
        val scope = pendingDeletionScope!!
        AlertDialog(
            onDismissRequest = { pendingDeletionScope = null },
            containerColor = SeraSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Konfirmasi Penghapusan Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = scope.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraRose
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scope.impactDescription,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = SeraTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onExecuteDeletion(scope)
                        pendingDeletionScope = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeraRose),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Ya, Hapus Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletionScope = null }) {
                    Text("Batalkan", color = SeraTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = SeraTextSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SeraPrimaryBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD0C8EC)
            )
        )
    }
}
