package com.openstream.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.openstream.app.core.designsystem.colors.CinematicError
import com.openstream.app.core.designsystem.colors.ElevatedSurfaceNavy
import com.openstream.app.core.designsystem.colors.LocalOpenStreamColors
import com.openstream.app.core.designsystem.colors.MidnightNavy
import com.openstream.app.core.designsystem.colors.MutedChampagne
import com.openstream.app.core.designsystem.colors.OpenStreamColorTokens
import com.openstream.app.core.designsystem.colors.SecondaryTextSlate
import com.openstream.app.core.designsystem.colors.SurfaceNavy
import com.openstream.app.core.designsystem.colors.WarmIvory
import com.openstream.app.core.designsystem.shapes.LocalOpenStreamShapes
import com.openstream.app.core.designsystem.shapes.OpenStreamShapeTokens
import com.openstream.app.core.designsystem.typography.LocalOpenStreamTypography
import com.openstream.app.core.designsystem.typography.OpenStreamTypographyTokens

private val DarkMaterialColorScheme = darkColorScheme(
    primary = MutedChampagne,
    onPrimary = MidnightNavy,
    primaryContainer = ElevatedSurfaceNavy,
    onPrimaryContainer = WarmIvory,
    secondary = WarmIvory,
    onSecondary = MidnightNavy,
    background = MidnightNavy,
    onBackground = WarmIvory,
    surface = SurfaceNavy,
    onSurface = WarmIvory,
    surfaceVariant = ElevatedSurfaceNavy,
    onSurfaceVariant = SecondaryTextSlate,
    error = CinematicError,
    onError = WarmIvory
)

@Composable
fun OpenStreamTheme(
    colors: OpenStreamColorTokens = OpenStreamColorTokens(),
    typography: OpenStreamTypographyTokens = OpenStreamTypographyTokens(),
    shapes: OpenStreamShapeTokens = OpenStreamShapeTokens(),
    spacing: OpenStreamSpacing = OpenStreamSpacing(),
    elevation: OpenStreamElevation = OpenStreamElevation(),
    animation: OpenStreamAnimation = OpenStreamAnimation(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalOpenStreamColors provides colors,
        LocalOpenStreamTypography provides typography,
        LocalOpenStreamShapes provides shapes,
        LocalOpenStreamSpacing provides spacing,
        LocalOpenStreamElevation provides elevation,
        LocalOpenStreamAnimation provides animation
    ) {
        MaterialTheme(
            colorScheme = DarkMaterialColorScheme,
            content = content
        )
    }
}

object OpenStreamTheme {
    val colors: OpenStreamColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenStreamColors.current

    val typography: OpenStreamTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenStreamTypography.current

    val shapes: OpenStreamShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenStreamShapes.current

    val spacing: OpenStreamSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenStreamSpacing.current

    val elevation: OpenStreamElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenStreamElevation.current

    val animation: OpenStreamAnimation
        @Composable
        @ReadOnlyComposable
        get() = LocalOpenStreamAnimation.current
}
