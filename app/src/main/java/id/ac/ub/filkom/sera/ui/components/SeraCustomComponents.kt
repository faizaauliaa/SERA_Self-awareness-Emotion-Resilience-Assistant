package id.ac.ub.filkom.sera.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ac.ub.filkom.sera.data.model.EmotionCheckIn
import id.ac.ub.filkom.sera.data.model.EmotionType
import id.ac.ub.filkom.sera.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// =============================================================================================
// 1. LIGHTWEIGHT PASTEL AURORA + FLOATING BUBBLES BACKGROUND
// =============================================================================================

private data class BubbleSpec(
    val xFraction: Float,
    val baseYFraction: Float,
    val radiusDp: Float,
    val speedFactor: Float,
    val swayAmplitude: Float,
    val phaseOffset: Float,
    val tintColor: Color
)

private val PresetBubbles = listOf(
    BubbleSpec(0.12f, 0.88f, 26f, 1.0f, 0.035f, 0.0f, Color(0xFFA7F3D0)),
    BubbleSpec(0.84f, 0.92f, 34f, 1.0f, 0.040f, 0.22f, Color(0xFFFBCFE8)),
    BubbleSpec(0.26f, 0.65f, 18f, 1.0f, 0.025f, 0.45f, Color(0xFFFDE68A)),
    BubbleSpec(0.72f, 0.54f, 24f, 1.0f, 0.030f, 0.61f, Color(0xFFBAE6FD)),
    BubbleSpec(0.48f, 0.78f, 30f, 1.0f, 0.035f, 0.14f, Color(0xFFDDD6FE)),
    BubbleSpec(0.16f, 0.36f, 22f, 1.0f, 0.028f, 0.78f, Color(0xFFFECDD3)),
    BubbleSpec(0.88f, 0.28f, 28f, 1.0f, 0.032f, 0.35f, Color(0xFFA7F3D0)),
    BubbleSpec(0.62f, 0.18f, 20f, 1.0f, 0.025f, 0.89f, Color(0xFFFEF08A)),
    BubbleSpec(0.35f, 0.42f, 38f, 1.0f, 0.042f, 0.52f, Color(0xFFBAE6FD)),
    BubbleSpec(0.08f, 0.15f, 16f, 1.0f, 0.020f, 0.67f, Color(0xFFDDD6FE)),
    BubbleSpec(0.92f, 0.70f, 21f, 1.0f, 0.030f, 0.11f, Color(0xFFFDE68A)),
    BubbleSpec(0.54f, 0.96f, 25f, 1.0f, 0.028f, 0.93f, Color(0xFFA7F3D0))
)

@Composable
fun PastelAuroraBubbleBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pastel_bubble_drift")
    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_phase"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val isDark = SeraThemeMode.isDarkMode

            drawRect(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0xFF100E20),
                            Color(0xFF15172E),
                            Color(0xFF12232D),
                            Color(0xFF1E1930),
                            Color(0xFF151226)
                        )
                    } else {
                        listOf(
                            Color(0xFFF3EEFF),
                            Color(0xFFE7F6FF),
                            Color(0xFFE8FAF2),
                            Color(0xFFFFF9E6),
                            Color(0xFFFEEEF5)
                        )
                    }
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x3334D399) else Color(0x55A7F3D0),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.22f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.15f, h * 0.22f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x28A78BFA) else Color(0x55FDE68A),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.45f),
                    radius = w * 0.60f
                ),
                radius = w * 0.60f,
                center = Offset(w * 0.88f, h * 0.45f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x2EF472B6) else Color(0x50FBCFE8),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.20f, h * 0.78f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.20f, h * 0.78f)
            )

            val densityScale = density
            PresetBubbles.forEach { b ->
                val rawProgress = (b.baseYFraction - driftPhase * b.speedFactor + b.phaseOffset) % 1.0f
                val normalizedY = if (rawProgress < 0f) rawProgress + 1f else rawProgress
                val angle = (driftPhase + b.phaseOffset) * 2f * PI.toFloat()
                val cx = (b.xFraction + sin(angle) * b.swayAmplitude) * w
                val cy = normalizedY * h
                val r = b.radiusDp * densityScale

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.20f else 0.58f),
                            b.tintColor.copy(alpha = if (isDark) 0.18f else 0.36f),
                            b.tintColor.copy(alpha = if (isDark) 0.06f else 0.12f)
                        ),
                        center = Offset(cx - r * 0.25f, cy - r * 0.25f),
                        radius = r * 1.2f
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = if (isDark) 0.24f else 0.65f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f * densityScale)
                )
                drawCircle(
                    color = Color.White.copy(alpha = if (isDark) 0.35f else 0.82f),
                    radius = r * 0.20f,
                    center = Offset(cx - r * 0.34f, cy - r * 0.34f)
                )
            }
        }

        content()
    }
}

// =============================================================================================
// 2. POMI — FULL-BODY ARTICULATED LIVING PURPLE BEAR MASCOT
//    (Seluruh Tubuh Bergerak Hidup: Kepala, Telinga, Mata, Alis, Mulut/Lidah, Badan, Tangan Melambai, Kaki Menendang!)
// =============================================================================================

enum class PomiMascotMood {
    HAPPY_HEART,
    SLEEPY_HEADPHONES,
    COMFORTING_SOS,
    LISTENING
}

@Composable
fun PomiBearMascot(
    size: Dp = 150.dp,
    mood: PomiMascotMood = PomiMascotMood.HAPPY_HEART,
    showOuterHalo: Boolean = true,
    onMascotTapped: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    // Continuous full-body lifecycle clock (0..2*PI)
    val infiniteTransition = rememberInfiniteTransition(label = "pomi_full_body")
    val cycleRad by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pomi_cycle_rad"
    )
    // Secondary slower cycle for pupil gaze & mouth talking
    val talkCycleRad by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pomi_talk_rad"
    )

    // Natural automatic blinking every ~2.8 seconds
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2700L)
            isBlinking = true
            delay(160L)
            isBlinking = false
            delay(130L)
            if (System.currentTimeMillis() % 2L == 0L) {
                isBlinking = true
                delay(140L)
                isBlinking = false
            }
        }
    }

    // Interactive Tap/Click Full-Body Dance & Talking Reaction!
    var tapCount by remember { mutableIntStateOf(0) }
    val danceJumpY = remember { Animatable(0f) }
    val danceSpinAngle = remember { Animatable(0f) }
    val armCheerBoost = remember { Animatable(0f) }
    val mouthTalkBoost = remember { Animatable(0f) }
    val heartBurstProgress = remember { Animatable(0f) }

    val pomiGreetings = remember {
        listOf(
            "Yay! Pomi senang kamu ajak main! Lihat tangan & kaki Pomi goyang-goyang 💜",
            "Halo sahabatku! Tarik napas pelan-pelan ya, kamu hebat banget hari ini ✨",
            "Kalau tugas kuliah bikin pusing, istirahat 5 menit bareng Pomi yuk 🌸",
            "Peluk hangat dari Pomi! Semua perasaanmu berharga dan aman di sini 🧸"
        )
    }

    fun triggerFullBodyDance() {
        val greeting = pomiGreetings[tapCount % pomiGreetings.size]
        tapCount += 1
        onMascotTapped?.invoke(greeting)

        // 1. Whole body jump & bounce
        scope.launch {
            danceJumpY.snapTo(0f)
            danceJumpY.animateTo(-18f, tween(150, easing = FastOutSlowInEasing))
            danceJumpY.animateTo(6f, tween(130))
            danceJumpY.animateTo(-12f, tween(140))
            danceJumpY.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        // 2. Joyful body & head sway
        scope.launch {
            danceSpinAngle.snapTo(0f)
            danceSpinAngle.animateTo(-14f, tween(120))
            danceSpinAngle.animateTo(14f, tween(150))
            danceSpinAngle.animateTo(-10f, tween(130))
            danceSpinAngle.animateTo(8f, tween(120))
            danceSpinAngle.animateTo(0f, tween(140))
        }
        // 3. Arms wave up high & feet kick fast
        scope.launch {
            armCheerBoost.snapTo(1f)
            armCheerBoost.animateTo(0f, tween(1100, easing = LinearOutSlowInEasing))
        }
        // 4. Mouth chatters happily
        scope.launch {
            mouthTalkBoost.snapTo(1f)
            delay(350L)
            mouthTalkBoost.animateTo(0.3f, tween(150))
            mouthTalkBoost.animateTo(1f, tween(150))
            delay(300L)
            mouthTalkBoost.animateTo(0f, tween(250))
        }
        // 5. Floating heart burst
        scope.launch {
            heartBurstProgress.snapTo(0.05f)
            heartBurstProgress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
            heartBurstProgress.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                triggerFullBodyDance()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val minDim = this.size.minDimension

            // 1. Optional Dashed Outer Halo
            if (showOuterHalo) {
                val outerR = minDim * 0.47f
                drawCircle(
                    color = Color(0xFFD6C7FF),
                    radius = outerR,
                    center = center,
                    style = Stroke(
                        width = 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )
            }

            // Inner soft 3D cushion circle background
            val circleR = if (showOuterHalo) minDim * 0.41f else minDim * 0.46f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF3EEFF),
                        Color(0xFFE7DEFF)
                    ),
                    center = Offset(center.x - circleR * 0.2f, center.y - circleR * 0.25f),
                    radius = circleR * 1.3f
                ),
                radius = circleR,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = circleR,
                center = center,
                style = Stroke(width = 6f)
            )

            // Kinematic variables for articulated limbs
            val isSleepy = mood == PomiMascotMood.SLEEPY_HEADPHONES
            val motionSpeedMultiplier = if (isSleepy) 0.45f else 1.0f
            val cheer = armCheerBoost.value

            // Body bounce & squash-stretch
            val idleBobY = sin(cycleRad) * (h * 0.018f) * motionSpeedMultiplier
            val totalJumpY = idleBobY + (danceJumpY.value * (h / 200f))
            val torsoScaleX = 1f + cos(cycleRad) * 0.025f * motionSpeedMultiplier + cheer * 0.04f
            val torsoScaleY = 1f + sin(cycleRad) * 0.032f * motionSpeedMultiplier - cheer * 0.02f

            // Head tilt & bob
            val headTilt = sin(cycleRad * 1.0f) * 6.5f * motionSpeedMultiplier + danceSpinAngle.value
            val headBob = cos(cycleRad) * (h * 0.014f) * motionSpeedMultiplier

            // Left & Right Ear independent flapping
            val leftEarAngle = sin(cycleRad * 2f) * (10f + cheer * 18f) * motionSpeedMultiplier
            val rightEarAngle = -sin(cycleRad * 2f + 0.8f) * (10f + cheer * 18f) * motionSpeedMultiplier

            // Left & Right Arm waving angles
            val leftArmWave = sin(cycleRad * 2f) * (16f + cheer * 38f) * motionSpeedMultiplier - (cheer * 28f)
            val rightArmWave = -sin(cycleRad * 2f + 0.5f) * (16f + cheer * 38f) * motionSpeedMultiplier + (cheer * 28f)

            // Left & Right Leg/Foot alternating kicking & paddling
            val leftFootKickY = sin(cycleRad * 2f) * (h * 0.028f) * (1f + cheer) * motionSpeedMultiplier
            val rightFootKickY = -sin(cycleRad * 2f) * (h * 0.028f) * (1f + cheer) * motionSpeedMultiplier
            val leftFootTilt = sin(cycleRad * 2f) * (15f + cheer * 20f)
            val rightFootTilt = -sin(cycleRad * 2f) * (15f + cheer * 20f)

            // Mouth opening factor (idle talking rhythm + tap boost)
            val idleMouthOpen = ((sin(talkCycleRad * 2f) + 0.35f).coerceAtLeast(0f) / 1.35f) *
                (if (isSleepy) 0.15f else 0.75f)
            val mouthOpen = (idleMouthOpen + mouthTalkBoost.value).coerceIn(0f, 1f)

            // Eye pupil gaze movement
            val pupilShiftX = sin(talkCycleRad) * (minDim * 0.008f)
            val pupilShiftY = cos(talkCycleRad * 2f) * (minDim * 0.004f)

            // Dynamic shadow under Pomi (shrinks when Pomi jumps up!)
            val shadowScale = (1f + totalJumpY / (h * 0.25f)).coerceIn(0.7f, 1.15f)
            drawOval(
                color = Color(0x286653E0),
                topLeft = Offset(w * (0.5f - 0.22f * shadowScale), h * 0.73f),
                size = Size(w * 0.44f * shadowScale, h * 0.075f * shadowScale)
            )

            val bearPurpleLight = Color(0xFFDCCBFF)
            val bearPurpleMid = Color(0xFFB799FA)
            val bearPurpleDeep = Color(0xFF9370DB)
            val innerEarPink = Color(0xFFF8BBD0)
            val cheekBlushPink = Color(0xFFF48FB1)

            translate(left = 0f, top = totalJumpY) {
                // --- 1. ARTICULATED TORSO & TUMMY ---
                scale(
                    scaleX = torsoScaleX,
                    scaleY = torsoScaleY,
                    pivot = Offset(w * 0.5f, h * 0.62f)
                ) {
                    val bodyTopLeft = Offset(w * 0.31f, h * 0.49f)
                    val bodySize = Size(w * 0.38f, h * 0.26f)
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                            center = Offset(w * 0.46f, h * 0.57f),
                            radius = w * 0.28f
                        ),
                        topLeft = bodyTopLeft,
                        size = bodySize,
                        cornerRadius = CornerRadius(w * 0.17f, w * 0.17f)
                    )

                    // Soft lighter tummy patch that expands with breath
                    drawOval(
                        color = Color(0xFFEDE4FF).copy(alpha = 0.72f),
                        topLeft = Offset(w * 0.38f, h * 0.535f),
                        size = Size(w * 0.24f, h * 0.18f)
                    )
                }

                // --- 2. ARTICULATED KICKING / PADDLING FEET (LEFT & RIGHT LEGS) ---
                val footR = minDim * 0.076f

                // Left Leg & Foot
                val leftFootCenter = Offset(w * 0.355f, h * 0.71f + leftFootKickY)
                rotate(degrees = leftFootTilt, pivot = leftFootCenter) {
                    // Little thigh connector
                    drawRoundRect(
                        color = bearPurpleMid,
                        topLeft = Offset(leftFootCenter.x - footR * 0.5f, h * 0.64f),
                        size = Size(footR * 1.1f, footR * 1.1f),
                        cornerRadius = CornerRadius(footR * 0.5f, footR * 0.5f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                            center = leftFootCenter,
                            radius = footR * 1.2f
                        ),
                        radius = footR,
                        center = leftFootCenter
                    )
                    // Pink paw pad + 3 little toe beans!
                    drawCircle(
                        color = innerEarPink.copy(alpha = 0.8f),
                        radius = footR * 0.42f,
                        center = Offset(leftFootCenter.x, leftFootCenter.y + footR * 0.12f)
                    )
                    for (tIdx in -1..1) {
                        drawCircle(
                            color = innerEarPink.copy(alpha = 0.85f),
                            radius = footR * 0.14f,
                            center = Offset(
                                leftFootCenter.x + tIdx * (footR * 0.32f),
                                leftFootCenter.y - footR * 0.32f
                            )
                        )
                    }
                }

                // Right Leg & Foot
                val rightFootCenter = Offset(w * 0.645f, h * 0.71f + rightFootKickY)
                rotate(degrees = rightFootTilt, pivot = rightFootCenter) {
                    drawRoundRect(
                        color = bearPurpleMid,
                        topLeft = Offset(rightFootCenter.x - footR * 0.6f, h * 0.64f),
                        size = Size(footR * 1.1f, footR * 1.1f),
                        cornerRadius = CornerRadius(footR * 0.5f, footR * 0.5f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                            center = rightFootCenter,
                            radius = footR * 1.2f
                        ),
                        radius = footR,
                        center = rightFootCenter
                    )
                    drawCircle(
                        color = innerEarPink.copy(alpha = 0.8f),
                        radius = footR * 0.42f,
                        center = Offset(rightFootCenter.x, rightFootCenter.y + footR * 0.12f)
                    )
                    for (tIdx in -1..1) {
                        drawCircle(
                            color = innerEarPink.copy(alpha = 0.85f),
                            radius = footR * 0.14f,
                            center = Offset(
                                rightFootCenter.x + tIdx * (footR * 0.32f),
                                rightFootCenter.y - footR * 0.32f
                            )
                        )
                    }
                }

                // --- 3. GLOWING PASTEL CRYSTAL HEART ON CHEST ---
                val heartBounceY = sin(cycleRad * 2f) * (h * 0.012f)
                val hcX = w * 0.50f
                val hcY = h * 0.60f + heartBounceY
                val hr = minDim * 0.066f * (1f + sin(cycleRad) * 0.08f + cheer * 0.15f)

                val heartPath = Path().apply {
                    moveTo(hcX, hcY + hr * 0.85f)
                    cubicTo(
                        hcX - hr * 1.4f, hcY + hr * 0.1f,
                        hcX - hr * 0.9f, hcY - hr * 0.95f,
                        hcX, hcY - hr * 0.35f
                    )
                    cubicTo(
                        hcX + hr * 0.9f, hcY - hr * 0.95f,
                        hcX + hr * 1.4f, hcY + hr * 0.1f,
                        hcX, hcY + hr * 0.85f
                    )
                    close()
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x99F48FB1),
                            Color(0x4490CAF9),
                            Color.Transparent
                        ),
                        center = Offset(hcX, hcY),
                        radius = hr * 2.2f
                    ),
                    radius = hr * 2.2f,
                    center = Offset(hcX, hcY)
                )
                drawPath(
                    path = heartPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD1E8),
                            Color(0xFFE1BEE7),
                            Color(0xFFB3E5FC)
                        ),
                        start = Offset(hcX - hr, hcY - hr),
                        end = Offset(hcX + hr, hcY + hr)
                    )
                )

                // --- 4. ARTICULATED WAVING ARMS / HANDS (LEFT & RIGHT) ---
                val leftShoulderPivot = Offset(w * 0.33f, h * 0.54f)
                rotate(degrees = leftArmWave, pivot = leftShoulderPivot) {
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                            center = Offset(w * 0.36f, h * 0.57f),
                            radius = minDim * 0.08f
                        ),
                        topLeft = Offset(w * 0.28f, h * 0.525f),
                        size = Size(w * 0.15f, h * 0.09f)
                    )
                    // Little pink paw pad on hand
                    drawCircle(
                        color = innerEarPink.copy(alpha = 0.7f),
                        radius = minDim * 0.018f,
                        center = Offset(w * 0.32f, h * 0.57f)
                    )
                }

                val rightShoulderPivot = Offset(w * 0.67f, h * 0.54f)
                rotate(degrees = rightArmWave, pivot = rightShoulderPivot) {
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                            center = Offset(w * 0.64f, h * 0.57f),
                            radius = minDim * 0.08f
                        ),
                        topLeft = Offset(w * 0.57f, h * 0.525f),
                        size = Size(w * 0.15f, h * 0.09f)
                    )
                    drawCircle(
                        color = innerEarPink.copy(alpha = 0.7f),
                        radius = minDim * 0.018f,
                        center = Offset(w * 0.68f, h * 0.57f)
                    )
                }

                // --- 5. ARTICULATED HEAD, FLAPPING EARS, EYES, EYEBROWS & TALKING MOUTH ---
                val headPivot = Offset(w * 0.50f, h * 0.48f)
                translate(left = 0f, top = headBob) {
                    rotate(degrees = headTilt, pivot = headPivot) {
                        val earRadius = minDim * 0.096f

                        // Left Ear with independent flap pivot
                        val leftEarBase = Offset(w * 0.34f, h * 0.33f)
                        val leftEarCenter = Offset(w * 0.325f, h * 0.295f)
                        rotate(degrees = leftEarAngle, pivot = leftEarBase) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                                    center = leftEarCenter,
                                    radius = earRadius * 1.2f
                                ),
                                radius = earRadius,
                                center = leftEarCenter
                            )
                            drawCircle(
                                color = innerEarPink.copy(alpha = 0.78f),
                                radius = earRadius * 0.52f,
                                center = leftEarCenter
                            )
                        }

                        // Right Ear with independent flap pivot
                        val rightEarBase = Offset(w * 0.66f, h * 0.33f)
                        val rightEarCenter = Offset(w * 0.675f, h * 0.295f)
                        rotate(degrees = rightEarAngle, pivot = rightEarBase) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(bearPurpleLight, bearPurpleMid, bearPurpleDeep),
                                    center = rightEarCenter,
                                    radius = earRadius * 1.2f
                                ),
                                radius = earRadius,
                                center = rightEarCenter
                            )
                            drawCircle(
                                color = innerEarPink.copy(alpha = 0.78f),
                                radius = earRadius * 0.52f,
                                center = rightEarCenter
                            )
                        }

                        // Optional Rose-Gold Headphones
                        if (isSleepy) {
                            drawArc(
                                color = Color(0xFFE6A4B4),
                                startAngle = 190f,
                                sweepAngle = 160f,
                                useCenter = false,
                                topLeft = Offset(w * 0.25f, h * 0.22f),
                                size = Size(w * 0.50f, h * 0.36f),
                                style = Stroke(width = 7f, cap = StrokeCap.Round)
                            )
                        }

                        // Main Plush Bear Head
                        val headTopLeft = Offset(w * 0.27f, h * 0.26f)
                        val headSize = Size(w * 0.46f, h * 0.32f)
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEEE5FF),
                                    bearPurpleLight,
                                    bearPurpleMid,
                                    bearPurpleDeep
                                ),
                                center = Offset(w * 0.46f, h * 0.37f),
                                radius = w * 0.32f
                            ),
                            topLeft = headTopLeft,
                            size = headSize,
                            cornerRadius = CornerRadius(w * 0.21f, w * 0.21f)
                        )

                        // Cute fluffy forehead curls that bounce
                        val curlBob = sin(cycleRad * 2f) * (h * 0.006f)
                        drawCircle(
                            color = Color(0xFFEDE4FF),
                            radius = minDim * 0.038f,
                            center = Offset(w * 0.48f, h * 0.285f + curlBob)
                        )
                        drawCircle(
                            color = Color(0xFFEDE4FF),
                            radius = minDim * 0.028f,
                            center = Offset(w * 0.525f, h * 0.29f - curlBob)
                        )

                        if (isSleepy) {
                            drawOval(
                                color = Color(0xFFD88C9A),
                                topLeft = Offset(w * 0.22f, h * 0.35f),
                                size = Size(w * 0.07f, h * 0.13f)
                            )
                            drawOval(
                                color = Color(0xFFD88C9A),
                                topLeft = Offset(w * 0.71f, h * 0.35f),
                                size = Size(w * 0.07f, h * 0.13f)
                            )
                        }

                        // Rosy Cheeks that puff when smiling/talking
                        val cheekScale = 1f + mouthOpen * 0.18f + cheer * 0.2f
                        val blushAlpha = (0.55f + mouthOpen * 0.2f + cheer * 0.25f).coerceAtMost(0.95f)
                        drawOval(
                            color = cheekBlushPink.copy(alpha = blushAlpha),
                            topLeft = Offset(w * 0.315f, h * 0.425f),
                            size = Size(w * 0.088f * cheekScale, h * 0.050f * cheekScale)
                        )
                        drawOval(
                            color = cheekBlushPink.copy(alpha = blushAlpha),
                            topLeft = Offset(w * 0.595f, h * 0.425f),
                            size = Size(w * 0.088f * cheekScale, h * 0.050f * cheekScale)
                        )

                        // Expressive Eyes & Moving Pupils
                        val eyeColor = Color(0xFF3B235A)
                        val shouldShowClosedEyes = isBlinking || isSleepy || cheer > 0.65f

                        if (shouldShowClosedEyes) {
                            drawArc(
                                color = eyeColor,
                                startAngle = 15f,
                                sweepAngle = 150f,
                                useCenter = false,
                                topLeft = Offset(w * 0.37f, h * 0.38f),
                                size = Size(w * 0.075f, h * 0.045f),
                                style = Stroke(width = 4.2f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = eyeColor,
                                startAngle = 15f,
                                sweepAngle = 150f,
                                useCenter = false,
                                topLeft = Offset(w * 0.555f, h * 0.38f),
                                size = Size(w * 0.075f, h * 0.045f),
                                style = Stroke(width = 4.2f, cap = StrokeCap.Round)
                            )
                        } else {
                            val leftEyeCenter = Offset(w * 0.405f + pupilShiftX, h * 0.405f + pupilShiftY)
                            val rightEyeCenter = Offset(w * 0.595f + pupilShiftX, h * 0.405f + pupilShiftY)
                            val eyeR = minDim * 0.029f

                            drawCircle(color = eyeColor, radius = eyeR, center = leftEyeCenter)
                            drawCircle(
                                color = Color.White,
                                radius = eyeR * 0.40f,
                                center = Offset(leftEyeCenter.x - eyeR * 0.25f, leftEyeCenter.y - eyeR * 0.25f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = eyeR * 0.18f,
                                center = Offset(leftEyeCenter.x + eyeR * 0.25f, leftEyeCenter.y + eyeR * 0.25f)
                            )

                            drawCircle(color = eyeColor, radius = eyeR, center = rightEyeCenter)
                            drawCircle(
                                color = Color.White,
                                radius = eyeR * 0.40f,
                                center = Offset(rightEyeCenter.x - eyeR * 0.25f, rightEyeCenter.y - eyeR * 0.25f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = eyeR * 0.18f,
                                center = Offset(rightEyeCenter.x + eyeR * 0.25f, rightEyeCenter.y + eyeR * 0.25f)
                            )
                        }

                        // Animated Eyebrows that lift when talking/happy
                        val browLift = -(mouthOpen * h * 0.012f + cheer * h * 0.015f)
                        drawArc(
                            color = eyeColor.copy(alpha = 0.7f),
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(w * 0.375f, h * 0.352f + browLift),
                            size = Size(w * 0.062f, h * 0.025f),
                            style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = eyeColor.copy(alpha = 0.7f),
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(w * 0.565f, h * 0.352f + browLift),
                            size = Size(w * 0.062f, h * 0.025f),
                            style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                        )

                        // --- SNOUT, NOSE & ANIMATED TALKING / SMILING MOUTH ---
                        drawOval(
                            color = Color(0xFFF6F0FF),
                            topLeft = Offset(w * 0.435f, h * 0.415f),
                            size = Size(w * 0.13f, h * 0.095f)
                        )
                        // Little nose bobs slightly with mouth
                        drawOval(
                            color = Color(0xFF7E57C2),
                            topLeft = Offset(w * 0.48f, h * 0.425f),
                            size = Size(w * 0.04f, h * 0.026f)
                        )

                        // Animated Mouth: smoothly transitions between 'w' smile and open happy mouth with tongue!
                        if (mouthOpen > 0.22f) {
                            val openH = (h * 0.022f) + (h * 0.028f * mouthOpen)
                            val mouthRectTopLeft = Offset(w * 0.468f, h * 0.453f)
                            val mouthRectSize = Size(w * 0.064f, openH)

                            // Open mouth cavity
                            drawOval(
                                color = Color(0xFF5D2A55),
                                topLeft = mouthRectTopLeft,
                                size = mouthRectSize
                            )
                            // Cute little pink tongue inside open mouth!
                            drawOval(
                                color = Color(0xFFF48FB1),
                                topLeft = Offset(w * 0.478f, h * 0.453f + openH * 0.42f),
                                size = Size(w * 0.044f, openH * 0.52f)
                            )
                        } else {
                            // Cute 'w' bear smile
                            drawArc(
                                color = eyeColor,
                                startAngle = 10f,
                                sweepAngle = 160f,
                                useCenter = false,
                                topLeft = Offset(w * 0.465f, h * 0.45f),
                                size = Size(w * 0.036f, h * 0.025f),
                                style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = eyeColor,
                                startAngle = 10f,
                                sweepAngle = 160f,
                                useCenter = false,
                                topLeft = Offset(w * 0.50f, h * 0.45f),
                                size = Size(w * 0.036f, h * 0.025f),
                                style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // Floating Heart Burst Particles when user taps/clicks Pomi!
            if (heartBurstProgress.value > 0f) {
                val p = heartBurstProgress.value
                val burstAngles = listOf(-75f, -35f, 0f, 35f, 75f, -115f, 115f)
                burstAngles.forEachIndexed { idx, deg ->
                    val rad = Math.toRadians(deg.toDouble())
                    val dist = (minDim * 0.24f) + (minDim * 0.28f * p)
                    val bx = center.x + (sin(rad) * dist).toFloat()
                    val by = center.y - (cos(rad) * dist).toFloat()
                    val particleColor = if (idx % 2 == 0) Color(0xFFE34D7B) else Color(0xFF7C4DFF)
                    drawCircle(
                        color = particleColor.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                        radius = (minDim * 0.030f) * (1.25f - p * 0.5f),
                        center = Offset(bx, by)
                    )
                }
            }
        }
    }
}

// =============================================================================================
// 3. SERA LOTUS / WAVE LOGO & TOP APP BAR
// =============================================================================================

@Composable
fun SeraWaveLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(SeraIris, SeraPrimaryBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.58f)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = 4.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val centerPetal = Path().apply {
                moveTo(w * 0.5f, h * 0.15f)
                cubicTo(w * 0.72f, h * 0.38f, w * 0.68f, h * 0.72f, w * 0.5f, h * 0.86f)
                cubicTo(w * 0.32f, h * 0.72f, w * 0.28f, h * 0.38f, w * 0.5f, h * 0.15f)
            }
            val leftPetal = Path().apply {
                moveTo(w * 0.5f, h * 0.86f)
                cubicTo(w * 0.20f, h * 0.82f, w * 0.10f, h * 0.52f, w * 0.18f, h * 0.36f)
                cubicTo(w * 0.32f, h * 0.44f, w * 0.42f, h * 0.58f, w * 0.5f, h * 0.86f)
            }
            val rightPetal = Path().apply {
                moveTo(w * 0.5f, h * 0.86f)
                cubicTo(w * 0.80f, h * 0.82f, w * 0.90f, h * 0.52f, w * 0.82f, h * 0.36f)
                cubicTo(w * 0.68f, h * 0.44f, w * 0.58f, h * 0.58f, w * 0.5f, h * 0.86f)
            }
            drawPath(centerPetal, color = Color.White, style = stroke)
            drawPath(leftPetal, color = Color.White, style = stroke)
            drawPath(rightPetal, color = Color.White, style = stroke)
        }
    }
}

@Composable
fun SeraTopAppBar(
    subtitleText: String = "WELLNESS",
    userName: String = "Nur",
    userPhotoUri: String? = null,
    isDarkMode: Boolean = SeraThemeMode.isDarkMode,
    unreadNotificationCount: Int,
    onToggleDarkMode: () -> Unit = {},
    onSosClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val avatarBitmap = remember(userPhotoUri) {
        if (userPhotoUri.isNullOrBlank()) null
        else {
            try {
                context.contentResolver.openInputStream(Uri.parse(userPhotoUri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    Surface(
        color = SeraSurface.copy(alpha = 0.88f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                SeraWaveLogo(size = 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SERA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SeraPrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SeraTeal)
                        )
                    }
                    Text(
                        text = subtitleText,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = SeraTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dark Mode Quick Toggle Button in Top Bar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SeraSurfaceSubtle)
                        .border(1.dp, SeraCardBorder, CircleShape)
                        .clickable(onClick = onToggleDarkMode),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDarkMode) "☀️" else "🌙",
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFF472B6), Color(0xFFE34D7B))
                            )
                        )
                        .clickable(onClick = onSosClick)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛟", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SeraSurface)
                        .border(1.dp, SeraCardBorder, CircleShape)
                        .clickable(onClick = onNotificationClick),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(19.dp)) {
                        val w = size.width
                        val h = size.height
                        val bellPath = Path().apply {
                            moveTo(w * 0.5f, h * 0.12f)
                            cubicTo(w * 0.25f, h * 0.12f, w * 0.2f, h * 0.38f, w * 0.2f, h * 0.62f)
                            lineTo(w * 0.12f, h * 0.74f)
                            lineTo(w * 0.88f, h * 0.74f)
                            lineTo(w * 0.8f, h * 0.62f)
                            cubicTo(w * 0.8f, h * 0.38f, w * 0.75f, h * 0.12f, w * 0.5f, h * 0.12f)
                            close()
                        }
                        drawPath(
                            path = bellPath,
                            color = SeraTextPrimary,
                            style = Stroke(width = 4.0f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-5).dp, y = 5.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(SeraRose)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(colors = listOf(SeraIris, SeraPrimaryBlue))
                        )
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = "Foto Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================================
// 4. BOTTOM NAVIGATION BAR
// =============================================================================================

enum class SeraBottomTab(val route: String, val label: String) {
    BERANDA("beranda", "Beranda"),
    CATAT("catat", "Catat"),
    RIWAYAT("riwayat", "Riwayat"),
    SELF_CARE("self_care", "Self-Care")
}

@Composable
fun SeraBottomNavigationBar(
    selectedTab: SeraBottomTab,
    onTabSelected: (SeraBottomTab) -> Unit
) {
    Surface(
        color = SeraSurface.copy(alpha = 0.94f),
        shadowElevation = 14.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = BorderStroke(1.dp, SeraCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeraBottomTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val tintColor = if (isSelected) SeraPrimaryBlue else SeraTextSecondary

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) SeraPrimaryBlueLight else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val w = size.width
                            val h = size.height
                            val stroke = Stroke(width = 4.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            when (tab) {
                                SeraBottomTab.BERANDA -> {
                                    val housePath = Path().apply {
                                        moveTo(w * 0.15f, h * 0.46f)
                                        lineTo(w * 0.50f, h * 0.16f)
                                        lineTo(w * 0.85f, h * 0.46f)
                                        moveTo(w * 0.25f, h * 0.42f)
                                        lineTo(w * 0.25f, h * 0.84f)
                                        lineTo(w * 0.75f, h * 0.84f)
                                        lineTo(w * 0.75f, h * 0.42f)
                                    }
                                    drawPath(housePath, color = tintColor, style = stroke)
                                }
                                SeraBottomTab.CATAT -> {
                                    drawLine(
                                        color = tintColor,
                                        start = Offset(w * 0.18f, h * 0.26f),
                                        end = Offset(w * 0.68f, h * 0.26f),
                                        strokeWidth = 4.2f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = tintColor,
                                        start = Offset(w * 0.18f, h * 0.50f),
                                        end = Offset(w * 0.54f, h * 0.50f),
                                        strokeWidth = 4.2f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = tintColor,
                                        start = Offset(w * 0.18f, h * 0.74f),
                                        end = Offset(w * 0.42f, h * 0.74f),
                                        strokeWidth = 4.2f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = tintColor,
                                        start = Offset(w * 0.58f, h * 0.76f),
                                        end = Offset(w * 0.84f, h * 0.46f),
                                        strokeWidth = 4.4f,
                                        cap = StrokeCap.Round
                                    )
                                }
                                SeraBottomTab.RIWAYAT -> {
                                    val path = Path().apply {
                                        moveTo(w * 0.14f, h * 0.72f)
                                        lineTo(w * 0.38f, h * 0.44f)
                                        lineTo(w * 0.58f, h * 0.60f)
                                        lineTo(w * 0.86f, h * 0.26f)
                                    }
                                    drawPath(path, color = tintColor, style = stroke)
                                    drawCircle(color = tintColor, radius = 3.5f, center = Offset(w * 0.38f, h * 0.44f))
                                    drawCircle(color = tintColor, radius = 3.5f, center = Offset(w * 0.86f, h * 0.26f))
                                }
                                SeraBottomTab.SELF_CARE -> {
                                    val centerLeaf = Path().apply {
                                        moveTo(w * 0.5f, h * 0.16f)
                                        cubicTo(w * 0.72f, h * 0.36f, w * 0.68f, h * 0.68f, w * 0.5f, h * 0.84f)
                                        cubicTo(w * 0.32f, h * 0.68f, w * 0.28f, h * 0.36f, w * 0.5f, h * 0.16f)
                                    }
                                    val leftPetal = Path().apply {
                                        moveTo(w * 0.5f, h * 0.84f)
                                        cubicTo(w * 0.22f, h * 0.82f, w * 0.12f, h * 0.52f, w * 0.2f, h * 0.38f)
                                        cubicTo(w * 0.32f, h * 0.44f, w * 0.42f, h * 0.58f, w * 0.5f, h * 0.84f)
                                    }
                                    val rightPetal = Path().apply {
                                        moveTo(w * 0.5f, h * 0.84f)
                                        cubicTo(w * 0.78f, h * 0.82f, w * 0.88f, h * 0.52f, w * 0.8f, h * 0.38f)
                                        cubicTo(w * 0.68f, h * 0.44f, w * 0.58f, h * 0.58f, w * 0.5f, h * 0.84f)
                                    }
                                    drawPath(centerLeaf, color = tintColor, style = stroke)
                                    drawPath(leftPetal, color = tintColor, style = stroke)
                                    drawPath(rightPetal, color = tintColor, style = stroke)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = tintColor
                    )
                }
            }
        }
    }
}

// =============================================================================================
// 5. DYNAMIC PERIOD-AWARE GELOMBANG PERASAAN CHART (HARIAN | MINGGUAN | BULANAN)
// =============================================================================================

data class ChartDataPoint(
    val axisLabel: String,
    val intensity: Float,
    val emotion: EmotionType,
    val detailTooltip: String
)

@Composable
fun EmotionBezierTrendChart(
    period: String = "Mingguan",
    entries: List<EmotionCheckIn>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartPoints = remember(period, entries) {
        when (period) {
            "Harian" -> listOf(
                ChartDataPoint("06:00", 2.0f, EmotionType.TENANG, "06:00 WIB • Tenang (Level 2/5) — Bangun pagi & sarapan tenang"),
                ChartDataPoint("09:00", 4.2f, EmotionType.CEMAS, "09:00 WIB • Cemas (Level 4/5) — Persiapan kuis kalkulus & praktikum"),
                ChartDataPoint("12:00", 2.8f, EmotionType.BIASA, "12:00 WIB • Biasa (Level 3/5) — Istirahat siang & makan bersama teman"),
                ChartDataPoint("15:00", 3.4f, EmotionType.CEMAS, "15:00 WIB • Cemas (Level 3/5) — Revisi kode proyek Android Studio"),
                ChartDataPoint("18:00", 2.1f, EmotionType.TENANG, "18:00 WIB • Tenang (Level 2/5) — Jalan santai sore di sekitar kampus"),
                ChartDataPoint("21:00", 1.8f, EmotionType.TENANG, "21:00 WIB • Tenang (Level 2/5) — Mendengarkan suara hujan 432Hz sebelum tidur")
            )
            "Bulanan" -> listOf(
                ChartDataPoint("Mgg 1", 2.2f, EmotionType.TENANG, "Minggu ke-1 Okt • Rata-rata 2.2/5.0 — Awal bulan stabil & tidur cukup"),
                ChartDataPoint("Mgg 2", 2.9f, EmotionType.BIASA, "Minggu ke-2 Okt • Rata-rata 2.9/5.0 — Mulai padat tugas kelompok"),
                ChartDataPoint("Mgg 3", 4.3f, EmotionType.CEMAS, "Minggu ke-3 Okt • Rata-rata 4.3/5.0 — Puncak UTS & tenggat laporan"),
                ChartDataPoint("Mgg 4", 2.6f, EmotionType.TENANG, "Minggu ke-4 Okt • Rata-rata 2.6/5.0 — Membaik drastis berkat Napas Kotak & Dekonstruksi Tugas"),
                ChartDataPoint("Akhir", 2.0f, EmotionType.SENANG, "Akhir Oktober • Rata-rata 2.0/5.0 — Ritme istirahat pulih & perasaan hangat")
            )
            else -> {
                val weekDays = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                val recent = entries.take(7).reversed()
                if (recent.size >= 7) {
                    recent.mapIndexed { idx, item ->
                        ChartDataPoint(
                            axisLabel = weekDays[idx],
                            intensity = item.intensity.toFloat(),
                            emotion = item.emotion,
                            detailTooltip = "${weekDays[idx]} (${item.dayOfMonth} ${item.monthName}) • ${item.emotion.label} (Level ${item.intensity}/5) — ${item.journalNote.take(65)}..."
                        )
                    }
                } else {
                    listOf(
                        ChartDataPoint("Sen", 2.0f, EmotionType.TENANG, "Senin • Tenang (Level 2/5) — Memulai pekan dengan prioritas langkah mikro"),
                        ChartDataPoint("Sel", 2.2f, EmotionType.SENANG, "Selasa • Senang (Level 2/5) — Presentasi UI/UX berjalan lancar"),
                        ChartDataPoint("Rab", 3.1f, EmotionType.SEDIH, "Rabu • Sedih (Level 3/5) — Kelelahan setelah jadwal kuliah penuh"),
                        ChartDataPoint("Kam", 4.6f, EmotionType.CEMAS, "Kamis • Puncak Cemas (Level 4.6/5) — Tenggat tugas & kuis menumpuk"),
                        ChartDataPoint("Jum", 3.0f, EmotionType.BIASA, "Jumat • Biasa (Level 3/5) — Menyelesaikan revisi modul praktikum"),
                        ChartDataPoint("Sab", 2.0f, EmotionType.TENANG, "Sabtu • Tenang (Level 2/5) — Jalan pagi di taman kampus (Sensor GPS)"),
                        ChartDataPoint("Min", 1.9f, EmotionType.TENANG, "Minggu • Tenang (Level 2/5) — Istirahat layar & teh hangat")
                    )
                }
            }
        }
    }

    val safeSelectedIndex = selectedIndex.coerceIn(0, (chartPoints.size - 1).coerceAtLeast(0))
    val peakPoint = chartPoints.maxByOrNull { it.intensity } ?: chartPoints.first()
    val selectedPoint = chartPoints[safeSelectedIndex]

    Column(modifier = modifier.fillMaxWidth()) {
        // Dynamic Peak Callout Badge
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF9F2A56)
            ) {
                Text(
                    text = "⚡ Puncak (${peakPoint.axisLabel}): ${peakPoint.emotion.label}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val topPad = 16f
                val bottomPad = 20f
                val usableH = h - topPad - bottomPad
                val stepX = w / (chartPoints.size - 1).coerceAtLeast(1)

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                for (level in 1..3) {
                    val y = topPad + (usableH * (level - 1) / 2f)
                    drawLine(
                        color = Color(0xFFEDE8F8),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                }

                val points = chartPoints.mapIndexed { idx, item ->
                    val normalized = (item.intensity.coerceIn(1f, 5f) - 1f) / 4f
                    val x = idx * stepX
                    val y = topPad + usableH * (1f - normalized)
                    Offset(x, y)
                }

                if (points.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val ctrlX1 = (p0.x + p1.x) / 2f
                            val ctrlX2 = (p0.x + p1.x) / 2f
                            cubicTo(ctrlX1, p0.y, ctrlX2, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, h - 4f)
                        lineTo(points.first().x, h - 4f)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SeraRose.copy(alpha = 0.18f),
                                SeraTeal.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = topPad,
                            endY = h
                        )
                    )

                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                SeraIris,
                                SeraRose,
                                Color(0xFFD97706),
                                SeraTeal
                            )
                        ),
                        style = Stroke(width = 7.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    points.forEachIndexed { idx, pt ->
                        val emo = chartPoints[idx].emotion
                        val nodeColor = when (emo) {
                            EmotionType.CEMAS, EmotionType.SEDIH -> SeraRose
                            EmotionType.TENANG, EmotionType.SENANG -> SeraTeal
                            else -> SeraIris
                        }
                        if (idx == safeSelectedIndex) {
                            drawCircle(
                                color = nodeColor.copy(alpha = 0.25f),
                                radius = 24f,
                                center = pt
                            )
                        }
                        drawCircle(color = Color.White, radius = 10.5f, center = pt)
                        drawCircle(color = nodeColor, radius = 7f, center = pt)
                    }
                }
            }
        }

        // Interactive X-Axis Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            chartPoints.forEachIndexed { index, pt ->
                val isSelected = index == safeSelectedIndex
                Surface(
                    onClick = { onPointSelected(index) },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) SeraRoseSoft else Color.Transparent
                ) {
                    Text(
                        text = pt.axisLabel,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) SeraRose else SeraTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Selected Node Tooltip Pill
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SeraSurfaceSubtle,
            border = BorderStroke(1.dp, SeraCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedPoint.emotion.emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedPoint.detailTooltip,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SeraTextPrimary
                )
            }
        }
    }
}

@Composable
fun SeraSectionTitleRow(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SeraTextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SeraTextSecondary
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            Surface(
                onClick = onActionClick,
                shape = RoundedCornerShape(50),
                color = SeraSurface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, SeraCardBorder)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeraPrimaryBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
