package cit.edu.primera.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PrimeraLightColorScheme = lightColorScheme(
    primary = PrimeraViolet,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimeraLilac,
    onPrimaryContainer = TextPrimary,
    secondary = PrimeraLilac,
    onSecondary = TextPrimary,
    secondaryContainer = DaysLeftBg,
    onSecondaryContainer = TextPrimary,
    background = BackgroundCream,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = MenuButtonBg,
    onSurfaceVariant = TextSecondary,
    outline = InputBorder,
    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = HeartRateBg,
    onErrorContainer = ErrorRed,
)

@Composable
fun PrimeraTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PrimeraLightColorScheme,
        typography  = PrimeraTypography,
        content = content,
    )
}
