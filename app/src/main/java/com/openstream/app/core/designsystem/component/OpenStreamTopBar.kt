package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

@Composable
fun OpenStreamTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    navigationIconContentDescription: String? = null,
    showBottomDivider: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(OpenStreamTheme.colors.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = OpenStreamTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateBack != null) {
                OpenStreamIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = navigationIconContentDescription ?: "Back",
                    onClick = onNavigateBack,
                    tint = OpenStreamTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
            }

            Text(
                text = title,
                style = OpenStreamTheme.typography.displaySmall,
                color = OpenStreamTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            actions()
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                thickness = 1.dp,
                color = OpenStreamTheme.colors.border
            )
        }
    }
}
