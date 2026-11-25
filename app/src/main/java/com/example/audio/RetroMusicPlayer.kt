package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural 8-Bit Retro Chiptune Background Music Player.
 * Generates a seamless 4-bar arcade chiptune loop (lead pulse, octave bass, and 8-bit percussion)
 * in pure PCM with zero external audio files, integrating directly with user volume & mute controls.
 */
class RetroMusicPlayer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val lock = Any()

    private var track: AudioTrack? = null
    private var pcmData: ShortArray? = null

    @Volatile
    private var isEnabled: Boolean = true

    @Volatile
    private var volume: Float = 0.8f

    @Volatile
    private var shouldPlay: Boolean = false

    @Volatile
    private var isReleased: Boolean = false

    init {
        // Pre-render the chiptune loop asynchronously so game start is instantaneous
        scope.launch {
            val generated = generateChiptuneLoop()
            synchronized(lock) {
                if (isReleased) return@synchronized
                pcmData = generated
                if (shouldPlay && isEnabled && volume > 0.01f) {
                    ensureTrackAndPlayLocked()
                }
            }
        }
    }

    fun updateSettings(enabled: Boolean, newVolume: Float) {
        val clampedVol = newVolume.coerceIn(0f, 1f)
        synchronized(lock) {
            isEnabled = enabled
            volume = clampedVol
            if (isReleased) return
            val activeTrack = track
            if (!isEnabled || volume <= 0.01f) {
                pauseTrackLocked(activeTrack)
            } else {
                applyVolumeLocked(activeTrack)
                if (shouldPlay) {
                    ensureTrackAndPlayLocked()
                }
            }
        }
    }

    fun start() {
        synchronized(lock) {
            shouldPlay = true
            if (isReleased || !isEnabled || volume <= 0.01f) return
            ensureTrackAndPlayLocked()
        }
    }

    fun pause() {
        synchronized(lock) {
            shouldPlay = false
            pauseTrackLocked(track)
        }
    }

    fun resetToStart() {
        synchronized(lock) {
            shouldPlay = false
            val activeTrack = track ?: return
            try {
                if (activeTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    activeTrack.pause()
                }
                activeTrack.setPlaybackHeadPosition(0)
            } catch (_: Exception) {
                // Safe fallback
            }
        }
    }

    fun release() {
        synchronized(lock) {
            isReleased = true
            shouldPlay = false
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
                // Safe fallback
            }
            track = null
            pcmData = null
        }
    }

    private fun ensureTrackAndPlayLocked() {
        val data = pcmData ?: return
        try {
            var activeTrack = track
            if (activeTrack == null || activeTrack.state != AudioTrack.STATE_INITIALIZED) {
                activeTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(data.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (activeTrack.state != AudioTrack.STATE_INITIALIZED) {
                    activeTrack.release()
                    return
                }
                activeTrack.write(data, 0, data.size)
                activeTrack.setLoopPoints(0, data.size, -1)
                track = activeTrack
            }
            applyVolumeLocked(activeTrack)
            if (activeTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                activeTrack.play()
            }
        } catch (_: Exception) {
            // Safe fallback on environments without audio hardware (e.g. unit tests)
        }
    }

    private fun pauseTrackLocked(activeTrack: AudioTrack?) {
        if (activeTrack == null) return
        try {
            if (activeTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                activeTrack.pause()
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun applyVolumeLocked(activeTrack: AudioTrack?) {
        if (activeTrack == null) return
        try {
            // Keep background music balanced cleanly beneath foreground SFX
            val musicGain = (volume * 0.36f).coerceIn(0f, 1f)
            activeTrack.setVolume(musicGain)
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun generateChiptuneLoop(): ShortArray {
        val steps = 64
        val stepDurationSec = 0.135 // ~111 BPM 16th-note arcade groove (8.64s seamless loop)
        val samplesPerStep = (SAMPLE_RATE * stepDurationSec).toInt()
        val totalSamples = steps * samplesPerStep
        val buffer = ShortArray(totalSamples)

        // 4-bar upbeat retro minor-pentatonic chiptune lead (Hz, 0.0 = rest)
        val leadNotes = doubleArrayOf(
            // Bar 1 (Am)
            440.00, 0.0, 523.25, 0.0, 659.25, 0.0, 880.00, 0.0,
            783.99, 0.0, 659.25, 0.0, 523.25, 587.33, 659.25, 0.0,
            // Bar 2 (F -> G)
            698.46, 0.0, 659.25, 0.0, 587.33, 0.0, 523.25, 0.0,
            587.33, 0.0, 659.25, 0.0, 783.99, 0.0, 659.25, 0.0,
            // Bar 3 (Am)
            440.00, 0.0, 523.25, 0.0, 659.25, 0.0, 880.00, 0.0,
            987.77, 0.0, 880.00, 0.0, 783.99, 0.0, 659.25, 0.0,
            // Bar 4 (Dm -> E -> Am)
            698.46, 0.0, 587.33, 0.0, 659.25, 0.0, 523.25, 0.0,
            587.33, 0.0, 493.88, 0.0, 440.00, 0.0, 392.00, 0.0
        )

        // Driving 8-bit octave bassline (Hz)
        val bassNotes = doubleArrayOf(
            // Bar 1 (A2 / A3)
            110.00, 220.00, 110.00, 220.00, 110.00, 220.00, 110.00, 220.00,
            110.00, 220.00, 110.00, 220.00, 130.81, 261.63, 146.83, 293.66,
            // Bar 2 (F2 / G2)
            87.31, 174.61, 87.31, 174.61, 87.31, 174.61, 87.31, 174.61,
            98.00, 196.00, 98.00, 196.00, 98.00, 196.00, 98.00, 196.00,
            // Bar 3 (A2 / C3 / E3)
            110.00, 220.00, 110.00, 220.00, 130.81, 261.63, 130.81, 261.63,
            164.81, 329.63, 164.81, 329.63, 146.83, 293.66, 130.81, 261.63,
            // Bar 4 (D2 -> G2 -> E2 -> A2)
            146.83, 293.66, 146.83, 293.66, 98.00, 196.00, 98.00, 196.00,
            82.41, 164.81, 82.41, 164.81, 110.00, 220.00, 98.00, 196.00
        )

        var leadPhase = 0.0
        var bassPhase = 0.0
        var noiseSeed = 1337

        for (step in 0 until steps) {
            val leadFreq = leadNotes[step]
            val bassFreq = bassNotes[step]
            val stepStart = step * samplesPerStep

            // Percussion pattern per 16th-note step
            val isKick = step % 4 == 0
            val isSnare = step % 8 == 4
            val isHat = step % 2 == 1

            for (i in 0 until samplesPerStep) {
                val t = i.toFloat() / samplesPerStep
                val attack = (i / (SAMPLE_RATE * 0.008f)).coerceIn(0f, 1f)
                val release = ((samplesPerStep - i) / (SAMPLE_RATE * 0.014f)).coerceIn(0f, 1f)
                val env = minOf(attack, release)

                // Lead: 25% pulse wave with subtle decay
                var leadSample = 0f
                if (leadFreq > 0.0) {
                    leadPhase = (leadPhase + leadFreq / SAMPLE_RATE) % 1.0
                    val pulse = if (leadPhase < 0.25) 1f else -1f
                    val noteDecay = (1f - t * 0.35f)
                    leadSample = pulse * 0.32f * env * noteDecay
                }

                // Bass: punchy 50% square/triangle hybrid
                var bassSample = 0f
                if (bassFreq > 0.0) {
                    bassPhase = (bassPhase + bassFreq / SAMPLE_RATE) % 1.0
                    val tri = (1.0 - kotlin.math.abs(bassPhase * 2.0 - 1.0) * 2.0).toFloat()
                    val sq = if (bassPhase < 0.5) 1f else -1f
                    bassSample = (tri * 0.6f + sq * 0.4f) * 0.34f * env * (1f - t * 0.25f)
                }

                // 8-bit Percussion
                var drumSample = 0f
                if (isKick && t < 0.45f) {
                    val kickFreq = 140.0 * (1.0 - t * 1.6)
                    val kickEnv = (1f - t / 0.45f)
                    drumSample += sin(2.0 * PI * kickFreq * (i.toDouble() / SAMPLE_RATE)).toFloat() * 0.22f * kickEnv
                }
                if (isSnare && t < 0.35f) {
                    noiseSeed = (noiseSeed * 1103515245 + 12345) and 0x7fffffff
                    val white = ((noiseSeed / 1073741824f) - 1f)
                    drumSample += white * 0.16f * (1f - t / 0.35f)
                } else if (isHat && t < 0.14f) {
                    noiseSeed = (noiseSeed * 1103515245 + 12345) and 0x7fffffff
                    val white = ((noiseSeed / 1073741824f) - 1f)
                    drumSample += white * 0.06f * (1f - t / 0.14f)
                }

                val mixed = (leadSample + bassSample + drumSample).coerceIn(-0.92f, 0.92f)
                buffer[stepStart + i] = (mixed * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }

    companion object {
        private const val SAMPLE_RATE = 22050
    }
}
