package com.openstream.app.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navTarget by viewModel.navigationTarget.collectAsState()
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        delay(700)
        viewModel.checkInitialRoute()
    }

    LaunchedEffect(navTarget) {
        if (navTarget is SplashNavigationTarget.Navigate) {
            val destination = (navTarget as SplashNavigationTarget.Navigate).destination
            onNavigate(destination)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = stringResource(R.string.content_desc_logo),
                modifier = Modifier.size(72.dp),
                tint = OpenStreamTheme.colors.accent
            )

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

            Text(
                text = stringResource(R.string.app_name),
                style = OpenStreamTheme.typography.displayLarge,
                color = OpenStreamTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

            Text(
                text = stringResource(R.string.tagline),
                style = OpenStreamTheme.typography.titleMedium,
                color = OpenStreamTheme.colors.accent
            )
        }
    }
}
