package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

@Composable
fun OpenStreamSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = OpenStreamTheme.spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = OpenStreamTheme.typography.eyebrow,
                    color = OpenStreamTheme.colors.accent
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = OpenStreamTheme.typography.displaySmall,
                color = OpenStreamTheme.colors.textPrimary
            )
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = OpenStreamTheme.typography.labelMedium,
                color = OpenStreamTheme.colors.accent,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false),
                        onClick = onActionClick
                    )
                    .padding(bottom = 2.dp)
            )
        }
    }
}
