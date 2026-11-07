package com.openstream.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.openstream.app.R
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.Movie

enum class MovieCardStyle {
    Portrait,
    Landscape
}

@Composable
fun OpenStreamMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardStyle: MovieCardStyle = MovieCardStyle.Portrait,
    cardWidth: Dp? = if (cardStyle == MovieCardStyle.Portrait) 144.dp else 240.dp,
    showProgress: Boolean = movie.playbackProgressPercentage != null
) {
    val cardModifier = if (cardWidth != null) {
        modifier.width(cardWidth)
    } else {
        modifier.fillMaxWidth()
    }

    val shape = OpenStreamTheme.shapes.medium
    val aspectRatio = if (cardStyle == MovieCardStyle.Portrait) 2f / 3f else 16f / 9f

    Column(
        modifier = cardModifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = OpenStreamTheme.colors.accent),
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(shape)
                .background(OpenStreamTheme.colors.surfaceElevated)
                .border(1.dp, OpenStreamTheme.colors.border, shape)
        ) {
            val imageUrl = if (cardStyle == MovieCardStyle.Landscape && movie.backdropUrl.isNotBlank()) {
                movie.backdropUrl
            } else {
                movie.posterUrl.ifBlank { movie.backdropUrl.ifBlank { movie.heroImageUrl } }
            }

            if (imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(200)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = stringResource(R.string.content_desc_movie_poster, movie.title),
                    contentScale = ContentScale.Crop,
                    loading = {
                        OpenStreamPosterPlaceholder(
                            title = movie.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    error = {
                        OpenStreamPosterPlaceholder(
                            title = movie.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                OpenStreamPosterPlaceholder(
                    title = movie.title,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cinematic gradient overlay on bottom of card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (cardStyle == MovieCardStyle.Portrait) 54.dp else 40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, OpenStreamTheme.colors.background.copy(alpha = 0.9f))
                        )
                    )
            )

            // Year or rating pill in top right corner
            OpenStreamBadge(
                text = "${movie.year}",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                variant = OpenStreamBadgeVariant.Gold
            )

            // Playback progress bar for continue watching
            if (showProgress && movie.playbackProgressPercentage != null) {
                LinearProgressIndicator(
                    progress = { movie.playbackProgressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .align(Alignment.BottomCenter),
                    color = OpenStreamTheme.colors.accent,
                    trackColor = OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

        Text(
            text = movie.title,
            style = OpenStreamTheme.typography.titleSmall,
            color = OpenStreamTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = movie.genre,
                style = OpenStreamTheme.typography.bodySmall,
                color = OpenStreamTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (showProgress && movie.playbackProgressPercentage != null) {
                val percentInt = (movie.playbackProgressPercentage * 100).toInt()
                Text(
                    text = " • $percentInt%",
                    style = OpenStreamTheme.typography.bodySmall,
                    color = OpenStreamTheme.colors.textSecondary
                )
            }
        }
    }
}
