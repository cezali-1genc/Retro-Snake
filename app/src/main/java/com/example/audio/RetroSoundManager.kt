package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural 8-bit Retro Audio & Haptics Engine.
 * Generates classic NES/GameBoy square-wave chirps, arpeggios, portal warps,
 * level fanfares, and noise effects in real-time with zero external audio assets.
 */
class RetroSoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isSoundEnabled: Boolean = true
    var soundVolume: Float = 0.8f
    var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playEat(combo: Int = 1) {
        vibrateOneShot(durationMs = 26L, amplitude = 150)
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        val pitchBoost = ((combo - 1).coerceIn(0, 4)) * 75.0
        scope.launch {
            val sampleRate = 22050
            val tone1 = generateSquareWave(sampleRate, frequency = 520.0 + pitchBoost, durationMs = 40, volume = 0.5f * soundVolume)
            val tone2 = generateSquareWave(sampleRate, frequency = 780.0 + pitchBoost * 1.2, durationMs = 56, volume = 0.6f * soundVolume)
            playPcm(tone1 + tone2, sampleRate)
        }
    }

    fun playBonus() {
        vibrateWaveform(longArrayOf(0L, 24L, 36L, 36L), intArrayOf(0, 170, 0, 220))
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val t1 = generateSquareWave(sampleRate, frequency = 587.0, durationMs = 40, volume = 0.6f * soundVolume)
            val t2 = generateSquareWave(sampleRate, frequency = 740.0, durationMs = 40, volume = 0.65f * soundVolume)
            val t3 = generateSquareWave(sampleRate, frequency = 880.0, durationMs = 80, volume = 0.7f * soundVolume)
            playPcm(t1 + t2 + t3, sampleRate)
        }
    }

    fun playFever() {
        vibrateWaveform(longArrayOf(0L, 25L, 30L, 35L, 30L, 45L), intArrayOf(0, 160, 0, 200, 0, 255))
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val t1 = generateSquareWave(sampleRate, frequency = 523.25, durationMs = 38, volume = 0.6f * soundVolume)
            val t2 = generateSquareWave(sampleRate, frequency = 659.25, durationMs = 38, volume = 0.65f * soundVolume)
            val t3 = generateSquareWave(sampleRate, frequency = 783.99, durationMs = 38, volume = 0.7f * soundVolume)
            val t4 = generateSquareWave(sampleRate, frequency = 1046.50, durationMs = 95, volume = 0.75f * soundVolume)
            playPcm(t1 + t2 + t3 + t4, sampleRate)
        }
    }

    fun playPortal() {
        vibrateOneShot(durationMs = 32L, amplitude = 175)
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val t1 = generateSquareWave(sampleRate, frequency = 392.0, durationMs = 35, volume = 0.5f * soundVolume)
            val t2 = generateSquareWave(sampleRate, frequency = 659.25, durationMs = 35, volume = 0.55f * soundVolume)
            val t3 = generateSquareWave(sampleRate, frequency = 987.77, durationMs = 55, volume = 0.6f * soundVolume)
            playPcm(t1 + t2 + t3, sampleRate)
        }
    }

    fun playShieldBreak() {
        vibrateWaveform(longArrayOf(0L, 40L, 25L, 50L), intArrayOf(0, 210, 0, 180))
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val t1 = generateSquareWave(sampleRate, frequency = 440.0, durationMs = 55, volume = 0.65f * soundVolume)
            val t2 = generateSquareWave(sampleRate, frequency = 311.13, durationMs = 85, volume = 0.55f * soundVolume)
            playPcm(t1 + t2, sampleRate)
        }
    }

    fun playLevelComplete() {
        vibrateWaveform(longArrayOf(0L, 30L, 35L, 45L, 35L, 70L), intArrayOf(0, 180, 0, 210, 0, 255))
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val t1 = generateSquareWave(sampleRate, frequency = 523.25, durationMs = 55, volume = 0.6f * soundVolume)
            val t2 = generateSquareWave(sampleRate, frequency = 659.25, durationMs = 55, volume = 0.65f * soundVolume)
            val t3 = generateSquareWave(sampleRate, frequency = 783.99, durationMs = 55, volume = 0.7f * soundVolume)
            val t4 = generateSquareWave(sampleRate, frequency = 1046.50, durationMs = 130, volume = 0.75f * soundVolume)
            playPcm(t1 + t2 + t3 + t4, sampleRate)
        }
    }

    fun playWin() = playLevelComplete()

    fun playWallCross() {
        vibrateOneShot(durationMs = 16L, amplitude = 100)
    }

    fun playCrash() {
        vibrateWaveform(longArrayOf(0L, 65L, 35L, 110L), intArrayOf(0, 240, 0, 255))
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val totalDurationMs = 350
            val totalSamples = (sampleRate * totalDurationMs) / 1000
            val buffer = ShortArray(totalSamples)

            var phase = 0.0
            for (i in 0 until totalSamples) {
                val progress = i.toDouble() / totalSamples
                val freq = 280.0 * (1.0 - progress * 0.8)
                val decay = (1.0 - progress).toFloat()

                val noise = if (Math.random() > 0.5) 0.3f else -0.3f
                phase += (2 * Math.PI * freq) / sampleRate
                val square = if (sin(phase) >= 0) 0.7f else -0.7f
                val sample = ((square * 0.7f + noise * 0.3f) * decay * Short.MAX_VALUE * 0.7f * soundVolume).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer, sampleRate)
        }
    }

    fun playMoveTick() {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val tick = generateSquareWave(sampleRate, frequency = 1150.0, durationMs = 10, volume = 0.12f * soundVolume)
            playPcm(tick, sampleRate)
        }
    }

    fun playButtonClick() {
        vibrateOneShot(durationMs = 16L, amplitude = 90)
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        scope.launch {
            val sampleRate = 22050
            val click = generateSquareWave(sampleRate, frequency = 880.0, durationMs = 24, volume = 0.38f * soundVolume)
            playPcm(click, sampleRate)
        }
    }

    private fun generateSquareWave(
        sampleRate: Int,
        frequency: Double,
        durationMs: Int,
        volume: Float
    ): ShortArray {
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val period = sampleRate / frequency
        val maxAmp = (Short.MAX_VALUE * volume).toInt()

        for (i in 0 until numSamples) {
            val sampleInPeriod = i % period
            val value = if (sampleInPeriod < period / 2) maxAmp else -maxAmp
            val envelope = if (i > numSamples - 100) (numSamples - i) / 100f else 1.0f
            buffer[i] = (value * envelope).toInt().toShort()
        }
        return buffer
    }

    private fun playPcm(pcmData: ShortArray, sampleRate: Int) {
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(pcmData.size * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track.play()

            val durationMs = (pcmData.size * 1000L) / sampleRate + 50L
            Thread.sleep(durationMs)
            track.stop()
            track.release()
        } catch (_: Exception) {
        }
    }

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        if (!isVibrationEnabled) return
        try {
            vibrator?.let { v ->
                if (!v.hasVibrator()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val safeAmp = if (v.hasAmplitudeControl()) amplitude.coerceIn(1, 255) else VibrationEffect.DEFAULT_AMPLITUDE
                    v.vibrate(VibrationEffect.createOneShot(durationMs, safeAmp))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        if (!isVibrationEnabled) return
        try {
            vibrator?.let { v ->
                if (!v.hasVibrator()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (v.hasAmplitudeControl()) {
                        v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        v.vibrate(VibrationEffect.createWaveform(timings, -1))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(timings, -1)
                }
            }
        } catch (_: Exception) {
        }
    }
}
