package com.openstream.app.domain.repository

import com.openstream.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    val profiles: Flow<List<UserProfile>>
    val activeProfile: Flow<UserProfile>
    val activeProfileId: Flow<String>

    suspend fun createProfile(
        name: String,
        avatarId: String,
        preferredGenres: List<String> = emptyList(),
        languageCode: String? = null
    ): UserProfile

    suspend fun updateProfile(profile: UserProfile)

    suspend fun deleteProfile(profileId: String): Boolean

    suspend fun switchProfile(profileId: String)

    suspend fun getActiveProfileSync(): UserProfile
}
