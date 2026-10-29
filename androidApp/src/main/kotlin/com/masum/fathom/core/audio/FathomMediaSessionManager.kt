package com.masum.fathom.core.audio

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import androidx.core.app.NotificationCompat
import com.masum.fathom.MainActivity
import com.masum.fathom.R
import com.masum.fathom.core.notification.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FathomMediaSessionManager(
    private val context: Context,
    private val audioEngine: SacredAudioEngine,
    private val ambienceEngine: SanctuaryAmbienceEngine
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaSession: MediaSession? = null
    private var observerJob: Job? = null
    private var receiverRegistered = false

    companion object {
        const val ACTION_PLAY = "com.masum.fathom.audio.ACTION_PLAY"
        const val ACTION_PAUSE = "com.masum.fathom.audio.ACTION_PAUSE"
        const val ACTION_STOP = "com.masum.fathom.audio.ACTION_STOP"
    }

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> handlePlay()
                ACTION_PAUSE -> handlePause()
                ACTION_STOP -> handleStop()
            }
        }
    }

    init {
        initMediaSession()
        registerReceiver()
        startObserving()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(context, "FathomSacredMedia").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    handlePlay()
                }

                override fun onPause() {
                    handlePause()
                }

                override fun onStop() {
                    handleStop()
                }

                override fun onSeekTo(pos: Long) {
                    handleSeekTo(pos)
                }
            })
            isActive = true
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_STOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(controlReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(controlReceiver, filter)
        }
        receiverRegistered = true
    }

    private fun startObserving() {
        observerJob?.cancel()
        observerJob = scope.launch {
            combine(audioEngine.playbackState, ambienceEngine.state) { sacred, ambience ->
                Pair(sacred, ambience)
            }.collect { (sacred, ambience) ->
                updatePlaybackAndNotification(sacred, ambience)
            }
        }
    }

    private fun handlePlay() {
        val sacred = audioEngine.playbackState.value
        val ambience = ambienceEngine.state.value
        if (sacred is SacredAudioState.Paused) {
            audioEngine.resume()
        } else if (!ambience.isPlaying && ambience.currentTrack != null) {
            ambienceEngine.togglePlayPause()
        }
    }

    private fun handlePause() {
        val sacred = audioEngine.playbackState.value
        val ambience = ambienceEngine.state.value
        if (sacred is SacredAudioState.Playing) {
            audioEngine.pause()
        } else if (ambience.isPlaying) {
            ambienceEngine.togglePlayPause()
        }
    }

    private fun handleStop() {
        audioEngine.stop()
        ambienceEngine.stop()
    }

    private fun handleSeekTo(posMs: Long) {
        val sacred = audioEngine.playbackState.value
        if (sacred is SacredAudioState.Playing || sacred is SacredAudioState.Paused) {
            val fraction = (posMs.toFloat() / 100_000f).coerceIn(0f, 1f)
            audioEngine.seekTo(fraction)
        }
    }

    private fun updatePlaybackAndNotification(sacred: SacredAudioState, ambience: AmbienceEngineState) {
        val session = mediaSession ?: return

        val isSacredActive = sacred is SacredAudioState.Playing || sacred is SacredAudioState.Paused
        val isAmbienceActive = ambience.isPlaying || (ambience.currentTrack != null && !ambience.isPlaying && sacred is SacredAudioState.Idle)

        if (!isSacredActive && (!isAmbienceActive || ambience.currentTrack == null)) {
            session.setPlaybackState(
                PlaybackState.Builder()
                    .setState(PlaybackState.STATE_STOPPED, 0, 0f)
                    .setActions(0)
                    .build()
            )
            notificationManager.cancel(NotificationChannels.AUDIO_PLAYBACK_ID)
            return
        }

        val isPlaying: Boolean
        val title: String
        val subtitle: String

        if (isSacredActive) {
            isPlaying = sacred is SacredAudioState.Playing
            val passage = when (sacred) {
                is SacredAudioState.Playing -> sacred.passage
                is SacredAudioState.Paused -> sacred.passage
            }
            title = passage.canonicalReference
            subtitle = passage.traditionId.displayName
        } else {
            isPlaying = ambience.isPlaying
            val track = ambience.currentTrack
            title = track?.title ?: "Sanctuary Ambience"
            subtitle = track?.subtitle ?: "Meditative Soundscape"
        }

        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val actions = if (isSacredActive) {
            PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP or PlaybackState.ACTION_SEEK_TO
        } else {
            PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP
        }

        val progressFraction = when (sacred) {
            is SacredAudioState.Playing -> sacred.progress
            is SacredAudioState.Paused -> sacred.progress
            else -> 0f
        }
        val durationMs = if (isSacredActive) 100_000L else -1L
        val currentPositionMs = if (isSacredActive) (progressFraction * durationMs).toLong() else 0L

        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(state, currentPositionMs, if (isPlaying) 1.0f else 0f)
                .setActions(actions)
                .build()
        )

        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, subtitle)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, "Fathom")

        if (isSacredActive) {
            metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)
        }

        session.setMetadata(metadataBuilder.build())

        showNotification(title, subtitle, isPlaying, session)
    }

    private fun showNotification(
        title: String,
        subtitle: String,
        isPlaying: Boolean,
        session: MediaSession
    ) {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            val pauseIntent = PendingIntent.getBroadcast(
                context,
                1,
                Intent(ACTION_PAUSE).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                R.drawable.ic_media_pause,
                "Pause",
                pauseIntent
            ).build()
        } else {
            val playIntent = PendingIntent.getBroadcast(
                context,
                2,
                Intent(ACTION_PLAY).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                R.drawable.ic_media_play,
                "Play",
                playIntent
            ).build()
        }

        val stopIntent = PendingIntent.getBroadcast(
            context,
            3,
            Intent(ACTION_STOP).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_media_stop,
            "Stop",
            stopIntent
        ).build()

        val builder = NotificationCompat.Builder(context, NotificationChannels.AUDIO_PLAYBACK)
            .setSmallIcon(R.drawable.ic_stat_fathom)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .addAction(playPauseAction)
            .addAction(stopAction)

        val frameworkSessionToken = session.sessionToken
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(android.support.v4.media.session.MediaSessionCompat.Token.fromToken(frameworkSessionToken))
                .setShowActionsInCompactView(0, 1)
        )

        notificationManager.notify(NotificationChannels.AUDIO_PLAYBACK_ID, builder.build())
    }

    fun release() {
        observerJob?.cancel()
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(controlReceiver)
            } catch (_: Exception) {}
            receiverRegistered = false
        }
        notificationManager.cancel(NotificationChannels.AUDIO_PLAYBACK_ID)
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }
}
