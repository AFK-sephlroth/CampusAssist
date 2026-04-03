package com.example.campusassist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ───────────────────────────────────────────────────────────────────
object CampusColors {
    val NavyDeep         = Color(0xFF080E1D)
    val NavyCard         = Color(0xFF101828)
    val NavySurface      = Color(0xFF182135)
    val NavyElevated     = Color(0xFF1E2C42)

    val Amber            = Color(0xFFFFC107)
    val AmberLight       = Color(0xFFFFD54F)
    val AmberDim         = Color(0xFF332500)

    val StatusPending    = Color(0xFFFFAB40)
    val StatusPendingBg  = Color(0xFF2B1A00)
    val StatusProgress   = Color(0xFF40C4FF)
    val StatusProgressBg = Color(0xFF00162B)
    val StatusDone       = Color(0xFF69F0AE)
    val StatusDoneBg     = Color(0xFF00230F)

    val PriorityHigh     = Color(0xFFFF5252)
    val PriorityMed      = Color(0xFFFFAB40)
    val PriorityLow      = Color(0xFF69F0AE)

    val CatIT            = Color(0xFF40C4FF)
    val CatFacilities    = Color(0xFFCE93D8)
    val CatLibrary       = Color(0xFF80CBC4)

    val TextPrimary      = Color(0xFFEEF2FF)
    val TextSecondary    = Color(0xFF7A90B8)
    val TextMuted        = Color(0xFF3A4E6E)

    // Light
    val LightBg          = Color(0xFFF2F6FC)
    val LightSurface     = Color(0xFFFFFFFF)
    val LightPrimary     = Color(0xFF102A5A)
    val LightAmber       = Color(0xFFE65100)
}

private val DarkColorScheme = darkColorScheme(
    primary              = CampusColors.Amber,
    onPrimary            = CampusColors.NavyDeep,
    primaryContainer     = CampusColors.AmberDim,
    onPrimaryContainer   = CampusColors.AmberLight,
    secondary            = CampusColors.CatIT,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF00162B),
    onSecondaryContainer = CampusColors.CatIT,
    tertiary             = CampusColors.CatLibrary,
    background           = CampusColors.NavyDeep,
    onBackground         = CampusColors.TextPrimary,
    surface              = CampusColors.NavyCard,
    onSurface            = CampusColors.TextPrimary,
    surfaceVariant       = CampusColors.NavySurface,
    onSurfaceVariant     = CampusColors.TextSecondary,
    outline              = CampusColors.TextMuted,
    error                = Color(0xFFFF5252),
    onError              = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary              = CampusColors.LightPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFD0DEFF),
    onPrimaryContainer   = CampusColors.LightPrimary,
    secondary            = Color(0xFF1565C0),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFDCEEFF),
    onSecondaryContainer = Color(0xFF003A80),
    background           = CampusColors.LightBg,
    onBackground         = Color(0xFF0A1628),
    surface              = CampusColors.LightSurface,
    onSurface            = Color(0xFF0A1628),
    surfaceVariant       = Color(0xFFE4ECF8),
    onSurfaceVariant     = Color(0xFF3A4E6A),
    outline              = Color(0xFFBBCCE0),
    error                = Color(0xFFB00020),
    onError              = Color.White
)

val CampusTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp)
)

@Composable
fun CampusAssistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = CampusTypography,
        content = content
    )
}