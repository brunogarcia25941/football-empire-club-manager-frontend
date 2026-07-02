package com.brunogarcia.footballempireclubmanager.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de cores escuro e premium de eSports / Champions League
private val FootballEmpireColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = MidnightBlue,
    primaryContainer = Color(0xFF033E35),
    onPrimaryContainer = NeonCyan,
    secondary = NeonGreen,
    onSecondary = MidnightBlue,
    background = MidnightBlue,
    onBackground = TextPrimary,
    surface = DarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun FootballEmpireTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FootballEmpireColorScheme,
        content = content
    )
}
