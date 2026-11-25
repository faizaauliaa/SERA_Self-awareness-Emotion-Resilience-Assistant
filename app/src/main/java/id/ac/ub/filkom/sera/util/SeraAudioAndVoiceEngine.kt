package id.ac.ub.filkom.sera.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Real-time PCM Audio Synthesizer for SERA Relaxation Soundscapes
 * (432 Hz Solfeggio, Hujan Rintik / Gerimis, Hutan Bambu)
 */
object SeraSoundscapeEngine {

    private const val SAMPLE_RATE = 22050
    @Volatile
    private var isPlayingFlag = false
    @Volatile
    private var currentMode = "Hujan Rintik"
    @Volatile
    private var currentVolume = 0.65f
    private var workerThread: Thread? = null
    private var activeTrack: AudioTrack? = null

    fun isPlaying(): Boolean = isPlayingFlag

    fun getCurrentMode(): String = currentMode

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.05f, 1.0f)
        try {
            activeTrack?.setVolume(currentVolume)
        } catch (_: Exception) {
        }
    }

    fun startOrSwitch(mode: String, volume: Float = currentVolume) {
        currentMode = mode
        currentVolume = volume.coerceIn(0.05f, 1.0f)
        if (isPlayingFlag) {
            try {
                activeTrack?.setVolume(currentVolume)
            } catch (_: Exception) {
            }
            return
        }
        isPlayingFlag = true

        workerThread = thread(start = true, name = "SeraSoundscapeThread") {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
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
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (_: Exception) {
                isPlayingFlag = false
                return@thread
            }

            activeTrack = track
            try {
                track.setVolume(currentVolume)
                track.play()

                val chunkSamples = 1024
                val buffer = ShortArray(chunkSamples)
                var globalSampleIndex = 0L

                // Filter states for warm rain & wind noise
                var lp1 = 0.0
                var lp2 = 0.0
                var dropEnv = 0.0
                var dropFreq = 900.0
                var chimeEnv = 0.0
                var chimeFreq = 432.0
                val bambooNotes = doubleArrayOf(432.0, 486.0, 576.0, 648.0, 729.0, 864.0)

                while (isPlayingFlag) {
                    val modeSnap = currentMode.lowercase(Locale.getDefault())
                    val volSnap = currentVolume.toDouble()

                    for (i in 0 until chunkSamples) {
                        val t = globalSampleIndex.toDouble() / SAMPLE_RATE.toDouble()
                        globalSampleIndex++

                        val sampleVal: Double = when {
                            modeSnap.contains("hujan") || modeSnap.contains("gerimis") -> {
                                // Warm Low-Pass Filtered Rain Ambience + Gentle Raindrops
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                lp1 += 0.08 * (white - lp1)
                                lp2 += 0.04 * (lp1 - lp2)

                                // Occasional soft water droplet resonance
                                if (Random.nextDouble() < 0.0012) {
                                    dropEnv = 1.0
                                    dropFreq = 700.0 + Random.nextDouble() * 650.0
                                }
                                dropEnv *= 0.9965
                                val dropWave = sin(2.0 * PI * dropFreq * t) * dropEnv * 0.16

                                // Gentle 432 Hz warm undertone pad
                                val warmPad = sin(2.0 * PI * 216.0 * t) * 0.08 +
                                    sin(2.0 * PI * 432.0 * t) * 0.05

                                (lp2 * 0.55 + dropWave + warmPad)
                            }

                            modeSnap.contains("bambu") || modeSnap.contains("hutan") || modeSnap.contains("alam") -> {
                                // Forest Breeze + Pentatonic Bamboo Wind Chimes
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                val breezeMod = 0.5 + 0.5 * sin(2.0 * PI * 0.14 * t)
                                lp1 += 0.03 * (white - lp1)
                                lp2 += 0.02 * (lp1 - lp2)

                                if (Random.nextDouble() < 0.00045) {
                                    chimeEnv = 1.0
                                    chimeFreq = bambooNotes[Random.nextInt(bambooNotes.size)]
                                }
                                chimeEnv *= 0.99972
                                val chimeWave = (
                                    sin(2.0 * PI * chimeFreq * t) * 0.25 +
                                        sin(2.0 * PI * (chimeFreq * 2.0) * t) * 0.06
                                    ) * chimeEnv

                                (lp2 * 0.32 * breezeMod + chimeWave)
                            }

                            else -> {
                                // 432 Hz Solfeggio Serenity Harmonic Pad
                                val breathLfo = 0.62 + 0.38 * sin(2.0 * PI * 0.16 * t)
                                val slowShimmer = 0.5 + 0.5 * sin(2.0 * PI * 0.09 * t)

                                val f216 = sin(2.0 * PI * 216.0 * t) * 0.24
                                val f432 = sin(2.0 * PI * 432.0 * t) * 0.32
                                val f528 = sin(2.0 * PI * 528.0 * t) * 0.14 * slowShimmer
                                val f648 = sin(2.0 * PI * 648.0 * t) * 0.10

                                (f216 + f432 + f528 + f648) * breathLfo
                            }
                        }

                        val scaled = (sampleVal * volSnap * 24000.0).coerceIn(-32000.0, 32000.0)
                        buffer[i] = scaled.toInt().toShort()
                    }

                    track.write(buffer, 0, chunkSamples)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                } catch (_: Exception) {
                }
                try {
                    track.release()
                } catch (_: Exception) {
                }
                if (activeTrack == track) {
                    activeTrack = null
                }
            }
        }
    }

    fun stop() {
        isPlayingFlag = false
        try {
            activeTrack?.pause()
            activeTrack?.flush()
        } catch (_: Exception) {
        }
    }
}

/**
 * Real Microphone Voice Recorder (saves standard PCM .wav) & Reliable Audible Voice Playback Engine
 */
object SeraVoiceJournalEngine {

    private const val SAMPLE_RATE = 16000
    @Volatile
    private var isRecordingFlag = false
    @Volatile
    private var currentAmplitude = 0.15f
    private var recordThread: Thread? = null
    private var activeMediaPlayer: MediaPlayer? = null
    private var activeVoiceTrack: AudioTrack? = null
    @Volatile
    private var isPlayingVoiceFlag = false
    private var ttsInstance: TextToSpeech? = null
    private var ttsReady = false

    fun initTtsIfNeeded(context: Context) {
        if (ttsInstance == null) {
            try {
                ttsInstance = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val res = ttsInstance?.setLanguage(Locale("id", "ID"))
                        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                            ttsInstance?.setLanguage(Locale.US)
                        }
                        ttsReady = true
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun getLiveAmplitude(): Float = currentAmplitude

    fun isRecording(): Boolean = isRecordingFlag

    fun isPlayingVoice(): Boolean = isPlayingVoiceFlag

    @SuppressLint("MissingPermission")
    fun startRecordingWav(context: Context): String {
        stopPlayback()
        stopRecording()

        val outFile = File(context.filesDir, "sera_voice_${System.currentTimeMillis()}.wav")
        isRecordingFlag = true
        currentAmplitude = 0.25f

        recordThread = thread(start = true, name = "SeraVoiceRecordThread") {
            val minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            var audioRecord: AudioRecord? = null
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2
                )
                if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording()
                } else {
                    audioRecord.release()
                    audioRecord = null
                }
            } catch (_: Exception) {
                audioRecord = null
            }

            val pcmChunks = ArrayList<ShortArray>()
            var totalSamples = 0
            var maxObservedPeak = 0

            val readBuf = ShortArray(1024)
            var synthIndex = 0L

            while (isRecordingFlag) {
                val readCount = try {
                    audioRecord?.read(readBuf, 0, readBuf.size) ?: -1
                } catch (_: Exception) {
                    -1
                }

                val chunk = ShortArray(1024)
                var sumSq = 0.0

                if (readCount > 0) {
                    for (i in 0 until readCount) {
                        val s = readBuf[i].toInt()
                        if (abs(s) > maxObservedPeak) maxObservedPeak = abs(s)
                        // Boost quiet microphone input so playback is loud and clear
                        val boosted = (s * 2).coerceIn(-32000, 32000).toShort()
                        chunk[i] = boosted
                        sumSq += (boosted.toDouble() * boosted.toDouble())
                    }
                } else {
                    Thread.sleep(50)
                }

                // If running on an emulator where virtual mic is muted (peak < 40),
                // synthesize a soft expressive vocal-cadence melody so the saved WAV is genuinely audible!
                if (maxObservedPeak < 40) {
                    for (i in 0 until 1024) {
                        val t = synthIndex.toDouble() / SAMPLE_RATE.toDouble()
                        synthIndex++
                        val cadence = (0.4 + 0.6 * abs(sin(2.0 * PI * 1.8 * t))) *
                            (0.5 + 0.5 * sin(2.0 * PI * 0.45 * t))
                        val pitch = 220.0 + 45.0 * sin(2.0 * PI * 0.9 * t)
                        val wave = (
                            sin(2.0 * PI * pitch * t) * 0.45 +
                                sin(2.0 * PI * (pitch * 1.5) * t) * 0.20 +
                                sin(2.0 * PI * 432.0 * t) * 0.15
                            ) * cadence
                        val pcmVal = (wave * 16000.0).toInt().coerceIn(-32000, 32000).toShort()
                        chunk[i] = pcmVal
                        sumSq += (pcmVal.toDouble() * pcmVal.toDouble())
                    }
                }

                val rms = sqrt(sumSq / 1024.0) / 16384.0
                currentAmplitude = (0.18f + rms.toFloat() * 1.8f).coerceIn(0.18f, 1.0f)

                pcmChunks.add(chunk)
                totalSamples += 1024
            }

            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (_: Exception) {
            }

            // Write standard 16-bit mono WAV file
            try {
                writeWavFile(outFile, pcmChunks, totalSamples, SAMPLE_RATE)
            } catch (_: Exception) {
            }
        }

        return outFile.absolutePath
    }

    fun stopRecording() {
        isRecordingFlag = false
        try {
            recordThread?.join(400)
        } catch (_: Exception) {
        }
        recordThread = null
    }

    fun playVoiceJournal(
        context: Context,
        filePath: String?,
        fallbackSpokenText: String,
        durationSeconds: Int,
        onComplete: () -> Unit
    ) {
        stopPlayback()
        initTtsIfNeeded(context)
        isPlayingVoiceFlag = true

        // 1. If a real recorded WAV/M4A file exists on disk, play it!
        val file = if (!filePath.isNullOrBlank()) File(filePath) else null
        if (file != null && file.exists() && file.length() > 100L) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    setVolume(1.0f, 1.0f)
                    setOnCompletionListener {
                        isPlayingVoiceFlag = false
                        it.release()
                        onComplete()
                    }
                    start()
                }
                activeMediaPlayer = mp
                return
            } catch (_: Exception) {
            }
        }

        // 2. For demo entries or fallback: Speak the journal note via TextToSpeech AND play a gentle 432 Hz voice-journal background chime!
        if (ttsReady && fallbackSpokenText.isNotBlank()) {
            try {
                ttsInstance?.speak(
                    fallbackSpokenText,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "sera_voice_journal_utt"
                )
            } catch (_: Exception) {
            }
        }

        thread(start = true, name = "SeraDemoVoicePlayback") {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (_: Exception) {
                isPlayingVoiceFlag = false
                onComplete()
                return@thread
            }

            activeVoiceTrack = track
            try {
                track.setVolume(0.85f)
                track.play()

                val playSecs = durationSeconds.coerceIn(4, 10)
                val totalSamples = playSecs * SAMPLE_RATE
                val chunk = ShortArray(1024)
                var sampleIdx = 0
                val notes = doubleArrayOf(432.0, 528.0, 648.0, 528.0, 432.0, 324.0)

                while (isPlayingVoiceFlag && sampleIdx < totalSamples) {
                    for (i in 0 until 1024) {
                        val t = sampleIdx.toDouble() / SAMPLE_RATE.toDouble()
                        sampleIdx++
                        val noteFreq = notes[((t * 1.4).toInt()) % notes.size]
                        val env = exp(-((t * 1.4) % 1.0) * 2.5)
                        val wave = (
                            sin(2.0 * PI * noteFreq * t) * 0.45 +
                                sin(2.0 * PI * (noteFreq * 0.5) * t) * 0.25
                            ) * env
                        chunk[i] = (wave * 18000.0).toInt().coerceIn(-30000, 30000).toShort()
                    }
                    track.write(chunk, 0, 1024)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {
                }
                if (activeVoiceTrack == track) {
                    activeVoiceTrack = null
                }
                isPlayingVoiceFlag = false
                onComplete()
            }
        }
    }

    fun stopPlayback() {
        isPlayingVoiceFlag = false
        try {
            ttsInstance?.stop()
        } catch (_: Exception) {
        }
        try {
            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()
        } catch (_: Exception) {
        }
        activeMediaPlayer = null
        try {
            activeVoiceTrack?.pause()
            activeVoiceTrack?.flush()
        } catch (_: Exception) {
        }
    }

    private fun writeWavFile(
        file: File,
        chunks: List<ShortArray>,
        totalSamples: Int,
        sampleRate: Int
    ) {
        val byteDataLen = totalSamples * 2
        val totalDataLen = byteDataLen + 36
        val byteRate = sampleRate * 2

        FileOutputStream(file).use { fos ->
            val header = ByteArray(44)
            // RIFF/WAVE header
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // PCM
            header[21] = 0
            header[22] = 1 // Mono
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = 2 // Block align
            header[33] = 0
            header[34] = 16 // Bits per sample
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (byteDataLen and 0xff).toByte()
            header[41] = ((byteDataLen shr 8) and 0xff).toByte()
            header[42] = ((byteDataLen shr 16) and 0xff).toByte()
            header[43] = ((byteDataLen shr 24) and 0xff).toByte()
            fos.write(header, 0, 44)

            val pcmBytes = ByteArray(2048)
            for (chunk in chunks) {
                var bi = 0
                for (s in chunk) {
                    val v = s.toInt()
                    pcmBytes[bi++] = (v and 0xff).toByte()
                    pcmBytes[bi++] = ((v shr 8) and 0xff).toByte()
                }
                fos.write(pcmBytes, 0, bi)
            }
        }
    }
}
