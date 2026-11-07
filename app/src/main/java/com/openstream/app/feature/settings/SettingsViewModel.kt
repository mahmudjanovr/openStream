package com.openstream.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.UserProfile
import com.openstream.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userProfileRepository: UserProfileRepository? = null
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = userPreferencesRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    val currentTheme: StateFlow<String> = userPreferencesRepository.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "dark")

    val activeProfile: StateFlow<UserProfile> = (userProfileRepository?.activeProfile ?: flowOf(UserProfile.createDefault()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile.createDefault())

    val profiles: StateFlow<List<UserProfile>> = (userProfileRepository?.profiles ?: flowOf(listOf(UserProfile.createDefault())))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(UserProfile.createDefault()))

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedLanguage(language)
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            userProfileRepository?.switchProfile(profileId)
        }
    }

    fun createProfile(name: String, avatarId: String, preferredGenres: List<String>) {
        viewModelScope.launch {
            userProfileRepository?.createProfile(name, avatarId, preferredGenres)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            userProfileRepository?.deleteProfile(profileId)
        }
    }

    companion object {
        fun provideFactory(
            userPreferencesRepository: UserPreferencesRepository,
            userProfileRepository: UserProfileRepository? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(userPreferencesRepository, userProfileRepository) as T
                }
            }
    }
}
