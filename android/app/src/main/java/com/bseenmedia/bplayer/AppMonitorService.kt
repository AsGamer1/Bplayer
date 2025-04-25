package com.bseenmedia.bplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class AppMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L // Intervalo de 5 segundos

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
        // Implementa la lógica para verificar si la app está en ejecución
        return true // Placeholder
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkAppStatus)
    }
}
