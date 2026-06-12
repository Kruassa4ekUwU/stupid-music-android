package com.stupidmusic.app.data.network

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages multiple Invidious instances with automatic fallback.
 * If the current instance fails, switches to the next one.
 */
@Singleton
class InstanceManager @Inject constructor() {

    companion object {
        // Publicly available Invidious instances — ordered by reliability
        val INSTANCES = listOf(
            "https://inv.nadeko.net/",
            "https://invidious.privacydev.net/",
            "https://yewtu.be/",
            "https://invidious.lunar.icu/",
            "https://vid.puffyan.us/",
            "https://inv.tux.pizza/",
            "https://invidious.nerdvpn.de/",
            "https://yt.cdaut.de/"
        )
    }

    private var currentIndex = 0

    val currentInstance: String
        get() = INSTANCES[currentIndex]

    fun nextInstance(): String {
        currentIndex = (currentIndex + 1) % INSTANCES.size
        Log.d("InstanceManager", "Switched to instance: ${INSTANCES[currentIndex]}")
        return INSTANCES[currentIndex]
    }

    fun resetToFirst() {
        currentIndex = 0
    }
}
