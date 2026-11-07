package com.openstream.app.core.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedPlayerFeaturesTest {

    @Test
    fun playerState_initializesWithDefaultPreferences() {
        val state = PlayerState()
        assertEquals(1.0f, state.playbackSpeed, 0.01f)
        assertFalse(state.isPlaying)
        assertFalse(state.isBuffering)
        assertFalse(state.isPictureInPicture)
        assertTrue(state.availableSubtitles.isEmpty())
        assertNull(state.selectedSubtitle)
    }

    @Test
    fun subtitleSelection_updatesSelectedState() {
        val tracks = listOf(
            SubtitleTrack(id = "en-0", name = "English [CC]", language = "en", isSelected = false),
            SubtitleTrack(id = "es-1", name = "Spanish", language = "es", isSelected = false)
        )
        val state = PlayerState(availableSubtitles = tracks)
        assertEquals(2, state.availableSubtitles.size)

        // Select English
        val englishSelected = state.copy(
            selectedSubtitle = tracks.first(),
            availableSubtitles = state.availableSubtitles.map { it.copy(isSelected = it.language == "en") }
        )
        assertTrue(englishSelected.availableSubtitles.first().isSelected)
        assertFalse(englishSelected.availableSubtitles[1].isSelected)
        assertEquals("English [CC]", englishSelected.selectedSubtitle?.name)

        // Turn Off Subtitles
        val subtitlesOff = englishSelected.copy(
            selectedSubtitle = null,
            availableSubtitles = englishSelected.availableSubtitles.map { it.copy(isSelected = false) }
        )
        assertNull(subtitlesOff.selectedSubtitle)
        assertFalse(subtitlesOff.availableSubtitles.any { it.isSelected })
    }

    @Test
    fun qualitySwitching_updatesActiveStreamVariant() {
        val qualities = listOf(
            QualityOption(label = "1080p Full HD", url = "https://example.com/movie_1080p.mp4", isSelected = false),
            QualityOption(label = "720p HD", url = "https://example.com/movie_720p.mp4", isSelected = true),
            QualityOption(label = "480p SD", url = "https://example.com/movie_480p.mp4", isSelected = false)
        )
        val state = PlayerState(
            availableQualities = qualities,
            selectedQuality = qualities[1]
        )

        assertEquals("720p HD", state.selectedQuality?.label)

        // Switch to 1080p Full HD
        val switched = state.copy(
            selectedQuality = qualities[0],
            availableQualities = qualities.map { it.copy(isSelected = it.url == qualities[0].url) }
        )
        assertEquals("1080p Full HD", switched.selectedQuality?.label)
        assertTrue(switched.availableQualities[0].isSelected)
        assertFalse(switched.availableQualities[1].isSelected)
    }

    @Test
    fun pictureInPicture_updatesStateFlag() {
        var state = PlayerState(isPictureInPicture = false)
        assertFalse(state.isPictureInPicture)

        state = state.copy(isPictureInPicture = true)
        assertTrue(state.isPictureInPicture)
    }

    @Test
    fun playbackSpeed_updatesCorrectly() {
        var state = PlayerState(playbackSpeed = 1.0f)
        assertEquals(1.0f, state.playbackSpeed, 0.01f)

        state = state.copy(playbackSpeed = 1.25f)
        assertEquals(1.25f, state.playbackSpeed, 0.01f)

        state = state.copy(playbackSpeed = 1.5f)
        assertEquals(1.5f, state.playbackSpeed, 0.01f)
    }
}
