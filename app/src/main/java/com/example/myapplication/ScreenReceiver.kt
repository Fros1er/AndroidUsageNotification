package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenOnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        alarm?.onWakeup()
    }
}

class ScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        alarm?.onSleep()
    }
}