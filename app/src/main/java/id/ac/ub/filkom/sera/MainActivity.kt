package id.ac.ub.filkom.sera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.ac.ub.filkom.sera.data.model.*
import id.ac.ub.filkom.sera.ui.components.PastelAuroraBubbleBackground
import id.ac.ub.filkom.sera.ui.components.SeraBottomNavigationBar
import id.ac.ub.filkom.sera.ui.components.SeraBottomTab
import id.ac.ub.filkom.sera.ui.components.SeraTopAppBar
import id.ac.ub.filkom.sera.ui.screens.*
import id.ac.ub.filkom.sera.ui.theme.*
import id.ac.ub.filkom.sera.viewmodel.SeraViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            SERATheme {
                SeraAppRoot()
            }
        }
    }
}

@Composable
fun SeraAppRoot(seraViewModel: SeraViewModel = viewModel()) {
    val uiState by seraViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showInitialSplash by remember { mutableStateOf(true) }
    var selectedBottomTab by remember { mutableStateOf(SeraBottomTab.BERANDA) }
    var isProfilePrivacyOpen by remember { mutableStateOf(false) }

    var journalInitialEmotion by remember { mutableStateOf(EmotionType.TENANG) }
    var editingCheckInEntry by remember { mutableStateOf<EmotionCheckIn?>(null) }

    // Interactive Full-Screen Modals & Dialogs
    var activeSelfCareSession by remember { mutableStateOf<SelfCareActivity?>(null) }
    var showEmergencySosDialog by remember { mutableStateOf(false) }
    var showDeepAnalysisDialog by remember { mutableStateOf(false) }
    var activeJournalDetailEntry by remember { mutableStateOf<EmotionCheckIn?>(null) }
    var showWeeklyReportDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var activeSupportContactToConfirm by remember { mutableStateOf<TrustedContact?>(null) }
    var showContactEditorDialog by remember { mutableStateOf(false) }
    var contactBeingEdited by remember { mutableStateOf<TrustedContact?>(null) }

    // Show full Splash Screen Impian with Pomi Bear Mascot for 4.2 seconds on launch
    LaunchedEffect(Unit) {
        delay(4200L)
        showInitialSplash = false
    }

    // Auto-dismiss status banner after 3.2 seconds
    LaunchedEffect(uiState.statusBannerMessage) {
        if (uiState.statusBannerMessage != null) {
            delay(3200L)
            seraViewModel.clearBannerMessage()
        }
    }

    if (showInitialSplash) {
        PastelAuroraBubbleBackground {
            SplashScreenContent(
                onSkipToOnboarding = { showInitialSplash = false }
            )
        }
        return
    }

    if (!uiState.isLoggedIn) {
        AuthAndOnboardingScreen(
            initialName = uiState.userName,
            startOnSplash = false,
            onLoginSuccess = { name, subtitle ->
                seraViewModel.updateAccountLogin(name, subtitle, true)
            }
        )
        return
    }

    val unreadCount = uiState.notifications.count { it.isUnread }
    val topBarSubtitle = when {
        isProfilePrivacyOpen -> "RUANG PRIVASI"
        selectedBottomTab == SeraBottomTab.BERANDA -> "WELLNESS"
        selectedBottomTab == SeraBottomTab.CATAT -> "RUANG TEDUH JIWA"
        selectedBottomTab == SeraBottomTab.RIWAYAT -> "RIWAYAT EMOSI"
        else -> "RUANG PEMULIHAN"
    }

    PastelAuroraBubbleBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SeraTopAppBar(
                    subtitleText = topBarSubtitle,
                    userName = uiState.userName,
                    userPhotoUri = uiState.userPhotoUri,
                    isDarkMode = uiState.privacyState.isDarkModeEnabled,
                    unreadNotificationCount = unreadCount,
                    onToggleDarkMode = { seraViewModel.toggleDarkMode() },
                    onSosClick = { showEmergencySosDialog = true },
                    onNotificationClick = { showNotificationDialog = true },
                    onProfileClick = { isProfilePrivacyOpen = !isProfilePrivacyOpen }
                )
            },
            bottomBar = {
                SeraBottomNavigationBar(
                    selectedTab = selectedBottomTab,
                    onTabSelected = { tab ->
                        isProfilePrivacyOpen = false
                        if (tab != SeraBottomTab.CATAT) {
                            editingCheckInEntry = null
                        }
                        selectedBottomTab = tab
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isProfilePrivacyOpen) {
                    ProfilePrivacyScreen(
                        uiState = uiState,
                        onBackClick = { isProfilePrivacyOpen = false },
                        onUpdateUserProfile = { newName, newSub, newPhoto ->
                            seraViewModel.updateUserProfileIdentity(newName, newSub, newPhoto)
                        },
                        onUpdatePrivacyState = { newPrivacy ->
                            seraViewModel.updatePrivacySettings(newPrivacy)
                        },
                        onExecuteDeletion = { scope ->
                            seraViewModel.executeControlledDataDeletion(scope)
                        },
                        onRestoreDemoData = {
                            seraViewModel.restoreDemoStudentData()
                        },
                        onExportPdfSummary = {
                            val reportText = buildString {
                                appendLine("📄 RINGKASAN JURNAL EMOSI & SELF-CARE SERA")
                                appendLine("Pengguna: ${uiState.userName} (${uiState.userRoleSubtitle})")
                                appendLine("Streak Check-in: ${uiState.streakDays} Hari")
                                appendLine("Total Catatan: ${uiState.checkInHistory.size} Entri")
                                appendLine("----------------------------------------")
                                uiState.checkInHistory.take(7).forEachIndexed { idx, item ->
                                    appendLine("${idx + 1}. [${item.timeLabel}] ${item.emotion.emoji} ${item.emotion.label} (Level ${item.intensity}/5)")
                                    appendLine("   Konteks: ${item.contexts.joinToString { it.shortLabel }} | ${item.sleepQuality.badgeSummary}")
                                    appendLine("   Jurnal: \"${item.journalNote}\"")
                                    if (item.voiceDurationSeconds > 0) {
                                        appendLine("   🎙️ Voice Emotion Journal: ${item.voiceDurationSeconds} detik")
                                    }
                                    appendLine()
                                }
                                appendLine("Dirawat dengan privasi penuh bersama SERA & Pomi 💜")
                            }
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Ringkasan Jurnal Emosi SERA - ${uiState.userName}")
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                }
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Ekspor & Bagikan Ringkasan Jurnal SERA")
                                )
                            } catch (_: Exception) {
                            }
                            seraViewModel.showStatusMessage(
                                "Ringkasan Jurnal (${uiState.checkInHistory.size} catatan) berhasil disiapkan untuk diekspor!"
                            )
                        },
                        onAddOrEditContact = { contact ->
                            contactBeingEdited = contact
                            showContactEditorDialog = true
                        },
                        onOpenWeeklyReport = {
                            showWeeklyReportDialog = true
                        },
                        onLogoutClick = {
                            isProfilePrivacyOpen = false
                            seraViewModel.updateAccountLogin(
                                uiState.userName,
                                uiState.userRoleSubtitle,
                                false
                            )
                        }
                    )
                } else {
                    when (selectedBottomTab) {
                        SeraBottomTab.BERANDA -> {
                            HomeScreen(
                                uiState = uiState,
                                onQuickEmotionSelect = { emotion ->
                                    seraViewModel.quickCheckInFromHome(emotion)
                                },
                                onOpenFullJournal = { emotion ->
                                    journalInitialEmotion = emotion
                                    editingCheckInEntry = null
                                    selectedBottomTab = SeraBottomTab.CATAT
                                },
                                onOpenPatternHistory = {
                                    selectedBottomTab = SeraBottomTab.RIWAYAT
                                },
                                onOpenDeepAnalysis = {
                                    showDeepAnalysisDialog = true
                                },
                                onOpenAllSelfCare = {
                                    selectedBottomTab = SeraBottomTab.SELF_CARE
                                },
                                onStartSelfCareSession = { activity ->
                                    activeSelfCareSession = activity
                                },
                                onSelectSupportContact = { contact ->
                                    activeSupportContactToConfirm = contact
                                },
                                onMascotGreeting = { greeting ->
                                    seraViewModel.showStatusMessage(greeting)
                                }
                            )
                        }

                        SeraBottomTab.CATAT -> {
                            CheckInJournalScreen(
                                userName = uiState.userName,
                                initialEmotion = journalInitialEmotion,
                                editingEntry = editingCheckInEntry,
                                showBackHeader = editingCheckInEntry != null,
                                onBackClick = {
                                    editingCheckInEntry = null
                                    selectedBottomTab = SeraBottomTab.RIWAYAT
                                },
                                onSaveCheckIn = { editingId, emotion, intensity, contexts, sleepQuality, note, voiceSec, voicePath, goSelfCare ->
                                    seraViewModel.saveFullEmotionCheckIn(
                                        editingId = editingId,
                                        emotion = emotion,
                                        intensity = intensity,
                                        contexts = contexts,
                                        sleepQuality = sleepQuality,
                                        journalNote = note,
                                        voiceDurationSeconds = voiceSec,
                                        voiceFilePath = voicePath
                                    )
                                    editingCheckInEntry = null
                                    selectedBottomTab = if (goSelfCare) {
                                        SeraBottomTab.SELF_CARE
                                    } else {
                                        SeraBottomTab.RIWAYAT
                                    }
                                }
                            )
                        }

                        SeraBottomTab.RIWAYAT -> {
                            HistoryPatternScreen(
                                uiState = uiState,
                                onSelectPeriod = { period ->
                                    seraViewModel.selectPatternPeriod(period)
                                },
                                onSelectCalendarDay = { day ->
                                    seraViewModel.selectCalendarDay(day)
                                },
                                onSearchAndFilterChange = { query, ctx, emo ->
                                    seraViewModel.updateHistoryFilter(query, ctx, emo)
                                },
                                onOpenJournalDetail = { entry ->
                                    activeJournalDetailEntry = entry
                                },
                                onEditEntry = { entry ->
                                    editingCheckInEntry = entry
                                    journalInitialEmotion = entry.emotion
                                    selectedBottomTab = SeraBottomTab.CATAT
                                },
                                onDeleteEntry = { entryId ->
                                    seraViewModel.deleteEmotionCheckIn(entryId)
                                },
                                onToggleAppreciate = { entryId ->
                                    seraViewModel.toggleEntryAppreciation(entryId)
                                },
                                onOpenWeeklyReport = {
                                    showWeeklyReportDialog = true
                                },
                                onOpenDeepAnalysis = {
                                    showDeepAnalysisDialog = true
                                }
                            )
                        }

                        SeraBottomTab.SELF_CARE -> {
                            SelfCareAndSupportScreen(
                                uiState = uiState,
                                onUpdatePlannerFilter = { emotion, intensity, duration, cat, bookmarksOnly ->
                                    seraViewModel.updatePlannerParameters(
                                        emotion = emotion,
                                        intensity = intensity,
                                        durationMinutes = duration,
                                        category = cat,
                                        bookmarksOnly = bookmarksOnly
                                    )
                                },
                                onStartActivitySession = { activity ->
                                    activeSelfCareSession = activity
                                },
                                onToggleMicroStep = { stepId ->
                                    seraViewModel.toggleMicroTaskStep(stepId)
                                },
                                onActivateGpsSensor = { granted ->
                                    seraViewModel.activateGpsAndFetchRecommendations(context, granted)
                                },
                                onDisableGpsSensor = {
                                    seraViewModel.disableGpsSensorPrivacy()
                                },
                                onSubmitEvaluation = { activity, rating, note ->
                                    seraViewModel.submitActivityEvaluation(activity, rating, note)
                                },
                                onSelectContactToReach = { contact ->
                                    activeSupportContactToConfirm = contact
                                },
                                onAddOrEditContactClick = { contact ->
                                    contactBeingEdited = contact
                                    showContactEditorDialog = true
                                }
                            )
                        }
                    }
                }

                // Floating Organic Feedback Banner
                AnimatedVisibility(
                    visible = uiState.statusBannerMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SeraPrimaryBlueDark,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.statusBannerMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // FULL-SCREEN INTERACTIVE MODALS & DIALOG

    if (activeSelfCareSession != null) {
        InteractiveSelfCareSessionDialog(
            activity = activeSelfCareSession!!,
            microTaskTitle = uiState.microTaskParentTitle,
            microTaskSteps = uiState.microTaskSteps,
            onToggleMicroStep = { stepId -> seraViewModel.toggleMicroTaskStep(stepId) },
            onUpdateMicroTaskTitle = { newTitle -> seraViewModel.updateMicroTaskTitle(newTitle) },
            onSaveSpontaneousIdea = { note -> seraViewModel.saveSpontaneousIdeaNote(note) },
            onCompleteSession = { completed ->
                seraViewModel.markActivityCompletedForEvaluation(completed)
                activeSelfCareSession = null
                selectedBottomTab = SeraBottomTab.SELF_CARE
            },
            onDismiss = { activeSelfCareSession = null }
        )
    }

    if (showEmergencySosDialog) {
        EmergencySosSanctuaryDialog(
            trustedContacts = uiState.trustedContacts,
            onReachContact = { contact ->
                showEmergencySosDialog = false
                activeSupportContactToConfirm = contact
            },
            onDialEmergencyNumber = { phone ->
                try {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(dialIntent)
                } catch (_: Exception) {
                }
            },
            onDismiss = { showEmergencySosDialog = false }
        )
    }

    if (showDeepAnalysisDialog) {
        DeepEmotionPatternAnalysisDialog(
            checkInCount = uiState.checkInHistory.size,
            privacyState = uiState.privacyState,
            onTogglePatternAnalysis = { allowed ->
                seraViewModel.updatePrivacySettings(
                    uiState.privacyState.copy(allowPatternAnalysis = allowed)
                )
            },
            onDeleteWeekEntries = {
                seraViewModel.executeControlledDataDeletion(DataDeletionScope.JOURNALS_TEXT_ONLY)
                showDeepAnalysisDialog = false
            },
            onDismiss = { showDeepAnalysisDialog = false }
        )
    }

    if (activeJournalDetailEntry != null) {
        val currentEntry = activeJournalDetailEntry!!
        JournalEntryDetailDialog(
            entry = currentEntry,
            onRepeatBreathwork = {
                activeJournalDetailEntry = null
                activeSelfCareSession = uiState.adaptiveRecommendations.firstOrNull()
            },
            onReachSupportCircle = {
                activeJournalDetailEntry = null
                activeSupportContactToConfirm = uiState.trustedContacts.firstOrNull()
            },
            onEditEntry = { entry ->
                activeJournalDetailEntry = null
                editingCheckInEntry = entry
                journalInitialEmotion = entry.emotion
                selectedBottomTab = SeraBottomTab.CATAT
            },
            onDeleteEntry = { entryId ->
                activeJournalDetailEntry = null
                seraViewModel.deleteEmotionCheckIn(entryId)
            },
            onDismiss = { activeJournalDetailEntry = null }
        )
    }

    if (showWeeklyReportDialog && uiState.weeklyReport != null) {
        WeeklyReflectionReportDialog(
            report = uiState.weeklyReport!!,
            onSelectNextWeekCommitment = { commitment ->
                seraViewModel.selectNextWeekCommitment(commitment)
            },
            onDismiss = { showWeeklyReportDialog = false }
        )
    }

    if (showNotificationDialog) {
        NotificationAndSupportSheetDialog(
            notifications = uiState.notifications,
            trustedContacts = uiState.trustedContacts,
            onSelectSupportContact = { contact ->
                activeSupportContactToConfirm = contact
            },
            onOpenWeeklyReport = {
                showDeepAnalysisDialog = true
            },
            onStartSensoryReset = {
                activeSelfCareSession = uiState.adaptiveRecommendations.find {
                    it.moduleType == InteractiveModuleType.SENSORY_RESET
                } ?: uiState.adaptiveRecommendations.firstOrNull()
            },
            onMarkAllRead = {
                seraViewModel.markAllNotificationsRead()
            },
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (activeSupportContactToConfirm != null) {
        SupportCircleConfirmDialog(
            contact = activeSupportContactToConfirm!!,
            userName = uiState.userName,
            userIdentitySubtitle = uiState.userRoleSubtitle,
            onConfirmLaunchWhatsApp = { contact, messageTemplate ->
                activeSupportContactToConfirm = null
                val targetPhone = "6281225905930"
                val encodedMsg = Uri.encode(messageTemplate)
                val waApiUri = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$encodedMsg")
                val waMeUri = Uri.parse("https://wa.me/$targetPhone?text=$encodedMsg")
                try {
                    val directWaIntent = Intent(Intent.ACTION_VIEW, waApiUri).apply {
                        setPackage("com.whatsapp")
                    }
                    context.startActivity(directWaIntent)
                } catch (_: Exception) {
                    try {
                        val fallbackWaIntent = Intent(Intent.ACTION_VIEW, waMeUri)
                        context.startActivity(fallbackWaIntent)
                    } catch (_: Exception) {
                        try {
                            val dialIntent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:+$targetPhone")
                            )
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {
                        }
                    }
                }
            },
            onDismiss = { activeSupportContactToConfirm = null }
        )
    }

    if (showContactEditorDialog) {
        TrustedContactEditorDialog(
            editingContact = contactBeingEdited,
            onSave = { editingId, name, badge, relation, availability, phone ->
                seraViewModel.saveTrustedContact(
                    editingId = editingId,
                    name = name,
                    badgeText = badge,
                    relationSubtitle = relation,
                    availabilityText = availability,
                    phoneNumber = phone
                )
            },
            onDismiss = {
                showContactEditorDialog = false
                contactBeingEdited = null
            }
        )
    }
}
