package com.masum.fathom.core.audio

import com.masum.fathom.core.sanctuary.ByokProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NeuralAudioSynthesizerTest {

    @Test
    fun testDefaultVoiceConstants() {
        assertEquals("21m00Tcm4TlvDq8ikWAM", NeuralAudioSynthesizer.DEFAULT_ELEVENLABS_VOICE)
        assertEquals("onyx", NeuralAudioSynthesizer.DEFAULT_OPENAI_VOICE)
    }

    @Test
    fun testByokProviders() {
        val providers = ByokProvider.entries
        assertEquals(3, providers.size)
        assertTrue(providers.contains(ByokProvider.NONE))
        assertTrue(providers.contains(ByokProvider.ELEVENLABS))
        assertTrue(providers.contains(ByokProvider.OPENAI))
    }

    @Test
    fun testProviderDisplayNames() {
        assertNotNull(ByokProvider.ELEVENLABS.displayName)
        assertNotNull(ByokProvider.OPENAI.displayName)
        assertNotNull(ByokProvider.NONE.displayName)
    }
}
