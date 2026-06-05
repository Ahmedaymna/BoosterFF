package com.booster.ff.antilag.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.booster.ff.antilag.BoosterApp
import com.booster.ff.antilag.R
import com.booster.ff.antilag.ui.MainActivity
import com.booster.ff.antilag.utils.RamUtils

class BoosterService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("🚀 Booster FF نشط"))
        isRunning = true
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                val ramInfo = RamUtils.getRamInfo(this@BoosterService)
                val status = when {
                    ramInfo.usedPercent > 85 -> "⚠️ RAM مرهق - اضغط للتحسين"
                    ramInfo.usedPercent > 65 -> "🟡 RAM متوسط ${ramInfo.usedPercent}%"
                    else -> "🟢 أداء ممتاز ${ramInfo.usedPercent}%"
                }
                updateNotification(status)
                handler.postDelayed(this, 5000)
            }
        })
    }

    private fun buildNotification(text: String): Notification {
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, BoosterApp.CHANNEL_ID)
            .setContentTitle("Booster FF Anti Lag")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(intent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
