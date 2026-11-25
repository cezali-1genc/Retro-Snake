package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.MusicStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Multi-Track Procedural Arcade Music Player.
 * Supports 3 distinct loopable soundtracks (Retro Arcade, Electronic, Neon Cyber)
 * generated cleanly in PCM with zero copyright issues and independent volume/mute control.
 */
class RetroMusicPlayer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val lock = Any()

    private var track: AudioTrack? = null
    private val pcmCache = mutableMapOf<MusicStyle, ShortArray>()

    @Volatile
    private var currentStyle: MusicStyle = MusicStyle.RETRO_ARCADE

    @Volatile
    private var isEnabled: Boolean = true

    @Volatile
    private var volume: Float = 0.7f

    @Volatile
    private var shouldPlay: Boolean = false

    @Volatile
    private var isReleased: Boolean = false

    init {
        preloadStyle(MusicStyle.RETRO_ARCADE)
    }

    private fun preloadStyle(style: MusicStyle) {
        scope.launch {
            val generated = generateLoopForStyle(style)
            synchronized(lock) {
                if (isReleased) return@synchronized
                pcmCache[style] = generated
                if (style == currentStyle && shouldPlay && isEnabled && volume > 0.01f) {
                    ensureTrackAndPlayLocked()
                }
            }
        }
    }

    fun updateSettings(enabled: Boolean, newVolume: Float, style: MusicStyle = currentStyle) {
        val clampedVol = newVolume.coerceIn(0f, 1f)
        val styleChanged: Boolean
        synchronized(lock) {
            styleChanged = style != currentStyle
            currentStyle = style
            isEnabled = enabled
            volume = clampedVol
            if (isReleased) return

            if (styleChanged) {
                releaseTrackOnlyLocked()
            }

            if (!isEnabled || volume <= 0.01f) {
                pauseTrackLocked(track)
            } else {
                if (!pcmCache.containsKey(style)) {
                    preloadStyle(style)
                } else {
                    applyVolumeLocked(track)
                    if (shouldPlay) {
                        ensureTrackAndPlayLocked()
                    }
                }
            }
        }
    }

    fun start() {
        synchronized(lock) {
            shouldPlay = true
            if (isReleased || !isEnabled || volume <= 0.01f) return
            if (!pcmCache.containsKey(currentStyle)) {
                preloadStyle(currentStyle)
            } else {
                ensureTrackAndPlayLocked()
            }
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
            }
        }
    }

    fun release() {
        synchronized(lock) {
            isReleased = true
            shouldPlay = false
            releaseTrackOnlyLocked()
            pcmCache.clear()
        }
    }

    private fun releaseTrackOnlyLocked() {
        try {
            track?.stop()
            track?.release()
        } catch (_: Exception) {
        }
        track = null
    }

    private fun ensureTrackAndPlayLocked() {
        val data = pcmCache[currentStyle] ?: return
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
        }
    }

    private fun pauseTrackLocked(activeTrack: AudioTrack?) {
        if (activeTrack == null) return
        try {
            if (activeTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                activeTrack.pause()
            }
        } catch (_: Exception) {
        }
    }

    private fun applyVolumeLocked(activeTrack: AudioTrack?) {
        if (activeTrack == null) return
        try {
            val musicGain = (volume * 0.40f).coerceIn(0f, 1f)
            activeTrack.setVolume(musicGain)
        } catch (_: Exception) {
        }
    }

    private fun generateLoopForStyle(style: MusicStyle): ShortArray {
        val steps = 64
        val stepDurationSec = when (style) {
            MusicStyle.RETRO_ARCADE -> 0.135
            MusicStyle.ELECTRONIC -> 0.118
            MusicStyle.NEON_CYBER -> 0.125
        }
        val samplesPerStep = (SAMPLE_RATE * stepDurationSec).toInt()
        val totalSamples = steps * samplesPerStep
        val buffer = ShortArray(totalSamples)

        val leadNotes = when (style) {
            MusicStyle.RETRO_ARCADE -> doubleArrayOf(
                440.00, 0.0, 523.25, 0.0, 659.25, 0.0, 880.00, 0.0,
                783.99, 0.0, 659.25, 0.0, 523.25, 587.33, 659.25, 0.0,
                698.46, 0.0, 659.25, 0.0, 587.33, 0.0, 523.25, 0.0,
                587.33, 0.0, 659.25, 0.0, 783.99, 0.0, 659.25, 0.0,
                440.00, 0.0, 523.25, 0.0, 659.25, 0.0, 880.00, 0.0,
                987.77, 0.0, 880.00, 0.0, 783.99, 0.0, 659.25, 0.0,
                698.46, 0.0, 587.33, 0.0, 659.25, 0.0, 523.25, 0.0,
                587.33, 0.0, 493.88, 0.0, 440.00, 0.0, 392.00, 0.0
            )
            MusicStyle.ELECTRONIC -> doubleArrayOf(
                523.25, 659.25, 783.99, 659.25, 1046.50, 0.0, 783.99, 659.25,
                587.33, 698.46, 880.00, 698.46, 987.77, 0.0, 880.00, 783.99,
                523.25, 659.25, 783.99, 1046.50, 987.77, 0.0, 783.99, 0.0,
                698.46, 587.33, 523.25, 587.33, 659.25, 783.99, 880.00, 0.0,
                523.25, 659.25, 783.99, 659.25, 1046.50, 0.0, 880.00, 783.99,
                698.46, 880.00, 1046.50, 880.00, 987.77, 783.99, 659.25, 0.0,
                587.33, 698.46, 880.00, 987.77, 1046.50, 0.0, 880.00, 783.99,
                659.25, 587.33, 523.25, 493.88, 440.00, 523.25, 659.25, 0.0
            )
            MusicStyle.NEON_CYBER -> doubleArrayOf(
                329.63, 0.0, 493.88, 0.0, 659.25, 587.33, 493.88, 0.0,
                392.00, 0.0, 587.33, 0.0, 783.99, 659.25, 587.33, 0.0,
                440.00, 0.0, 659.25, 0.0, 880.00, 783.99, 659.25, 0.0,
                392.00, 0.0, 493.88, 587.33, 659.25, 0.0, 493.88, 0.0,
                329.63, 0.0, 493.88, 659.25, 783.99, 0.0, 659.25, 0.0,
                440.00, 0.0, 523.25, 659.25, 880.00, 0.0, 783.99, 0.0,
                349.23, 0.0, 440.00, 587.33, 698.46, 0.0, 587.33, 0.0,
                392.00, 0.0, 493.88, 0.0, 587.33, 0.0, 493.88, 0.0
            )
        }

        val bassNotes = doubleArrayOf(
            110.00, 220.00, 110.00, 220.00, 110.00, 220.00, 110.00, 220.00,
            110.00, 220.00, 110.00, 220.00, 130.81, 261.63, 146.83, 293.66,
            87.31, 174.61, 87.31, 174.61, 87.31, 174.61, 87.31, 174.61,
            98.00, 196.00, 98.00, 196.00, 98.00, 196.00, 98.00, 196.00,
            110.00, 220.00, 110.00, 220.00, 130.81, 261.63, 130.81, 261.63,
            164.81, 329.63, 164.81, 329.63, 146.83, 293.66, 130.81, 261.63,
            146.83, 293.66, 146.83, 293.66, 98.00, 196.00, 98.00, 196.00,
            82.41, 164.81, 82.41, 164.81, 110.00, 220.00, 98.00, 196.00
        )

        var leadPhase = 0.0
        var bassPhase = 0.0
        var noiseSeed = 1337 + style.ordinal * 91

        for (step in 0 until steps) {
            val leadFreq = leadNotes[step]
            val bassFreq = bassNotes[step]
            val stepStart = step * samplesPerStep

            val isKick = step % 4 == 0
            val isSnare = step % 8 == 4
            val isHat = step % 2 == 1

            for (i in 0 until samplesPerStep) {
                val t = i.toFloat() / samplesPerStep
                val attack = (i / (SAMPLE_RATE * 0.008f)).coerceIn(0f, 1f)
                val release = ((samplesPerStep - i) / (SAMPLE_RATE * 0.014f)).coerceIn(0f, 1f)
                val env = minOf(attack, release)

                var leadSample = 0f
                if (leadFreq > 0.0) {
                    leadPhase = (leadPhase + leadFreq / SAMPLE_RATE) % 1.0
                    val wave = when (style) {
                        MusicStyle.RETRO_ARCADE -> if (leadPhase < 0.25) 1f else -1f
                        MusicStyle.ELECTRONIC -> (leadPhase.toFloat() * 2f - 1f) // sawtooth lead
                        MusicStyle.NEON_CYBER -> sin(2.0 * PI * leadPhase).toFloat() * 0.75f + (if (leadPhase < 0.5) 0.25f else -0.25f)
                    }
                    leadSample = wave * 0.30f * env * (1f - t * 0.32f)
                }

                var bassSample = 0f
                if (bassFreq > 0.0) {
                    bassPhase = (bassPhase + bassFreq / SAMPLE_RATE) % 1.0
                    val tri = (1.0 - kotlin.math.abs(bassPhase * 2.0 - 1.0) * 2.0).toFloat()
                    val sq = if (bassPhase < 0.5) 1f else -1f
                    bassSample = (tri * 0.6f + sq * 0.4f) * 0.34f * env * (1f - t * 0.25f)
                }

                var drumSample = 0f
                if (isKick && t < 0.45f) {
                    val kickFreq = 140.0 * (1.0 - t * 1.6)
                    val kickEnv = (1f - t / 0.45f)
                    drumSample += sin(2.0 * PI * kickFreq * (i.toDouble() / SAMPLE_RATE)).toFloat() * 0.22f * kickEnv
                }
                if (isSnare && t < 0.35f) {
                    noiseSeed = (noiseSeed * 1103515245 + 12345) and 0x7fffffff
                    val white = ((noiseSeed / 1073741824f) - 1f)
                    drumSample += white * 0.15f * (1f - t / 0.35f)
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
