package com.vanarsena.ramsetu.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.vanarsena.ramsetu.data.HapticIntensity

class HapticManager(context: Context) {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var intensity: HapticIntensity = HapticIntensity.MEDIUM

    private fun device(): Vibrator? {
        if (intensity == HapticIntensity.OFF) return null
        val device = vibrator ?: return null
        if (!device.hasVibrator()) return null
        val systemEnabled = Settings.System.getInt(
            appContext.contentResolver,
            Settings.System.HAPTIC_FEEDBACK_ENABLED,
            1
        ) != 0
        return if (systemEnabled) device else null
    }

    fun playStoneTap() {
        val device = device() ?: return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale.coerceIn(0.1f, 1f))
                device.vibrate(composition.compose())
                return
            } catch (e: Exception) {
                // Fall back to waveform
            }
        }

        val duration = (22 * scale).toLong().coerceAtLeast(8L)
        val amplitude = (255 * scale).toInt().coerceIn(1, 255)
        if (device.hasAmplitudeControl()) {
            device.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            device.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun playComboMilestone() {
        val device = device() ?: return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.7f * scale)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f * scale)
                device.vibrate(composition.compose())
                return
            } catch (e: Exception) {
                // Fall back
            }
        }

        val timings = longArrayOf(0, 15, 30, 25)
        val amplitudes = intArrayOf(
            0,
            (120 * scale).toInt().coerceIn(1, 255),
            0,
            (255 * scale).toInt().coerceIn(1, 255)
        )
        if (device.hasAmplitudeControl()) {
            device.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            device.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun playSpeedLevelUp() {
        val device = device() ?: return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f * scale)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.5f * scale, 60)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f * scale, 40)
                device.vibrate(composition.compose())
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
        if (device.hasAmplitudeControl()) {
            device.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            device.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun playGameOver() {
        val device = device() ?: return
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

        if (device.hasAmplitudeControl()) {
            device.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            device.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun playMiss() {
        val device = device() ?: return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                device.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                return
            } catch (e: Exception) {
                // Fall back
            }
        }

        val duration = (28 * scale).toLong().coerceAtLeast(12L)
        val amplitude = (220 * scale).toInt().coerceIn(1, 255)
        if (device.hasAmplitudeControl()) {
            device.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            device.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun playButtonTap() {
        val device = device() ?: return
        val scale = intensity.multiplier

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                device.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                return
            } catch (e: Exception) {
                // Fall back
            }
        }

        val duration = (12 * scale).toLong().coerceAtLeast(5L)
        val amplitude = (150 * scale).toInt().coerceIn(1, 255)
        if (device.hasAmplitudeControl()) {
            device.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            device.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
