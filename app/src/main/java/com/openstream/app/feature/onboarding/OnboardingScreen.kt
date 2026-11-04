package com.openstream.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamButton
import com.openstream.app.core.designsystem.component.OpenStreamButtonVariant
import com.openstream.app.core.designsystem.theme.OpenStreamTheme

private val onboardingPages = listOf(
    OnboardingPageData(
        titleResId = R.string.onboarding_title_1,
        descriptionResId = R.string.onboarding_desc_1,
        illustrationResId = R.drawable.ic_onboarding_cinema
    ),
    OnboardingPageData(
        titleResId = R.string.onboarding_title_2,
        descriptionResId = R.string.onboarding_desc_2,
        illustrationResId = R.drawable.ic_onboarding_public_domain
    ),
    OnboardingPageData(
        titleResId = R.string.onboarding_title_3,
        descriptionResId = R.string.onboarding_desc_3,
        illustrationResId = R.drawable.ic_onboarding_experience
    )
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    val currentPage = onboardingPages[uiState.currentPageIndex]
    val isLastPage = uiState.currentPageIndex == uiState.totalPages - 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(OpenStreamTheme.spacing.large)
    ) {
        // Skip Button in Top Right
        if (!isLastPage) {
            TextButton(
                onClick = viewModel::onSkipClicked,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = OpenStreamTheme.typography.labelLarge,
                    color = OpenStreamTheme.colors.textSecondary
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_artwork_transition"
            ) { page ->
                Image(
                    painter = painterResource(id = page.illustrationResId),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.extraLarge))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_text_transition"
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = page.titleResId),
                        style = OpenStreamTheme.typography.displayMedium,
                        color = OpenStreamTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

                    Text(
                        text = stringResource(id = page.descriptionResId),
                        style = OpenStreamTheme.typography.bodyLarge,
                        color = OpenStreamTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
                    )
                }
            }
        }

        // Bottom Controls: Page Indicator + Action Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until uiState.totalPages) {
                    val isSelected = i == uiState.currentPageIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.surfaceElevated
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

            OpenStreamButton(
                text = stringResource(
                    if (isLastPage) R.string.onboarding_get_started else R.string.onboarding_next
                ),
                onClick = viewModel::onNextClicked,
                modifier = Modifier.fillMaxWidth(),
                variant = if (isLastPage) OpenStreamButtonVariant.Primary else OpenStreamButtonVariant.Outlined
            )
        }
    }
}
