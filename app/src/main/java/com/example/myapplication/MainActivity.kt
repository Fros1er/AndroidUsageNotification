package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme


@SuppressLint("StaticFieldLeak")
var alarm: Alarm? = null

var keepAliveServiceIntent: Intent? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarm = Alarm(applicationContext)
        keepAliveServiceIntent = Intent(applicationContext, RunnerService::class.java)

        val screenOn = IntentFilter()
        screenOn.addAction("android.intent.action.SCREEN_ON")
        val screenOff = IntentFilter()
        screenOff.addAction("android.intent.action.SCREEN_OFF")
        val screenOnReceiver = ScreenOnReceiver()
        val screenOffReceiver = ScreenOffReceiver()
        registerReceiver(screenOnReceiver, screenOn)
        registerReceiver(screenOffReceiver, screenOff)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }

        requestPermission(Manifest.permission.WAKE_LOCK, this)
//        requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requestPermission(Manifest.permission.FOREGROUND_SERVICE, this)
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            requestPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION, this)
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(Manifest.permission.POST_NOTIFICATIONS, this)
            requestPermission(Manifest.permission.USE_EXACT_ALARM, this)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!checkPermission(ACTION_REQUEST_SCHEDULE_EXACT_ALARM, applicationContext)) {
                startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Column {
        val text by alarm!!.text
        val nextAlertText by alarm!!.nextAlertText
        val nextLengthText by alarm!!.nextLengthText
        Text(
            text = text,
            modifier = modifier.padding(24.dp)
        )
        Text(
            text = "下次提醒：$nextAlertText",
            modifier = modifier.padding(24.dp)
        )
        Text(
            text = "本次间隔时长：$nextLengthText",
            modifier = modifier.padding(24.dp)
        )
        Button(onClick = {
            alarm?.begin()
        }, modifier = modifier.padding(24.dp)) {
            Text(text = "启动！！！！", modifier = modifier)
        }
        Button(onClick = {
            alarm?.stop()
        }, modifier = modifier.padding(24.dp)) {
            Text(text = "重置", modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting()
    }
}