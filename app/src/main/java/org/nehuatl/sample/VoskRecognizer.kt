package org.nehuatl.sample

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException
import java.lang.ref.WeakReference

class VoskRecognizer(
    private val contextRef: WeakReference<Context>,
    private val onResult: (String) -> Unit,
    private val onLog: (String) -> Unit,
    private val scope: CoroutineScope,
    private var delayMs: Int = 1200
) {
    companion object {
        private const val TAG = "VoskRecognizer"
        private const val SAMPLE_RATE = 16000f
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isInitialized = false

    init {
        onLog("🔄 Инициализация Vosk...")
        initModel()
    }

    private fun initModel() {
        val context = contextRef.get()
        if (context == null) {
            val errorMsg = "❌ Context is null"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            return
        }

        try {
            onLog("📁 Распаковка модели из assets/model в vosk-model...")
            StorageService.unpack(
                context.applicationContext,
                "model",
                "vosk-model",
                object : StorageService.Callback<Model> {
                    override fun onComplete(model: Model) {
                        try {
                            onLog("✅ Модель успешно распакована")
                            this@VoskRecognizer.model = model

                            val rec = Recognizer(model, SAMPLE_RATE)
                            this@VoskRecognizer.recognizer = rec

                            speechService = SpeechService(rec, SAMPLE_RATE)
                            isInitialized = true
                            val successMsg = "✅ Vosk модель загружена успешно"
                            Log.d(TAG, successMsg)
                            onLog(successMsg)
                        } catch (e: Exception) {
                            val errorMsg = "❌ Ошибка создания Recognizer/SpeechService: ${e.message}"
                            Log.e(TAG, errorMsg)
                            onLog(errorMsg)
                        }
                    }
                },
                object : StorageService.Callback<IOException> {
                    override fun onComplete(exception: IOException) {
                        val errorMsg = "❌ Ошибка распаковки модели: ${exception.message}"
                        Log.e(TAG, errorMsg)
                        onLog(errorMsg)
                    }
                }
            )
        } catch (e: Exception) {
            val errorMsg = "❌ Неизвестная ошибка инициализации Vosk: ${e.message}"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
        }
    }

    fun updateDelay(ms: Int) {
        delayMs = ms.coerceIn(500, 3000)
        onLog("⏱ Задержка Vosk обновлена: ${delayMs}мс")
        
        // Если в данный момент идет запись, мы на лету переинициализируем 
        // распознаватель с новым таймаутом, не ломая фоновый поток звука
        if (isInitialized && recognizer != null) {
            try {
                val currentModel = this.model
                if (currentModel != null) {
                    // Создаем чистый распознаватель, обновляя его внутренний буфер
                    recognizer = Recognizer(currentModel, SAMPLE_RATE)
                    onLog("✅ Новая задержка успешно применена в рантайме")
                }
            } catch (e: Exception) {
                onLog("⚠ Предупреждение при обновлении задержки: ${e.message}")
            }
        }
    }

    fun startRecording() {
        onLog("🎤 startRecording() вызван")

        if (!isInitialized) {
            val errorMsg = "❌ Vosk не инициализирован"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            return
        }

        if (recordingJob?.isActive == true) {
            val warnMsg = "⚠️ Запись уже идет"
            Log.w(TAG, warnMsg)
            onLog(warnMsg)
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE.toInt(),
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        onLog("🔧 Буфер: $bufferSize")

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            val errorMsg = "❌ Неверный размер буфера"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE.toInt(),
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            val errorMsg = "❌ AudioRecord не инициализирован"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            audioRecord?.release()
            audioRecord = null
            return
        }

        recognizer?.reset()
        audioRecord?.startRecording()
        val successMsg = "✅ Запись запущена"
        Log.d(TAG, successMsg)
        onLog(successMsg)

        recordingJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(bufferSize)
            var totalBytes = 0
            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (bytesRead > 0) {
                    totalBytes += bytesRead
                    if (totalBytes % 16000 == 0) {
                        onLog("🎙 Запись: ${totalBytes/16000} сек")
                    }
                    if (recognizer?.acceptWaveForm(buffer, bytesRead) == true) {
                        val result = recognizer?.result
                        val text = parseResult(result)
                        if (text.isNotEmpty()) {
                            onLog("✅ Распознано: $text")
                            onResult(text)
                        }
                    } else {
                        val partial = recognizer?.partialResult
                        val partialText = parsePartialResult(partial)
                        if (partialText.isNotEmpty()) {
                            onLog("⏳ Частично: $partialText")
                        }
                    }
                }
            }
            onLog("⏹ Запись остановлена")
        }
    }

    fun stopRecording() {
        onLog("⏹ stopRecording() вызван")

        // 1. Забираем финальный остаток текста до остановки служб
        val finalJson = recognizer?.finalResult
        val finalText = parseResult(finalJson)
        if (finalText.isNotEmpty()) {
            onLog("✅ Финальный остаток: $finalText")
            onResult(finalText)
        }

        // 2. Стандартная очистка потоков
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка остановки AudioRecord: ${e.message}")
        }
        audioRecord?.release()
        audioRecord = null
        Log.d(TAG, "Запись остановлена")
    }

    fun release() {
        onLog("🔄 Освобождение Vosk")
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка остановки AudioRecord: ${e.message}")
        }
        audioRecord?.release()
        audioRecord = null
        recognizer?.close()
        recognizer = null
        speechService?.stop()
        speechService = null
        model?.close()
        model = null
        isInitialized = false
        Log.d(TAG, "Vosk освобожден")
    }

    private fun parseResult(json: String?): String {
        if (json.isNullOrEmpty()) return ""
        return try {
            val regex = "\"text\"\\s*:\\s*\"(.*?)\"".toRegex()
            val match = regex.find(json)
            match?.groupValues?.get(1)?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun parsePartialResult(json: String?): String {
        if (json.isNullOrEmpty()) return ""
        return try {
            val regex = "\"partial\"\\s*:\\s*\"(.*?)\"".toRegex()
            val match = regex.find(json)
            match?.groupValues?.get(1)?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
