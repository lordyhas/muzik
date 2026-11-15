package com.lordyhas.muzik.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts a vivid accent color from album artwork, tuned to read well on the dark theme.
 * Results are cached per URI so scrolling and recomposition stay cheap.
 */
object ArtworkColorExtractor {

    private val cache = LruCache<String, Int>(64)

    /** Forgets every cached color, after covers changed inside the files (Tag editor). */
    fun clear() = cache.evictAll()

    private const val NO_COLOR = 0

    suspend fun extract(context: Context, uri: Uri?): Color? {
        if (uri == null) return null
        val key = uri.toString()
        cache.get(key)?.let { return if (it == NO_COLOR) null else Color(it) }

        val argb = withContext(Dispatchers.Default) {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(64)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            val bitmap = (result as? SuccessResult)?.drawable?.toBitmap(64, 64)
            bitmap?.let { dominantVividColor(it) } ?: NO_COLOR
        }
        cache.put(key, argb)
        return if (argb == NO_COLOR) null else Color(argb)
    }

    /**
     * Buckets pixels by hue, weighting saturated/bright pixels higher, then returns the
     * average of the strongest bucket normalized to a pleasant brightness range.
     */
    private fun dominantVividColor(bitmap: Bitmap): Int {
        val bins = 12
        val weights = FloatArray(bins)
        val sumH = FloatArray(bins)
        val sumS = FloatArray(bins)
        val sumV = FloatArray(bins)
        val hsv = FloatArray(3)

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            if ((pixel ushr 24) < 128) continue
            android.graphics.Color.colorToHSV(pixel, hsv)
            val s = hsv[1]
            val v = hsv[2]
            if (v < 0.15f || s < 0.12f) continue
            val weight = s * s * v
            val bin = ((hsv[0] / 360f) * bins).toInt().coerceIn(0, bins - 1)
            weights[bin] += weight
            sumH[bin] += hsv[0] * weight
            sumS[bin] += s * weight
            sumV[bin] += v * weight
        }

        val best = weights.indices.maxByOrNull { weights[it] } ?: return NO_COLOR
        val w = weights[best]
        if (w <= 0f) return NO_COLOR

        val out = floatArrayOf(
            sumH[best] / w,
            (sumS[best] / w).coerceIn(0.45f, 0.85f),
            (sumV[best] / w).coerceIn(0.70f, 0.95f)
        )
        return android.graphics.Color.HSVToColor(out).let { if (it == NO_COLOR) 1 else it }
    }
}

/**
 * Animated accent color derived from [uri]'s artwork, falling back to [fallback]
 * when there's no artwork or it's too desaturated.
 */
@Composable
fun rememberArtworkColor(uri: Uri?, fallback: Color = AccentPrimary): Color {
    val context = LocalContext.current
    var target by remember { mutableStateOf(fallback) }

    LaunchedEffect(uri) {
        target = ArtworkColorExtractor.extract(context, uri) ?: fallback
    }

    val animated by animateColorAsState(
        targetValue = target,
        animationSpec = tween(600),
        label = "artwork_color"
    )
    return animated
}
