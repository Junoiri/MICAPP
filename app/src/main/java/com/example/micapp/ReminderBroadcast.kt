package com.example.micapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcast : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "new_event_channel"
        val channelName = "New Event Notifications"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationTitle = intent.getStringExtra("NOTIFICATION_TITLE") ?: "\uD83D\uDCD8 New Academic Event!"
        val notificationMessage = intent.getStringExtra("NOTIFICATION_MESSAGE") ?: "A new academic event has been scheduled. Check it out! \uD83D\uDCC5✨"

        var notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_micapp_logo_outline)
            setContentTitle(notificationTitle)
            setContentText(notificationMessage)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        NotificationManagerCompat.from(context).notify(200, notificationBuilder.build())
    }
}
