package com.bolaenglish.app.tutor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VoiceState(
    val available: Boolean, val listening: Boolean = false, val finishing: Boolean = false,
    val interim: String = "", val finalText: String = "", val notice: String = "",
    val canSpeak: Boolean = false, val playing: String? = null
)

class VoicePipelineManager(context: Context) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val mutable = MutableStateFlow(VoiceState(SpeechRecognizer.isRecognitionAvailable(app)))
    val state = mutable.asStateFlow()
    private var recognizer: SpeechRecognizer? = null
    private var generation = 0
    private var sequence = 0
    private var closed = false
    private var locale = Locale.forLanguageTag("en-IN")
    private var tts: TextToSpeech? = null
    private var initialized = false

    init {
        tts = TextToSpeech(app) { status -> main.post {
            if (!closed && status == TextToSpeech.SUCCESS) {
                initialized = true
                setLocale(locale.toLanguageTag())
            }
        } }
    }

    fun setLocale(tag: String) {
        locale = Locale.forLanguageTag(tag)
        if (initialized) {
            val supported = (tts?.setLanguage(locale) ?: -2) >= 0 || (tts?.setLanguage(Locale.US) ?: -2) >= 0
            mutable.update { it.copy(canSpeak = supported) }
        }
    }

    fun start() {
        pause()
        if (app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionDenied()
            return
        }
        if (!mutable.value.available) return
        val session = generation
        try {
            val engine = SpeechRecognizer.createSpeechRecognizer(app)
            recognizer = engine
            engine.setRecognitionListener(object : RecognitionListener {
                private fun text(bundle: Bundle?) = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                override fun onPartialResults(partialResults: Bundle?) {
                    if (session == generation) mutable.update { it.copy(interim = text(partialResults)) }
                }
                override fun onResults(results: Bundle?) {
                    if (session != generation) return
                    val final = text(results)
                    endRecognition()
                    mutable.update { it.copy(finalText = final, notice = if (final.isBlank()) "No speech captured. Try again or type." else "") }
                }
                override fun onError(error: Int) {
                    if (session != generation) return
                    endRecognition()
                    mutable.update { it.copy(notice = "Speech recognition stopped. Try again or type your sentence.") }
                }
                override fun onEndOfSpeech() {
                    if (session == generation) mutable.update { it.copy(listening = false, finishing = true) }
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            mutable.update { it.copy(listening = true, notice = "") }
            engine.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true))
        } catch (_: RuntimeException) {
            endRecognition()
            mutable.update { it.copy(notice = "Microphone unavailable. Type your sentence.") }
        }
    }

    fun finishListening() {
        mutable.update { it.copy(listening = false, finishing = true) }
        recognizer?.stopListening()
    }

    fun permissionDenied() { mutable.update { it.copy(notice = "Microphone permission is off. You can type below.") } }
    fun consumed() { mutable.update { it.copy(finalText = "") } }

    private fun endRecognition() {
        generation++
        recognizer?.destroy()
        recognizer = null
        mutable.update { it.copy(listening = false, finishing = false, interim = "") }
    }

    fun speak(text: String) {
        if (!mutable.value.canSpeak) return
        pause()
        val id = (++sequence).toString()
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = ended(utteranceId)
            @Deprecated("Android callback")
            override fun onError(utteranceId: String?) = ended(utteranceId)
            private fun ended(utteranceId: String?) {
                main.post { if (utteranceId == id && mutable.value.playing == id) mutable.update { it.copy(playing = null) } }
            }
        })
        mutable.update { it.copy(playing = id) }
        if (tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) {
            mutable.update { it.copy(playing = null, notice = "Playback unavailable. Read the reply below.") }
        }
    }

    fun pause() {
        endRecognition()
        tts?.stop()
        mutable.update { it.copy(playing = null) }
    }

    fun close() {
        closed = true
        pause()
        tts?.shutdown()
        tts = null
    }
}
