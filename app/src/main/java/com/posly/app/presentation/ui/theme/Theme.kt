package com.posly.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PoslyColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = Neutral100,
    onSecondaryContainer = TextPrimary,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessGreenLight,
    onTertiaryContainer = Color(0xFF064E3B),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedLight,
    onErrorContainer = Color(0xFF7F1D1D),
    background = Background,
    onBackground = TextPrimary,
    surface = Background,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDivider,
    outlineVariant = Neutral100,
    scrim = Scrim,
    inverseSurface = Neutral800,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryContainer,
)

@Composable
fun PoslyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PoslyColorScheme,
        typography = PoslyTypography,
        content = content
    )
}
