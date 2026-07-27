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
import java.io.File
import java.lang.ref.WeakReference

class VoskRecognizer(
    private val contextRef: WeakReference<Context>,
    private val onResult: (String) -> Unit,
    private val onLog: (String) -> Unit,
    private val scope: CoroutineScope,
    private val externalModelPath: String? // Абсолютный путь к файлу модели (1.5 ГБ)
) {
    companion object {
        private const val TAG = "VoskRecognizer"
        private const val SAMPLE_RATE = 16000f
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isInitialized = false

    init {
        onLog("🔄 Инициализация Vosk с прямым путем к модели...")
        initModel()
    }

    /**
     * Инициализация модели синхронно по абсолютному пути.
     */
    private fun initModel() {
        val context = contextRef.get()
        if (context == null) {
            val errorMsg = "❌ Context is null"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            return
        }

        if (externalModelPath.isNullOrEmpty()) {
            val errorMsg = "❌ Путь к модели не указан. Необходимо передать absolutePath к файлу модели (1.5 ГБ)."
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            return
        }

        val modelFile = File(externalModelPath)
        if (!modelFile.exists()) {
            val errorMsg = "❌ Модель не найдена по пути: $externalModelPath"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
            return
        }

        try {
            onLog("📁 Загрузка модели из: $externalModelPath")
            val startTime = System.currentTimeMillis()

            // Инициализация модели синхронно по пути
            val loadedModel = Model(externalModelPath)
            this.model = loadedModel

            val duration = System.currentTimeMillis() - startTime
            onLog("✅ Модель успешно загружена за ${duration}мс")

            // Создание распознавателя
            val rec = Recognizer(loadedModel, SAMPLE_RATE)
            this.recognizer = rec
            this.isInitialized = true

            val successMsg = "✅ Vosk модель успешно инициализирована"
            Log.d(TAG, successMsg)
            onLog(successMsg)

        } catch (e: Exception) {
            val errorMsg = "❌ Ошибка инициализации Vosk: ${e.message}"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)

            // Очистка при ошибке
            try {
                model?.close()
            } catch (_: Exception) {}
            model = null
            recognizer = null
            isInitialized = false
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

    /**
     * Полное освобождение ресурсов с гарантированным .close() для native-объектов.
     */
    fun release() {
        onLog("🔄 Освобождение Vosk (принудительное закрытие native-объектов)")

        // 1. Остановка записи
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка остановки AudioRecord: ${e.message}")
        }
        audioRecord?.release()
        audioRecord = null

        // 2. Принудительное закрытие распознавателя
        try {
            recognizer?.close()
            onLog("✅ Recognizer закрыт")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка закрытия Recognizer: ${e.message}")
        }
        recognizer = null

        // 3. Принудительное закрытие модели (освобождение C++ памяти)
        try {
            model?.close()
            onLog("✅ Model закрыта (C++ память освобождена)")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка закрытия Model: ${e.message}")
        }
        model = null

        isInitialized = false
        Log.d(TAG, "Vosk полностью освобожден")
        onLog("🔄 Vosk полностью освобожден")
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
