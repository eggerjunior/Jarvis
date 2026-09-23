package br.app.egger.jarvis.session

import org.junit.Assert.assertEquals
import org.junit.Test

class JarvisSessionTest {

    @Test
    fun testJarvisStateRawValues() {
        assertEquals("Aguardando ativação", JarvisState.IDLE.rawValue)
        assertEquals("Ouvindo", JarvisState.LISTENING.rawValue)
        assertEquals("Pensando", JarvisState.THINKING.rawValue)
        assertEquals("Falando", JarvisState.SPEAKING.rawValue)
        assertEquals("Atenção", JarvisState.ERROR.rawValue)
    }
}
