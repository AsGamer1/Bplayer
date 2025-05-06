package com.bseenmedia.bplayer

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class AppMonitorService : Service() {
    private val CHANNELID = "AppMonitorChannel"
    private val NOTIFICATIONID = 1
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val notification = createNotification()

        startForeground(NOTIFICATIONID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(checkAppStatus)
        return START_STICKY
    }

    private val checkAppStatus = object : Runnable {
        override fun run() {
            if (!isAppRunning()) {
                restartApp()
            }
            handler.postDelayed(this, checkInterval)
        }
    }

    private fun isAppRunning(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = am.getRunningTasks(1)
        val topActivity = runningTasks.firstOrNull()?.topActivity
        return topActivity?.packageName == packageName
    }

    private fun restartApp() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("exitApp", false)) {
            Log.d("AppMonitor", "Reiniciando app desde el servicio")
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkAppStatus)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNELID,
                "App Monitor Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNELID)
        } else {
            Notification.Builder(this)
        }

        return builder.setContentTitle("App Monitor Service")
            .setContentText("Monitoring app usage")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}