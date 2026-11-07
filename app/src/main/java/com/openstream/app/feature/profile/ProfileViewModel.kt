package com.openstream.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.domain.model.UserProfile
import com.openstream.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    val profiles: StateFlow<List<UserProfile>> = profileRepository.profiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(UserProfile.createDefault()))

    val activeProfile: StateFlow<UserProfile> = profileRepository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile.createDefault())

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.switchProfile(profileId)
        }
    }

    fun createProfile(
        name: String,
        avatarId: String,
        preferredGenres: List<String> = emptyList(),
        languageCode: String? = null
    ) {
        viewModelScope.launch {
            profileRepository.createProfile(name, avatarId, preferredGenres, languageCode)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
        }
    }

    companion object {
        fun provideFactory(profileRepository: UserProfileRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(profileRepository) as T
                }
            }
    }
}
