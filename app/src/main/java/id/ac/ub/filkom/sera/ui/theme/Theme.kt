package id.ac.ub.filkom.sera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SERATheme(
    darkTheme: Boolean = SeraThemeMode.isDarkMode,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = SeraPrimaryBlue,
            onPrimary = Color(0xFF121024),
            primaryContainer = SeraPrimaryBlueLight,
            onPrimaryContainer = SeraTextPrimary,
            secondary = SeraTeal,
            onSecondary = Color(0xFF121024),
            secondaryContainer = SeraTealSoft,
            onSecondaryContainer = SeraTextPrimary,
            tertiary = SeraIris,
            onTertiary = Color(0xFF121024),
            tertiaryContainer = SeraIrisSoft,
            onTertiaryContainer = SeraIris,
            background = SeraBackground,
            onBackground = SeraTextPrimary,
            surface = SeraSurface,
            onSurface = SeraTextPrimary,
            surfaceVariant = SeraSurfaceSubtle,
            onSurfaceVariant = SeraTextSecondary,
            outline = SeraCardBorder,
            error = SeraRose,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = SeraPrimaryBlue,
            onPrimary = SeraSurface,
            primaryContainer = SeraPrimaryBlueLight,
            onPrimaryContainer = SeraPrimaryBlueDark,
            secondary = SeraTeal,
            onSecondary = SeraSurface,
            secondaryContainer = SeraTealSoft,
            onSecondaryContainer = SeraPrimaryBlueDark,
            tertiary = SeraIris,
            onTertiary = SeraSurface,
            tertiaryContainer = SeraIrisSoft,
            onTertiaryContainer = SeraIris,
            background = SeraBackground,
            onBackground = SeraTextPrimary,
            surface = SeraSurface,
            onSurface = SeraTextPrimary,
            surfaceVariant = SeraSurfaceSubtle,
            onSurfaceVariant = SeraTextSecondary,
            outline = SeraCardBorder,
            error = SeraRose,
            onError = SeraSurface
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SeraTypography,
        content = content
    )
}
