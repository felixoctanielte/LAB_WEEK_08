package com.example.lab_week_08

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(MainActivity.EXTRA_ID) ?: "No ID"

        //  Notification setup
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Second Foreground Service")
            .setContentText("Tracking process for ID: $id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        //  Simulasi proses berjalan 2 detik
        Thread {
            for (i in 3 downTo 1) {
                Thread.sleep(1000L)
            }
            trackingCompletion.postValue(id)
            stopSelf()
        }.start()

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Second Notification Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "SECOND_NOTIFICATION_CHANNEL"
        const val NOTIFICATION_ID = 2001
        val trackingCompletion = MutableLiveData<String>()
    }
}
