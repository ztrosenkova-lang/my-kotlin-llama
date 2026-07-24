package org.nehuatl.sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    private var tts: TextToSpeech? = null

    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Напоминание без текста"
        Log.d("AlarmReceiver", "⏰ Будильник сработал! Текст: $message")
        
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
}
