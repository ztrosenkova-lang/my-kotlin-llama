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
import kotlin.math.PI
import kotlin.math.sin
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

                       // Амплитуда покачивания окна overlay (bob), в dp.
        private const val BOB_WINDOW_AMPLITUDE_DP = 11
        // Период покачивания окна overlay, в мс.
        private const val BOB_WINDOW_PERIOD_MS = 2400L
        // Амплитуда прыжка окна overlay, в dp.
        private const val JUMP_WINDOW_AMPLITUDE_DP = 80
    }

    private lateinit var windowManager: WindowManager
    private var robotView: ComposeOverlayView? = null
    private var voiceRecognizer: VoiceRecognizer? = null

        // Флаги для единой анимации окна (bob + jump).
    // Объявлены как поля класса, чтобы обе корутины видели их.
    @Volatile private var jumpActive: Boolean = false
    @Volatile private var jumpElapsedMs: Long = 0L

    // Базовая Y-позиция окна. Обновляется при перетаскивании робота
    // пользователем, чтобы корутина анимации не возвращала окно
    // на старое место.
    @Volatile private var baselineY: Int = -1

        // Флаг «палец сейчас на роботе». Пока true — корутина анимации
    // пропускает итерации, чтобы не дёргать окно под пальцем.
    @Volatile private var isDragging: Boolean = false

    // Pinch-жест: масштаб робота в overlay.
    // Расстояние между пальцами при ACTION_POINTER_DOWN запоминается как опорное.
    // Текущий масштаб = опорный масштаб * (текущее расстояние / опорное расстояние).
    private var pinchBaseDistance: Float = 0f
    private var pinchBaseScale: Float = 1f
    private var isPinching: Boolean = false

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

                                       // Отдельная корутина: следит за сигналом прыжка и взводит флаг.
        // Стартует ДО корутины анимации, чтобы флаг не терялся, если сигнал
        // придёт в первые миллисекунды. Корректно работает и позже.
        lifecycleScope.launch {
            MainViewModel.instance?.robotJumpSignal?.collect { isJumping ->
                if (isJumping) {
                    jumpActive = true
                    jumpElapsedMs = 0L
                }
            }
        }

                               // ========== ЕДИНАЯ АНИМАЦИЯ ПОЛОЖЕНИЯ ОКНА ==========
        // Одна корутина управляет params.y, складывая две анимации:
        //  - bob: непрерывное покачивание окна вверх-вниз (амплитуда 11 dp,
        //    период 2400 мс, синус). Внутренний bob канваса отключён
        //    (bobAmplitude = 0f в RobotOverlayContent), чтобы не было наложения.
        //  - jump: прыжок окна вверх при robotJumpSignal (амплитуда 80 dp,
        //    тайминги синхронны с внутренним jumpProgressAnim: подъём 200 мс,
        //    пауза 200 мс, спуск 200 мс).
        // Пока идёт прыжок, bob приостанавливается, чтобы не конфликтовать.
        //
        // baseY определяется ДИНАМИЧЕСКИ: при первом появлении robotView
        // запоминается его текущий params.y. До появления робота цикл просто
        // ждёт. Это решает проблему, что onCreate выполняется раньше
        // onStartCommand (когда robotView ещё null).
        lifecycleScope.launch {
            val density = resources.displayMetrics.density
            val bobAmplitudePx = (BOB_WINDOW_AMPLITUDE_DP * density).toInt()
            val jumpAmplitudePx = (JUMP_WINDOW_AMPLITUDE_DP * density).toInt()

                        var bobPhaseMs = 0L
            val frameMs = 16L

            while (true) {
                delay(frameMs)

                val currentView = robotView
                val currentParams = currentView?.layoutParams as? WindowManager.LayoutParams

                                // Если робота ещё нет — ждём.
                if (currentView == null || currentParams == null) {
                    continue
                }

                // Пока палец на роботе — анимация не двигает окно.
                // Позицию задаёт ACTION_MOVE перетаскиванием.
                if (isDragging) {
                    continue
                }

                // При первом появлении робота — запоминаем его текущий Y
                // как базовый. baselineY — поле класса, обновляется при
                // перетаскивании робота пользователем, чтобы анимация
                // не возвращала окно на старое место.
                if (baselineY < 0) {
                    baselineY = currentParams.y
                }
                val baseline = baselineY

                // Пока идёт прыжок — bob не двигаем, ждём окончания.
                if (jumpActive) {
                    jumpElapsedMs += frameMs

                    // Фазы прыжка (синхронно с jumpProgressAnim в ThinkingRobotAnimation):
                    //   0–250    мс — стоим (пламя разгорается)
                    //   250–450  мс — подъём на jumpAmplitudePx
                    //   450–650  мс — стоим в верхней точке
                    //   650–900  мс — спуск
                    //   900–1100 мс — стоим (пламя гаснет)
                    val jumpOffsetPx: Int = when {
                        jumpElapsedMs < 250L -> 0
                        jumpElapsedMs < 450L -> {
                            val t = (jumpElapsedMs - 250L).toFloat() / 200f
                            val eased = t * t * (3f - 2f * t)
                            -(jumpAmplitudePx * eased).toInt()
                        }
                        jumpElapsedMs < 650L -> -jumpAmplitudePx
                        jumpElapsedMs < 900L -> {
                            val t = (jumpElapsedMs - 650L).toFloat() / 250f
                            val eased = t * t * (3f - 2f * t)
                            -(jumpAmplitudePx * (1f - eased)).toInt()
                        }
                        jumpElapsedMs < 1100L -> 0
                        else -> {
                            jumpActive = false
                            jumpElapsedMs = 0L
                            0
                        }
                    }

                    val newY = (baseline + jumpOffsetPx).coerceAtLeast(0)
                    if (currentParams.y != newY) {
                        currentParams.y = newY
                        try {
                            windowManager.updateViewLayout(currentView, currentParams)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to update window layout on jump: ${e.message}")
                        }
                    }
                } else {
                    // Bob: синус, полный цикл туда-обратно за BOB_WINDOW_PERIOD_MS.
                    bobPhaseMs = (bobPhaseMs + frameMs) % BOB_WINDOW_PERIOD_MS
                    val phase = (bobPhaseMs.toFloat() / BOB_WINDOW_PERIOD_MS.toFloat()) * 2f * PI.toFloat()
                    val bobOffsetPx = -(sin(phase) * bobAmplitudePx).toInt()

                    val newY = (baseline + bobOffsetPx).coerceAtLeast(0)
                    if (currentParams.y != newY) {
                        currentParams.y = newY
                                             try {
                            windowManager.updateViewLayout(currentView, currentParams)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to update window layout on bob: ${e.message}")
                        }
                    }
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

    val widthPx = (340 * resources.displayMetrics.density).toInt()
    val heightPx = (380 * resources.displayMetrics.density).toInt()

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

        // Расстояние между двумя пальцами в текущем событии.
        // Возвращает -1, если пальцев меньше двух.
        private fun pinchDistance(e: MotionEvent): Float {
            if (e.pointerCount < 2) return -1f
            val dx = e.getX(0) - e.getX(1)
            val dy = e.getY(0) - e.getY(1)
            return kotlin.math.sqrt(dx * dx + dy * dy)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    downTime = System.currentTimeMillis()
                    // Пока палец на роботе — корутина анимации не двигает окно.
                    isDragging = true
                    isPinching = false
                    pinchBaseDistance = 0f
                    return true
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    // Второй палец коснулся экрана — начинаем pinch.
                    // Перетаскивание одним пальцем отключается на время pinch.
                    val dist = pinchDistance(event)
                    if (dist > 0f) {
                        pinchBaseDistance = dist
                        pinchBaseScale = MainViewModel.instance?.overlayRobotScale?.value ?: 1f
                        isPinching = true
                    }
                    return true
                }

                               MotionEvent.ACTION_MOVE -> {
                    if (isPinching && event.pointerCount >= 2) {
                        val dist = pinchDistance(event)
                        if (dist > 0f && pinchBaseDistance > 0f) {
                                                        val ratio = dist / pinchBaseDistance
                            val newScale = (pinchBaseScale * ratio).coerceIn(0.5f, 3.0f)

                            // Запоминаем старые размеры и позицию до изменения.
                            val oldW = params.width
                            val oldH = params.height
                            val oldX = params.x
                            val oldY = params.y

                            // Новые размеры окна — базовые 340 × 380 dp, умноженные на scale.
                            // Но не больше размера экрана: окно останавливается на экране,
                            // а канвас внутри продолжает расти и обрезается краями окна.
                            val density = resources.displayMetrics.density
                            val screenW = resources.displayMetrics.widthPixels
                            val screenH = resources.displayMetrics.heightPixels
                            val wantedW = (340 * density * newScale).toInt()
                            val wantedH = (380 * density * newScale).toInt()
                            val newW = wantedW.coerceAtMost(screenW)
                            val newH = wantedH.coerceAtMost(screenH)

                            // Сдвигаем позицию так, чтобы центр окна остался на месте.
                            val deltaX = (newW - oldW) / 2
                            val deltaY = (newH - oldH) / 2

                            params.width = newW
                            params.height = newH
                            params.x = oldX - deltaX
                            params.y = oldY - deltaY

                            // Не выпускаем центр окна за пределы экрана.
                            // Робот всегда остаётся в видимой части экрана.
                            val centerX = params.x + newW / 2
                            val centerY = params.y + newH / 2
                            val clampedCenterX = centerX.coerceIn(0, screenW)
                            val clampedCenterY = centerY.coerceIn(0, screenH)
                            params.x = clampedCenterX - newW / 2
                            params.y = clampedCenterY - newH / 2

                            // Синхронизируем baselineY — чтобы bob/jump не дёрнулись.
                            if (baselineY >= 0) {
                                baselineY = baselineY - deltaY
                            }

                            windowManager.updateViewLayout(view, params)
                            MainViewModel.instance?.setOverlayRobotScale(newScale)
                        }
                        return true
                    }

                    // Один палец — перетаскивание.
                    val dxTotal = event.rawX - touchX
                    val dyTotal = event.rawY - touchY
                    val moved = kotlin.math.abs(dxTotal) > 15 || kotlin.math.abs(dyTotal) > 15

                    if (moved) {
                        params.x = initialX + dxTotal.toInt()
                        params.y = initialY + dyTotal.toInt()
                        windowManager.updateViewLayout(view, params)
                        baselineY = params.y
                    }
                    return true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    // Один из двух пальцев поднят. Pinch заканчивается.
                    // Оставшийся палец больше не тащит окно до нового ACTION_DOWN —
                    // иначе окно дёрнется на разницу между позицией пальцев.
                    isPinching = false
                    pinchBaseDistance = 0f
                    return true
                }

                 MotionEvent.ACTION_UP -> {
                    // Палец отпущен — анимация снова может двигать окно.
                    isDragging = false
                    isPinching = false
                    pinchBaseDistance = 0f

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

                            // Не запускаем распознавание, пока робот занят:
                            // говорит, думает, сжимает беседу или загружает модель.
                            // Иначе можно случайным тапом отправить новое сообщение,
                            // пока предыдущее ещё не обработано.
                            if (vm.isSpeaking.value ||
                                vm.state.value.isActive() ||
                                vm.isCompressing.value ||
                                vm.cloudState.value.isActive()) {
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
