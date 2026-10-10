package com.masum.fathom.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.ByokProvider
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

enum class RecitationMode {
    TRANSLATION_ONLY,
    ORIGINAL_ONLY,
    FULL
}

sealed interface SacredAudioState {
    data object Idle : SacredAudioState
    data class Playing(
        val passage: Passage,
        val isOriginalPart: Boolean,
        val mode: RecitationMode,
        val progress: Float = 0f,
        val currentChar: Int = 0,
        val speed: Float = 1.0f,
        val isNeural: Boolean = false
    ) : SacredAudioState
    data class Paused(
        val passage: Passage,
        val isOriginalPart: Boolean,
        val mode: RecitationMode,
        val progress: Float = 0f,
        val currentChar: Int = 0,
        val speed: Float = 1.0f,
        val isNeural: Boolean = false
    ) : SacredAudioState
}

class SacredAudioEngine(
    private val context: Context,
    private val preferences: SanctuaryPreferences
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val neuralSynthesizer = NeuralAudioSynthesizer(context, preferences)

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackingJob: Job? = null

    private var pendingPlayAction: (() -> Unit)? = null
    private var currentTextBeingSpoken: String = ""
    private var currentOffset: Int = 0
    private var currentSpeed: Float = 1.0f

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

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                val current = _playbackState.value
                if (current is SacredAudioState.Playing && !current.isNeural) {
                    val fullLength = currentTextBeingSpoken.length
                    val totalChar = (currentOffset + start).coerceIn(0, fullLength)
                    val prog = if (fullLength > 0) (totalChar.toFloat() / fullLength).coerceIn(0f, 1f) else 0f
                    scope.launch {
                        _playbackState.value = current.copy(
                            progress = prog,
                            currentChar = totalChar
                        )
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    val current = _playbackState.value
                    if (current is SacredAudioState.Playing && !current.isNeural) {
                        if (utteranceId?.startsWith("ORIGINAL_") == true) {
                            if (current.mode == RecitationMode.FULL) {
                                playTranslationPart(current.passage, current.mode, 0)
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

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?, errorCode: Int) {
                scope.launch {
                    _playbackState.value = SacredAudioState.Idle
                }
            }
        })
    }

    fun play(passage: Passage, mode: RecitationMode = RecitationMode.TRANSLATION_ONLY, fromCharOffset: Int = 0) {
        stopInternal()

        val hasOriginal = !passage.originalText.isNullOrBlank() && passage.originalText != passage.translation
        val targetMode = if (!hasOriginal && mode != RecitationMode.TRANSLATION_ONLY) RecitationMode.TRANSLATION_ONLY else mode

        val settings = preferences.settings.value
        val useNeural = settings.byokProvider != ByokProvider.NONE && settings.byokApiKey.isNotBlank()

        if (useNeural) {
            playNeural(passage, targetMode)
        } else {
            playViaTts(passage, targetMode, fromCharOffset)
        }
    }

    private fun playNeural(passage: Passage, mode: RecitationMode) {
        scope.launch {
            val isOriginal = mode == RecitationMode.ORIGINAL_ONLY
            val textToSpeak = when (mode) {
                RecitationMode.ORIGINAL_ONLY -> cleanPhonetics(passage.originalText ?: passage.translation)
                RecitationMode.TRANSLATION_ONLY, RecitationMode.FULL -> sanitizeText(passage.translation)
            }

            _playbackState.value = SacredAudioState.Playing(
                passage = passage,
                isOriginalPart = isOriginal,
                mode = mode,
                progress = 0f,
                currentChar = 0,
                speed = currentSpeed,
                isNeural = true
            )

            val audioFile: File? = neuralSynthesizer.synthesize(textToSpeak, passage.traditionId, isOriginal)
            if (audioFile != null && audioFile.exists()) {
                startMediaPlayer(audioFile, passage, mode, isOriginal)
            } else {
                playViaTts(passage, mode, 0)
            }
        }
    }

    private fun startMediaPlayer(audioFile: File, passage: Passage, mode: RecitationMode, isOriginal: Boolean) {
        try {
            stopMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                playbackParams = playbackParams.setSpeed(currentSpeed)
                setOnPreparedListener { mp ->
                    mp.start()
                    startProgressTracker(passage, mode, isOriginal)
                }
                setOnCompletionListener {
                    progressTrackingJob?.cancel()
                    if (mode == RecitationMode.FULL && isOriginal) {
                        playNeural(passage, RecitationMode.TRANSLATION_ONLY)
                    } else {
                        _playbackState.value = SacredAudioState.Idle
                    }
                }
                setOnErrorListener { _, _, _ ->
                    progressTrackingJob?.cancel()
                    _playbackState.value = SacredAudioState.Idle
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            playViaTts(passage, mode, 0)
        }
    }

    private fun startProgressTracker(passage: Passage, mode: RecitationMode, isOriginal: Boolean) {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val duration = player.duration
                    val current = player.currentPosition
                    val progress = if (duration > 0) (current.toFloat() / duration).coerceIn(0f, 1f) else 0f
                    _playbackState.value = SacredAudioState.Playing(
                        passage = passage,
                        isOriginalPart = isOriginal,
                        mode = mode,
                        progress = progress,
                        currentChar = 0,
                        speed = currentSpeed,
                        isNeural = true
                    )
                }
                delay(100)
            }
        }
    }

    private fun playViaTts(passage: Passage, mode: RecitationMode, fromCharOffset: Int) {
        if (!isTtsInitialized) {
            pendingPlayAction = { playViaTts(passage, mode, fromCharOffset) }
            return
        }

        when (mode) {
            RecitationMode.TRANSLATION_ONLY -> {
                playTranslationPart(passage, mode, fromCharOffset)
            }
            RecitationMode.ORIGINAL_ONLY -> {
                val hasOriginal = !passage.originalText.isNullOrBlank() && passage.originalText != passage.translation
                if (hasOriginal) {
                    playOriginalPart(passage, mode, fromCharOffset)
                } else {
                    playTranslationPart(passage, mode, fromCharOffset)
                }
            }
            RecitationMode.FULL -> {
                val hasOriginal = !passage.originalText.isNullOrBlank() && passage.originalText != passage.translation
                if (hasOriginal) {
                    playOriginalPart(passage, mode, fromCharOffset)
                } else {
                    playTranslationPart(passage, mode, fromCharOffset)
                }
            }
        }
    }

    private fun playOriginalPart(passage: Passage, mode: RecitationMode, fromCharOffset: Int = 0) {
        val targetLocale = getOriginalLocale(passage.traditionId)
        configureVoice(targetLocale, rate = currentSpeed * 0.92f, pitch = 1.0f)

        currentTextBeingSpoken = cleanPhonetics(passage.originalText ?: passage.translation)
        currentOffset = fromCharOffset.coerceIn(0, currentTextBeingSpoken.length)
        val initialProgress = if (currentTextBeingSpoken.isNotEmpty()) (currentOffset.toFloat() / currentTextBeingSpoken.length).coerceIn(0f, 1f) else 0f
        _playbackState.value = SacredAudioState.Playing(
            passage = passage,
            isOriginalPart = true,
            mode = mode,
            progress = initialProgress,
            currentChar = currentOffset,
            speed = currentSpeed,
            isNeural = false
        )
        val textToSpeak = if (currentOffset < currentTextBeingSpoken.length) {
            currentTextBeingSpoken.substring(currentOffset)
        } else {
            currentTextBeingSpoken
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ORIGINAL_${passage.id}")
        }
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "ORIGINAL_${passage.id}")
    }

    private fun playTranslationPart(passage: Passage, mode: RecitationMode, fromCharOffset: Int = 0) {
        configureVoice(Locale.US, rate = currentSpeed, pitch = 1.0f)

        currentTextBeingSpoken = sanitizeText(passage.translation)
        currentOffset = fromCharOffset.coerceIn(0, currentTextBeingSpoken.length)
        val initialProgress = if (currentTextBeingSpoken.isNotEmpty()) (currentOffset.toFloat() / currentTextBeingSpoken.length).coerceIn(0f, 1f) else 0f
        _playbackState.value = SacredAudioState.Playing(
            passage = passage,
            isOriginalPart = false,
            mode = mode,
            progress = initialProgress,
            currentChar = currentOffset,
            speed = currentSpeed,
            isNeural = false
        )
        val cleanTranslation = if (currentOffset < currentTextBeingSpoken.length) {
            currentTextBeingSpoken.substring(currentOffset)
        } else {
            currentTextBeingSpoken
        }

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TRANSLATION_${passage.id}")
        }
        tts?.speak(cleanTranslation, TextToSpeech.QUEUE_FLUSH, params, "TRANSLATION_${passage.id}")
    }

    private fun configureVoice(preferredLocale: Locale, rate: Float, pitch: Float) {
        val engine = tts ?: return
        try {
            val avail = engine.isLanguageAvailable(preferredLocale)
            if (avail >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = preferredLocale
            } else {
                engine.language = Locale.US
            }

            val voices = engine.voices
            val matchedVoice = voices?.filter {
                it.locale.language == engine.language.language && !it.isNetworkConnectionRequired
            }?.maxByOrNull { it.quality }

            if (matchedVoice != null) {
                engine.voice = matchedVoice
            }
        } catch (_: Exception) {
            engine.language = Locale.US
        }

        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
    }

    private fun getOriginalLocale(traditionId: TraditionId): Locale {
        return when (traditionId) {
            TraditionId.HINDUISM, TraditionId.BUDDHISM -> Locale.Builder().setLanguage("hi").setRegion("IN").build()
            TraditionId.ISLAM -> Locale.Builder().setLanguage("ar").setRegion("SA").build()
            TraditionId.TAOISM -> Locale.SIMPLIFIED_CHINESE
            TraditionId.JUDAISM -> Locale.Builder().setLanguage("he").setRegion("IL").build()
            TraditionId.CHRISTIANITY, TraditionId.STOICISM -> Locale.US
        }
    }

    private fun cleanPhonetics(raw: String): String {
        val blocks = raw.split(Regex("\n{2,}"))
        val nonLatinBlocks = blocks.filter { block ->
            block.isNotBlank() && block.any { char ->
                val unicodeBlock = Character.UnicodeBlock.of(char)
                unicodeBlock != Character.UnicodeBlock.BASIC_LATIN &&
                unicodeBlock != Character.UnicodeBlock.LATIN_1_SUPPLEMENT &&
                unicodeBlock != Character.UnicodeBlock.LATIN_EXTENDED_A &&
                unicodeBlock != Character.UnicodeBlock.LATIN_EXTENDED_B &&
                unicodeBlock != Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL
            }
        }
        val content = if (nonLatinBlocks.isNotEmpty()) {
            nonLatinBlocks.joinToString("\n\n")
        } else {
            blocks.firstOrNull { it.isNotBlank() } ?: raw
        }
        return sanitizeText(content)
    }

    private fun sanitizeText(text: String): String {
        return text
            .replace(Regex("(।।|॥|\\|\\||[।|])\\s*[0-9०-९]+([.:-][0-9०-९]+)?\\s*(।।|॥|\\|\\||[।|])?"), "")
            .replace(Regex("\\[\\s*[0-9०-९]+([.:-][0-9०-९]+)?\\s*\\]"), "")
            .replace(Regex("\\(\\s*[0-9०-९]+([.:-][0-9०-९]+)?\\s*\\)"), "")
            .replace(Regex("\\b[0-9०-९]+[:.-][0-9०-९]+\\b"), "")
            .replace(Regex("(।।|॥|\\|\\||[।|]|\\s)+[0-9०-९]+$"), "")
            .replace(Regex("—.*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun seekTo(fraction: Float) {
        val current = _playbackState.value
        val passage = when (current) {
            is SacredAudioState.Playing -> current.passage
            is SacredAudioState.Paused -> current.passage
            is SacredAudioState.Idle -> return
        }
        val isOriginal = when (current) {
            is SacredAudioState.Playing -> current.isOriginalPart
            is SacredAudioState.Paused -> current.isOriginalPart
            is SacredAudioState.Idle -> false
        }
        val mode = when (current) {
            is SacredAudioState.Playing -> current.mode
            is SacredAudioState.Paused -> current.mode
            is SacredAudioState.Idle -> RecitationMode.TRANSLATION_ONLY
        }

        if (current is SacredAudioState.Playing && current.isNeural) {
            mediaPlayer?.let { mp ->
                val targetMs = (mp.duration * fraction.coerceIn(0f, 1f)).toInt()
                mp.seekTo(targetMs)
            }
            return
        }

        val totalLen = currentTextBeingSpoken.length
        val targetChar = (totalLen * fraction.coerceIn(0f, 1f)).toInt()
        val nearestWordBoundary = findNearestWordBoundary(currentTextBeingSpoken, targetChar)

        play(passage, mode, nearestWordBoundary)
    }

    private fun findNearestWordBoundary(text: String, charIndex: Int): Int {
        if (text.isEmpty() || charIndex <= 0) return 0
        if (charIndex >= text.length) return text.length
        var i = charIndex
        while (i > 0 && !text[i - 1].isWhitespace()) {
            i--
        }
        return i
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed
        mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
        val current = _playbackState.value
        if (current is SacredAudioState.Playing && !current.isNeural) {
            play(current.passage, current.mode, current.currentChar)
        } else if (current is SacredAudioState.Paused) {
            _playbackState.value = current.copy(speed = speed)
        }
    }

    fun setMode(mode: RecitationMode) {
        val current = _playbackState.value
        val passage = when (current) {
            is SacredAudioState.Playing -> current.passage
            is SacredAudioState.Paused -> current.passage
            is SacredAudioState.Idle -> return
        }
        play(passage, mode, 0)
    }

    fun pause() {
        val current = _playbackState.value
        if (current is SacredAudioState.Playing) {
            if (current.isNeural) {
                mediaPlayer?.pause()
                progressTrackingJob?.cancel()
            } else {
                tts?.stop()
            }
            _playbackState.value = SacredAudioState.Paused(
                passage = current.passage,
                isOriginalPart = current.isOriginalPart,
                mode = current.mode,
                progress = current.progress,
                currentChar = current.currentChar,
                speed = current.speed,
                isNeural = current.isNeural
            )
        }
    }

    fun resume() {
        val current = _playbackState.value
        if (current is SacredAudioState.Paused) {
            if (current.isNeural) {
                mediaPlayer?.start()
                startProgressTracker(current.passage, current.mode, current.isOriginalPart)
                _playbackState.value = SacredAudioState.Playing(
                    passage = current.passage,
                    isOriginalPart = current.isOriginalPart,
                    mode = current.mode,
                    progress = current.progress,
                    currentChar = current.currentChar,
                    speed = current.speed,
                    isNeural = true
                )
            } else {
                play(current.passage, current.mode, current.currentChar)
            }
        }
    }

    fun stop() {
        stopInternal()
        _playbackState.value = SacredAudioState.Idle
    }

    private fun stopInternal() {
        tts?.stop()
        stopMediaPlayer()
        progressTrackingJob?.cancel()
    }

    private fun stopMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun toggle(passage: Passage, mode: RecitationMode = RecitationMode.TRANSLATION_ONLY) {
        val current = _playbackState.value
        if (current is SacredAudioState.Playing && current.passage.id == passage.id) {
            pause()
        } else if (current is SacredAudioState.Paused && current.passage.id == passage.id) {
            resume()
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
