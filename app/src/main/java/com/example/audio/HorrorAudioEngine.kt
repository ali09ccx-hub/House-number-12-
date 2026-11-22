package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SurfaceType {
    WOOD, TILE, ASPHALT
}

class HorrorAudioEngine {
    private val sampleRate = 22050
    private val running = AtomicBoolean(false)
    private var audioThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    var masterVolume: Float = 0.85f

    @Volatile
    var tensionLevel: Float = 0.05f

    @Volatile
    var isMuted: Boolean = false

    @Volatile
    var isIndoors: Boolean = false

    private data class ActiveSound(
        val samples: FloatArray,
        var index: Int = 0,
        val gain: Float = 1.0f
    )

    private val activeSounds = CopyOnWriteArrayList<ActiveSound>()

    // Pre-synthesized realistic sound buffers
    private val woodStepBuf = synthFootstep(SurfaceType.WOOD)
    private val tileStepBuf = synthFootstep(SurfaceType.TILE)
    private val asphaltStepBuf = synthFootstep(SurfaceType.ASPHALT)
    private val doorKnockBuf = synthDoorKnock()
    private val doorCreakBuf = synthDoorCreak()
    private val lockClickBuf = synthLockClick()
    private val mopScrubBuf = synthMopScrub()
    private val phoneChimeBuf = synthPhoneChime()
    private val uiClickBuf = synthUiClick()
    private val cashRegisterBuf = synthCashRegister()
    private val breakerClackBuf = synthBreakerClack()
    private val heartbeatBuf = synthHeartbeat()
    private val stingerBuf = synthTensionStinger()
    private val sirenBuf = synthPoliceSiren()
    private val rattleBuf = synthHandleRattle()

    fun start() {
        if (running.getAndSet(true)) return
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

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
            .setBufferSizeInBytes(minBuf * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        audioThread = Thread({
            val chunk = ShortArray(512)
            var windFilter = 0f
            var dronePhase1 = 0.0
            var dronePhase2 = 0.0
            var lfoPhase = 0.0
            val rng = Random(42)

            while (running.get()) {
                val vol = if (isMuted) 0f else masterVolume
                val tension = tensionLevel.coerceIn(0f, 1f)
                val windAmp = if (isIndoors) 0.018f else 0.055f

                for (i in chunk.indices) {
                    // Filtered autumn night wind noise
                    val white = rng.nextFloat() * 2f - 1f
                    windFilter = windFilter * 0.96f + white * 0.04f

                    // Subtle psychological sub-bass drone that deepens with tension
                    lfoPhase += 2.0 * PI * 0.18 / sampleRate
                    val mod = 1.0 + 0.02 * sin(lfoPhase)
                    dronePhase1 += 2.0 * PI * (54.0 * mod) / sampleRate
                    dronePhase2 += 2.0 * PI * 81.3 / sampleRate
                    val drone = (sin(dronePhase1).toFloat() * 0.6f + sin(dronePhase2).toFloat() * 0.4f) *
                        (0.015f + tension * 0.095f)

                    var mix = windFilter * windAmp + drone

                    // Mix active one-shot sound effects
                    val iterator = activeSounds.iterator()
                    while (iterator.hasNext()) {
                        val s = iterator.next()
                        if (s.index < s.samples.size) {
                            mix += s.samples[s.index] * s.gain
                            s.index++
                        } else {
                            activeSounds.remove(s)
                        }
                    }

                    val clamped = (mix * vol).coerceIn(-0.95f, 0.95f)
                    chunk[i] = (clamped * 32767f).toInt().toShort()
                }
                track.write(chunk, 0, chunk.size)
            }
        }, "HorrorAudioThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running.set(false)
        try {
            audioThread?.join(300)
        } catch (_: InterruptedException) {
        }
        audioThread = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: IllegalStateException) {
        }
        audioTrack = null
    }

    fun playFootstep(surface: SurfaceType) {
        val buf = when (surface) {
            SurfaceType.WOOD -> woodStepBuf
            SurfaceType.TILE -> tileStepBuf
            SurfaceType.ASPHALT -> asphaltStepBuf
        }
        trigger(buf, 0.45f)
    }

    fun playDoorKnock() = trigger(doorKnockBuf, 0.92f)
    fun playDoorCreak() = trigger(doorCreakBuf, 0.65f)
    fun playDoorLock() = trigger(lockClickBuf, 0.78f)
    fun playMopScrub() = trigger(mopScrubBuf, 0.68f)
    fun playPhoneChime() = trigger(phoneChimeBuf, 0.72f)
    fun playUiClick() = trigger(uiClickBuf, 0.35f)
    fun playCashRegister() = trigger(cashRegisterBuf, 0.65f)
    fun playBreakerClack() = trigger(breakerClackBuf, 0.85f)
    fun playHeartbeat() = trigger(heartbeatBuf, 0.78f)
    fun playTensionStinger() = trigger(stingerBuf, 0.68f)
    fun playPoliceSiren() = trigger(sirenBuf, 0.62f)
    fun playHandleRattle() = trigger(rattleBuf, 0.80f)

    private fun trigger(samples: FloatArray, gain: Float) {
        if (!running.get() || isMuted) return
        if (activeSounds.size > 12) activeSounds.removeAt(0)
        activeSounds.add(ActiveSound(samples, 0, gain))
    }

    private fun synthFootstep(surface: SurfaceType): FloatArray {
        val len = (sampleRate * 0.13).toInt()
        val out = FloatArray(len)
        val rng = Random(surface.ordinal + 7)
        val baseFreq = when (surface) {
            SurfaceType.WOOD -> 115.0
            SurfaceType.TILE -> 240.0
            SurfaceType.ASPHALT -> 165.0
        }
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 32.0).toFloat()
            val thud = sin(2.0 * PI * baseFreq * exp(-t * 8.0) * t).toFloat()
            val grit = (rng.nextFloat() * 2f - 1f) * if (surface == SurfaceType.ASPHALT) 0.55f else 0.25f
            out[i] = (thud * 0.65f + grit * 0.35f) * env
        }
        return out
    }

    private fun synthDoorKnock(): FloatArray {
        val len = (sampleRate * 1.15).toInt()
        val out = FloatArray(len)
        val knockTimes = doubleArrayOf(0.04, 0.32, 0.58)
        for (kt in knockTimes) {
            val start = (kt * sampleRate).toInt()
            val dur = (0.14 * sampleRate).toInt()
            for (j in 0 until dur) {
                val idx = start + j
                if (idx >= len) break
                val t = j.toDouble() / sampleRate
                val env = exp(-t * 36.0).toFloat()
                val wood = sin(2.0 * PI * 145.0 * t).toFloat() * 0.7f +
                    sin(2.0 * PI * 310.0 * t).toFloat() * 0.3f
                out[idx] += wood * env * 0.85f
            }
        }
        return out
    }

    private fun synthDoorCreak(): FloatArray {
        val len = (sampleRate * 0.42).toInt()
        val out = FloatArray(len)
        var phase = 0.0
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = sin(PI * (t / 0.42)).toFloat()
            val freq = 270.0 + 110.0 * sin(t * 22.0)
            phase += 2.0 * PI * freq / sampleRate
            out[i] = (sin(phase) * 0.4 + sin(phase * 2.1) * 0.25).toFloat() * env * 0.6f
        }
        return out
    }

    private fun synthLockClick(): FloatArray {
        val len = (sampleRate * 0.16).toInt()
        val out = FloatArray(len)
        val rng = Random(91)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 48.0).toFloat()
            val metal = sin(2.0 * PI * 980.0 * t).toFloat() * 0.6f + (rng.nextFloat() * 2f - 1f) * 0.4f
            out[i] = metal * env * 0.7f
        }
        return out
    }

    private fun synthMopScrub(): FloatArray {
        val len = (sampleRate * 0.48).toInt()
        val out = FloatArray(len)
        val rng = Random(15)
        var lp = 0f
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = sin(PI * (t / 0.48)).toFloat() * (0.6f + 0.4f * sin(t * 28.0).toFloat())
            val noise = rng.nextFloat() * 2f - 1f
            lp = lp * 0.72f + noise * 0.28f
            out[i] = lp * env * 0.75f
        }
        return out
    }

    private fun synthPhoneChime(): FloatArray {
        val len = (sampleRate * 0.52).toInt()
        val out = FloatArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val freq = if (t < 0.16) 880.0 else 1174.66
            val localT = if (t < 0.16) t else t - 0.16
            val env = exp(-localT * 9.0).toFloat()
            out[i] = sin(2.0 * PI * freq * localT).toFloat() * env * 0.5f
        }
        return out
    }

    private fun synthUiClick(): FloatArray {
        val len = (sampleRate * 0.045).toInt()
        val out = FloatArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            out[i] = sin(2.0 * PI * 720.0 * t).toFloat() * exp(-t * 75.0).toFloat() * 0.4f
        }
        return out
    }

    private fun synthCashRegister(): FloatArray {
        val len = (sampleRate * 0.55).toInt()
        val out = FloatArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 7.5).toFloat()
            val bell = sin(2.0 * PI * 1568.0 * t).toFloat() * 0.55f +
                sin(2.0 * PI * 2349.0 * t).toFloat() * 0.35f
            out[i] = bell * env * 0.5f
        }
        return out
    }

    private fun synthBreakerClack(): FloatArray {
        val len = (sampleRate * 0.28).toInt()
        val out = FloatArray(len)
        val rng = Random(303)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 25.0).toFloat()
            val thump = sin(2.0 * PI * 160.0 * t).toFloat() * 0.6f +
                sin(2.0 * PI * 640.0 * t).toFloat() * 0.25f +
                (rng.nextFloat() * 2f - 1f) * 0.25f
            out[i] = thump * env * 0.85f
        }
        return out
    }

    private fun synthHeartbeat(): FloatArray {
        val len = (sampleRate * 0.72).toInt()
        val out = FloatArray(len)
        for (beat in doubleArrayOf(0.02, 0.24)) {
            val s = (beat * sampleRate).toInt()
            val dur = (0.16 * sampleRate).toInt()
            for (j in 0 until dur) {
                val idx = s + j
                if (idx >= len) break
                val t = j.toDouble() / sampleRate
                val env = exp(-t * 24.0).toFloat()
                out[idx] += sin(2.0 * PI * 58.0 * t).toFloat() * env * 0.85f
            }
        }
        return out
    }

    private fun synthTensionStinger(): FloatArray {
        val len = (sampleRate * 1.4).toInt()
        val out = FloatArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val env = (t / 0.12).coerceAtMost(1.0).toFloat() * exp(-t * 2.1).toFloat()
            val s = sin(2.0 * PI * 110.0 * t) * 0.45 +
                sin(2.0 * PI * 116.5 * t) * 0.45 +
                sin(2.0 * PI * 233.0 * t) * 0.2
            out[i] = s.toFloat() * env * 0.7f
        }
        return out
    }

    private fun synthPoliceSiren(): FloatArray {
        val len = (sampleRate * 2.8).toInt()
        val out = FloatArray(len)
        var phase = 0.0
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val fadeIn = (t / 1.2).coerceAtMost(1.0).toFloat()
            val freq = 620.0 + 260.0 * sin(2.0 * PI * 1.4 * t)
            phase += 2.0 * PI * freq / sampleRate
            out[i] = sin(phase).toFloat() * fadeIn * 0.36f
        }
        return out
    }

    private fun synthHandleRattle(): FloatArray {
        val len = (sampleRate * 0.6).toInt()
        val out = FloatArray(len)
        val rng = Random(55)
        for (hit in doubleArrayOf(0.03, 0.18, 0.34)) {
            val s = (hit * sampleRate).toInt()
            val dur = (0.09 * sampleRate).toInt()
            for (j in 0 until dur) {
                val idx = s + j
                if (idx >= len) break
                val t = j.toDouble() / sampleRate
                val env = exp(-t * 40.0).toFloat()
                out[idx] += (sin(2.0 * PI * 820.0 * t).toFloat() * 0.5f + (rng.nextFloat() * 2f - 1f) * 0.5f) * env
            }
        }
        return out
    }
}
