package com.booster.ff.antilag

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds

class BoosterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize AdMob
        MobileAds.initialize(this) {}
        // Create notification channel
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Booster Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Booster FF running in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "booster_ff_channel"
    }
}
