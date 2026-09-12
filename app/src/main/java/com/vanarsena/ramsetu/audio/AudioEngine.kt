package com.vanarsena.ramsetu.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import com.vanarsena.ramsetu.R
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null

    private var soundStartId = 0
    private var soundEndId = 0
    private var soundTapId = 0
    private var tapLoaded = false

    var onFocusLost: (() -> Unit)? = null

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
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0 && sampleId == soundTapId) tapLoaded = true
                }
                try {
                    soundStartId = load(context, R.raw.start, 1)
                    soundEndId = load(context, R.raw.end_bg, 1)
                    soundTapId = load(ensureTapWav().absolutePath, 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    fun playStartSound() {
        if (!isSoundEnabled) return
        soundPool?.play(soundStartId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun playStoneTap(comboCount: Int = 0) {
        if (!isSoundEnabled || !tapLoaded) return
        val pitch = (1.0f + (comboCount.coerceAtMost(20) * 0.03f)).coerceIn(0.5f, 2.0f)
        soundPool?.play(soundTapId, 0.9f, 0.9f, 1, 0, pitch)
    }

    fun playGameOverSound() {
        if (!isSoundEnabled) return
        soundPool?.play(soundEndId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun startMusic() {
        if (!isMusicEnabled) return
        if (!requestAudioFocus()) return
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
        if (!isMusicEnabled) return
        if (!requestAudioFocus()) return
        if (mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.setVolume(0.55f, 0.55f)
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
        abandonAudioFocus()
    }

    fun release() {
        stopMusic()
        soundPool?.release()
        soundPool = null
        tapLoaded = false
    }

    private fun requestAudioFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        pauseMusic()
                        onFocusLost?.invoke()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        mediaPlayer?.setVolume(0.15f, 0.15f)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        mediaPlayer?.setVolume(0.55f, 0.55f)
                    }
                }
            }
            .build()
        focusRequest = request
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun ensureTapWav(): File {
        val file = File(context.cacheDir, "stone_tap.wav")
        if (file.exists() && file.length() > 64L) return file

        val sampleRate = 22050
        val durationMs = 80
        val numSamples = sampleRate * durationMs / 1000
        val pcm = ByteArray(numSamples * 2)
        val baseFreq = 480.0
        val overtoneFreq = 960.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-35.0 * t)
            val sample = (sin(2.0 * Math.PI * baseFreq * t) * 0.7 +
                sin(2.0 * Math.PI * overtoneFreq * t) * 0.3) * envelope
            val value = (sample * Short.MAX_VALUE * 0.75).toInt().toShort()
            pcm[i * 2] = (value.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
        }

        FileOutputStream(file).use { out ->
            writeWavHeader(out, pcm.size, sampleRate)
            out.write(pcm)
        }
        return file
    }

    private fun writeWavHeader(out: FileOutputStream, dataBytes: Int, sampleRate: Int) {
        val total = 36 + dataBytes
        val byteRate = sampleRate * 2
        out.write("RIFF".toByteArray())
        out.write(intToBytes(total))
        out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray())
        out.write(intToBytes(16))
        out.write(shortToBytes(1))
        out.write(shortToBytes(1))
        out.write(intToBytes(sampleRate))
        out.write(intToBytes(byteRate))
        out.write(shortToBytes(2))
        out.write(shortToBytes(16))
        out.write("data".toByteArray())
        out.write(intToBytes(dataBytes))
    }

    private fun intToBytes(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToBytes(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte()
    )
}
