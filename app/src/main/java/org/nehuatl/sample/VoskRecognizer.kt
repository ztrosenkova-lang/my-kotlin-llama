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
import java.io.IOException
import java.lang.ref.WeakReference

class VoskRecognizer(
    private val contextRef: WeakReference<Context>,
    private val onResult: (String) -> Unit,
    private val onLog: (String) -> Unit,
    private val scope: CoroutineScope
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
            val modelDir = File(context.applicationContext.filesDir, "vosk-model-small-ru-0.22")
            onLog("📁 Путь к модели: ${modelDir.absolutePath}")

            if (!modelDir.exists()) {
                val errorMsg = "❌ Модель не найдена: ${modelDir.absolutePath}"
                Log.e(TAG, errorMsg)
                onLog(errorMsg)
                return
            }

            val files = modelDir.listFiles()
            onLog("📄 Файлов в модели: ${files?.size ?: 0}")

            model = Model(modelDir.absolutePath)
            recognizer = Recognizer(model, SAMPLE_RATE)
            isInitialized = true
            val successMsg = "✅ Vosk модель загружена успешно"
            Log.d(TAG, successMsg)
            onLog(successMsg)
        } catch (e: IOException) {
            val errorMsg = "❌ Ошибка загрузки модели Vosk: ${e.message}"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
        } catch (e: Exception) {
            val errorMsg = "❌ Неизвестная ошибка загрузки Vosk: ${e.message}"
            Log.e(TAG, errorMsg)
            onLog(errorMsg)
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
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.d(TAG, "Запись остановлена")
    }

    fun release() {
        onLog("🔄 Освобождение Vosk")
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recognizer?.close()
        recognizer = null
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
