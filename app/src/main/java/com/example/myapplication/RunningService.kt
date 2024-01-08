package com.example.myapplication

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder


class RunnerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(
            applicationContext,
            "正在后台运行",
            Intent(applicationContext, MainActivity::class.java),
            ongoing = true,
            importance = NotificationManager.IMPORTANCE_LOW,
            noticeNow = false
        )
        startForeground(notificationId, notification)
        return START_STICKY
    }

    // stop your service from being in the foreground, call this
    // before stopping the service
    private fun stopForeground() {
        notificationId++
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopForeground()
        super.onDestroy()
    }

    companion object {
        private var notificationId = 1
    }
}