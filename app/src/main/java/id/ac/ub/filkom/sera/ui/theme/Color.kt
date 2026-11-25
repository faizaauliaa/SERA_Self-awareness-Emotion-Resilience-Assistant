package id.ac.ub.filkom.sera.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Global Reactive Dark Mode State (Switches the entire SERA app between Serene Light & Night Sanctuary Dark Mode)
object SeraThemeMode {
    var isDarkMode by mutableStateOf(false)
}

// Pastel Aurora Background Palette (Hijau, Kuning, Pink, Biru, Lavender Pastel Muda)
val PastelAuroraMint = Color(0xFFE3FAF2)
val PastelAuroraYellow = Color(0xFFFFF8E1)
val PastelAuroraPink = Color(0xFFFEEAF3)
val PastelAuroraBlue = Color(0xFFE5F3FF)
val PastelAuroraLavender = Color(0xFFF2EBFF)

// Translucent Glass Surface & Organic Borders (Dynamic Light / Dark Mode)
val SeraBackground: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF110F24) else Color(0xFFF6F4FD)
val SeraSurface: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF1C1936) else Color(0xFFFFFFFF)
val SeraSurfaceGlass: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xEB1C1936) else Color(0xF5FFFFFF)
val SeraSurfaceSubtle: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF252147) else Color(0xFFF5F3FC)
val SeraCardBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF363163) else Color(0xFFEAE5F8)
val SeraDivider: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF2E2954) else Color(0xFFEDE9F8)

// Primary Indigo-Violet & Serene Emerald Accents (Matching UI_SERA.zip)
val SeraPrimaryBlue: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF9D91FF) else Color(0xFF5A4FCF)
val SeraPrimaryBlueDark: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF292259) else Color(0xFF4237A8)
val SeraPrimaryBlueLight: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF2A2456) else Color(0xFFEEEBFF)
val SeraTeal: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF34D399) else Color(0xFF159A7C)
val SeraTealSoft: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF143832) else Color(0xFFE2F8F1)

// Iris / Violet Accent (Pomi Mascot & Empathy AI)
val SeraIris: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFA78BFA) else Color(0xFF6653E0)
val SeraIrisMedium: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF8B75F5) else Color(0xFF836FF0)
val SeraIrisSoft: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF282252) else Color(0xFFF1EDFF)
val SeraIrisBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF43397A) else Color(0xFFDDD4FF)

// Hero Gradient Colors
val SeraHeroSky = Color(0xFFE6F4FF)
val SeraHeroLavender = Color(0xFFF0E8FF)
val SeraHeroMint = Color(0xFFE4FAF2)
val SeraHeroPeach = Color(0xFFFFEFEA)

// SOS & Warm Rose Accent
val SeraRose: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFF472B6) else Color(0xFFE34D7B)
val SeraRoseSoft: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF3D1E36) else Color(0xFFFEEBF1)
val SeraRoseBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF6E2E55) else Color(0xFFFACADD)

// Text Hierarchy
val SeraTextPrimary: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFF4F2FF) else Color(0xFF1D1B38)
val SeraTextSecondary: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFBDB8DF) else Color(0xFF5D5A7A)
val SeraTextMuted: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF8A85B2) else Color(0xFF8E8BA8)

// Emotion Specific Palettes (Organic Bubbles)
val EmotionHappyBg: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF3B2E16) else Color(0xFFFFF5D9)
val EmotionHappyAccent: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706)
val EmotionHappyBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF785719) else Color(0xFFFDE092)

val EmotionCalmBg: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF13362F) else Color(0xFFDEF9EE)
val EmotionCalmAccent: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF34D399) else Color(0xFF138A6B)
val EmotionCalmTeal: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF34D399) else Color(0xFF159A7C)
val EmotionCalmBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF1F6351) else Color(0xFF8EE5C8)

val EmotionNeutralBg: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF38271E) else Color(0xFFFFF0E2)
val EmotionNeutralAccent: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFFB923C) else Color(0xFFC2642B)
val EmotionNeutralBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF6E4730) else Color(0xFFFAD3B4)

val EmotionSadBg: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF192B48) else Color(0xFFE7F3FF)
val EmotionSadAccent: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF60A5FA) else Color(0xFF2B6CB0)
val EmotionSadBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF2D4F85) else Color(0xFFB3D8FF)

val EmotionAnxiousBg: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF28204D) else Color(0xFFF2EBFF)
val EmotionAnxiousAccent: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFA78BFA) else Color(0xFF6653E0)
val EmotionAnxiousBorder: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF4C3C8C) else Color(0xFFD6C7FF)

// Status & Badge Colors
val SeraSuccessGreen: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF34D399) else Color(0xFF138A6B)
val SeraSuccessSoft: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF13362F) else Color(0xFFDEF9EE)
val SeraWarningAmber: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFFFBBF24) else Color(0xFFB45309)
val SeraWarningSoft: Color
    get() = if (SeraThemeMode.isDarkMode) Color(0xFF3B2E16) else Color(0xFFFEF3C7)
