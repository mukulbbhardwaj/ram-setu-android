package com.vanarsena.ramsetu.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import com.vanarsena.ramsetu.R
import kotlin.math.exp
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null

    private var soundStartId = 0
    private var soundEndId = 0
    private var soundTapId = 0

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                resumeMusic()
            } else {
                pauseMusic()
            }
        }

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                try {
                    soundStartId = load(context, R.raw.start, 1)
                    soundEndId = load(context, R.raw.end_bg, 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    /**
     * Play instant conch shell blast / game start sound.
     */
    fun playStartSound() {
        if (!isSoundEnabled) return
        soundPool?.play(soundStartId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    /**
     * Play crisp stone placement splash / tap sound.
     * Uses a generated temple bell / resonant stone click so latency is sub-10ms.
     */
    fun playStoneTap(comboCount: Int = 0) {
        if (!isSoundEnabled) return

        // Pitch up slightly with combo streaks for satisfying musical progression
        val pitchMultiplier = (1.0f + (comboCount.coerceAtMost(20) * 0.03f))

        // Play short synthesized resonant stone drop/temple bell click
        playSynthesizedStoneClick(pitchMultiplier)
    }

    /**
     * Play game over sound when stone sinks into ocean.
     */
    fun playGameOverSound() {
        if (!isSoundEnabled) return
        soundPool?.play(soundEndId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    /**
     * Synthesize an organic resonant stone placement tone with exponential decay.
     */
    private fun playSynthesizedStoneClick(pitchFactor: Float = 1.0f) {
        Thread {
            try {
                val sampleRate = 22050
                val durationMs = 80
                val numSamples = (sampleRate * durationMs / 1000)
                val buffer = ShortArray(numSamples)

                val baseFreq = 480.0 * pitchFactor // Rich resonant bell/stone tone
                val overtoneFreq = 960.0 * pitchFactor

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Fast attack, natural exponential damping decay
                    val envelope = exp(-35.0 * t)
                    val sample = (sin(2.0 * Math.PI * baseFreq * t) * 0.7 +
                                 sin(2.0 * Math.PI * overtoneFreq * t) * 0.3) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE * 0.75).toInt().toShort()
                }

                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )
                track.write(buffer, 0, buffer.size)
                track.play()
                Thread.sleep(durationMs.toLong() + 20)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Ignore background sound synthesis failure
            }
        }.start()
    }

    /**
     * Start background devotional music.
     */
    fun startMusic() {
        if (!isMusicEnabled) return
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.bg_music)?.apply {
                    isLooping = true
                    setVolume(0.55f, 0.55f)
                }
            }
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resumeMusic() {
        if (isMusicEnabled && mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stopMusic()
        soundPool?.release()
        soundPool = null
    }
}
