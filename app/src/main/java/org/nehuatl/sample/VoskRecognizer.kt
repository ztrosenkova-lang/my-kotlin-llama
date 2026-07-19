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

class VoskRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "VoskRecognizer"
        private const val SAMPLE_RATE = 16000
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isInitialized = false

    init {
        initModel()
    }

    private fun initModel() {
        try {
            val modelDir = File(context.filesDir, "vosk-model-small-ru-0.22")
            if (!modelDir.exists()) {
                Log.e(TAG, "Модель не найдена: ${modelDir.absolutePath}")
                return
            }
            model = Model(modelDir.absolutePath)
            recognizer = Recognizer(model, SAMPLE_RATE)
            isInitialized = true
            Log.d(TAG, "Vosk модель загружена успешно")
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка загрузки модели Vosk: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка загрузки Vosk: ${e.message}")
        }
    }

    fun startRecording() {
        if (!isInitialized) {
            Log.e(TAG, "Vosk не инициализирован")
            return
        }

        if (recordingJob?.isActive == true) {
            Log.w(TAG, "Запись уже идет")
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Неверный размер буфера")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord не инициализирован")
            audioRecord?.release()
            audioRecord = null
            return
        }

        recognizer?.reset()
        audioRecord?.startRecording()
        Log.d(TAG, "Запись запущена")

        recordingJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(bufferSize)
            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (bytesRead > 0) {
                    if (recognizer?.acceptWaveForm(buffer, bytesRead) == true) {
                        val result = recognizer?.result
                        val text = parseResult(result)
                        if (text.isNotEmpty()) {
                            onResult(text)
                        }
                    } else {
                        val partial = recognizer?.partialResult
                        val partialText = parsePartialResult(partial)
                        if (partialText.isNotEmpty()) {
                            Log.d(TAG, "Частичный результат: $partialText")
                        }
                    }
                }
            }
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.d(TAG, "Запись остановлена")
    }

    fun release() {
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
