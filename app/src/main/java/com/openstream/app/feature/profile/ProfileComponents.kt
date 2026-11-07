package com.openstream.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.UserProfile

fun getAvatarIcon(avatarId: String): ImageVector = when (avatarId) {
    "clapper" -> Icons.Default.Theaters
    "popcorn" -> Icons.Default.Fastfood
    "camera" -> Icons.Default.CameraAlt
    "star" -> Icons.Default.Star
    "projector" -> Icons.Default.Tv
    else -> Icons.Default.Movie
}

@Composable
fun ProfileAvatarBadge(
    avatarId: String,
    isActive: Boolean = false,
    size: Int = 56,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isActive) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.border
    val borderWidth = if (isActive) 2.dp else 1.dp

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(OpenStreamTheme.colors.surfaceElevated)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getAvatarIcon(avatarId),
            contentDescription = null,
            tint = if (isActive) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.textPrimary,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun ProfileCard(
    profile: UserProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isActive) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.border

    Column(
        modifier = modifier
            .clip(shape)
            .background(OpenStreamTheme.colors.surface)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onSelect)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            ProfileAvatarBadge(
                avatarId = profile.avatarId,
                isActive = isActive,
                size = 64
            )
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(OpenStreamTheme.colors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = OpenStreamTheme.colors.background,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = profile.name,
            style = OpenStreamTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = OpenStreamTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (profile.isDefault) {
            OpenStreamBadge(
                text = stringResource(R.string.profile_badge_default),
                variant = OpenStreamBadgeVariant.Subtle
            )
        } else if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete_profile),
                    tint = OpenStreamTheme.colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileSwitchDialog(
    profiles: List<UserProfile>,
    activeProfile: UserProfile,
    onSelectProfile: (String) -> Unit,
    onCreateProfile: (name: String, avatarId: String, genres: List<String>) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateProfileDialog(
            onConfirm = { name, avatarId, genres ->
                onCreateProfile(name, avatarId, genres)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profile_dialog_title),
                style = OpenStreamTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = OpenStreamTheme.colors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.profile_dialog_desc),
                    style = OpenStreamTheme.typography.bodySmall,
                    color = OpenStreamTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    items(profiles, key = { it.id }) { p ->
                        ProfileCard(
                            profile = p,
                            isActive = p.id == activeProfile.id,
                            onSelect = {
                                onSelectProfile(p.id)
                                onDismiss()
                            },
                            onDelete = if (profiles.size > 1 && !p.isDefault) {
                                { onDeleteProfile(p.id) }
                            } else null
                        )
                    }

                    // Add Profile Card
                    item {
                        AddProfileCard(onClick = { showCreateDialog = true })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_close),
                    color = OpenStreamTheme.colors.accent
                )
            }
        },
        containerColor = OpenStreamTheme.colors.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddProfileCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .clip(shape)
            .background(OpenStreamTheme.colors.surfaceElevated)
            .border(1.dp, OpenStreamTheme.colors.border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OpenStreamTheme.colors.border),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = OpenStreamTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.profile_add_button),
            style = OpenStreamTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = OpenStreamTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProfileDialog(
    onConfirm: (name: String, avatarId: String, genres: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(UserProfile.AVAILABLE_AVATARS.first()) }
    val availableGenres = listOf("Silent Era", "Film Noir", "Sci-Fi & Horror", "Comedy", "Drama")
    val selectedGenres = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profile_create_title),
                style = OpenStreamTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = OpenStreamTheme.colors.textPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profile_name_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenStreamTheme.colors.accent,
                        unfocusedBorderColor = OpenStreamTheme.colors.border,
                        focusedTextColor = OpenStreamTheme.colors.textPrimary,
                        unfocusedTextColor = OpenStreamTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.profile_avatar_label),
                    style = OpenStreamTheme.typography.labelMedium,
                    color = OpenStreamTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UserProfile.AVAILABLE_AVATARS.forEach { avatarId ->
                        val isSelected = avatarId == selectedAvatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) OpenStreamTheme.colors.accent.copy(alpha = 0.2f) else OpenStreamTheme.colors.surfaceElevated)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.border,
                                    CircleShape
                                )
                                .clickable { selectedAvatar = avatarId },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAvatarIcon(avatarId),
                                contentDescription = null,
                                tint = if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.profile_preferred_genres_label),
                    style = OpenStreamTheme.typography.labelMedium,
                    color = OpenStreamTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableGenres.forEach { genre ->
                        val isSelected = selectedGenres.contains(genre)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.surfaceElevated)
                                .clickable {
                                    if (isSelected) selectedGenres.remove(genre) else selectedGenres.add(genre)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = genre,
                                style = OpenStreamTheme.typography.labelSmall,
                                color = if (isSelected) OpenStreamTheme.colors.background else OpenStreamTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedAvatar, selectedGenres.toList())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.common_create),
                    color = if (name.isNotBlank()) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.textSecondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = OpenStreamTheme.colors.textSecondary
                )
            }
        },
        containerColor = OpenStreamTheme.colors.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
