package org.nehuatl.sample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService

/**
 * Foreground service, который держит процесс приложения живым в фоне.
 * Повышает приоритет процесса — система реже его убивает.
 * Держит PARTIAL_WAKE_LOCK, чтобы CPU не засыпал.
 */
class KeepAliveService : LifecycleService() {

    companion object {
        private const val TAG = "KeepAliveService"
        private const val CHANNEL_ID = "keep_alive_channel"
        private const val NOTIFICATION_ID = 1002

        const val ACTION_START = "org.nehuatl.sample.KEEP_ALIVE_START"
        const val ACTION_STOP = "org.nehuatl.sample.KEEP_ALIVE_STOP"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "KeepAliveService created")
        isRunning = true

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // PARTIAL_WAKE_LOCK — держит CPU активным в фоне
        // (критично для локального инференса LLM)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "org.nehuatl.sample:KeepAliveWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire()
        }
        Log.d(TAG, "WakeLock acquired")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Уже запущен через onCreate
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false

        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock release failed: ${e.message}")
        }
        wakeLock = null

        Log.d(TAG, "KeepAliveService destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ИИ-Друг: фоновая работа",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Держит модель и чат в памяти"
                setShowBadge(false)
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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ИИ-Друг")
            .setContentText("Модель и чат в памяти")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
