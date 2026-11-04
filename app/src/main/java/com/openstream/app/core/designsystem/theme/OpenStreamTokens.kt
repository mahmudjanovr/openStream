package com.openstream.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class OpenStreamSpacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val sectionSpacing: Dp = 28.dp,
    val minTouchTarget: Dp = 48.dp
)

val LocalOpenStreamSpacing = staticCompositionLocalOf { OpenStreamSpacing() }

@Immutable
data class OpenStreamElevation(
    val flat: Dp = 0.dp,
    val card: Dp = 2.dp,
    val bottomNav: Dp = 8.dp,
    val dialog: Dp = 16.dp
)

val LocalOpenStreamElevation = staticCompositionLocalOf { OpenStreamElevation() }

@Immutable
data class OpenStreamAnimation(
    val quickMs: Int = 150,
    val standardMs: Int = 300,
    val cinematicMs: Int = 500,
    val splashFadeMs: Int = 700
)

val LocalOpenStreamAnimation = staticCompositionLocalOf { OpenStreamAnimation() }
