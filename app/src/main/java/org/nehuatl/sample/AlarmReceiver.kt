package org.nehuatl.sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    private var tts: TextToSpeech? = null

    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Напоминание без текста"
        val timeMillis = intent.getLongExtra("timeMillis", 0L)
        Log.d("AlarmReceiver", "⏰ Будильник сработал! Текст: $message, timeMillis=$timeMillis")

        removeFromOrganizer(context, timeMillis, message)

        // Показываем быстрый тост на экране
        Toast.makeText(context, "⏰ Напоминание: $message", Toast.LENGTH_LONG).show()

        // Инициализируем голосовой движок Android (TTS) для озвучки
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ru") // Озвучиваем строго на русском языке
                
                // Формируем текст: повторяем фразу 5 раз для надежности
                val speechText = "Внимание! Напоминание: $message. ".repeat(5)
                
                // Произносим текст вслух
                tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "AlarmTTS")
                       } else {
                Log.e("AlarmReceiver", "Ошибка инициализации TextToSpeech")
            }
        }
    }

    private fun removeFromOrganizer(context: Context, timeMillis: Long, message: String) {
        try {
            val organizerFile = File(context.filesDir, "organizer.txt")
            if (!organizerFile.exists()) return

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val lines = organizerFile.readText().split("\n").toMutableList()

            val indexToRemove = lines.indexOfFirst { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty()) return@indexOfFirst false
                val sep = trimmed.indexOf('|')
                if (sep < 0) return@indexOfFirst false
                val timeStr = trimmed.substring(0, sep).trim()
                val msg = trimmed.substring(sep + 1).trim()

                val timeMatch = if (timeMillis > 0L) {
                    try {
                        dateFormat.parse(timeStr)?.time == timeMillis
                    } catch (e: Exception) { false }
                } else false

                timeMatch || (timeMillis == 0L && msg == message)
            }

            if (indexToRemove >= 0) {
                lines.removeAt(indexToRemove)
                val newContent = lines.joinToString("\n").trim()
                organizerFile.writeText(if (newContent.isEmpty()) "" else newContent + "\n")
                Log.d("AlarmReceiver", "Удалено из organizer.txt: $message")
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Ошибка удаления из organizer.txt: ${e.message}")
        }
    }
}
