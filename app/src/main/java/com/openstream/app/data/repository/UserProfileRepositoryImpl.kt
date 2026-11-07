package com.openstream.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openstream.app.domain.model.UserProfile
import com.openstream.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

private val Context.profilesDataStore by preferencesDataStore(name = "openstream_profiles_prefs")

class UserProfileRepositoryImpl(
    private val context: Context? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserProfileRepository {

    private val gson = Gson()

    private object Keys {
        val PROFILES_JSON = stringPreferencesKey("profiles_json")
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
    }

    // In-memory fallback for testing
    private val memoryProfiles = MutableStateFlow(listOf(UserProfile.createDefault()))
    private val memoryActiveId = MutableStateFlow(UserProfile.DEFAULT_PROFILE_ID)

    override val profiles: Flow<List<UserProfile>> = if (context != null) {
        context.profilesDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs ->
                val json = prefs[Keys.PROFILES_JSON]
                if (json.isNullOrBlank()) {
                    listOf(UserProfile.createDefault())
                } else {
                    val type = object : TypeToken<List<UserProfile>>() {}.type
                    val list: List<UserProfile>? = gson.fromJson(json, type)
                    if (list.isNullOrEmpty()) listOf(UserProfile.createDefault()) else list
                }
            }
    } else {
        memoryProfiles.asStateFlow()
    }

    override val activeProfileId: Flow<String> = if (context != null) {
        context.profilesDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs ->
                prefs[Keys.ACTIVE_PROFILE_ID] ?: UserProfile.DEFAULT_PROFILE_ID
            }
    } else {
        memoryActiveId.asStateFlow()
    }

    override val activeProfile: Flow<UserProfile> = if (context != null) {
        context.profilesDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs ->
                val currentId = prefs[Keys.ACTIVE_PROFILE_ID] ?: UserProfile.DEFAULT_PROFILE_ID
                val json = prefs[Keys.PROFILES_JSON]
                val list: List<UserProfile> = if (json.isNullOrBlank()) {
                    listOf(UserProfile.createDefault())
                } else {
                    val type = object : TypeToken<List<UserProfile>>() {}.type
                    gson.fromJson(json, type) ?: listOf(UserProfile.createDefault())
                }
                list.firstOrNull { it.id == currentId } ?: list.firstOrNull() ?: UserProfile.createDefault()
            }
    } else {
        memoryProfiles.asStateFlow().map { list ->
            list.firstOrNull { it.id == memoryActiveId.value } ?: list.first()
        }
    }

    override suspend fun getActiveProfileSync(): UserProfile = withContext(ioDispatcher) {
        activeProfile.first()
    }

    override suspend fun createProfile(
        name: String,
        avatarId: String,
        preferredGenres: List<String>,
        languageCode: String?
    ): UserProfile = withContext(ioDispatcher) {
        val newProfile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "Viewer" },
            avatarId = avatarId,
            isDefault = false,
            preferredGenres = preferredGenres,
            languageCode = languageCode
        )

        if (context != null) {
            context.profilesDataStore.edit { prefs ->
                val currentList = getCurrentProfiles(prefs[Keys.PROFILES_JSON]).toMutableList()
                currentList.add(newProfile)
                prefs[Keys.PROFILES_JSON] = gson.toJson(currentList)
            }
        } else {
            val list = memoryProfiles.value.toMutableList()
            list.add(newProfile)
            memoryProfiles.value = list
        }
        newProfile
    }

    override suspend fun updateProfile(profile: UserProfile): Unit = withContext(ioDispatcher) {
        if (context != null) {
            context.profilesDataStore.edit { prefs ->
                val currentList = getCurrentProfiles(prefs[Keys.PROFILES_JSON]).toMutableList()
                val index = currentList.indexOfFirst { it.id == profile.id }
                if (index >= 0) {
                    currentList[index] = profile
                    prefs[Keys.PROFILES_JSON] = gson.toJson(currentList)
                }
            }
        } else {
            val list = memoryProfiles.value.toMutableList()
            val index = list.indexOfFirst { it.id == profile.id }
            if (index >= 0) {
                list[index] = profile
                memoryProfiles.value = list
            }
        }
    }

    override suspend fun deleteProfile(profileId: String): Boolean = withContext(ioDispatcher) {
        if (context != null) {
            var deleted = false
            context.profilesDataStore.edit { prefs ->
                val currentList = getCurrentProfiles(prefs[Keys.PROFILES_JSON]).toMutableList()
                if (currentList.size > 1) {
                    val toRemove = currentList.firstOrNull { it.id == profileId }
                    if (toRemove != null) {
                        currentList.remove(toRemove)
                        prefs[Keys.PROFILES_JSON] = gson.toJson(currentList)
                        deleted = true

                        // If deleted active profile, revert active to first available
                        val activeId = prefs[Keys.ACTIVE_PROFILE_ID]
                        if (activeId == profileId) {
                            prefs[Keys.ACTIVE_PROFILE_ID] = currentList.first().id
                        }
                    }
                }
            }
            deleted
        } else {
            val list = memoryProfiles.value.toMutableList()
            if (list.size > 1) {
                val index = list.indexOfFirst { it.id == profileId }
                if (index >= 0) {
                    list.removeAt(index)
                    memoryProfiles.value = list
                    if (memoryActiveId.value == profileId) {
                        memoryActiveId.value = list.first().id
                    }
                    return@withContext true
                }
            }
            false
        }
    }

    override suspend fun switchProfile(profileId: String): Unit = withContext(ioDispatcher) {
        if (context != null) {
            context.profilesDataStore.edit { prefs ->
                prefs[Keys.ACTIVE_PROFILE_ID] = profileId
            }
        } else {
            memoryActiveId.value = profileId
        }
    }

    private fun getCurrentProfiles(json: String?): List<UserProfile> {
        if (json.isNullOrBlank()) return listOf(UserProfile.createDefault())
        return try {
            val type = object : TypeToken<List<UserProfile>>() {}.type
            gson.fromJson(json, type) ?: listOf(UserProfile.createDefault())
        } catch (_: Exception) {
            listOf(UserProfile.createDefault())
        }
    }
}
