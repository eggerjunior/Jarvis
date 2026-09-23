package br.app.egger.jarvis.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import br.app.egger.jarvis.model.JarvisVoiceOption
import br.app.egger.jarvis.model.JarvisVoicePreference
import java.util.Locale

interface JarvisSpeechSynthesizerListener {
    fun onSpeechStarted()
    fun onSpeechFinished()
}

class JarvisSpeechSynthesizer(private val context: Context) : TextToSpeech.OnInitListener {

    var listener: JarvisSpeechSynthesizerListener? = null
    var voicePreference: JarvisVoicePreference = JarvisVoicePreference.MASCULINE
    var selectedVoiceIdentifier: String? = null

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null
    private var pendingIsPreview = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        listener?.onSpeechStarted()
                    }

                    override fun onDone(utteranceId: String?) {
                        listener?.onSpeechFinished()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        listener?.onSpeechFinished()
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        listener?.onSpeechFinished()
                    }
                })

                pendingSpeechText?.let { text ->
                    if (pendingIsPreview) {
                        preview(text, selectedVoiceIdentifier)
                    } else {
                        speak(text)
                    }
                    pendingSpeechText = null
                }
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            pendingSpeechText = text
            pendingIsPreview = false
            return
        }

        applyVoiceSettings(selectedVoiceIdentifier)
        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "jarvis_utterance_${System.currentTimeMillis()}")
    }

    fun preview(text: String, voiceIdentifier: String?) {
        if (!isInitialized) {
            pendingSpeechText = text
            pendingIsPreview = true
            return
        }

        applyVoiceSettings(voiceIdentifier)
        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "jarvis_preview_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        if (isInitialized) {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        }
    }

    fun getAvailableVoices(): List<JarvisVoiceOption> {
        if (!isInitialized) return emptyList()
        val voices = tts?.voices ?: return emptyList()

        return voices
            .filter { it.locale.language == "pt" || it.locale.language == "por" }
            .sortedBy { it.name }
            .map { voice ->
                val gender = when {
                    voice.name.lowercase().contains("male") || voice.name.lowercase().contains("masc") -> "Masculina"
                    voice.name.lowercase().contains("female") || voice.name.lowercase().contains("fem") -> "Feminina"
                    else -> "Indefinida"
                }
                val quality = if (voice.quality >= 400) "Alta" else "Padrão"
                JarvisVoiceOption(
                    id = voice.name,
                    name = voice.name,
                    language = voice.locale.displayName,
                    gender = gender,
                    quality = quality
                )
            }
    }

    private fun applyVoiceSettings(explicitVoiceId: String?) {
        val ttsEngine = tts ?: return

        // Explicit voice identifier from installed voices
        if (!explicitVoiceId.isNullOrEmpty()) {
            val matchingVoice = ttsEngine.voices?.firstOrNull { it.name == explicitVoiceId }
            if (matchingVoice != null) {
                ttsEngine.voice = matchingVoice
            }
        } else {
            // Pick based on preference
            val ptVoices = ttsEngine.voices?.filter { it.locale.language == "pt" } ?: emptyList()
            val preferredVoice = when (voicePreference) {
                JarvisVoicePreference.MASCULINE -> {
                    ptVoices.firstOrNull { v ->
                        val n = v.name.lowercase()
                        n.contains("male") || n.contains("felipe") || n.contains("thiago") || n.contains("daniel") || n.contains("joao")
                    }
                }
                JarvisVoicePreference.FEMININE -> {
                    ptVoices.firstOrNull { v ->
                        val n = v.name.lowercase()
                        n.contains("female") || n.contains("luciana") || n.contains("fernanda") || n.contains("maria")
                    }
                }
                JarvisVoicePreference.AUTOMATIC -> null
            }
            if (preferredVoice != null) {
                ttsEngine.voice = preferredVoice
            }
        }

        // Apply pitch & rate prosody according to preference
        when (voicePreference) {
            JarvisVoicePreference.MASCULINE -> {
                ttsEngine.setPitch(0.70f)
                ttsEngine.setSpeechRate(0.88f)
            }
            JarvisVoicePreference.AUTOMATIC -> {
                ttsEngine.setPitch(1.00f)
                ttsEngine.setSpeechRate(0.96f)
            }
            JarvisVoicePreference.FEMININE -> {
                ttsEngine.setPitch(1.15f)
                ttsEngine.setSpeechRate(1.00f)
            }
        }
    }
}
