package br.app.egger.jarvis.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.text.Normalizer
import java.util.Locale

interface JarvisSpeechRecognizerListener {
    fun onSpeechDetected()
    fun onFinalText(text: String)
    fun onError(errorMessage: String)
}

class JarvisSpeechRecognizer(private val context: Context) {

    var listener: JarvisSpeechRecognizerListener? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private var handler = Handler(Looper.getMainLooper())
    private var silenceRunnable: Runnable? = null
    private var lastText: String = ""
    private var isListening: Boolean = false

    fun start() {
        stop()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener?.onError("Reconhecimento de fala indisponível no dispositivo.")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio."
                        SpeechRecognizer.ERROR_CLIENT -> "Erro do cliente de fala."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de áudio não concedida."
                        SpeechRecognizer.ERROR_NETWORK -> "Erro de rede."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tempo limite de rede excedido."
                        SpeechRecognizer.ERROR_NO_MATCH -> "Nenhuma fala reconhecida."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconhecedor ocupado."
                        SpeechRecognizer.ERROR_SERVER -> "Erro no servidor de voz."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tempo limite de fala atingido."
                        else -> "Erro de fala ($error)."
                    }
                    stop()
                    listener?.onError(message)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim() ?: ""
                    if (text.isNotEmpty()) {
                        cancelSilenceTimer()
                        lastText = text
                        listener?.onFinalText(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim() ?: ""
                    if (text.isNotEmpty() && text != lastText) {
                        lastText = text
                        listener?.onSpeechDetected()
                        resetSilenceTimer(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        lastText = ""
        isListening = true
        speechRecognizer?.startListening(intent)
    }

    fun stop() {
        cancelSilenceTimer()
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun resetSilenceTimer(text: String) {
        cancelSilenceTimer()
        val delayMillis = silenceIntervalMillis(text)
        silenceRunnable = Runnable {
            if (lastText.isNotEmpty()) {
                val textToSend = lastText
                lastText = ""
                listener?.onFinalText(textToSend)
            }
        }
        handler.postDelayed(silenceRunnable!!, delayMillis)
    }

    private fun cancelSilenceTimer() {
        silenceRunnable?.let { handler.removeCallbacks(it) }
        silenceRunnable = null
    }

    private fun silenceIntervalMillis(text: String): Long {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .trim()

        val words = normalized.split("\\s+".toRegex())
        val lastWord = words.lastOrNull() ?: return 3000L

        val continuationWords = setOf(
            "a", "as", "com", "da", "das", "de", "do", "dos", "e", "em", "na", "nas", "no", "nos",
            "o", "os", "para", "por", "que", "qual", "quais", "quem", "sobre"
        )

        return if (continuationWords.contains(lastWord)) 5000L else 3000L
    }
}
