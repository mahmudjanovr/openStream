package com.openstream.app.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.navigation.OpenStreamDestinations
import com.openstream.app.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface SplashNavigationTarget {
    data object Pending : SplashNavigationTarget
    data class Navigate(val destination: String) : SplashNavigationTarget
}

class SplashViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _navigationTarget = MutableStateFlow<SplashNavigationTarget>(SplashNavigationTarget.Pending)
    val navigationTarget: StateFlow<SplashNavigationTarget> = _navigationTarget.asStateFlow()

    fun checkInitialRoute() {
        viewModelScope.launch {
            val isOnboardingCompleted = userPreferencesRepository.isOnboardingCompleted.first()
            val destination = if (isOnboardingCompleted) {
                OpenStreamDestinations.HOME
            } else {
                OpenStreamDestinations.ONBOARDING
            }
            _navigationTarget.value = SplashNavigationTarget.Navigate(destination)
        }
    }

    companion object {
        fun provideFactory(userPreferencesRepository: UserPreferencesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SplashViewModel(userPreferencesRepository) as T
                }
            }
    }
}
