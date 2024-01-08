package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


val importanceChannelIdMap = mapOf(
    NotificationManager.IMPORTANCE_HIGH to "channel_importance_high",
    NotificationManager.IMPORTANCE_LOW to "channel_importance_low"
)

@Suppress("DEPRECATION")
val importanceLegacyMap = mapOf(
    NotificationManager.IMPORTANCE_HIGH to Notification.PRIORITY_HIGH,
    NotificationManager.IMPORTANCE_LOW to Notification.PRIORITY_LOW
)

private var notificationManager: NotificationManager? = null
fun createNotification(
    ctx: Context,
    aMessage: String?,
    intent: Intent,
    ongoing: Boolean = false,
    importance: Int = NotificationManager.IMPORTANCE_HIGH,
    noticeNow: Boolean = true
): Notification {
    val NOTIFY_ID = 1002

    // There are hardcoding only for show it's just strings
    val name = importanceChannelIdMap[importance]!!
    val id = name
    val description = name // The user-visible description of the channel.
    val builder: NotificationCompat.Builder

    if (notificationManager == null) {
        notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val pendingIntent: PendingIntent =
        PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        var mChannel = notificationManager!!.getNotificationChannel(id)
        if (mChannel == null) {
            mChannel = NotificationChannel(id, name, importance)
            mChannel.description = description
            mChannel.enableVibration(true)
            mChannel.lightColor = Color.GREEN
            mChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager!!.createNotificationChannel(mChannel)
        }
        builder = NotificationCompat.Builder(ctx, id)
    } else {
        @Suppress("DEPRECATION")
        builder = NotificationCompat.Builder(ctx)
        builder.setPriority(importanceLegacyMap[importance]!!)
    }

    builder.setContentTitle(aMessage) // required
        .setSmallIcon(R.drawable.ic_launcher_foreground) // required
        .setContentText(ctx.getString(R.string.app_name)) // required
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setOnlyAlertOnce(false)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        .setOngoing(ongoing)
    if (ongoing) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
    }
    val notification = builder.build()
    if (noticeNow) {
        notificationManager!!.notify(NOTIFY_ID, notification)
    }
    return notification
}

fun clearNotification(ctx: Context) {
    if (notificationManager == null) {
        notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }
    notificationManager!!.cancelAll()
}

fun checkPermission(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun requestPermission(permission: String, activity: ComponentActivity) {
    if (checkPermission(permission, activity.applicationContext)) {
        return
    }
    val req: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(activity.applicationContext, "缺少权限：$permission", Toast.LENGTH_LONG).show()
            }
        }
    req.launch(permission)
}