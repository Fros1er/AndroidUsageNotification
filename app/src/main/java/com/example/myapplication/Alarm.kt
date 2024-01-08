package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import java.util.Calendar
import kotlin.math.min

const val TWENTY_MINUTE = 10 * 60 * 1000L
//const val TEN_MINUTE = 10 * 60 * 1000L

const val TEN_MINUTE = 5 * 1000L
const val FIVE_MINUTE = 5 * 60 * 1000L
const val THREE_MINUTE = 3 * 60 * 1000L
//const val THREE_MINUTE = 15 * 1000L

class TimeStatus {
    private var initialTime = 0L
    private var timeRemains = 0L
    private var resumedTime = 0L
    private var pausedTime = 0L
    private var refreshStartedTime = 0L // 第一次息屏设置。如果中间亮屏超过三分钟，重新设置。息屏超过20min则可以刷新时长

    fun reset(initialTimeInMillis: Long) {
        timeRemains = initialTimeInMillis
        initialTime = initialTimeInMillis
        val now = Calendar.getInstance().timeInMillis
        resumedTime = now
    }

    fun pause() {
        val now = Calendar.getInstance().timeInMillis
        timeRemains -= (now - resumedTime)
        pausedTime = now
        if (refreshStartedTime == 0L || now - resumedTime > THREE_MINUTE) {
            refreshStartedTime = now
        }
    }

    fun resume(): Pair<Long, Boolean> {
        val now = Calendar.getInstance().timeInMillis
        resumedTime = now
        val canRefresh = refreshStartedTime != 0L && now - refreshStartedTime > TWENTY_MINUTE
        timeRemains += (now - pausedTime) // 额外添加息屏时间这么长的允许时长
        timeRemains = min(timeRemains, initialTime) // 不超过初始时长
        return Pair(timeRemains, canRefresh)
    }
}

class Alarm(context: Context) {
    private val context: Context
    val text = mutableStateOf("未开始")
    val nextAlertText = mutableStateOf("N/A")
    val nextLengthText = mutableStateOf("十分钟")
    private val timeStatus = TimeStatus()
    private var alertCount = 0
        set(value) {
            field = value
            nextLengthText.value = when (value) {
                0 -> "十分钟"
                1 -> "五分钟"
                else -> "三分钟"
            }
        }
    private var enabled = false
        set(value) {
            field = value
            text.value = if (value) "已开始" else "未开始"
        }
    private var nextIntent: PendingIntent? = null
    private val timeFormat = SimpleDateFormat.getDateTimeInstance()

    init {
        this.context = context.applicationContext
    }

    private fun getCurrTimeAlert(): Long {
        return when (alertCount) {
            0 -> TEN_MINUTE
            1 -> FIVE_MINUTE
            else -> THREE_MINUTE
        }
    }

    private fun triggerAlert(timeInterval: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            Toast.makeText(context, "没有SCHEDULE_EXACT_ALARM权限", Toast.LENGTH_LONG).show()
        }
        val intent = PendingIntent.getBroadcast(
            context, 1, Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        nextIntent = intent
        timeStatus.reset(timeInterval)
        val cal = Calendar.getInstance()
        cal.add(Calendar.MILLISECOND, timeInterval.toInt())
        nextAlertText.value = timeFormat.format(cal.time)
        try {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
        } catch (e: SecurityException) {
            Log.d("Alarm", e.message, e)
        }
    }

    private fun cancel() {
        if (nextIntent != null) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(nextIntent!!)
            nextIntent = null
        }
    }

    fun onAlert() {
        createNotification(context, "${nextLengthText.value}又过去了...", Intent(context, MainActivity::class.java))
        alertCount++
        triggerAlert(getCurrTimeAlert())
    }

    fun onWakeup() {
        val res = timeStatus.resume()
        val remains = res.first
        val refreshes = res.second
        if (refreshes) {
            createNotification(context, "时间已重置", Intent(context, MainActivity::class.java))
            stop()
        } else {
            triggerAlert(remains)
        }
    }

    fun onSleep() {
        timeStatus.pause()
        cancel()
    }

    fun begin() {
        triggerAlert(getCurrTimeAlert())
        enabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(keepAliveServiceIntent)
        } else {
            context.startService(keepAliveServiceIntent)
        }
    }

    fun stop() {
        enabled = false
        alertCount = 0
        cancel()
        nextAlertText.value = "N/A"
        clearNotification(context)
        context.stopService(keepAliveServiceIntent)
    }
}
