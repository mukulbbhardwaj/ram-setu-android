package com.vanarsena.ramsetu.data

import android.content.Context
import android.content.SharedPreferences

enum class HapticIntensity(val multiplier: Float) {
    OFF(0f),
    SUBTLE(0.4f),
    MEDIUM(0.8f),
    STRONG(1.0f)
}

data class InterruptedRun(
    val score: Int,
    val combo: Int,
    val maxCombo: Int,
    val tierLevel: Int
)

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

    var hasSeenTutorial: Boolean
        get() = prefs.getBoolean(KEY_SEEN_TUTORIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEN_TUTORIAL, value).apply()

    fun updateScore(newScore: Int): Boolean {
        totalStones += newScore
        if (newScore > highScore) {
            highScore = newScore
            return true
        }
        return false
    }

    fun unlockAchievement(id: String): Boolean {
        val current = HashSet(prefs.getStringSet(KEY_ACHIEVEMENTS, emptySet()) ?: emptySet())
        if (id in current) return false
        current.add(id)
        prefs.edit().putStringSet(KEY_ACHIEVEMENTS, current).apply()
        return true
    }

    fun unlockedAchievementIds(): Set<String> =
        prefs.getStringSet(KEY_ACHIEVEMENTS, emptySet()) ?: emptySet()

    fun saveInterruptedRun(score: Int, combo: Int, maxCombo: Int, tierLevel: Int) {
        prefs.edit()
            .putBoolean(KEY_INTERRUPTED, true)
            .putInt(KEY_INTERRUPTED_SCORE, score)
            .putInt(KEY_INTERRUPTED_COMBO, combo)
            .putInt(KEY_INTERRUPTED_MAX_COMBO, maxCombo)
            .putInt(KEY_INTERRUPTED_TIER, tierLevel)
            .apply()
    }

    fun interruptedRun(): InterruptedRun? {
        if (!prefs.getBoolean(KEY_INTERRUPTED, false)) return null
        return InterruptedRun(
            score = prefs.getInt(KEY_INTERRUPTED_SCORE, 0),
            combo = prefs.getInt(KEY_INTERRUPTED_COMBO, 0),
            maxCombo = prefs.getInt(KEY_INTERRUPTED_MAX_COMBO, 0),
            tierLevel = prefs.getInt(KEY_INTERRUPTED_TIER, 0)
        )
    }

    fun clearInterruptedRun() {
        prefs.edit()
            .putBoolean(KEY_INTERRUPTED, false)
            .remove(KEY_INTERRUPTED_SCORE)
            .remove(KEY_INTERRUPTED_COMBO)
            .remove(KEY_INTERRUPTED_MAX_COMBO)
            .remove(KEY_INTERRUPTED_TIER)
            .apply()
    }

    companion object {
        private const val KEY_HIGH_SCORE = "key_high_score"
        private const val KEY_TOTAL_STONES = "key_total_stones"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_MUSIC_ENABLED = "key_music_enabled"
        private const val KEY_HAPTIC_INTENSITY = "key_haptic_intensity"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_SEEN_TUTORIAL = "key_seen_tutorial"
        private const val KEY_ACHIEVEMENTS = "key_achievements"
        private const val KEY_INTERRUPTED = "key_interrupted"
        private const val KEY_INTERRUPTED_SCORE = "key_interrupted_score"
        private const val KEY_INTERRUPTED_COMBO = "key_interrupted_combo"
        private const val KEY_INTERRUPTED_MAX_COMBO = "key_interrupted_max_combo"
        private const val KEY_INTERRUPTED_TIER = "key_interrupted_tier"
    }
}
