package com.vanarsena.ramsetu.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.vanarsena.ramsetu.data.HapticIntensity

class HapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var intensity: HapticIntensity = HapticIntensity.MEDIUM

    /**
     * Crisp, heavy mechanical feedback when a stone is tapped and locked into the bridge.
     */
    fun playStoneTap() {
        if (intensity == HapticIntensity.OFF || vibrator == null || !vibrator.hasVibrator()) return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale.coerceIn(0.1f, 1f))
                vibrator.vibrate(composition.compose())
                return
            } catch (e: Exception) {
                // Fall back to waveform
            }
        }

        // Fallback for API 26-29
        val duration = (22 * scale).toLong().coerceAtLeast(8L)
        val amplitude = (255 * scale).toInt().coerceIn(1, 255)
        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * Ascending tactile burst on reaching combo streaks (10, 25, 50, etc.).
     */
    fun playComboMilestone() {
        if (intensity == HapticIntensity.OFF || vibrator == null || !vibrator.hasVibrator()) return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.7f * scale)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f * scale)
                vibrator.vibrate(composition.compose())
                return
            } catch (e: Exception) {
                // Fall back
            }
        }

        // Fallback waveform
        val timings = longArrayOf(0, 15, 30, 25)
        val amplitudes = intArrayOf(
            0,
            (120 * scale).toInt().coerceIn(1, 255),
            0,
            (255 * scale).toInt().coerceIn(1, 255)
        )
        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * Distinct dual-pulse pattern alerting player that the falling speed has accelerated.
     */
    fun playSpeedLevelUp() {
        if (intensity == HapticIntensity.OFF || vibrator == null || !vibrator.hasVibrator()) return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f * scale)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.5f * scale, 60)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f * scale, 40)
                vibrator.vibrate(composition.compose())
                return
            } catch (e: Exception) {
                // Fall back
            }
        }

        val timings = longArrayOf(0, 30, 50, 40)
        val amplitudes = intArrayOf(
            0,
            (200 * scale).toInt().coerceIn(1, 255),
            0,
            (255 * scale).toInt().coerceIn(1, 255)
        )
        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * Low-frequency decaying rumble simulating a stone sinking into ocean depths (Game Over).
     */
    fun playGameOver() {
        if (intensity == HapticIntensity.OFF || vibrator == null || !vibrator.hasVibrator()) return
        val scale = intensity.multiplier

        val timings = longArrayOf(0, 60, 40, 70, 50, 90, 60, 110)
        val amplitudes = intArrayOf(
            0,
            (240 * scale).toInt().coerceIn(1, 255),
            0,
            (180 * scale).toInt().coerceIn(1, 255),
            0,
            (120 * scale).toInt().coerceIn(1, 255),
            0,
            (60 * scale).toInt().coerceIn(1, 255)
        )

        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * Subtle tactile tick for UI buttons, tabs, and toggles.
     */
    fun playButtonTap() {
        if (intensity == HapticIntensity.OFF || vibrator == null || !vibrator.hasVibrator()) return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                return
            } catch (e: Exception) {
                // Fall back
            }
        }

        val duration = (12 * scale).toLong().coerceAtLeast(5L)
        val amplitude = (150 * scale).toInt().coerceIn(1, 255)
        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
