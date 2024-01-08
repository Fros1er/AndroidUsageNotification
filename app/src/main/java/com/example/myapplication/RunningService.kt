package com.example.myapplication

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder


class RunnerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(
            applicationContext,
            "正在后台运行",
            Intent(applicationContext, MainActivity::class.java),
            ongoing = true,
            importance = NotificationManager.IMPORTANCE_LOW,
            noticeNow = false
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(notificationId, notification)
        } else {
            startForeground(
                notificationId, notification,
                FOREGROUND_SERVICE_TYPE_LOCATION)
        }
        return START_STICKY
    }

    // stop your service from being in the foreground, call this
    // before stopping the service
    fun stopForeground() {
        notificationId++
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        private var notificationId = 1
    }
}