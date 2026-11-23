package com.lordyhas.muzik.service

import com.lordyhas.muzik.service.SpectrumMath.BANDS

/**
 * Hands the spectrum computed on the playback thread ([SpectrumAudioProcessor]) to the visualizer
 * on Now Playing. Shared in-process, like [PlaybackSettings].
 *
 * Audio is analyzed before it goes into the AudioTrack buffer, so ahead of what is heard. Each
 * analysis is stamped with the time it will actually play: an audio clock that advances by the
 * duration of every buffer processed and never falls behind the wall clock. Once the AudioTrack
 * buffer is full, processing only moves as fast as playback frees it, so the clock stays that far
 * ahead of "now", which is exactly the buffer's latency.
 */
object SpectrumAnalyzer {

    /** True while the visualizer is on screen: analysis is skipped otherwise. */
    @Volatile
    var enabled = false

    /** Playback speed: processed audio plays faster or slower than it is decoded. */
    @Volatile
    var speed = 1f

    /** The device's own output delay after the AudioTrack (speaker, DAC). */
    private const val OUTPUT_LATENCY_NS = 40_000_000L

    /** An analysis older than this means nothing is playing: the bars fall back. */
    private const val STALE_NS = 150_000_000L

    private const val HISTORY = 64

    private val lock = Any()
    private val times = LongArray(HISTORY)
    private val history = Array(HISTORY) { FloatArray(BANDS) }
    private var head = 0
    private var count = 0
    private var clockNs = 0L
    private var pausedAtNs = 0L

    /** Moves the audio clock past [frames] frames; returns when the first one will play. */
    internal fun advance(frames: Int, sampleRate: Int): Long = synchronized(lock) {
        val start = maxOf(System.nanoTime(), clockNs)
        clockNs = start + (frames * nanosPerFrame(sampleRate)).toLong()
        start
    }

    internal fun nanosPerFrame(sampleRate: Int): Double = 1e9 / sampleRate / speed.coerceAtLeast(0.1f)

    internal fun publish(timeNs: Long, levels: FloatArray) = synchronized(lock) {
        head = (head + 1) % HISTORY
        times[head] = timeNs
        levels.copyInto(history[head])
        if (count < HISTORY) count++
    }

    /** Seek or new stream: the AudioTrack buffer was emptied, so was what it held. */
    internal fun onFlush() = synchronized(lock) {
        clockNs = 0L
        count = 0
    }

    /** The AudioTrack keeps its buffer while paused; that audio plays first on resume. */
    fun onPause() = synchronized(lock) {
        if (pausedAtNs == 0L) pausedAtNs = System.nanoTime()
    }

    fun onResume() = synchronized(lock) {
        val pausedAt = pausedAtNs
        if (pausedAt == 0L) return@synchronized
        pausedAtNs = 0L
        val delta = System.nanoTime() - pausedAt
        // What was still in the buffer at the pause is heard only now
        for (i in 0 until count) {
            val slot = (head - i + HISTORY) % HISTORY
            if (times[slot] > pausedAt) times[slot] += delta
        }
        if (clockNs > pausedAt) clockNs += delta
    }

    /**
     * Copies into [out] the levels being heard at [nowNs] ([System.nanoTime]). Returns false when
     * nothing is playing, so the visualizer lets its bars fall.
     */
    fun levelsAt(nowNs: Long, out: FloatArray): Boolean = synchronized(lock) {
        val target = nowNs - OUTPUT_LATENCY_NS
        for (i in 0 until count) {
            val slot = (head - i + HISTORY) % HISTORY
            val time = times[slot]
            if (time <= target) {
                if (target - time > STALE_NS) return@synchronized false
                history[slot].copyInto(out)
                return@synchronized true
            }
        }
        false
    }
}
