package com.lordyhas.muzik.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.lordyhas.muzik.data.model.Pin
import com.lordyhas.muzik.data.model.decodePins
import com.lordyhas.muzik.data.model.encodePins
import com.lordyhas.muzik.data.model.toggled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Albums and playlists pinned to Home, newest first, persisted in `muzik_prefs`. */
object PinStore {

    private const val PREFS = "muzik_prefs"
    private const val KEY_PINS = "home_pins"

    private var prefs: SharedPreferences? = null

    private val _pins = MutableStateFlow<List<Pin>>(emptyList())
    val pins: StateFlow<List<Pin>> = _pins.asStateFlow()

    @Synchronized
    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _pins.value = decodePins(p.getString(KEY_PINS, null))
    }

    /** Pins or unpins; returns true when it is now pinned. */
    fun toggle(pin: Pin): Boolean {
        val next = _pins.value.toggled(pin)
        save(next)
        return pin in next
    }

    fun remove(pin: Pin) {
        if (pin in _pins.value) save(_pins.value - pin)
    }

    private fun save(next: List<Pin>) {
        _pins.value = next
        prefs?.edit { putString(KEY_PINS, next.encodePins()) }
    }
}
