package com.openstream.app.core.designsystem.colors

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val MidnightNavy = Color(0xFF101827)
val SurfaceNavy = Color(0xFF182233)
val ElevatedSurfaceNavy = Color(0xFF252F40)
val WarmIvory = Color(0xFFF2EFE8)
val MutedChampagne = Color(0xFFC6AD7A)
val SecondaryTextSlate = Color(0xFF9AA5B5)

val BorderSubtle = Color(0xFF2C394E)
val BorderHighlight = Color(0xFF4A5B73)
val ChampagneHighlight = Color(0xFFDFCAA0)
val ChampagneDark = Color(0xFFA58E5D)
val GlassSurface = Color(0xDE182233)
val GlassSurfaceElevated = Color(0xEE252F40)
val CinematicError = Color(0xFFCF6679)
val ProgressGold = Color(0xFFD4AF37)

val DarkScrimGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x00101827),
        Color(0x66101827),
        Color(0xCC101827),
        Color(0xFF101827)
    )
)

val HeroOverlayGradient = Brush.verticalGradient(
    0.0f to Color.Transparent,
    0.4f to Color(0x33101827),
    0.75f to Color(0xDD101827),
    1.0f to Color(0xFF101827)
)

val GoldAccentGradient = Brush.horizontalGradient(
    colors = listOf(MutedChampagne, ChampagneHighlight)
)

@Immutable
data class OpenStreamColorTokens(
    val background: Color = MidnightNavy,
    val surface: Color = SurfaceNavy,
    val surfaceElevated: Color = ElevatedSurfaceNavy,
    val glassSurface: Color = GlassSurface,
    val textPrimary: Color = WarmIvory,
    val textSecondary: Color = SecondaryTextSlate,
    val accent: Color = MutedChampagne,
    val accentVariant: Color = ChampagneHighlight,
    val border: Color = BorderSubtle,
    val borderHighlight: Color = BorderHighlight,
    val error: Color = CinematicError,
    val progressGold: Color = ProgressGold,
    val isDark: Boolean = true
)

val LocalOpenStreamColors = staticCompositionLocalOf { OpenStreamColorTokens() }
