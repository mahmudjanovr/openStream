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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

@Composable
fun OpenStreamEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(OpenStreamTheme.spacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(OpenStreamTheme.colors.surfaceElevated)
                    .border(1.dp, OpenStreamTheme.colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = OpenStreamTheme.colors.accent
                )
            }

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

            Text(
                text = title,
                style = OpenStreamTheme.typography.displaySmall,
                color = OpenStreamTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

            Text(
                text = description,
                style = OpenStreamTheme.typography.bodyMedium,
                color = OpenStreamTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
            )

            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.extraLarge))
                OpenStreamButton(
                    text = actionText,
                    onClick = onActionClick,
                    variant = OpenStreamButtonVariant.Primary
                )
            }
        }
    }
}
