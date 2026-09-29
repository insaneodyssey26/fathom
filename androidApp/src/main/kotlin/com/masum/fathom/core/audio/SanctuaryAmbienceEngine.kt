package com.masum.fathom.core.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SoundscapeTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val assetFileName: String
) {
    TEMPLE_RAIN("rain", "Temple Rain", "Gentle drops over stone sanctuary", "temple_rain.ogg"),
    DISTANT_STORM("storm", "Distant Storm", "Deep rolling thunder & soft rainfall", "distant_storm.ogg"),
    SANCTUARY_NIGHT("night", "Sanctuary Night", "Evening crickets and tranquil breeze", "sanctuary_night.ogg"),
    SACRED_RIVER("river", "Sacred Brook", "Living waters flowing across pebbles", "sacred_river.ogg"),
    SINGING_BOWL("singing_bowl", "Tibetan Singing Bowl", "Resonant meditative bronze tone", "singing_bowl.ogg"),
    DEEP_OCEAN("ocean", "Deep Ocean Surf", "Calming ocean waves & brown noise", "deep_brown_noise.ogg")
}

@Immutable
data class AmbienceEngineState(
    val currentTrack: SoundscapeTrack? = null,
    val isPlaying: Boolean = false,
    val volume: Float = 0.5f,
    val sleepTimerMinutes: Int? = null,
    val sleepTimerSecondsRemaining: Long? = null
)

class SanctuaryAmbienceEngine(private val context: Context) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(AmbienceEngineState())
    val state: StateFlow<AmbienceEngineState> = _state.asStateFlow()

    fun selectTrack(track: SoundscapeTrack) {
        if (_state.value.currentTrack == track && _state.value.isPlaying) return
        playTrack(track)
    }

    private fun playTrack(track: SoundscapeTrack) {
        releasePlayer()
        try {
            val afd: AssetFileDescriptor = context.assets.openFd("ambience/${track.assetFileName}")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                val vol = _state.value.volume
                setVolume(vol, vol)
                prepare()
                start()
            }
            afd.close()
            _state.update { it.copy(currentTrack = track, isPlaying = true) }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.update { it.copy(isPlaying = false) }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player == null) {
            val track = _state.value.currentTrack ?: SoundscapeTrack.TEMPLE_RAIN
            playTrack(track)
            return
        }
        if (player.isPlaying) {
            player.pause()
            _state.update { it.copy(isPlaying = false) }
        } else {
            player.start()
            _state.update { it.copy(isPlaying = true) }
        }
    }

    fun stop() {
        cancelSleepTimer()
        releasePlayer()
        _state.update { it.copy(isPlaying = false, currentTrack = null) }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
        _state.update { it.copy(volume = clamped) }
    }

    fun setSleepTimer(minutes: Int?) {
        cancelSleepTimer()
        if (minutes == null || minutes <= 0) {
            _state.update { it.copy(sleepTimerMinutes = null, sleepTimerSecondsRemaining = null) }
            return
        }

        val totalSeconds = minutes * 60L
        _state.update {
            it.copy(
                sleepTimerMinutes = minutes,
                sleepTimerSecondsRemaining = totalSeconds
            )
        }

        timerJob = coroutineScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _state.update { it.copy(sleepTimerSecondsRemaining = remaining) }
                if (remaining in 1..60) {
                    val fadeVol = (_state.value.volume * (remaining.toFloat() / 60f)).coerceAtLeast(0.01f)
                    mediaPlayer?.setVolume(fadeVol, fadeVol)
                }
            }
            stop()
        }
    }

    fun cancelSleepTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(sleepTimerMinutes = null, sleepTimerSecondsRemaining = null) }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    fun release() {
        cancelSleepTimer()
        releasePlayer()
    }
}
