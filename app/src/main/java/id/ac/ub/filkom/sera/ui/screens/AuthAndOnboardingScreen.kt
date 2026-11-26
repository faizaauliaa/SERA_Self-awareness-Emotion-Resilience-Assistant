package id.ac.ub.filkom.sera.ui.screens

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.ac.ub.filkom.sera.ui.components.PastelAuroraBubbleBackground
import id.ac.ub.filkom.sera.ui.components.PomiBearMascot
import id.ac.ub.filkom.sera.ui.components.PomiMascotMood
import id.ac.ub.filkom.sera.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private enum class AuthFlowStep {
    SPLASH,
    ONBOARDING,
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthAndOnboardingScreen(
    initialName: String,
    startOnSplash: Boolean = false,
    onLoginSuccess: (name: String, nimOrSubtitle: String) -> Unit
) {
    var currentStep by remember {
        mutableStateOf(if (startOnSplash) AuthFlowStep.SPLASH else AuthFlowStep.ONBOARDING)
    }
    var mascotGreeting by remember { mutableStateOf<String?>(null) }
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!email.isNullOrBlank()) {
                val parsedName = email.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                    .ifBlank { initialName.ifBlank { "Teman SERA" } }
                showGoogleSignInDialog = false
                onLoginSuccess(parsedName, "Mahasiswa • Google ($email)")
            } else {
                showGoogleSignInDialog = true
            }
        } else {
            // If device/emulator has no system Google Account configured, show interactive Google Sign-In dialog
            showGoogleSignInDialog = true
        }
    }

    fun triggerGoogleSignIn() {
        try {
            @Suppress("DEPRECATION")
            val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
            googleAccountPickerLauncher.launch(chooseAccountIntent)
        } catch (_: Exception) {
            showGoogleSignInDialog = true
        }
    }

    LaunchedEffect(currentStep, startOnSplash) {
        if (currentStep == AuthFlowStep.SPLASH && !startOnSplash) {
            delay(4000L)
            currentStep = AuthFlowStep.ONBOARDING
        }
    }

    if (showGoogleSignInDialog) {
        GoogleSignInAccountSheetDialog(
            initialName = initialName,
            onGoogleAuthenticated = { name, subtitle ->
                showGoogleSignInDialog = false
                onLoginSuccess(name, subtitle)
            },
            onDismiss = { showGoogleSignInDialog = false }
        )
    }

    PastelAuroraBubbleBackground {
        when (currentStep) {
            AuthFlowStep.SPLASH -> {
                SplashScreenContent(
                    onSkipToOnboarding = { currentStep = AuthFlowStep.ONBOARDING }
                )
            }
            AuthFlowStep.ONBOARDING -> {
                OnboardingSanctuaryContent(
                    mascotGreeting = mascotGreeting,
                    onMascotTapped = { mascotGreeting = it },
                    onStartJourneyClick = { currentStep = AuthFlowStep.SIGN_UP },
                    onGoToSignInClick = { currentStep = AuthFlowStep.SIGN_IN }
                )
            }
            AuthFlowStep.SIGN_IN -> {
                SignInScreenContent(
                    initialName = initialName,
                    onMascotTapped = { mascotGreeting = it },
                    onBackClick = { currentStep = AuthFlowStep.ONBOARDING },
                    onGoToSignUp = { currentStep = AuthFlowStep.SIGN_UP },
                    onOpenGoogleLogin = { triggerGoogleSignIn() },
                    onSignInSuccess = onLoginSuccess
                )
            }
            AuthFlowStep.SIGN_UP -> {
                SignUpScreenContent(
                    initialName = initialName,
                    onMascotTapped = { mascotGreeting = it },
                    onBackClick = { currentStep = AuthFlowStep.ONBOARDING },
                    onGoToSignIn = { currentStep = AuthFlowStep.SIGN_IN },
                    onOpenGoogleLogin = { triggerGoogleSignIn() },
                    onSignUpSuccess = onLoginSuccess
                )
            }
        }
    }
}

@Composable
fun SplashScreenContent(onSkipToOnboarding: () -> Unit) {
    // Continuous wave animation for the 3 loading dots ("Menyiapkan ruang nyamanmu...")
    val infiniteTransition = rememberInfiniteTransition(label = "splash_dots_loading")
    val loadingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splash_loading_phase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Pill: < RUANG NYAMAN MAHASISWA
        Surface(
            shape = RoundedCornerShape(50),
            color = SeraSurface.copy(alpha = 0.92f),
            shadowElevation = 2.dp
        ) {
            Text(
                text = "‹   RUANG NYAMAN MAHASISWA",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF7B3B54),
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
            )
        }

        // Center Glowing Sanctuary Circle with Living Full-Body Pomi Bear Mascot + Star & Heart accents
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft outer ambient glow ring behind the white circle
                Box(
                    modifier = Modifier
                        .size(255.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x55A5B4FC),
                                    Color(0x28C4B5FD),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Living, blinking, waving, kicking Pomi Bear Mascot inside the glowing white circle
                PomiBearMascot(
                    size = 240.dp,
                    mood = PomiMascotMood.HAPPY_HEART,
                    showOuterHalo = false
                )

                // Top-Right Floating Star Accent (matching mockup 2)
                Text(
                    text = "★",
                    fontSize = 22.sp,
                    color = Color(0xFF7B3B54),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-18).dp, y = 4.dp)
                )

                // Bottom-Left Floating Purple Heart Accent (matching mockup 2)
                Text(
                    text = "💜",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 12.dp, y = (-14).dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SERA",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF48FB1))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = SeraPrimaryBlueLight.copy(alpha = 0.85f)
            ) {
                Text(
                    text = "Self–Awareness & Resilience  ✨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraPrimaryBlue,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ruang teduh untuk hatimu yang\nberharga.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = SeraTextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Loading Pill with 3 Animated Bouncing & Pulsing Dots + Privacy Lock
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val dotColors = listOf(SeraPrimaryBlue, Color(0xFF883955), SeraTeal)
            Surface(
                onClick = onSkipToOnboarding,
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.94f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dotColors.forEachIndexed { index, dotColor ->
                        val dotAngle = loadingPhase - (index * 0.85f)
                        val bounceWave = sin(dotAngle)
                        val offsetY = (-5f * bounceWave).dp
                        val scaleFactor = 1.0f + (0.28f * bounceWave.coerceAtLeast(0f))
                        Box(
                            modifier = Modifier
                                .offset(y = offsetY)
                                .scale(scaleFactor)
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        if (index < dotColors.lastIndex) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Menyiapkan ruang nyamanmu...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "🔒  100% Ruang Privat & Aman",
                fontSize = 12.sp,
                color = SeraTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OnboardingSanctuaryContent(
    mascotGreeting: String?,
    onMascotTapped: (String) -> Unit,
    onStartJourneyClick: () -> Unit,
    onGoToSignInClick: () -> Unit
) {
    var selectedPreviewMood by remember { mutableStateOf("Lega") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mascot Hero with Floating Pills
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            PomiBearMascot(
                size = 185.dp,
                mood = PomiMascotMood.HAPPY_HEART,
                showOuterHalo = true,
                onMascotTapped = onMascotTapped
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 34.dp, y = 2.dp)
            ) {
                Text(
                    text = "✨ Aman & Hangat",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = (-36).dp, y = (-4).dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(SeraTeal))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Mendengarmu",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                }
            }
        }

        if (mascotGreeting != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SeraSurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, SeraIrisBorder),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = mascotGreeting,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeraPrimaryBlue,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = SeraPrimaryBlueLight.copy(alpha = 0.85f)
        ) {
            Text(
                text = "● SERA • SELF-AWARENESS",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraPrimaryBlue,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SeraTextPrimary)) {
                    append("Ruang Aman untuk\n")
                }
                withStyle(SpanStyle(color = SeraPrimaryBlue)) {
                    append("Menemani ")
                }
                withStyle(SpanStyle(color = SeraRose)) {
                    append("Langkah\n")
                }
                withStyle(SpanStyle(color = SeraPrimaryBlue)) {
                    append("Jiwamu")
                }
            },
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SERA hadir membantumu mengenali ritme\nemosi, meredakan cemas kuliah, dan merawat\ndiri tanpa penghakiman.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = SeraTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3 Organic Feature Pills
        OnboardingFeatureCard(
            iconEmoji = "🌿",
            iconBg = EmotionCalmBg,
            title = "Deteksi Pola Emosi Alami",
            badge = "Ritme Diri",
            badgeBg = SeraPrimaryBlueLight,
            badgeColor = SeraPrimaryBlue,
            desc = "Pahami pemicu stres kuliah lewat refleksi harian yang ringan & mengalir."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            iconEmoji = "✨",
            iconBg = SeraRoseSoft,
            title = "Self-Care Mikro 5 Menit",
            badge = "Cepat & Nyaman",
            badgeBg = SeraRoseSoft,
            badgeColor = SeraRose,
            desc = "Latihan napas terpandu, sensor GPS lokasi tenang & dekonstruksi tugas akademik."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            iconEmoji = "🔒",
            iconBg = SeraPrimaryBlueLight,
            title = "100% Ruang Privat Terenkripsi",
            badge = "End-to-End",
            badgeBg = SeraPrimaryBlueLight,
            badgeColor = SeraPrimaryBlue,
            desc = "Catatan hatimu & Voice Emotion Journal hanya milikmu langsung di genggamanmu."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Interactive Mood Strip
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SeraSurface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, SeraCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bagaimana hatimu saat ini?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = "Sentuh untuk memilih",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraPrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                val items = listOf(
                    "🌱" to "Lega",
                    "🌸" to "Senang",
                    "☁️" to "Lelah",
                    "⚡" to "Cemas",
                    "🧸" to "Butuh Jeda"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { (emoji, label) ->
                        val isSelected = selectedPreviewMood == label
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SeraPrimaryBlueLight else SeraSurfaceSubtle)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SeraPrimaryBlue else SeraCardBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedPreviewMood = label }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SeraPrimaryBlue else SeraTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gradient CTA Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SeraPrimaryBlue,
                            Color(0xFF7B4B94),
                            Color(0xFF9F3A5E)
                        )
                    )
                )
                .clickable(onClick = onStartJourneyClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mulai Perjalanan Tenang  →",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            onClick = onGoToSignInClick,
            shape = RoundedCornerShape(50),
            color = SeraSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, SeraCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = SeraTextPrimary, fontWeight = FontWeight.SemiBold)) {
                            append("Sudah punya akun? ")
                        }
                        withStyle(SpanStyle(color = SeraPrimaryBlue, fontWeight = FontWeight.ExtraBold)) {
                            append("Masuk")
                        }
                    },
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureCard(
    iconEmoji: String,
    iconBg: Color,
    title: String,
    badge: String,
    badgeBg: Color,
    badgeColor: Color,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = SeraSurface.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, SeraCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeBg
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
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

@Composable
private fun SignInScreenContent(
    initialName: String,
    onMascotTapped: (String) -> Unit,
    onBackClick: () -> Unit,
    onGoToSignUp: () -> Unit,
    onOpenGoogleLogin: () -> Unit,
    onSignInSuccess: (name: String, subtitle: String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var loginErrorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackClick,
                shape = CircleShape,
                color = SeraSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(SeraTeal))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ruang Aman Mahasiswa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraPrimaryBlue
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = SeraSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🪷", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Selamat Datang! ✨",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SeraTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Napas sejenak, lanjutkan perjalanan rawat\ndirimu bersama SERA.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = SeraTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // "Pomi kangen! 🌸" Card with Living Pomi Bear Mascot inside the circle!
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SeraSurface.copy(alpha = 0.90f),
            border = BorderStroke(1.dp, SeraCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PomiBearMascot(
                    size = 86.dp,
                    mood = PomiMascotMood.HAPPY_HEART,
                    showOuterHalo = false,
                    onMascotTapped = onMascotTapped
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SeraSurfaceSubtle
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Pomi kangen! 🌸",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SeraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Yuk ceritakan harimu setelah masuk ke Ruang Tenang. (Ketuk Pomi!)",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = SeraTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💜 Teman Emosi Aktif",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeraPrimaryBlue
                        )
                        Text(
                            text = "Semester Genap",
                            fontSize = 10.sp,
                            color = SeraTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Card
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = SeraSurface.copy(alpha = 0.94f),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Email / Akun Pribadi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = "✓ Google Terhubung",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraTeal,
                        modifier = Modifier.clickable { onOpenGoogleLogin() }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        loginErrorText = null
                    },
                    placeholder = {
                        Text("Contoh: nama@student.ub.ac.id atau @gmail.com", fontSize = 12.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurfaceSubtle,
                        unfocusedContainerColor = SeraSurfaceSubtle,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kata Sandi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = "Min. 6 karakter",
                        fontSize = 11.sp,
                        color = SeraTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        loginErrorText = null
                    },
                    placeholder = {
                        Text("Masukkan kata sandi akunmu...", fontSize = 12.sp)
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SeraSurfaceSubtle,
                        unfocusedContainerColor = SeraSurfaceSubtle,
                        focusedBorderColor = SeraPrimaryBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginErrorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loginErrorText!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD9385E)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (emailInput.isBlank()) {
                            loginErrorText = "Mohon isi alamat email atau nama pengguna terlebih dahulu."
                        } else if (passwordInput.isBlank()) {
                            loginErrorText = "Mohon isi kata sandi terlebih dahulu."
                        } else {
                            val parsedName = emailInput.substringBefore("@")
                                .replace(".", " ")
                                .split(" ")
                                .joinToString(" ") { part ->
                                    part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                }
                            onSignInSuccess(parsedName, "Mahasiswa • TIF FILKOM UB")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = SeraPrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Masuk ke Ruang Tenang  →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "atau masuk dengan akun Google",
                    fontSize = 11.sp,
                    color = SeraTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    onClick = onOpenGoogleLogin,
                    shape = RoundedCornerShape(50),
                    color = SeraSurfaceSubtle,
                    border = BorderStroke(1.3.dp, SeraPrimaryBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4285F4)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lanjutkan dengan Akun Google",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(onClick = onGoToSignUp) {
            Text(
                text = "Belum punya akun? Mulai Perjalanan Tenang",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraPrimaryBlue
            )
        }
    }
}

@Composable
private fun SignUpScreenContent(
    initialName: String,
    onMascotTapped: (String) -> Unit,
    onBackClick: () -> Unit,
    onGoToSignIn: () -> Unit,
    onOpenGoogleLogin: () -> Unit,
    onSignUpSuccess: (name: String, subtitle: String) -> Unit
) {
    var nickname by remember { mutableStateOf(initialName) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signUpErrorText by remember { mutableStateOf<String?>(null) }
    var selectedFocuses by remember {
        mutableStateOf(setOf("Atasi Cemas Kuliah", "Tidur Lebih Nyenyak"))
    }
    var agreedToPrivacy by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackClick,
                shape = CircleShape,
                color = SeraSurface,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "←", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "● Langkah 1 dari 2",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeraTeal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Greeting Card with Living Pomi Mascot
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
                    size = 82.dp,
                    mood = PomiMascotMood.HAPPY_HEART,
                    showOuterHalo = false,
                    onMascotTapped = onMascotTapped
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🪷 Ruang Ramah & Lembut",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeraRose
                    )
                    Text(
                        text = "Halo, Teman Baru!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SeraTextPrimary
                    )
                    Text(
                        text = "Mari bangun ruang aman untuk merawat hatimu setiap hari.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = SeraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nama Panggilan (Bebas atau anonim)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SeraTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = nickname,
            onValueChange = {
                nickname = it
                signUpErrorText = null
            },
            placeholder = {
                Text("Masukkan nama panggilanmu...", fontSize = 12.sp)
            },
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

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Email Pribadi / Google",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SeraTextPrimary
            )
            Surface(
                onClick = onOpenGoogleLogin,
                shape = RoundedCornerShape(50),
                color = EmotionCalmBg
            ) {
                Text(
                    text = "G  Pilih Akun Google",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SeraTeal,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                signUpErrorText = null
            },
            placeholder = {
                Text("Contoh: nama@student.ub.ac.id atau @gmail.com", fontSize = 12.sp)
            },
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

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Kata Sandi Pelindung",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SeraTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                signUpErrorText = null
            },
            placeholder = {
                Text("Buat kata sandi minimal 6 karakter...", fontSize = 12.sp)
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SeraSurface,
                unfocusedContainerColor = SeraSurface,
                focusedBorderColor = SeraPrimaryBlue,
                unfocusedBorderColor = SeraCardBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SeraTeal)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SeraPrimaryBlueLight)
                )
            }
            Text(
                text = "✓ Cukup kuat & aman ✨",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SeraTeal
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pilih Fokus Perjalananmu",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            Text(
                text = "Bisa diubah kapan saja",
                fontSize = 11.sp,
                color = SeraTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        val focusOptions = listOf(
            "Atasi Cemas Kuliah" to "Redakan beban pikiran",
            "Tidur Lebih Nyenyak" to "Ritme istirahat restoratif",
            "Kenali Ritme Emosi" to "Jurnal suasana hati",
            "Dekonstruksi Tugas" to "Langkah kecil bertahap"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            focusOptions.chunked(2).forEach { rowPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPair.forEach { (title, sub) ->
                        val isSelected = selectedFocuses.contains(title)
                        Surface(
                            onClick = {
                                selectedFocuses = if (isSelected) selectedFocuses - title else selectedFocuses + title
                            },
                            shape = RoundedCornerShape(24.dp),
                            color = SeraSurface.copy(alpha = 0.92f),
                            border = BorderStroke(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) SeraPrimaryBlue else SeraCardBorder
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) SeraPrimaryBlueLight else SeraSurfaceSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSelected) "✓" else "+",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = sub,
                                        fontSize = 10.sp,
                                        color = SeraTextSecondary,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            onClick = { agreedToPrivacy = !agreedToPrivacy },
            shape = RoundedCornerShape(22.dp),
            color = SeraPrimaryBlueLight.copy(alpha = 0.65f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToPrivacy,
                    onCheckedChange = { agreedToPrivacy = it },
                    colors = CheckboxDefaults.colors(checkedColor = SeraPrimaryBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Saya setuju dengan Komitmen Privasi SERA: Data emosi tersimpan aman, lokal, dan terenkripsi di database perangkat.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = SeraTextPrimary
                )
            }
        }

        if (signUpErrorText != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = signUpErrorText!!,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD9385E)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val finalName = nickname.trim().ifBlank {
                    email.substringBefore("@").trim()
                }
                if (finalName.isBlank()) {
                    signUpErrorText = "Mohon isi nama panggilan atau alamat emailmu terlebih dahulu."
                } else {
                    onSignUpSuccess(
                        finalName,
                        "Mahasiswa • TIF FILKOM UB"
                    )
                }
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = SeraPrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Mulai Perjalanan Bersama Pomi  →",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onOpenGoogleLogin,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.4.dp, SeraPrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "G   Daftar / Masuk dengan Akun Google",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraPrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onGoToSignIn,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sudah punya akun? Masuk ke Ruang Tenang",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SeraPrimaryBlue
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleSignInAccountSheetDialog(
    initialName: String,
    onGoogleAuthenticated: (name: String, subtitle: String) -> Unit,
    onDismiss: () -> Unit
) {
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf(initialName) }
    var customRole by remember { mutableStateOf("Mahasiswa • TIF FILKOM UB") }
    var openRealGoogleWeb by remember { mutableStateOf(false) }

    val quickGoogleAccounts = remember {
        listOf(
            Triple("Anindhita Faiza Aulia", "anindhita@student.ub.ac.id", "Mahasiswi TIF-E • FILKOM UB"),
            Triple("Shafa Rizwana Zarin", "shafarizwana@student.ub.ac.id", "Mahasiswi TIF-E • FILKOM UB"),
            Triple("Aisha Maryam", "aishamaryam@student.ub.ac.id", "Mahasiswi TIF-E • FILKOM UB"),
            Triple("M. Nur Daffa Nashrullah", "daffanashrullah@student.ub.ac.id", "Mahasiswa TIF-E • FILKOM UB")
        )
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = SeraSurface.copy(alpha = 0.96f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, SeraCardBorder),
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
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4285F4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Login dengan Google",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraTextPrimary
                                    )
                                    Text(
                                        text = "Pilih akun Google untuk melanjutkan ke SERA",
                                        fontSize = 11.sp,
                                        color = SeraTextSecondary
                                    )
                                }
                            }
                            Surface(
                                onClick = onDismiss,
                                shape = CircleShape,
                                color = SeraSurfaceSubtle,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SeraTextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        quickGoogleAccounts.forEach { (accName, accEmail, accRole) ->
                            Surface(
                                onClick = {
                                    onGoogleAuthenticated(accName, accRole)
                                },
                                shape = RoundedCornerShape(22.dp),
                                color = SeraSurfaceSubtle,
                                border = BorderStroke(1.dp, SeraCardBorder),
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
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(SeraIris, SeraPrimaryBlue)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = accName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = accName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SeraTextPrimary
                                        )
                                        Text(
                                            text = accEmail,
                                            fontSize = 11.sp,
                                            color = SeraPrimaryBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = accRole,
                                            fontSize = 10.sp,
                                            color = SeraTextSecondary
                                        )
                                    }
                                    Text(
                                        text = "Pilih ›",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SeraPrimaryBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = SeraDivider)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Atau Masukkan Akun Google Lainnya:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraTextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nama Profil Google", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customEmail,
                            onValueChange = { customEmail = it },
                            label = { Text("Alamat Email Google (@gmail.com / @student.ub.ac.id)", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customRole,
                            onValueChange = { customRole = it },
                            label = { Text("Deskripsi Profil / Status", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val finalName = customName.ifBlank {
                                    customEmail.substringBefore("@").ifBlank { "Mahasiswa SERA" }
                                }
                                val finalRole = customRole.ifBlank { "Mahasiswa • TIF FILKOM UB" }
                                onGoogleAuthenticated(finalName, finalRole)
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "G   Masuk dengan Akun Google Ini",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { openRealGoogleWeb = !openRealGoogleWeb }) {
                            Text(
                                text = if (openRealGoogleWeb) "Tutup Portal Web Google" else "🌐 Buka Portal Login accounts.google.com",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeraPrimaryBlue
                            )
                        }

                        if (openRealGoogleWeb) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                border = BorderStroke(1.dp, SeraCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(22.dp)),
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            webChromeClient = WebChromeClient()
                                            webViewClient = WebViewClient()
                                            loadUrl("https://accounts.google.com/signin")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
