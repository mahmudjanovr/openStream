package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

@Composable
fun OpenStreamTopBar(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    navigationIconContentDescription: String? = null,
    transparentBackground: Boolean = false,
    showBottomDivider: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val bgModifier = if (transparentBackground) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    OpenStreamTheme.colors.background.copy(alpha = 0.95f),
                    OpenStreamTheme.colors.background.copy(alpha = 0.7f),
                    Color.Transparent
                )
            )
        )
    } else {
        Modifier.background(OpenStreamTheme.colors.surface)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(bgModifier)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = OpenStreamTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateBack != null) {
                OpenStreamIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = navigationIconContentDescription ?: stringResource(R.string.content_desc_back),
                    onClick = onNavigateBack,
                    containerColor = if (transparentBackground) OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.8f) else Color.Transparent,
                    isCircle = transparentBackground,
                    tint = OpenStreamTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
            }

            Column(modifier = Modifier.weight(1f)) {
                if (!eyebrow.isNullOrBlank()) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = OpenStreamTheme.typography.eyebrow,
                        color = OpenStreamTheme.colors.accent
                    )
                }
                Text(
                    text = title,
                    style = OpenStreamTheme.typography.displaySmall,
                    color = OpenStreamTheme.colors.textPrimary,
                    maxLines = 1
                )
            }

            actions()
        }

        if (showBottomDivider && !transparentBackground) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                thickness = 1.dp,
                color = OpenStreamTheme.colors.border
            )
        }
    }
}
