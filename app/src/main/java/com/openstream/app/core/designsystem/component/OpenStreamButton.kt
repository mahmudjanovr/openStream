package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

enum class OpenStreamButtonVariant {
    Primary,
    Outlined,
    Text
}

@Composable
fun OpenStreamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OpenStreamButtonVariant = OpenStreamButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val shape = OpenStreamTheme.shapes.medium
    val minHeight = OpenStreamTheme.spacing.minTouchTarget
    val contentPadding = PaddingValues(
        horizontal = OpenStreamTheme.spacing.large,
        vertical = OpenStreamTheme.spacing.small
    )

    when (variant) {
        OpenStreamButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OpenStreamTheme.colors.accent,
                    contentColor = OpenStreamTheme.colors.background,
                    disabledContainerColor = OpenStreamTheme.colors.surfaceElevated,
                    disabledContentColor = OpenStreamTheme.colors.textSecondary
                )
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, textColor = OpenStreamTheme.colors.background)
            }
        }
        OpenStreamButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                border = BorderStroke(1.dp, if (enabled) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OpenStreamTheme.colors.textPrimary,
                    disabledContentColor = OpenStreamTheme.colors.textSecondary
                )
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, textColor = OpenStreamTheme.colors.textPrimary)
            }
        }
        OpenStreamButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = OpenStreamTheme.colors.accent,
                    disabledContentColor = OpenStreamTheme.colors.textSecondary
                )
            ) {
                ButtonContent(text = text, leadingIcon = leadingIcon, textColor = OpenStreamTheme.colors.accent)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) {
            androidx.compose.material3.Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
        }
        Text(
            text = text,
            style = OpenStreamTheme.typography.labelLarge,
            color = textColor
        )
    }
}
