package com.lordyhas.muzik.service

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure math behind the spectrum visualizer: an FFT and the grouping of its bins into a few
 * log-spaced bands, each turned into a 0..1 level. Kept free of Android so it can be unit-tested.
 */
internal object SpectrumMath {

    /** Samples per analysis (~46 ms at 44.1 kHz): enough resolution for the low bands. */
    const val FFT_SIZE = 2048

    /** Bars drawn by the visualizer. */
    const val BANDS = 16

    private const val MIN_HZ = 40f
    private const val MAX_HZ = 16_000f

    /** dBFS shown as an empty bar and as a full one, after the tilt below. */
    private const val FLOOR_DB = -72f
    private const val CEIL_DB = -12f

    /** Music loses energy as pitch rises; lift each octave above 1 kHz so the highs still move. */
    private const val TILT_DB_PER_OCTAVE = 3f

    fun hannWindow(size: Int): FloatArray =
        FloatArray(size) { i -> (0.5 - 0.5 * cos(2.0 * PI * i / (size - 1))).toFloat() }

    /** In-place radix-2 FFT. [re] and [im] must have the same power-of-two size. */
    fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        require(n == im.size && n > 0 && n and (n - 1) == 0) { "FFT size must be a power of two" }

        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                re[i] = re[j].also { re[j] = re[i] }
                im[i] = im[j].also { im[j] = im[i] }
            }
        }

        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val wRe = cos(angle).toFloat()
            val wIm = sin(angle).toFloat()
            var start = 0
            while (start < n) {
                var curRe = 1f
                var curIm = 0f
                for (k in 0 until len / 2) {
                    val a = start + k
                    val b = a + len / 2
                    val tRe = re[b] * curRe - im[b] * curIm
                    val tIm = re[b] * curIm + im[b] * curRe
                    re[b] = re[a] - tRe
                    im[b] = im[a] - tIm
                    re[a] += tRe
                    im[a] += tIm
                    val nextRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = nextRe
                }
                start += len
            }
            len = len shl 1
        }
    }

    /**
     * FFT bin boundaries of [bands] log-spaced bands: band `b` covers bins `edges[b] until edges[b + 1]`.
     * Every band gets at least one bin, so the lowest bands don't share one.
     */
    fun bandEdges(bands: Int, sampleRate: Int, fftSize: Int): IntArray {
        val binHz = sampleRate.toFloat() / fftSize
        val maxBin = fftSize / 2
        val top = minOf(MAX_HZ, sampleRate / 2f)
        val edges = IntArray(bands + 1)
        for (b in 0..bands) {
            val hz = MIN_HZ * (top / MIN_HZ).pow(b / bands.toFloat())
            edges[b] = (hz / binHz).toInt().coerceIn(1, maxBin)
        }
        for (b in 1..bands) {
            if (edges[b] <= edges[b - 1]) edges[b] = minOf(edges[b - 1] + 1, maxBin)
        }
        return edges
    }

    /**
     * Turns an FFT result (of Hann-windowed samples in -1..1) into one 0..1 level per band:
     * the band's loudest bin in dBFS, tilted per octave, mapped from [FLOOR_DB]..[CEIL_DB].
     */
    fun bandLevels(re: FloatArray, im: FloatArray, edges: IntArray, sampleRate: Int, out: FloatArray) {
        val n = re.size
        // A full-scale sine peaks at n/4 once Hann-windowed (coherent gain 0.5)
        val fullScale = n / 4f
        val binHz = sampleRate.toFloat() / n
        for (b in 0 until edges.size - 1) {
            val from = edges[b]
            val until = maxOf(edges[b + 1], from + 1).coerceAtMost(n / 2)
            var peak = 0f
            for (k in from until until) {
                val m = re[k] * re[k] + im[k] * im[k]
                if (m > peak) peak = m
            }
            val magnitude = sqrt(peak) / fullScale
            if (magnitude <= 0f) {
                out[b] = 0f
                continue
            }
            val centerHz = ((from + until) / 2f) * binHz
            val tilt = TILT_DB_PER_OCTAVE * log2(centerHz / 1000f)
            val db = 20f * log10(magnitude) + tilt
            out[b] = ((db - FLOOR_DB) / (CEIL_DB - FLOOR_DB)).coerceIn(0f, 1f)
        }
    }
}
