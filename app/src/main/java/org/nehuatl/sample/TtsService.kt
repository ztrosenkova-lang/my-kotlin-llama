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
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TtsService : Service() {
    companion object {
        private const val TAG = "TtsService"
        private const val CHANNEL_ID = "tts_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_SPEAK = "org.nehuatl.sample.action.SPEAK"
        const val ACTION_STOP = "org.nehuatl.sample.action.STOP"
        const val ACTION_TTS_READY = "org.nehuatl.sample.action.TTS_READY"
        const val ACTION_TTS_ERROR = "org.nehuatl.sample.action.TTS_ERROR"
        const val ACTION_SPEAK_START = "org.nehuatl.sample.action.SPEAK_START"
        const val ACTION_SPEAK_END = "org.nehuatl.sample.action.SPEAK_END"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_MESSAGE = "extra_message"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val ttsMutex = Mutex()
    private var offlineTts: OfflineTts? = null
    private var audioTrack: AudioTrack? = null
    private var isTtsInitialized = false

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
                    serviceScope.launch {
                        waitForTts()
                        speakText(text)
                    }
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
            ttsMutex.withLock {
                try {
                    // Копируем espeak-ng-data из assets в файловую систему
                    val dataDir = copyDataDir("tts-model/espeak-ng-data")
                    Log.d(TAG, "espeak-ng-data copied to: $dataDir")

                    val modelConfig = OfflineTtsVitsModelConfig(
                        model = "tts-model/ru_RU-ruslan-medium.onnx",
                        tokens = "tts-model/tokens.txt",
                        dataDir = dataDir
                    )
                    val ttsConfig = OfflineTtsConfig(
                        model = OfflineTtsModelConfig(vits = modelConfig),
                        ruleFsts = "",
                        maxNumSentences = 1
                    )
                    offlineTts = OfflineTts(assets, ttsConfig)
                    isTtsInitialized = true
                    Log.d(TAG, "Sherpa-ONNX TTS initialized in separate process")

                    val readyIntent = Intent(ACTION_TTS_READY).apply {
                        setPackage(packageName)
                        putExtra(EXTRA_MESSAGE, "✅ Офлайн голосовой движок успешно загружен")
                    }
                    sendBroadcast(readyIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "TTS init error: ${e.message}", e)
                    isTtsInitialized = false

                    val errorIntent = Intent(ACTION_TTS_ERROR).apply {
                        setPackage(packageName)
                        putExtra(EXTRA_MESSAGE, "❌ Ошибка инициализации офлайн TTS: ${e.message}")
                    }
                    sendBroadcast(errorIntent)
                }
            }
        }
    }

    private suspend fun waitForTts() {
        var attempts = 0
        while (!isTtsInitialized && attempts < 100) {
            delay(100)
            attempts++
        }
    }

    private suspend fun speakText(text: String) {
        ttsMutex.withLock {
            try {
                val tts = offlineTts ?: return
                val filtered = text.replace(Regex("[^\\p{L}\\p{N}\\s.,!?]"), "")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (filtered.isBlank()) return

                try {
                    val audio = tts.generate(filtered, sid = 0, speed = 0.85f)
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
                        
                        // Отправляем Broadcast — начало озвучивания
                        // Только после того, как звук реально начал воспроизводиться
                        val startIntent = Intent(ACTION_SPEAK_START).apply {
                            setPackage(packageName)
                        }
                        sendBroadcast(startIntent)
                        Log.d(TAG, "SPEAK_START broadcast sent")
                        
                        audioTrack?.write(audio.samples, 0, audio.samples.size, AudioTrack.WRITE_BLOCKING)
                        audioTrack?.stop()
                        audioTrack?.release()
                        audioTrack = null
                    }
                } finally {
                    // Отправляем Broadcast — конец озвучивания
                    val endIntent = Intent(ACTION_SPEAK_END).apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(endIntent)
                    Log.d(TAG, "SPEAK_END broadcast sent")
                }
            } catch (e: Exception) {
                Log.e(TAG, "TTS playback error: ${e.message}", e)
                // В случае ошибки тоже отправляем конец озвучивания
                val endIntent = Intent(ACTION_SPEAK_END).apply {
                    setPackage(packageName)
                }
                sendBroadcast(endIntent)
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

    /**
     * Копирует директорию espeak-ng-data из assets во внешнее хранилище.
     * Возвращает полный путь к скопированной директории.
     */
    private fun copyDataDir(dataDir: String): String {
        Log.i(TAG, "data dir is $dataDir")
        copyAssets(dataDir)

        val newDataDir = getExternalFilesDir(null)!!.absolutePath
        Log.i(TAG, "newDataDir: $newDataDir")
        return "$newDataDir/$dataDir"
    }

    /**
     * Рекурсивно копирует файлы и директории из assets в файловую систему.
     */
    private fun copyAssets(path: String) {
        val assets: Array<String>?
        try {
            assets = application.assets.list(path)
            if (assets!!.isEmpty()) {
                copyFile(path)
            } else {
                val fullPath = "${getExternalFilesDir(null)}/$path"
                val dir = File(fullPath)
                dir.mkdirs()
                for (asset in assets.iterator()) {
                    val p: String = if (path == "") "" else path + "/"
                    copyAssets(p + asset)
                }
            }
        } catch (ex: IOException) {
            Log.e(TAG, "Failed to copy $path. $ex")
        }
    }

    /**
     * Копирует одиночный файл из assets в файловую систему.
     */
    private fun copyFile(filename: String) {
        try {
            val istream = application.assets.open(filename)
            val newFilename = getExternalFilesDir(null).toString() + "/" + filename
            val ostream = FileOutputStream(newFilename)
            val buffer = ByteArray(1024)
            var read = 0
            while (read != -1) {
                ostream.write(buffer, 0, read)
                read = istream.read(buffer)
            }
            istream.close()
            ostream.flush()
            ostream.close()
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to copy $filename, $ex")
        }
    }
}
