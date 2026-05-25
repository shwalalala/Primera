package com.example.primera.frontend.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PrimeraLightColorScheme = lightColorScheme(
    primary          = PrimeraViolet,
    secondary        = PrimeraLilac,
    background       = BackgroundCream,
    surface          = SurfaceWhite,
    onPrimary        = SurfaceWhite,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    outline          = InputBorder,
    error            = ErrorRed,
)

@Composable
fun PrimeraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PrimeraLightColorScheme,
        typography  = PrimeraTypography,
        content     = content
    )
}
