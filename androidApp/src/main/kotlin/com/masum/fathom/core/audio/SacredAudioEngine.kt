package com.masum.fathom.core.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class RecitationMode {
    FULL,
    ORIGINAL_ONLY,
    TRANSLATION_ONLY
}

sealed interface SacredAudioState {
    data object Idle : SacredAudioState
    data class Playing(
        val passage: Passage,
        val isOriginalPart: Boolean,
        val mode: RecitationMode
    ) : SacredAudioState
    data class Paused(
        val passage: Passage,
        val isOriginalPart: Boolean,
        val mode: RecitationMode
    ) : SacredAudioState
}

class SacredAudioEngine(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingPlayAction: (() -> Unit)? = null

    private val _playbackState = MutableStateFlow<SacredAudioState>(SacredAudioState.Idle)
    val playbackState: StateFlow<SacredAudioState> = _playbackState.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                setupProgressListener()
                pendingPlayAction?.invoke()
                pendingPlayAction = null
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val current = _playbackState.value
                if (current is SacredAudioState.Playing) {
                    val isOriginal = utteranceId?.startsWith("ORIGINAL_") == true
                    scope.launch {
                        _playbackState.value = current.copy(isOriginalPart = isOriginal)
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    val current = _playbackState.value
                    if (current is SacredAudioState.Playing) {
                        if (utteranceId?.startsWith("ORIGINAL_") == true) {
                            if (current.mode == RecitationMode.FULL) {
                                playTranslationPart(current.passage, current.mode)
                            } else {
                                _playbackState.value = SacredAudioState.Idle
                            }
                        } else if (utteranceId?.startsWith("TRANSLATION_") == true) {
                            _playbackState.value = SacredAudioState.Idle
                        }
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                scope.launch {
                    _playbackState.value = SacredAudioState.Idle
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                scope.launch {
                    _playbackState.value = SacredAudioState.Idle
                }
            }
        })
    }

    fun play(passage: Passage, mode: RecitationMode = RecitationMode.FULL) {
        if (!isTtsInitialized) {
            pendingPlayAction = { play(passage, mode) }
            return
        }

        stop()

        val hasOriginal = !passage.originalText.isNullOrBlank() && passage.originalText != passage.translation
        val targetMode = if (!hasOriginal && mode == RecitationMode.ORIGINAL_ONLY) RecitationMode.TRANSLATION_ONLY else mode

        when (targetMode) {
            RecitationMode.ORIGINAL_ONLY -> {
                if (hasOriginal) {
                    _playbackState.value = SacredAudioState.Playing(passage, isOriginalPart = true, mode = targetMode)
                    playOriginalPart(passage)
                } else {
                    _playbackState.value = SacredAudioState.Playing(passage, isOriginalPart = false, mode = targetMode)
                    playTranslationPart(passage, targetMode)
                }
            }
            RecitationMode.TRANSLATION_ONLY -> {
                _playbackState.value = SacredAudioState.Playing(passage, isOriginalPart = false, mode = targetMode)
                playTranslationPart(passage, targetMode)
            }
            RecitationMode.FULL -> {
                if (hasOriginal) {
                    _playbackState.value = SacredAudioState.Playing(passage, isOriginalPart = true, mode = targetMode)
                    playOriginalPart(passage)
                } else {
                    _playbackState.value = SacredAudioState.Playing(passage, isOriginalPart = false, mode = targetMode)
                    playTranslationPart(passage, targetMode)
                }
            }
        }
    }

    private fun playOriginalPart(passage: Passage) {
        val targetLocale = getOriginalLocale(passage.traditionId)
        configureVoice(targetLocale, rate = 0.82f, pitch = 0.92f)

        val textToSpeak = cleanPhonetics(passage.originalText ?: passage.translation)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ORIGINAL_${passage.id}")
        }
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "ORIGINAL_${passage.id}")
    }

    private fun playTranslationPart(passage: Passage, mode: RecitationMode) {
        _playbackState.value = SacredAudioState.Playing(passage, isOriginalPart = false, mode = mode)
        configureVoice(Locale.US, rate = 0.86f, pitch = 0.95f)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TRANSLATION_${passage.id}")
        }
        tts?.speak(passage.translation, TextToSpeech.QUEUE_FLUSH, params, "TRANSLATION_${passage.id}")
    }

    private fun configureVoice(preferredLocale: Locale, rate: Float, pitch: Float) {
        val engine = tts ?: return
        val avail = engine.isLanguageAvailable(preferredLocale)
        if (avail >= TextToSpeech.LANG_AVAILABLE) {
            engine.language = preferredLocale
        } else {
            val fallback = if (preferredLocale.language == "hi" || preferredLocale.language == "sa") {
                Locale("hi", "IN")
            } else if (preferredLocale.language == "ar") {
                Locale("ar")
            } else if (preferredLocale.language == "zh") {
                Locale.SIMPLIFIED_CHINESE
            } else {
                Locale.US
            }
            if (engine.isLanguageAvailable(fallback) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = fallback
            } else {
                engine.language = Locale.US
            }
        }
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
    }

    private fun getOriginalLocale(traditionId: TraditionId): Locale {
        return when (traditionId) {
            TraditionId.HINDUISM -> Locale("hi", "IN")
            TraditionId.ISLAM -> Locale("ar", "SA")
            TraditionId.TAOISM -> Locale.SIMPLIFIED_CHINESE
            TraditionId.BUDDHISM -> Locale("hi", "IN")
            TraditionId.JUDAISM -> Locale("he", "IL")
            TraditionId.CHRISTIANITY, TraditionId.STOICISM -> Locale.US
        }
    }

    private fun cleanPhonetics(raw: String): String {
        val parts = raw.split("\n\n")
        return parts.firstOrNull { it.isNotBlank() } ?: raw
    }

    fun stop() {
        tts?.stop()
        _playbackState.value = SacredAudioState.Idle
    }

    fun toggle(passage: Passage, mode: RecitationMode = RecitationMode.FULL) {
        val current = _playbackState.value
        if (current is SacredAudioState.Playing && current.passage.id == passage.id) {
            stop()
        } else {
            play(passage, mode)
        }
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        isTtsInitialized = false
    }
}
