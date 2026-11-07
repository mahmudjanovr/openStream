package com.openstream.app.core.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    @Test
    fun defaultPlayerState_isIdleAndUnbuffered() {
        val state = PlayerState()
        assertFalse(state.isPlaying)
        assertFalse(state.isBuffering)
        assertFalse(state.isEnded)
        assertEquals(0L, state.currentPositionMs)
        assertEquals(0L, state.durationMs)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun bufferingState_updatesFlagsCorrectly() {
        val state = PlayerState().copy(
            isBuffering = true,
            durationMs = 90_000L
        )
        assertTrue(state.isBuffering)
        assertEquals(90_000L, state.durationMs)
    }

    @Test
    fun playingState_updatesPlaybackStatus() {
        val state = PlayerState(
            isPlaying = true,
            currentPositionMs = 15_000L,
            durationMs = 90_000L,
            bufferedPositionMs = 30_000L
        )
        assertTrue(state.isPlaying)
        assertEquals(15_000L, state.currentPositionMs)
        assertEquals(30_000L, state.bufferedPositionMs)
    }

    @Test
    fun errorState_clearsPlayingAndSetsMessage() {
        val state = PlayerState(
            isPlaying = false,
            errorMessage = "Connection timeout"
        )
        assertFalse(state.isPlaying)
        assertEquals("Connection timeout", state.errorMessage)
    }
}
