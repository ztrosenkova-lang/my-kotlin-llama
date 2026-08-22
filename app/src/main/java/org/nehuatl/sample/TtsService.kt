package org.nehuatl.sample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TtsService : Service() {
    companion object {
        private const val TAG = "TtsService"
        private const val CHANNEL_ID = "tts_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_SPEAK = "org.nehuatl.sample.action.SPEAK"
        const val ACTION_STOP = "org.nehuatl.sample.action.STOP"
        const val EXTRA_TEXT = "extra_text"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var offlineTts: OfflineTts? = null
    private var audioTrack: AudioTrack? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        initTts()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SPEAK -> {
                val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
                if (text.isNotBlank()) {
                    speakText(text)
                }
            }
            ACTION_STOP -> {
                stopSpeaking()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopSpeaking()
        offlineTts = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TTS Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ИИ-Друг")
            .setContentText("Озвучка активна")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun initTts() {
        serviceScope.launch {
            try {
                val modelConfig = OfflineTtsVitsModelConfig(
                    model = "tts-model/ru_RU-ruslan-medium.onnx",
                    tokens = "tts-model/tokens.txt",
                    dataDir = "tts-model/espeak-ng-data"
                )
                val ttsConfig = OfflineTtsConfig(
                    model = OfflineTtsModelConfig(vits = modelConfig),
                    ruleFsts = "",
                    maxNumSentences = 1
                )
                offlineTts = OfflineTts(assets, ttsConfig)
                Log.d(TAG, "Sherpa-ONNX TTS initialized in separate process")
            } catch (e: Exception) {
                Log.e(TAG, "TTS init error: ${e.message}", e)
            }
        }
    }

    private fun speakText(text: String) {
        serviceScope.launch {
            try {
                val tts = offlineTts ?: return@launch
                val filtered = text.replace(Regex("[^\\p{L}\\p{N}\\s.,!?]"), "")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (filtered.isBlank()) return@launch

                val audio = tts.generate(filtered, sid = 0, speed = 1.0f)
                if (audio.samples.isNotEmpty()) {
                    val sampleRate = audio.sampleRate
                    val bufferSize = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT
                    )
                    audioTrack = AudioTrack(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                        bufferSize,
                        AudioTrack.MODE_STREAM,
                        0
                    )
                    audioTrack?.play()
                    audioTrack?.write(audio.samples, 0, audio.samples.size, AudioTrack.WRITE_BLOCKING)
                    audioTrack?.stop()
                    audioTrack?.release()
                    audioTrack = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "TTS playback error: ${e.message}", e)
            }
        }
    }

    private fun stopSpeaking() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Stop error: ${e.message}", e)
        }
    }
}
