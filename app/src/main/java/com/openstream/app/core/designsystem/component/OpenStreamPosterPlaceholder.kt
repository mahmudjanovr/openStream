package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

@Composable
fun OpenStreamPosterPlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    val shape = OpenStreamTheme.shapes.medium
    val gradient = Brush.radialGradient(
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
        // Film sprocket notches at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 4.dp)
                        .background(OpenStreamTheme.colors.border)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = OpenStreamTheme.spacing.medium, vertical = OpenStreamTheme.spacing.large)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(OpenStreamTheme.shapes.small)
                    .background(OpenStreamTheme.colors.surface)
                    .border(1.dp, OpenStreamTheme.colors.accent.copy(alpha = 0.5f), OpenStreamTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = OpenStreamTheme.colors.accent
                )
            }

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

            Text(
                text = title,
                style = OpenStreamTheme.typography.displaySmall,
                color = OpenStreamTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        // Film sprocket notches at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 4.dp)
                        .background(OpenStreamTheme.colors.border)
                )
            }
        }
    }
}
