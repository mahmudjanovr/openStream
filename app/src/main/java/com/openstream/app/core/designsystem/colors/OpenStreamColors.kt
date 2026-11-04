package com.openstream.app.core.designsystem.colors

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val MidnightNavy = Color(0xFF101827)
val SurfaceNavy = Color(0xFF182233)
val ElevatedSurfaceNavy = Color(0xFF252F40)
val WarmIvory = Color(0xFFF2EFE8)
val MutedChampagne = Color(0xFFC6AD7A)
val SecondaryTextSlate = Color(0xFF9AA5B5)

val BorderSubtle = Color(0xFF2C394E)
val OverlayGradientStart = Color(0x00101827)
val OverlayGradientEnd = Color(0xE6101827)
val ChampagneHighlight = Color(0xFFDFCAA0)
val CinematicError = Color(0xFFCF6679)

@Immutable
data class OpenStreamColorTokens(
    val background: Color = MidnightNavy,
    val surface: Color = SurfaceNavy,
    val surfaceElevated: Color = ElevatedSurfaceNavy,
    val textPrimary: Color = WarmIvory,
    val textSecondary: Color = SecondaryTextSlate,
    val accent: Color = MutedChampagne,
    val accentVariant: Color = ChampagneHighlight,
    val border: Color = BorderSubtle,
    val error: Color = CinematicError,
    val isDark: Boolean = true
)

val LocalOpenStreamColors = staticCompositionLocalOf { OpenStreamColorTokens() }
