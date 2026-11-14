package com.clawd.pet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ClawdPetService : Service() {

    private var petView: ClawdPetView? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "clawd_pet_channel"
        const val ACTION_STOP = "com.clawd.pet.ACTION_STOP"
        const val ACTION_UPDATE_CONFIG = "com.clawd.pet.ACTION_UPDATE_CONFIG"
        const val ACTION_SET_BUBBLE = "com.clawd.pet.ACTION_SET_BUBBLE"
        const val EXTRA_SIZE = "extra_size"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_BUBBLE_TEXT = "extra_bubble_text"

        var isRunning: Boolean = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()

        petView = ClawdPetView(this).apply {
            val prefs = getSharedPreferences("clawd_prefs", Context.MODE_PRIVATE)
            crabSizeDp = prefs.getInt("pet_size", 80)
            walkSpeedMultiplier = prefs.getFloat("pet_speed", 1.0f)
            show()
        }
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_CONFIG -> {
                val size = intent.getIntExtra(EXTRA_SIZE, -1)
                val speed = intent.getFloatExtra(EXTRA_SPEED, -1f)
                if (size > 0) petView?.crabSizeDp = size
                if (speed > 0f) petView?.walkSpeedMultiplier = speed
            }
            ACTION_SET_BUBBLE -> {
                val bubbleText = intent.getStringExtra(EXTRA_BUBBLE_TEXT)
                petView?.setBubbleText(bubbleText)
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, ClawdPetService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🦀 Clawd Pet đang hoạt động")
            .setContentText("Chạm để mở cài đặt • Vuốt để đóng")
            .setSmallIcon(R.drawable.bubble_bg)
            .setContentIntent(pendingOpenApp)
            .addAction(0, "Tắt chú cua", pendingStop)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Clawd Pet Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị thông báo khi chú cua Clawd đang chạy nổi trên màn hình"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        petView?.hide()
        petView = null
        isRunning = false
    }
}
