package com.openstream.app.data.repository

import com.openstream.app.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserProfileRepositoryTest {

    private lateinit var repository: UserProfileRepositoryImpl

    @Before
    fun setup() {
        // InMemory testing mode
        repository = UserProfileRepositoryImpl(context = null)
    }

    @Test
    fun defaultProfile_isInitializedOnStart() = runTest {
        val profiles = repository.profiles.first()
        assertEquals(1, profiles.size)
        val defaultProfile = profiles.first()
        assertTrue(defaultProfile.isDefault)
        assertEquals(UserProfile.DEFAULT_PROFILE_ID, defaultProfile.id)
        assertEquals("reel", defaultProfile.avatarId)
    }

    @Test
    fun createProfile_addsNewProfileToList() = runTest {
        val created = repository.createProfile(
            name = "Film Noir Fan",
            avatarId = "camera",
            preferredGenres = listOf("Film Noir", "Mystery")
        )

        assertNotNull(created.id)
        assertEquals("Film Noir Fan", created.name)
        assertEquals("camera", created.avatarId)
        assertFalse(created.isDefault)

        val allProfiles = repository.profiles.first()
        assertEquals(2, allProfiles.size)
        assertTrue(allProfiles.any { it.id == created.id })
    }

    @Test
    fun switchProfile_changesActiveProfile() = runTest {
        val second = repository.createProfile(
            name = "Silent Fan",
            avatarId = "projector"
        )

        assertEquals(UserProfile.DEFAULT_PROFILE_ID, repository.activeProfileId.first())

        repository.switchProfile(second.id)
        assertEquals(second.id, repository.activeProfileId.first())
        val active = repository.activeProfile.first()
        assertEquals("Silent Fan", active.name)
    }

    @Test
    fun deleteProfile_preventsDeletingLastProfile() = runTest {
        val initialProfiles = repository.profiles.first()
        assertEquals(1, initialProfiles.size)

        // Attempting to delete the only profile should fail and return false
        val deleted = repository.deleteProfile(initialProfiles.first().id)
        assertFalse(deleted)
        assertEquals(1, repository.profiles.first().size)
    }

    @Test
    fun deleteProfile_whenMultipleExist_deletesSuccessfully() = runTest {
        val second = repository.createProfile(name = "Temp User", avatarId = "star")
        assertEquals(2, repository.profiles.first().size)

        val deleted = repository.deleteProfile(second.id)
        assertTrue(deleted)
        assertEquals(1, repository.profiles.first().size)
    }
}
