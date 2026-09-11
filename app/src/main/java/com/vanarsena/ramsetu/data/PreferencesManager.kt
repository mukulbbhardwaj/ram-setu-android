package com.vanarsena.ramsetu.data

import android.content.Context
import android.content.SharedPreferences

enum class HapticIntensity(val multiplier: Float) {
    OFF(0f),
    SUBTLE(0.4f),
    MEDIUM(0.8f),
    STRONG(1.0f)
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ram_setu_prefs", Context.MODE_PRIVATE)

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()

    var totalStones: Long
        get() = prefs.getLong(KEY_TOTAL_STONES, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_STONES, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()

    var hapticIntensity: HapticIntensity
        get() {
            val name = prefs.getString(KEY_HAPTIC_INTENSITY, HapticIntensity.MEDIUM.name)
            return try {
                HapticIntensity.valueOf(name ?: HapticIntensity.MEDIUM.name)
            } catch (e: Exception) {
                HapticIntensity.MEDIUM
            }
        }
        set(value) = prefs.edit().putString(KEY_HAPTIC_INTENSITY, value.name).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "hi") ?: "hi"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    fun updateScore(newScore: Int): Boolean {
        totalStones += newScore
        if (newScore > highScore) {
            highScore = newScore
            return true // New high score!
        }
        return false
    }

    companion object {
        private const val KEY_HIGH_SCORE = "key_high_score"
        private const val KEY_TOTAL_STONES = "key_total_stones"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_MUSIC_ENABLED = "key_music_enabled"
        private const val KEY_HAPTIC_INTENSITY = "key_haptic_intensity"
        private const val KEY_LANGUAGE = "key_language"
    }
}
