package com.lordyhas.muzik.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.media.audiofx.Equalizer
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.lordyhas.muzik.MainActivity
import com.lordyhas.muzik.data.repository.LibraryRepository
import com.lordyhas.muzik.service.QueuePlayer.cleanUpAfterLoop
import com.lordyhas.muzik.service.QueuePlayer.isLoopTransition
import com.lordyhas.muzik.service.QueuePlayer.reorderUpcoming
import com.lordyhas.muzik.widget.NowPlayingWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Background playback. Also a MediaLibraryService so Android Auto / media browsers can browse the
 * library, and so the system can resume the last session (e.g. Bluetooth play) after process death.
 */
@UnstableApi
class MusicPlaybackService : MediaLibraryService() {

    private var mediaSession: MediaLibrarySession? = null
    private lateinit var libraryTree: MediaLibraryTree
    private var widgetArtJob: Job? = null
    private var widgetArtUri: Uri? = null
    private var widgetArt: Bitmap? = null
    private var exoPlayer: ExoPlayer? = null
    private var equalizer: Equalizer? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var sleepJob: Job? = null
    private lateinit var libraryRepository: LibraryRepository

    /** Song waiting to be counted as "played" once playback actually starts. */
    private var pendingPlayRecord: String? = null
    private var progressSaverJob: Job? = null
    private var lastIndex = 0
    private var lastCount = 0

    private val playerListener = object : Player.Listener {
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            attachEqualizer(audioSessionId)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Counted here (not in the UI) so plays are tracked even when the app is closed
            pendingPlayRecord = mediaItem?.mediaId
            if (exoPlayer?.playWhenReady == true) recordPendingPlay()

            // Handled here too, so the queue rules hold with the app closed or in Android Auto
            exoPlayer?.let { player ->
                if (player.isLoopTransition(lastIndex, lastCount, reason)) {
                    player.cleanUpAfterLoop(PlaybackSettings.shuffle.value)
                }
            }
            rememberPosition()
            saveProgress()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            rememberPosition()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            // Muzik shuffles the queue itself (QueueRules); a shuffle button outside the app
            // (Android Auto) turns on the player's own mode, so switch ours instead
            val player = exoPlayer ?: return
            if (!shuffleModeEnabled) return
            player.shuffleModeEnabled = false
            val enabled = !PlaybackSettings.shuffle.value
            PlaybackSettings.setShuffle(enabled)
            player.reorderUpcoming(enabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updateCustomLayout()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            updateWidget()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateWidget()
            if (isPlaying) SpectrumAnalyzer.onResume() else SpectrumAnalyzer.onPause()
            if (isPlaying) {
                recordPendingPlay()
                startProgressSaver()
            } else {
                progressSaverJob?.cancel()
                saveProgress()
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            // "End of track" sleep timer: ExoPlayer paused for us at the end of the item
            if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM &&
                PlaybackSettings.sleepTimer.value == SleepTimer.EndOfTrack
            ) {
                exoPlayer?.pauseAtEndOfMediaItems = false
                PlaybackSettings.onSleepTimerFinished()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        PlaybackSettings.init(this)
        libraryRepository = LibraryRepository(this)
        libraryTree = MediaLibraryTree(this)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // The spectrum visualizer reads the decoded audio from inside the chain (no permission needed)
        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink = DefaultAudioSink.Builder(context)
                .setEnableFloatOutput(enableFloatOutput)
                .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                .setAudioProcessors(arrayOf(SpectrumAudioProcessor()))
                .build()
        }

        val player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true) // true = handle audio focus automatically
            .setHandleAudioBecomingNoisy(true) // pause when headphones unplugged
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        exoPlayer = player
        player.addListener(playerListener)
        attachEqualizer(player.audioSessionId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaLibrarySession.Builder(this, NextAlwaysPlayer(player), LibraryCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        observeSettings()
        serviceScope.launch {
            PlaybackSettings.shuffle.collect { updateCustomLayout() }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    override fun onDestroy() {
        saveProgress()
        serviceScope.cancel()
        PlaybackSettings.cancelSleepTimer()
        releaseEqualizer()
        exoPlayer?.removeListener(playerListener)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }

    /** Where the player was, to recognize the album looping back to its start. */
    private fun rememberPosition() {
        val player = exoPlayer ?: return
        lastIndex = player.currentMediaItemIndex
        lastCount = player.mediaItemCount
    }

    /** Keeps the saved session fresh while playing with the UI closed. */
    private fun saveProgress() {
        val player = exoPlayer ?: return
        PlaybackStateStore.save(this, player)
    }

    private fun startProgressSaver() {
        progressSaverJob?.cancel()
        progressSaverJob = serviceScope.launch {
            while (true) {
                delay(15_000)
                saveProgress()
            }
        }
    }

    private fun recordPendingPlay() {
        val songId = pendingPlayRecord?.toLongOrNull() ?: return
        pendingPlayRecord = null
        serviceScope.launch(Dispatchers.IO) {
            try {
                libraryRepository.recordPlay(songId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ─── Notification buttons (shuffle, repeat) ─────────────────────────────

    /** Shuffle and repeat buttons shown next to previous/play/next in the media notification. */
    private fun updateCustomLayout() {
        val session = mediaSession ?: return
        val player = exoPlayer ?: return
        val shuffle = PlaybackSettings.shuffle.value
        val shuffleButton = CommandButton.Builder(
            if (shuffle) CommandButton.ICON_SHUFFLE_ON else CommandButton.ICON_SHUFFLE_OFF
        )
            .setDisplayName(if (shuffle) "Shuffle on" else "Shuffle off")
            .setSessionCommand(SHUFFLE_COMMAND)
            .build()
        val repeatButton = CommandButton.Builder(
            when (player.repeatMode) {
                Player.REPEAT_MODE_ALL -> CommandButton.ICON_REPEAT_ALL
                Player.REPEAT_MODE_ONE -> CommandButton.ICON_REPEAT_ONE
                else -> CommandButton.ICON_REPEAT_OFF
            }
        )
            .setDisplayName(
                when (player.repeatMode) {
                    Player.REPEAT_MODE_ALL -> "Repeat all"
                    Player.REPEAT_MODE_ONE -> "Repeat one"
                    else -> "Repeat off"
                }
            )
            .setSessionCommand(REPEAT_COMMAND)
            .build()
        session.setCustomLayout(ImmutableList.of(shuffleButton, repeatButton))
    }

    /** Same as the in-app shuffle button (MusicController.toggleShuffle). */
    private fun toggleShuffle() {
        val player = exoPlayer ?: return
        val enabled = !PlaybackSettings.shuffle.value
        PlaybackSettings.setShuffle(enabled)
        player.shuffleModeEnabled = false
        player.reorderUpcoming(enabled)
    }

    /** Same cycle as the in-app repeat button: off → all → one → off. */
    private fun cycleRepeat() {
        val player = exoPlayer ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        saveProgress()
    }

    // ─── Settings → player ──────────────────────────────────────────────────

    private fun observeSettings() {
        serviceScope.launch {
            PlaybackSettings.speed.collect { speed ->
                SpectrumAnalyzer.speed = speed
                exoPlayer?.setPlaybackSpeed(speed)
            }
        }
        serviceScope.launch {
            PlaybackSettings.equalizer.collect { applyEqualizer(it) }
        }
        serviceScope.launch {
            PlaybackSettings.sleepTimer.collect { applySleepTimer(it) }
        }
    }

    private fun applySleepTimer(timer: SleepTimer) {
        val player = exoPlayer ?: return
        sleepJob?.cancel()
        player.pauseAtEndOfMediaItems = false
        player.volume = 1f

        when (timer) {
            SleepTimer.Off -> Unit
            SleepTimer.EndOfTrack -> player.pauseAtEndOfMediaItems = true
            is SleepTimer.At -> sleepJob = serviceScope.launch {
                val remaining = timer.endsAtMillis - System.currentTimeMillis()
                if (remaining > FADE_OUT_MS) delay(remaining - FADE_OUT_MS)

                // Gentle fade-out over the last seconds instead of an abrupt stop
                val fadeLeft = (timer.endsAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                val steps = 20
                repeat(steps) { step ->
                    player.volume = 1f - (step + 1) / steps.toFloat()
                    delay(fadeLeft / steps)
                }
                player.pause()
                player.volume = 1f
                PlaybackSettings.onSleepTimerFinished()
            }
        }
    }

    // ─── Equalizer ──────────────────────────────────────────────────────────

    private fun attachEqualizer(audioSessionId: Int) {
        releaseEqualizer()
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET) return

        // Not every device exposes an equalizer; the feature simply stays hidden then
        val eq = try {
            Equalizer(0, audioSessionId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: return
        equalizer = eq

        try {
            val bands = eq.numberOfBands.toInt()
            val range = eq.bandLevelRange
            val presets = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
            val presetLevels = (0 until eq.numberOfPresets).map { preset ->
                eq.usePreset(preset.toShort())
                (0 until bands).map { eq.getBandLevel(it.toShort()).toInt() }
            }
            PlaybackSettings.publishEqualizerInfo(
                EqualizerInfo(
                    bandFrequenciesHz = (0 until bands).map { eq.getCenterFreq(it.toShort()) / 1000 },
                    minLevel = range[0].toInt(),
                    maxLevel = range[1].toInt(),
                    presets = presets,
                    presetLevels = presetLevels
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        applyEqualizer(PlaybackSettings.equalizer.value)
    }

    private fun applyEqualizer(settings: EqualizerSettings) {
        val eq = equalizer ?: return
        try {
            if (settings.enabled) {
                if (settings.preset in 0 until eq.numberOfPresets) {
                    eq.usePreset(settings.preset.toShort())
                } else {
                    settings.bandLevels.forEachIndexed { band, level ->
                        if (band < eq.numberOfBands) eq.setBandLevel(band.toShort(), level.toShort())
                    }
                }
            }
            eq.enabled = settings.enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseEqualizer() {
        equalizer?.release()
        equalizer = null
    }

    // ─── Media library (Android Auto, browsers, resumption) ─────────────────

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val result = super.onConnect(session, controller)
            val commands = result.availableSessionCommands.buildUpon()
                .add(SHUFFLE_COMMAND)
                .add(REPEAT_COMMAND)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(commands)
                .setAvailablePlayerCommands(result.availablePlayerCommands)
                .build()
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            updateCustomLayout()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_SHUFFLE -> toggleShuffle()
                ACTION_REPEAT -> cycleRepeat()
                else -> return super.onCustomCommand(session, controller, customCommand, args)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> =
            Futures.immediateFuture(LibraryResult.ofItem(libraryTree.root(), params))

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future {
            val all = libraryTree.children(parentId)
            val from = (page.toLong() * pageSize).coerceIn(0, all.size.toLong()).toInt()
            val to = (from.toLong() + pageSize).coerceAtMost(all.size.toLong()).toInt()
            LibraryResult.ofItemList(all.subList(from, to), params)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> = future {
            libraryTree.item(mediaId)?.let { LibraryResult.ofItem(it, null) }
                ?: LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> = future {
            libraryTree.resolve(mediaItems).toMutableList()
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = future {
            libraryTree.expand(mediaItems, startIndex, startPositionMs)
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = future {
            libraryTree.resumption() ?: throw UnsupportedOperationException("No saved session to resume")
        }
    }

    /** Runs [block] on the service scope and exposes it as the ListenableFuture Media3 expects. */
    private fun <T> future(block: suspend () -> T): ListenableFuture<T> {
        val result = SettableFuture.create<T>()
        serviceScope.launch {
            try {
                result.set(block())
            } catch (e: Exception) {
                result.setException(e)
            }
        }
        return result
    }

    // ─── Home-screen widget ─────────────────────────────────────────────────

    private fun updateWidget() {
        if (!NowPlayingWidget.hasWidgets(this)) return
        val player = exoPlayer ?: return
        val meta = player.mediaMetadata
        val title = meta.title?.toString()
        val artist = meta.artist?.toString()
        val artUri = meta.artworkUri

        NowPlayingWidget.update(this, title, artist, player.isPlaying, widgetArt.takeIf { widgetArtUri == artUri })

        if (artUri != widgetArtUri) {
            widgetArtJob?.cancel()
            widgetArtJob = serviceScope.launch {
                val bitmap = artUri?.let { loadArtwork(it) }
                widgetArtUri = artUri
                widgetArt = bitmap
                NowPlayingWidget.update(this@MusicPlaybackService, title, artist, exoPlayer?.isPlaying == true, bitmap)
            }
        }
    }

    private suspend fun loadArtwork(uri: Uri): Bitmap? = try {
        val request = ImageRequest.Builder(this)
            .data(uri)
            .size(256)
            .allowHardware(false)
            .build()
        (imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val FADE_OUT_MS = 10_000L
        const val ACTION_SHUFFLE = "com.lordyhas.muzik.SHUFFLE"
        const val ACTION_REPEAT = "com.lordyhas.muzik.REPEAT"
        val SHUFFLE_COMMAND = SessionCommand(ACTION_SHUFFLE, Bundle.EMPTY)
        val REPEAT_COMMAND = SessionCommand(ACTION_REPEAT, Bundle.EMPTY)
    }
}
