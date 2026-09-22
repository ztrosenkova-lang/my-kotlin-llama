package org.nehuatl.sample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService

class FloatingRobotService : LifecycleService() {

    companion object {
        private const val TAG = "FloatingRobotService"
        private const val CHANNEL_ID = "floating_robot_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "org.nehuatl.sample.START_FLOATING"
        const val ACTION_STOP = "org.nehuatl.sample.STOP_FLOATING"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private lateinit var windowManager: WindowManager
    private var robotView: ComposeOverlayView? = null
    private var micView: ImageButton? = null
    private var voiceRecognizer: VoiceRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        isRunning = true

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Инициализируем распознавание речи
        voiceRecognizer = VoiceRecognizer(
            context = applicationContext,
            onResult = { text ->
                Log.d(TAG, "Voice result: $text")
                MainViewModel.instance?.sendUserMessage(text)
                    ?: Log.w(TAG, "MainViewModel.instance is null, cannot send message")
                updateMicIcon(listening = false)
            },
            onError = { error ->
                Log.w(TAG, "Voice error: $error")
                updateMicIcon(listening = false)
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                if (robotView == null) addRobotOverlay()
                if (micView == null) addMicOverlay()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeRobotOverlay()
        removeMicOverlay()
        voiceRecognizer?.destroy()
        voiceRecognizer = null
        isRunning = false
        Log.d(TAG, "Service destroyed")
    }

    // ========== OVERLAY РОБОТА ==========

    private fun addRobotOverlay() {
        val viewModel = MainViewModel.instance
        if (viewModel == null) {
            Log.w(TAG, "MainViewModel.instance is null — robot overlay won't render")
            return
        }

        val view = ComposeOverlayView(this).apply {
    setOverlayContent {
        RobotOverlayContent(viewModel = viewModel)
    }
}

        val sizePx = (160 * resources.displayMetrics.density).toInt()

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        // Перетаскивание
        view.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - touchX).toInt()
                        params.y = initialY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(view, params)
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager.addView(view, params)
            robotView = view
            Log.d(TAG, "Robot overlay added")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add robot overlay: ${e.message}", e)
        }
    }

    private fun removeRobotOverlay() {
        robotView?.let {
            try {
                windowManager.removeView(it)
                Log.d(TAG, "Robot overlay removed")
            } catch (e: Exception) {
                Log.w(TAG, "removeRobotOverlay failed: ${e.message}")
            }
            robotView = null
        }
    }

    // ========== OVERLAY МИКРОФОНА ==========

    private fun addMicOverlay() {
        val button = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setBackgroundColor(Color.parseColor("#CC74C0FC"))
            contentDescription = "Голосовой ввод"
            setPadding(24, 24, 24, 24)
        }

        val sizePx = (72 * resources.displayMetrics.density).toInt()

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 40
            y = 300
        }

        // Перетаскивание + обработка клика
        button.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX - (event.rawX - touchX).toInt()
                        params.y = initialY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(button, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = kotlin.math.abs(event.rawX - touchX)
                        val dy = kotlin.math.abs(event.rawY - touchY)
                        if (dx < 15 && dy < 15) {
                            onMicClicked()
                            return true
                        }
                    }
                }
                return false
            }
        })

        try {
            windowManager.addView(button, params)
            micView = button
            Log.d(TAG, "Mic overlay added")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add mic overlay: ${e.message}", e)
        }
    }

    private fun removeMicOverlay() {
        micView?.let {
            try {
                windowManager.removeView(it)
                Log.d(TAG, "Mic overlay removed")
            } catch (e: Exception) {
                Log.w(TAG, "removeMicOverlay failed: ${e.message}")
            }
            micView = null
        }
    }

    // ========== ЛОГИКА МИКРОФОНА ==========

    private fun onMicClicked() {
        val vm = MainViewModel.instance
        if (vm == null) {
            Log.w(TAG, "MainViewModel.instance is null, cannot recognize")
            return
        }

        Log.d(TAG, "Mic clicked, starting recognition")
        updateMicIcon(listening = true)
        voiceRecognizer?.start()
    }

    private fun updateMicIcon(listening: Boolean) {
        micView?.let {
            it.setBackgroundColor(
                if (listening) Color.parseColor("#FF2E7D32")   // зелёный — слушает
                else Color.parseColor("#CC74C0FC")              // синий — ждёт
            )
        }
    }

    // ========== NOTIFICATION ==========

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Плавающий ИИ-Друг",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Робот работает поверх других приложений"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingRobotService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ИИ-Друг работает")
            .setContentText("Робот на экране. Нажмите микрофон для вопроса.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Выключить",
                stopIntent
            )
            .setOngoing(true)
            .build()
    }
}
