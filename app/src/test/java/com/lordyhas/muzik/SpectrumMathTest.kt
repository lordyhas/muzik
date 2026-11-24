package com.lordyhas.muzik

import com.lordyhas.muzik.service.SpectrumMath
import com.lordyhas.muzik.service.SpectrumMath.BANDS
import com.lordyhas.muzik.service.SpectrumMath.FFT_SIZE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SpectrumMathTest {

    private val sampleRate = 44_100

    private fun levelsOf(signal: (Int) -> Float): FloatArray {
        val window = SpectrumMath.hannWindow(FFT_SIZE)
        val re = FloatArray(FFT_SIZE) { signal(it) * window[it] }
        val im = FloatArray(FFT_SIZE)
        SpectrumMath.fft(re, im)
        val out = FloatArray(BANDS)
        SpectrumMath.bandLevels(re, im, SpectrumMath.bandEdges(BANDS, sampleRate, FFT_SIZE), sampleRate, out)
        return out
    }

    private fun sine(hz: Double, amplitude: Float = 0.5f): (Int) -> Float =
        { i -> (amplitude * sin(2 * PI * hz * i / sampleRate)).toFloat() }

    private fun bandOf(hz: Double): Int {
        val edges = SpectrumMath.bandEdges(BANDS, sampleRate, FFT_SIZE)
        val bin = kotlin.math.round(hz * FFT_SIZE / sampleRate).toInt() // nearest bin
        return (0 until BANDS).first { bin >= edges[it] && bin < edges[it + 1] }
    }

    @Test
    fun fftFindsTheFrequencyOfASine() {
        val bin = 64
        val re = FloatArray(FFT_SIZE) { sin(2 * PI * bin * it / FFT_SIZE).toFloat() }
        val im = FloatArray(FFT_SIZE)
        SpectrumMath.fft(re, im)
        val magnitudes = (0 until FFT_SIZE / 2).map { re[it] * re[it] + im[it] * im[it] }
        assertEquals(bin, magnitudes.indices.maxBy { magnitudes[it] })
        // A unit sine peaks at n/2 without a window
        assertEquals(FFT_SIZE / 2f, kotlin.math.sqrt(magnitudes[bin]), 1f)
    }

    @Test
    fun silenceIsAllEmpty() {
        assertTrue(levelsOf { 0f }.all { it == 0f })
    }

    @Test
    fun aToneLightsItsOwnBandTheMost() {
        for (hz in listOf(100.0, 1_000.0, 6_000.0)) {
            val levels = levelsOf(sine(hz))
            val loudest = levels.indices.maxBy { levels[it] }
            assertEquals("tone at $hz Hz", bandOf(hz), loudest)
            assertTrue("tone at $hz Hz is visible", levels[loudest] > 0.5f)
        }
    }

    @Test
    fun louderMeansHigher() {
        val band = bandOf(1_000.0)
        val quiet = levelsOf(sine(1_000.0, amplitude = 0.02f))[band]
        val loud = levelsOf(sine(1_000.0, amplitude = 0.5f))[band]
        assertTrue(loud > quiet)
    }

    @Test
    fun bandEdgesAreIncreasingAndInRange() {
        for (rate in listOf(22_050, 44_100, 48_000, 96_000)) {
            val edges = SpectrumMath.bandEdges(BANDS, rate, FFT_SIZE)
            assertEquals(BANDS + 1, edges.size)
            for (b in 1..BANDS) assertTrue("rate $rate band $b", edges[b] > edges[b - 1])
            assertTrue(edges.first() >= 1 && edges.last() <= FFT_SIZE / 2)
        }
    }
}
