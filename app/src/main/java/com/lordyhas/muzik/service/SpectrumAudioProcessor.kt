package com.lordyhas.muzik.service

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.service.SpectrumMath.BANDS
import com.lordyhas.muzik.service.SpectrumMath.FFT_SIZE
import java.nio.ByteBuffer

/**
 * Sits in ExoPlayer's audio chain and passes the audio through untouched. While the visualizer is
 * on screen ([SpectrumAnalyzer.enabled]) it also mixes it to mono and runs an FFT about 50 times a
 * second, publishing the band levels to [SpectrumAnalyzer]. Needs no permission, unlike the
 * platform Visualizer, since it only reads the app's own decoded audio.
 */
@UnstableApi
class SpectrumAudioProcessor : BaseAudioProcessor() {

    private val window = SpectrumMath.hannWindow(FFT_SIZE)
    private val samples = FloatArray(FFT_SIZE) // last FFT_SIZE mono samples, circular
    private val re = FloatArray(FFT_SIZE)
    private val im = FloatArray(FFT_SIZE)
    private val levels = FloatArray(BANDS)
    private var edges = IntArray(BANDS + 1)
    private var writePos = 0
    private var sinceAnalysis = 0
    private var hop = FFT_SIZE / 2

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        val supported = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT ||
            inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        // Anything else (or a passthrough stream) skips this processor altogether
        if (!supported || inputAudioFormat.channelCount <= 0 || inputAudioFormat.sampleRate <= 0) {
            return AudioFormat.NOT_SET
        }
        edges = SpectrumMath.bandEdges(BANDS, inputAudioFormat.sampleRate, FFT_SIZE)
        hop = (inputAudioFormat.sampleRate / ANALYSES_PER_SECOND).coerceAtLeast(256)
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size == 0) return
        val format = inputAudioFormat
        val frames = size / format.bytesPerFrame
        val startNs = SpectrumAnalyzer.advance(frames, format.sampleRate)
        if (SpectrumAnalyzer.enabled) analyze(inputBuffer, frames, startNs, format)
        replaceOutputBuffer(size).put(inputBuffer).flip()
    }

    private fun analyze(buffer: ByteBuffer, frames: Int, startNs: Long, format: AudioFormat) {
        val channels = format.channelCount
        val isFloat = format.encoding == C.ENCODING_PCM_FLOAT
        val sampleBytes = if (isFloat) 4 else 2
        val nanosPerFrame = SpectrumAnalyzer.nanosPerFrame(format.sampleRate)
        var index = buffer.position()
        for (frame in 0 until frames) {
            var sum = 0f
            for (c in 0 until channels) {
                sum += if (isFloat) buffer.getFloat(index) else buffer.getShort(index) / 32768f
                index += sampleBytes
            }
            samples[writePos] = sum / channels
            writePos = (writePos + 1) and (FFT_SIZE - 1)
            if (++sinceAnalysis >= hop) {
                sinceAnalysis = 0
                runFft(startNs + ((frame + 1) * nanosPerFrame).toLong(), format.sampleRate)
            }
        }
    }

    private fun runFft(timeNs: Long, sampleRate: Int) {
        for (i in 0 until FFT_SIZE) {
            re[i] = samples[(writePos + i) and (FFT_SIZE - 1)] * window[i]
            im[i] = 0f
        }
        SpectrumMath.fft(re, im)
        SpectrumMath.bandLevels(re, im, edges, sampleRate, levels)
        SpectrumAnalyzer.publish(timeNs, levels)
    }

    override fun onFlush() {
        samples.fill(0f)
        writePos = 0
        sinceAnalysis = 0
        SpectrumAnalyzer.onFlush()
    }

    override fun onReset() {
        onFlush()
    }

    private companion object {
        const val ANALYSES_PER_SECOND = 50
    }
}
