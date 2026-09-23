package br.app.egger.jarvis.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    @Test
    fun testAIProviderFromId() {
        assertEquals(AIProvider.ANTHROPIC, AIProvider.fromId("anthropic"))
        assertEquals(AIProvider.OPEN_ROUTER, AIProvider.fromId("openRouter"))
        assertEquals(AIProvider.OPEN_ROUTER, AIProvider.fromId("OPENROUTER"))
        assertEquals(AIProvider.ANTHROPIC, AIProvider.fromId("unknown_provider"))
    }

    @Test
    fun testJarvisVoicePreferenceFromId() {
        assertEquals(JarvisVoicePreference.MASCULINE, JarvisVoicePreference.fromId("masculine"))
        assertEquals(JarvisVoicePreference.AUTOMATIC, JarvisVoicePreference.fromId("automatic"))
        assertEquals(JarvisVoicePreference.FEMININE, JarvisVoicePreference.fromId("feminine"))
        assertEquals(JarvisVoicePreference.MASCULINE, JarvisVoicePreference.fromId("invalid"))
    }

    @Test
    fun testJarvisVoiceOptionLabel() {
        val voiceOption = JarvisVoiceOption("1", "Luciana", "pt-BR", "Feminino", "Alta")
        assertEquals("Luciana · pt-BR · Feminino · Alta", voiceOption.label)
    }

    @Test
    fun testSecondBrainInitialData() {
        assertTrue(SecondBrain.areas.containsKey("metas"))
        assertTrue(SecondBrain.initialNotes.isNotEmpty())

        val initialNote = SecondBrain.initialNotes.firstOrNull { it.id == "voce-ildemar" }
        assertNotNull(initialNote)
        assertEquals("Ildemar", initialNote?.title)
        assertEquals("meta", initialNote?.area)
    }

    @Test
    fun testVersionHistory() {
        val currentVersion = VersionHistory.entries.firstOrNull { it.isCurrent }
        assertNotNull(currentVersion)
        assertEquals(VersionHistory.CURRENT_VERSION_FALLBACK, currentVersion?.version)
        assertEquals(VersionHistory.CURRENT_BUILD_FALLBACK, currentVersion?.build)
    }
}
