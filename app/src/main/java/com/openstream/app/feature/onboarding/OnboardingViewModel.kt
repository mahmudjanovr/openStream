package com.openstream.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val titleResId: Int,
    val descriptionResId: Int,
    val illustrationResId: Int
)

data class OnboardingUiState(
    val currentPageIndex: Int = 0,
    val totalPages: Int = 3,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNextClicked() {
        val current = _uiState.value.currentPageIndex
        if (current < _uiState.value.totalPages - 1) {
            _uiState.value = _uiState.value.copy(currentPageIndex = current + 1)
        } else {
            completeOnboarding()
        }
    }

    fun onPageChanged(newIndex: Int) {
        if (newIndex in 0 until _uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(currentPageIndex = newIndex)
        }
    }

    fun onSkipClicked() {
        completeOnboarding()
    }

    fun onGetStartedClicked() {
        completeOnboarding()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)
        }
    }

    companion object {
        fun provideFactory(userPreferencesRepository: UserPreferencesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OnboardingViewModel(userPreferencesRepository) as T
                }
            }
    }
}
