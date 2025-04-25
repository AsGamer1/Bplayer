package com.bseenmedia.bplayer

import android.app.*
import android.content.Intent
import android.os.*
import android.graphics.Color
import android.util.Log

class AppMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "monitor_channel"
            val channel = NotificationChannel(
                channelId,
                "App Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Supervisión activa")
                .setContentText("Verificando estado de la app")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

            startForeground(1, notification)
        }
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
        Log.d("AppMonitor", "Reiniciando app desde el servicio")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkAppStatus)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
