package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

@Composable
fun OpenStreamPosterPlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    val shape = OpenStreamTheme.shapes.medium
    val gradient = Brush.verticalGradient(
        colors = listOf(
            OpenStreamTheme.colors.surfaceElevated,
            OpenStreamTheme.colors.surface,
            OpenStreamTheme.colors.background
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(gradient)
            .border(1.dp, OpenStreamTheme.colors.border, shape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(OpenStreamTheme.spacing.small)
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = OpenStreamTheme.colors.accent
            )
            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
            Text(
                text = title,
                style = OpenStreamTheme.typography.labelSmall,
                color = OpenStreamTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
