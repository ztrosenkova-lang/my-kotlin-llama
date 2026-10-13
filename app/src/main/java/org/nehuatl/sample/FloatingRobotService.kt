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
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
class FloatingRobotService : LifecycleService() {

    companion object {
        private const val TAG = "FloatingRobotService"
        private const val CHANNEL_ID = "floating_robot_channel"
        private const val NOTIFICATION_ID = 1001

                const val ACTION_START = "org.nehuatl.sample.START_FLOATING"
        const val ACTION_STOP = "org.nehuatl.sample.STOP_FLOATING"
        const val ACTION_HIDE = "org.nehuatl.sample.HIDE_FLOATING"
        const val ACTION_SHOW = "org.nehuatl.sample.SHOW_FLOATING"

               @Volatile
        var isRunning: Boolean = false
            private set

                // На сколько dp окно overlay поднимается при прыжке.
        private const val JUMP_WINDOW_EXPAND_DP = 40
    }

    private lateinit var windowManager: WindowManager
    private var robotView: ComposeOverlayView? = null
    private var voiceRecognizer: VoiceRecognizer? = null

        override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        isRunning = true
        MainViewModel.instance?.setFloatingRunning(true)

    windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Инициализируем распознавание речи
        voiceRecognizer = VoiceRecognizer(
            context = applicationContext,
                                              onResult = { text ->
                Log.d(TAG, "Voice result: $text")
                val vm = MainViewModel.instance
                if (vm == null) {
                    Log.w(TAG, "MainViewModel.instance is null, cannot send message")
                } else {
                    val command = text.trim().lowercase()
                    when (command) {
                        "махни рукой" -> vm.triggerOverlayWave()
                        "уйди" -> {
                            val stopIntent = Intent(this@FloatingRobotService, FloatingRobotService::class.java).apply {
                                action = ACTION_STOP
                            }
                            startService(stopIntent)

                            try {
                                val intent = Intent(this@FloatingRobotService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to bring MainActivity to front: ${e.message}")
                            }
                        }
                        "умный режим" -> vm.enableSmartMode()
                        "режим калькулятора" -> vm.disableSmartMode()
                        else -> vm.sendUserMessage(text)
                    }
                }
                MainViewModel.instance?.setOverlayListening(false)
            },
                        onError = { error ->
                Log.w(TAG, "Voice error: $error")
                MainViewModel.instance?.setOverlayListening(false)
            }
        )

                // ========== РЕАКЦИЯ НА ПРЫЖОК ==========
        // Когда робот прыгает (robotJumpSignal == true), окно overlay
        // плавно сдвигается вверх на 40dp, висит 200 мс, плавно возвращается.
        // Высота окна не меняется — двигается только params.y.
        lifecycleScope.launch {
            MainViewModel.instance?.robotJumpSignal?.collect { isJumping ->
                if (!isJumping) return@collect
                val view = robotView ?: return@collect
                val params = view.layoutParams as? WindowManager.LayoutParams ?: return@collect

                val jumpHeightPx = (40 * resources.displayMetrics.density).toInt()
                val startY = params.y
                val targetY = startY - jumpHeightPx

                try {
                    // Плавный подъём: 200 мс, ~16 мс на кадр
                    val upSteps = 12
                    for (step in 1..upSteps) {
                        val t = step.toFloat() / upSteps
                        // FastOutSlowIn-подобное сглаживание
                        val eased = t * t * (3f - 2f * t)
                        params.y = (startY - jumpHeightPx * eased).toInt()
                        windowManager.updateViewLayout(view, params)
                        delay(200L / upSteps)
                    }
                    params.y = targetY
                    windowManager.updateViewLayout(view, params)

                    // Пауза в верхней точке
                    delay(200)

                    // Плавный спуск: 200 мс
                    val downSteps = 12
                    for (step in 1..downSteps) {
                        val t = step.toFloat() / downSteps
                        val eased = t * t * (3f - 2f * t)
                        params.y = (targetY + jumpHeightPx * eased).toInt()
                        windowManager.updateViewLayout(view, params)
                        delay(200L / downSteps)
                    }
                    params.y = startY
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update window layout on jump: ${e.message}")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

                when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_HIDE -> {
                robotView?.visibility = android.view.View.GONE
                Log.d(TAG, "Robot overlay hidden (app in foreground)")
            }
            ACTION_SHOW -> {
                robotView?.visibility = android.view.View.VISIBLE
                Log.d(TAG, "Robot overlay shown (app in background)")
            }
            else -> {
                if (robotView == null) addRobotOverlay()
                robotView?.visibility = android.view.View.VISIBLE
            }
        }
        return START_STICKY
    }

        override fun onDestroy() {
        super.onDestroy()
        removeRobotOverlay()
        voiceRecognizer?.destroy()
        voiceRecognizer = null
        isRunning = false
        MainViewModel.instance?.setFloatingRunning(false)
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

        val widthPx = (280 * resources.displayMetrics.density).toInt()
    val heightPx = (320 * resources.displayMetrics.density).toInt()

    val params = WindowManager.LayoutParams(
        widthPx,
        heightPx,
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

                   view.setOnTouchListener(object : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var touchX = 0f
        private var touchY = 0f
        private var downTime = 0L
        private var lastTapTime = 0L
        // Флаг: тап был обработан как одиночный — если в течение 400 мс придёт
        // второй тап, он будет считаться двойным, но распознавание уже запущено.
        private var singleTapHandled = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    downTime = System.currentTimeMillis()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dxTotal = event.rawX - touchX
                    val dyTotal = event.rawY - touchY
                    val moved = kotlin.math.abs(dxTotal) > 15 || kotlin.math.abs(dyTotal) > 15

                    if (moved) {
                        // Перетаскивание робота
                        params.x = initialX + dxTotal.toInt()
                        params.y = initialY + dyTotal.toInt()
                        windowManager.updateViewLayout(view, params)
                    }
                    return true
                }
                               MotionEvent.ACTION_UP -> {
                    val dxSigned = event.rawX - touchX
                    val dySigned = event.rawY - touchY
                    val dx = kotlin.math.abs(dxSigned)
                    val dy = kotlin.math.abs(dySigned)
                    val dt = System.currentTimeMillis() - downTime

                    // СВАЙП ВВЕРХ: быстрое движение (<300 мс), вверх (>50 px),
                    // без большого бокового сноса. Триггерит прыжок.
                    if (dt < 300 && dySigned < -50f && dx < dy * 0.6f) {
                        MainViewModel.instance?.triggerRobotJump()
                        return true
                    }

                    // СВАЙП ВЛЕВО/ВПРАВО: быстрое движение (<300 мс), по X (>50 px),
                    // без большого вертикального сноса. Триггерит наклон головы.
                    if (dt < 300 && dx > 50 && dx > dy * 1.5f) {
                        val direction = if (dxSigned < 0f) -1f else 1f
                        val vm = MainViewModel.instance
                        vm?.triggerOverlayHeadTilt(direction)
                        return true
                    }

                                       // ТАП: не двигались и быстро отпустили
                    if (dx < 15 && dy < 15 && dt < 300) {
                        val now = System.currentTimeMillis()
                        val vm = MainViewModel.instance ?: return true

                        val isListening = vm.overlayListening.value
                        val isDoubleTap = (now - lastTapTime < 400) && singleTapHandled

                        if (isDoubleTap) {
                            // ДВОЙНОЙ ТАП — убираем overlay, поднимаем MainActivity.
                            // Распознавание, если работает, глушим жёстко.
                            singleTapHandled = false
                            lastTapTime = 0L

                            voiceRecognizer?.stop()
                            voiceRecognizer?.destroy()
                            voiceRecognizer = null
                            vm.setOverlayListening(false)

                            val stopIntent = Intent(this@FloatingRobotService, FloatingRobotService::class.java).apply {
                                action = ACTION_STOP
                            }
                            startService(stopIntent)

                            try {
                                val intent = Intent(this@FloatingRobotService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to bring MainActivity to front: ${e.message}")
                            }
                        } else if (isListening) {
                            // ОДИНОЧНЫЙ ТАП В РЕЖИМЕ СЛУШАНИЯ — отмена.
                            // Жёстко убиваем recognizer, чтобы отменить отложенный onResult,
                            // и сбрасываем флаг, чтобы аура погасла. Overlay остаётся.
                            Log.d(TAG, "Single tap while listening — cancel recognition")
                            singleTapHandled = false
                            lastTapTime = 0L

                            voiceRecognizer?.stop()
                            voiceRecognizer?.destroy()
                            voiceRecognizer = null
                            vm.setOverlayListening(false)
                        } else {
                            // ОДИНОЧНЫЙ ТАП В РЕЖИМЕ ПОКОЯ — запуск распознавания.
                            singleTapHandled = true
                            lastTapTime = now

                            // Не запускаем распознавание, пока робот говорит или модель думает.
                            if (vm.isSpeaking.value || vm.state.value.isActive()) {
                                Log.d(TAG, "Robot busy — skip voice recognition")
                                return true
                            }

                            vm.setOverlayListening(true)
                            voiceRecognizer?.start()
                        }
                    }
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
            .setContentText("Робот на экране. Коснитесь робота для вопроса.")
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
