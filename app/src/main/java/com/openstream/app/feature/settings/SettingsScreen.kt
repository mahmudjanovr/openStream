package com.openstream.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.component.OpenStreamTopBar
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.feature.profile.ProfileSwitchDialog
import com.openstream.app.feature.profile.getAvatarIcon

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    onCheckForUpdates: (() -> Unit)? = null
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val profiles by viewModel.profiles.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog) {
        ProfileSwitchDialog(
            profiles = profiles,
            activeProfile = activeProfile,
            onSelectProfile = viewModel::switchProfile,
            onCreateProfile = viewModel::createProfile,
            onDeleteProfile = viewModel::deleteProfile,
            onDismiss = { showProfileDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
    ) {
        OpenStreamTopBar(
            title = stringResource(R.string.settings_title),
            eyebrow = "SYSTEM & PREFERENCES"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = OpenStreamTheme.spacing.medium,
                vertical = OpenStreamTheme.spacing.large
            ),
            verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.large)
        ) {
            // User Profiles & Personalization Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.profile_section_title))
                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                SettingsCard {
                    SettingsRowItem(
                        icon = getAvatarIcon(activeProfile.avatarId),
                        title = activeProfile.name,
                        subtitle = "${profiles.size} ${stringResource(R.string.profile_count_suffix)}",
                        badgeText = stringResource(R.string.profile_action_switch),
                        onClick = { showProfileDialog = true }
                    )
                }
            }

            // Preferences Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_section_general))
                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                SettingsCard {
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(currentLanguage.displayNameResId),
                        badgeText = currentLanguage.code.uppercase(),
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = OpenStreamTheme.colors.border, thickness = 1.dp)
                    SettingsRowItem(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.settings_theme),
                        subtitle = stringResource(R.string.settings_theme_dark),
                        badgeText = "ACTIVE",
                        onClick = { /* Dark OTT is the primary aesthetic */ }
                    )
                }
            }

            // About openStream Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_section_about))
                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                SettingsCard {
                    Column(modifier = Modifier.padding(OpenStreamTheme.spacing.medium)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(OpenStreamTheme.colors.surfaceElevated)
                                    .border(1.dp, OpenStreamTheme.colors.accent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = OpenStreamTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = OpenStreamTheme.typography.displaySmall,
                                    color = OpenStreamTheme.colors.textPrimary
                                )
                                Text(
                                    text = stringResource(R.string.tagline),
                                    style = OpenStreamTheme.typography.eyebrow,
                                    color = OpenStreamTheme.colors.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

                        Text(
                            text = stringResource(R.string.settings_about_desc),
                            style = OpenStreamTheme.typography.bodyMedium,
                            color = OpenStreamTheme.colors.textSecondary
                        )
                    }

                    HorizontalDivider(color = OpenStreamTheme.colors.border, thickness = 1.dp)

                    SettingsRowItem(
                        icon = Icons.Default.Gavel,
                        title = "Copyright & Territory Verification",
                        subtitle = stringResource(R.string.settings_copyright_pledge),
                        onClick = { /* Copyright info */ }
                    )

                    HorizontalDivider(color = OpenStreamTheme.colors.border, thickness = 1.dp)

                    SettingsRowItem(
                        icon = Icons.Default.PrivacyTip,
                        title = stringResource(R.string.settings_privacy),
                        subtitle = stringResource(R.string.settings_privacy_note),
                        onClick = { /* Privacy info */ }
                    )

                    HorizontalDivider(color = OpenStreamTheme.colors.border, thickness = 1.dp)

                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_version),
                        subtitle = stringResource(R.string.settings_phase),
                        badgeText = "v${com.openstream.app.BuildConfig.VERSION_NAME}",
                        onClick = { onCheckForUpdates?.invoke() }
                    )

                    HorizontalDivider(color = OpenStreamTheme.colors.border, thickness = 1.dp)

                    SettingsRowItem(
                        icon = Icons.Default.SystemUpdate,
                        title = stringResource(R.string.settings_check_updates),
                        subtitle = stringResource(R.string.settings_check_updates_desc),
                        onClick = { onCheckForUpdates?.invoke() }
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { lang ->
                viewModel.onLanguageSelected(lang)
                onLanguageChange(lang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = OpenStreamTheme.typography.eyebrow,
        color = OpenStreamTheme.colors.accent
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    val shape = OpenStreamTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OpenStreamTheme.colors.surface)
            .border(1.dp, OpenStreamTheme.colors.border, shape)
    ) {
        content()
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = OpenStreamTheme.spacing.medium,
                vertical = OpenStreamTheme.spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OpenStreamTheme.colors.accent,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = OpenStreamTheme.typography.titleSmall,
                color = OpenStreamTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = OpenStreamTheme.typography.bodySmall,
                color = OpenStreamTheme.colors.textSecondary
            )
        }

        if (badgeText != null) {
            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
            OpenStreamBadge(
                text = badgeText,
                variant = OpenStreamBadgeVariant.Gold
            )
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_language),
                style = OpenStreamTheme.typography.titleLarge,
                color = OpenStreamTheme.colors.textPrimary
            )
        },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    val isSelected = language == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onLanguageSelected(language) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = OpenStreamTheme.colors.accent,
                                unselectedColor = OpenStreamTheme.colors.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(language.displayNameResId),
                            style = OpenStreamTheme.typography.bodyLarge,
                            color = if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    style = OpenStreamTheme.typography.labelLarge,
                    color = OpenStreamTheme.colors.accent
                )
            }
        },
        containerColor = OpenStreamTheme.colors.surfaceElevated,
        shape = OpenStreamTheme.shapes.large
    )
}
