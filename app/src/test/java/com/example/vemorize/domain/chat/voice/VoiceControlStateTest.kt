package com.example.vemorize.domain.chat.voice

import org.junit.Assert.*
import org.junit.Test

class VoiceControlStateTest {

    @Test
    fun `displayName returns correct values for all states`() {
        assertEquals("Stopped", VoiceControlState.Stopped.displayName())
        assertEquals("Listening for commands", VoiceControlState.ActiveListening.displayName())
        assertEquals("Say wake word to activate", VoiceControlState.WakeWordMode.displayName())
    }

    @Test
    fun `Stopped can transition to ActiveListening`() {
        val stopped = VoiceControlState.Stopped
        assertTrue(stopped.canTransitionTo(VoiceControlState.ActiveListening))
    }

    @Test
    fun `Stopped cannot transition to WakeWordMode`() {
        val stopped = VoiceControlState.Stopped
        assertFalse(stopped.canTransitionTo(VoiceControlState.WakeWordMode))
    }

    @Test
    fun `Stopped cannot transition to Stopped`() {
        val stopped = VoiceControlState.Stopped
        assertFalse(stopped.canTransitionTo(VoiceControlState.Stopped))
    }

    @Test
    fun `ActiveListening can transition to WakeWordMode`() {
        val active = VoiceControlState.ActiveListening
        assertTrue(active.canTransitionTo(VoiceControlState.WakeWordMode))
    }

    @Test
    fun `ActiveListening can transition to Stopped`() {
        val active = VoiceControlState.ActiveListening
        assertTrue(active.canTransitionTo(VoiceControlState.Stopped))
    }

    @Test
    fun `ActiveListening cannot transition to ActiveListening`() {
        val active = VoiceControlState.ActiveListening
        assertFalse(active.canTransitionTo(VoiceControlState.ActiveListening))
    }

    @Test
    fun `WakeWordMode can transition to ActiveListening`() {
        val wakeWord = VoiceControlState.WakeWordMode
        assertTrue(wakeWord.canTransitionTo(VoiceControlState.ActiveListening))
    }

    @Test
    fun `WakeWordMode can transition to Stopped`() {
        val wakeWord = VoiceControlState.WakeWordMode
        assertTrue(wakeWord.canTransitionTo(VoiceControlState.Stopped))
    }

    @Test
    fun `WakeWordMode cannot transition to WakeWordMode`() {
        val wakeWord = VoiceControlState.WakeWordMode
        assertFalse(wakeWord.canTransitionTo(VoiceControlState.WakeWordMode))
    }

    @Test
    fun `transitionTo returns new state when valid`() {
        val stopped = VoiceControlState.Stopped
        val active = stopped.transitionTo(VoiceControlState.ActiveListening)
        assertEquals(VoiceControlState.ActiveListening, active)
    }

    @Test(expected = IllegalStateException::class)
    fun `transitionTo throws when invalid from Stopped to WakeWordMode`() {
        val stopped = VoiceControlState.Stopped
        stopped.transitionTo(VoiceControlState.WakeWordMode)
    }

    @Test(expected = IllegalStateException::class)
    fun `transitionTo throws when invalid from ActiveListening to ActiveListening`() {
        val active = VoiceControlState.ActiveListening
        active.transitionTo(VoiceControlState.ActiveListening)
    }

    @Test
    fun `full cycle - Stopped to Active to WakeWord to Active to Stopped`() {
        var state: VoiceControlState = VoiceControlState.Stopped

        state = state.transitionTo(VoiceControlState.ActiveListening)
        assertEquals(VoiceControlState.ActiveListening, state)

        state = state.transitionTo(VoiceControlState.WakeWordMode)
        assertEquals(VoiceControlState.WakeWordMode, state)

        state = state.transitionTo(VoiceControlState.ActiveListening)
        assertEquals(VoiceControlState.ActiveListening, state)

        state = state.transitionTo(VoiceControlState.Stopped)
        assertEquals(VoiceControlState.Stopped, state)
    }
}
