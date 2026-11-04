package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

enum class OpenStreamBadgeVariant {
    Gold,
    Subtle,
    Solid
}

@Composable
fun OpenStreamBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: OpenStreamBadgeVariant = OpenStreamBadgeVariant.Subtle
) {
    val shape = OpenStreamTheme.shapes.extraSmall

    val (background, border, textColor) = when (variant) {
        OpenStreamBadgeVariant.Gold -> Triple(
            OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.85f),
            OpenStreamTheme.colors.accent.copy(alpha = 0.6f),
            OpenStreamTheme.colors.accent
        )
        OpenStreamBadgeVariant.Subtle -> Triple(
            OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.7f),
            OpenStreamTheme.colors.border,
            OpenStreamTheme.colors.textSecondary
        )
        OpenStreamBadgeVariant.Solid -> Triple(
            OpenStreamTheme.colors.accent,
            Color.Transparent,
            OpenStreamTheme.colors.background
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(0.75.dp, border, shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = OpenStreamTheme.typography.labelSmall,
            color = textColor
        )
    }
}
